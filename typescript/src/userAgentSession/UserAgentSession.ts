/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Identifier from '../Identifier';
import JwsToken from '../crypto/protocols/jose/jws/JwsToken';
import UserAgentOptions from '../UserAgentOptions';
import IResolver from '../resolvers/IResolver';
import CryptoFactory from '../crypto/plugin/CryptoFactory';
import OIDCAuthenticationRequest from '../crypto/protocols/did/requests/OIDCAuthenticationRequest';
import OIDCAuthenticationResponse from '../crypto/protocols/did/responses/OIDCAuthenticationResponse';
import UserAgentError from '../UserAgentError';
import PublicKey from '../crypto/keys/PublicKey';
import IManifest from './oidc/requests/IManifest';
import IRequestPrompt, { IPermissionRequestPrompt } from './oidc/requests/IRequestPrompt';
import IScopeDefinition, { IScopeRequest } from './oidc/requests/IScopeDefinition';
import nodeFetch from 'node-fetch';
import { URL } from 'url';
import { PERMISSION_GRANT_CONTEXT, PERMISSION_GRANT_TYPE } from '../hubSession/objects/IPermissionGrant';
import Permissions from '../hubInterfaces/Permissions';
import { HubInterfaceType, CommitStrategyType } from '../hubInterfaces/HubInterface';
import IResponseReceipt, { IPermissionReceipt } from './oidc/response/IResponseReceipt';
import { HubWriteResponse, CryptoOptions } from '..';

/**
 * OIDC optional parameters
 */
export interface OIDCRequestOptions {
  /**
   * claims to request be included in the response
   * Should follow https://openid.net/specs/openid-connect-core-1_0.html#ClaimsParameter
   * @throws Claims are a future feature and not yet supported by the user agent.
   */
  claimRequests?: {[claimName: string]: any},
  /**
   * State to include in the response
   */
  state?: string,
  /**
   * Requester manifest for UI presentation
   */
  manifest?: IManifest,
  /**
   * Additional URLs of @see IScopeDefinition to include
   */
  scopes?: IScopeRequest[]
}

/**
 * Class for creating a User Agent Session for sending and verifying
 * Authentication Requests and Responses.
 */
export default class UserAgentSession {

  private sender: Identifier;
  private resolver: IResolver;
  private keyReference: string
  private options: UserAgentOptions;
  private cryptoFactory: CryptoFactory;
  private permissions: Permissions | undefined;
  
  constructor (sender: Identifier, keyReference: string, resolver: IResolver) {
    this.sender = sender;
    this.resolver = resolver;
    this.keyReference = keyReference;
    this.options = <UserAgentOptions>sender.options;
    this.cryptoFactory = this.options.cryptoFactory;
  }

  /**
   * Sign a User Agent Request.
   * @param redirectUrl url that recipient should send response back to.
   * @param nonce nonce that will come back in response.
   * @param options Open ID Connect optional parameters
   */
  public async signRequest(redirectUrl: string, nonce: string, options?: OIDCRequestOptions): Promise<string> {

    const request: Partial<OIDCAuthenticationRequest> = {
      iss: this.sender.id,
      response_type: 'id_token',
      response_mode: 'form_post',
      client_id: redirectUrl,
      scope: 'openid',
      nonce
    };

    if (options) {
      if (options.scopes) {
        const additionalScopes = options.scopes.map((scope) => Buffer.from(JSON.stringify(scope)).toString('base64')).join(' ');
        request.scope = `openid ${additionalScopes}`;
      }

      if (options.state) {
        Object.assign(request, {state: options.state});
      }

      if (options.claimRequests) {
        throw new Error('UserAgent does not currently support open ID Connect claims');
        // Object.assign(request, {claims: {id_token: options.claimRequests}});
      }
  
      if (options.manifest) {
        Object.assign(request, {registration: JSON.stringify(options.manifest)});
      }
    }

    return this.sender.sign(request, this.keyReference);
  }

