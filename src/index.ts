/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

// Identifier
import Identifier from './Identifier';
import IdentifierDocument from './IdentifierDocument';
import UserAgentOptions from './UserAgentOptions';
export { Identifier, IdentifierDocument, UserAgentOptions };

// Credentials
import CredentialManifest from './credentials/CredentialManifest';
import CredentialIssuer from './credentials/CredentialIssuer';
import ICredential from './credentials/ICredential';
import SelfIssuedCredential from './credentials/SelfIssuedCredential';
import IDataHandler from './credentials/IDataHandler';
export { CredentialManifest, CredentialIssuer, ICredential, SelfIssuedCredential };
export { IDataHandler };

// Registrar
import SidetreeRegistrar from './registrars/SidetreeRegistrar';
export { SidetreeRegistrar };

// KeyStore
import InMemoryKeyStore from './keystores/InMemoryKeyStore';
export { InMemoryKeyStore };

// Resolvers
import HttpResolver from './resolvers/HttpResolver';
export { HttpResolver };

// Cryptography
export { default as CommitSigner } from './hub/crypto/CommitSigner';
export { default as ICommitSigner } from './hub/crypto/ICommitSigner';
import { IKeyStore, KeyStoreMem, ProtectionFormat } from '@decentralized-identity/did-auth-jose';
export { IKeyStore, KeyStoreMem, ProtectionFormat } ;

// Hub Requests
export { default as HubRequest } from './hub/requests/HubRequest';
export { default as HubObjectQueryRequest } from './hub/requests/HubObjectQueryRequest';
export { default as HubCommitQueryRequest } from './hub/requests/HubCommitQueryRequest';
export { default as HubWriteRequest } from './hub/requests/HubCommitWriteRequest';

// Hub Responses
export { default as HubObjectQueryResponse } from './hub/responses/HubObjectQueryResponse';
export { default as HubCommitQueryResponse } from './hub/responses/HubCommitQueryResponse';
export { default as HubWriteResponse } from './hub/responses/HubWriteResponse';

// Hub SDK
export { default as Commit } from './hub/Commit';
export { default as CommitStrategyBasic } from './hub/CommitStrategyBasic';
export { default as HubError } from './hub/HubError';
export { default as HubSession } from './hub/HubSession';
export { default as SignedCommit } from './hub/SignedCommit';