  /**
   * Send an Open ID Connect User Agent Response.
   * @param request Open ID Connect request to respond to.
   * @param claims Future Feature: any claims the request asked for.
   * @param grants any permissionGrants approved by the user.
   * @throws Throws if claims are included
   */
  public async sendResponse (request: OIDCAuthenticationRequest, grants?: IPermissionRequestPrompt[], claims?: any): Promise<any> {

    // check if sender has keystore
    if (!this.sender.options || !this.sender.options.keyStore) {
      throw new UserAgentError(`No KeyStore specified for '${this.sender.id}`);
    }

    // get public key of key referenced by keyReference in keystore
    const publicKey = <PublicKey> await this.sender.options.keyStore.get(this.keyReference, true);

    // milliseconds to seconds
    const milliseconds = 1000;
    const expirationTimeOffsetInMinutes = 5;
    const expiration = new Date(Date.now() + milliseconds * 60 * expirationTimeOffsetInMinutes); // 5 minutes from now
    const exp = Math.floor(expiration.getTime() / milliseconds);
    const iat = Math.floor(Date.now() / milliseconds); // ms to seconds

    // create OIDC response object
    let response: OIDCAuthenticationResponse = {
      iss: 'https://self-issued.me',
      sub: await PublicKey.getThumbprint(publicKey),
      aud: request.client_id,
      nonce: request.nonce,
      state: request.state,
      did: this.sender.id,
      sub_jwk: publicKey,
      iat,
      exp
    };

    // add requested claims to response
    if (claims) {
      response = Object.assign(response, claims);
    }
    // response = Object.assign(response, claims);

    let receipt: IResponseReceipt = {
      credentials: [],
      permissionGrants: []
    };

    if (grants) {
      // Send hub permission objects
      if (!this.permissions) {
        this.permissions = new Permissions({
          hubOwner: this.sender,
          clientIdentifier: this.sender,
          recipientsPublicKeys: undefined,
          context: PERMISSION_GRANT_CONTEXT,
          type: PERMISSION_GRANT_TYPE,
          hubInterface: HubInterfaceType.Permissions,
          commitStrategy: CommitStrategyType.Basic,
          cryptoOptions: new CryptoOptions(),
          hubProtectionStrategy: undefined
        });
      }

      for (let i = 0; i < grants.length; i++) {
        const permissionBundle = grants[i];
        const permissionReceipt: IPermissionReceipt = {
          owner: this.sender.id,
          grantee: request.iss,
          definition: {
            name: permissionBundle.name,
            description: permissionBundle.description,
            iconUrl: permissionBundle.iconUrl
          },
          objects: [],
        };
        for (let j = 0; j < permissionBundle.grants.length; j++) {
          let grant = permissionBundle.grants[j];
          grant.owner = this.sender.id;
          grant.grantee = request.iss;
          const grantWriteResponse: HubWriteResponse = await this.permissions.addObject(grants[i]);
          permissionReceipt.objects.push(grantWriteResponse.getRevisions()[0]);
        }
        receipt.permissionGrants.push(permissionReceipt);
      }
    }

    // sign the response
    const jws = await this.sender.sign(response, this.keyReference);
    const oidcResponse = await nodeFetch(request.client_id, {
      method: 'POST',
      body: jws,
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': jws.length.toString()
      },
    });
    if (oidcResponse.status !== 200) {
      throw new Error(`OpenID Connect response failed to send: ${oidcResponse.text()}`);
    }
    
    if (!claims && !grants) {
      return oidcResponse;
    }
    return receipt;
  }

  /**
   * Verify a request was signed and sent by Identifier.
   * @param jws Signed Payload
   */
  public async verify(jws: string): Promise<any> {

    // get identifier id from key id in header.
    const token : JwsToken = await JwsToken.deserialize(jws, {cryptoFactory: this.cryptoFactory});
    const payload = JSON.parse(token.payload.toString());

    /**
     * If iss parameter is 'https://selfissued.me', payload is an OIDC auth request
     * and did is in the did parameter. Else the payload is a OIDC auth response,
     * so the did is in the iss parameter. If either parameter is undefined,
     * the payload is not formatted properly, so throw an error.
     */
    const issuerIdentifier = payload.iss === 'https://self-issued.me' ? payload.did : payload.iss;

    if (!issuerIdentifier) {
      throw new UserAgentError('Unable to identify issuer of the token.');
    }

    // verify jws and return payload. 
    const identifier = new Identifier(issuerIdentifier, this.options);
    const document = await identifier.getDocument();

    const publicKeysFromDocument = document.getPublicKeysFromDocument();
    if (token.signatures.length < 0) {
      throw new UserAgentError('No signature included');
    }
    let keyMatches: RegExpMatchArray | null = null;
    const keyIdRegex = /([^#]*)#?(.+$)/;
    if (token.signatures[0].protected && (token.signatures[0].protected).has('kid')) {
      const keyIdentifier: string = (token.signatures[0].protected).get('kid');
      keyMatches = keyIdentifier.match(keyIdRegex);
    } else if (token.signatures[0].header && (token.signatures[0].header).has('kid')) {
      const keyIdentifier: string = (token.signatures[0].header).get('kid');
      keyMatches = keyIdentifier.match(keyIdRegex);
    }
    if (keyMatches ===  null) {
      throw new UserAgentError('Cannot locate keyID');
    }
    if (keyMatches[1].length > 0 && keyMatches[1] !== identifier.id) {
      throw new UserAgentError('Issuer signer does not match issuer');
    }
    const keyId = keyMatches[2];

    const matchingPublicKeys = publicKeysFromDocument.filter((publicKey) => {
      return publicKey.kid && publicKey.kid.endsWith(keyId);
    });

    if (!await token.verify(matchingPublicKeys)) {
      throw new UserAgentError('Invalid signature');
    }
    return JSON.parse(payload);
  }

  /**
   * Verify an Open ID Connect response was signed and sent by response.did.
   * @param responseJws Signed response token
   */
  public async verifyResponse(responseJws: string): Promise<OIDCAuthenticationResponse> {
    return <Promise<OIDCAuthenticationResponse>> this.verify(responseJws);
  }

  /**
   * Verify an Open ID Connect request was signed and sent by Identifier, and return all contents
   * @param request Signed request token
   */
  public async verifyAndHydrateRequest(requestJws: string): Promise<IRequestPrompt> {
    const request: OIDCAuthenticationRequest = await this.verify(requestJws);
    const prompt: IRequestPrompt = Object.assign({}, request);

    // if there are credentials requested, include them.
    if (request.claims && 'credential' in request.claims.id_token) {
      prompt.credentialsRequested = Object.keys(request.claims.id_token.credential);
    }

    // if there are scopes requested include them.
    let scopes = request.scope.split(' ');
    if (scopes.length > 1) {
      scopes = scopes.filter((scope) => scope !== 'openid');
      prompt.identityHubPermissionsRequested = [];
      // Not forEach to allow proper async await
      for (let i = 0; i < scopes.length; i++) {
        prompt.identityHubPermissionsRequested.push(...(await this.parseScopeValue(request.iss, scopes[i])));
      }
    }

    // if there are credentials minted, include them.
    // const MICROSOFT_BLUE = '#0078d4';

    // if there is a registration, pull requester information
    if (request.registration) {
      let manifest: IManifest;
      if (typeof(request.registration) === 'string') {
        manifest = <IManifest> JSON.parse(request.registration);
      } else {
        manifest = request.registration;
      }
      prompt.name = manifest.client_name;
      prompt.logoUrl = manifest.logo_uri;
      prompt.dataUsePolicy = manifest.policy_uri;
      prompt.homepage = manifest.client_uri;
      prompt.termsOfService = manifest.tos_uri;
    }

    // any additional scraping we can do from the request metadata itself
    if (request.client_id.startsWith('http')) {
      // the redirect_uri is over http(s)
      const redirectUrl = new URL(request.client_id);
      prompt.host = redirectUrl.host;
    }
    return prompt;
  }

  /**
   * Parses a encoded scope value and constructs IPermissionRequestPrompts
   * @param requester the DID of the requester for this permission
   * @param encodedScope The Encoded scope value
   * @returns IPermissionRequestPrompt(s) corresponding to the scopes requested
   */
  private async parseScopeValue(requester: string, encodedScope: string): Promise<IPermissionRequestPrompt[]> {
    const prompts: IPermissionRequestPrompt[] = [];
    const scope: IScopeRequest = JSON.parse(Buffer.from(encodedScope, 'base64').toString());
    const scopeDefinitionUris = Object.keys(scope);
    for (let j = 0; j < scopeDefinitionUris.length; j++) {
      const scopeDefinitionUri = scopeDefinitionUris[j];
      const scopeModifiers = scope[scopeDefinitionUri];
      // Performance Opprotunity: opprotunity to parallelize
      const response = await nodeFetch(scopeDefinitionUri);
      if (response.status !== 200) {
        throw Error(`Failed to retrieve Scope ${scopeDefinitionUri}. ${await response.text()}`)
      }
      const scopeDefinition = <IScopeDefinition> JSON.parse(await response.text());
      prompts.push({
        required: scopeModifiers && scopeModifiers.essential ? scopeModifiers.essential : false,
        name: scopeDefinition.resourceBundle.name,
        description: scopeDefinition.resourceBundle.description,
        iconUrl: scopeDefinition.resourceBundle.icon_uri,
        grants: scopeDefinition.access.map((grantRequest) => {
          const contextTypeRegex = /(^[^\/]+\/\/.*)\/(.*)$/i;
          const matches = grantRequest.resource_type.match(contextTypeRegex);
          if (!matches) {
            throw new Error(`Scope Definition ${scopeDefinitionUri} has invalid access resource_type ${grantRequest.resource_type}`);
          }
          return {
            owner: this.sender.id,
            grantee: requester,
            allow: grantRequest.allow,
            context: matches[1],
            type: matches[2]
          };
        }),
      });
    }
    return prompts;
  }
}
