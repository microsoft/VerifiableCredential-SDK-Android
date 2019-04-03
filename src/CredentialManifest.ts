// /*---------------------------------------------------------------------------------------------
//  *  Copyright (c) Microsoft Corporation. All rights reserved.
//  *  Licensed under the MIT License. See License.txt in the project root for license information.
//  *--------------------------------------------------------------------------------------------*/

// import { CredentialInput, DataInput, OpenIDInput, CredentialManifestIssuerOptions } from './types';

// /**
//  * context for credentialManifest
//  */
// const context = 'https://identity.foundation/schemas/credentials';

// /**
//  * type for credentialManifest
//  */
// const type = 'CredentialManifest';

// /**
//  *  Class defining methods and properties for a ClaimManifest object.
//  *  based off of the CredentialManifest spec: {@link https://github.com/decentralized-identity/credential-manifest/blob/master/explainer.md}
//  */
// export default class CredentialManifest {

//   /**
//    * Name of the credential
//    */
//   public readonly credential: string;

//   /**
//    * Languages supported for localization
//    */
//   public readonly language: Array<string>;

//   /**
//    * the keeper DID whose hub stores the CredentialManifest.
//    */
//   public readonly keeper: string;

//   /**
//    * Version of the CredentialManifest
//    */
//   public readonly version: string;

//   /**
//    * preconditions for the manifest.
//    */
//   public readonly preconditions: any;

//   /**
//    * inputs parameter for manifest.
//    */
//   public readonly inputs: Array<CredentialInput | DataInput | OpenIDInput>;

//   /**
//    * issuer options for things such as the style of the manifest in the UI.
//    */
//   public issuerOptions: CredentialManifestIssuerOptions;

//   /**
//    * Constructs an instance of the CredentialManifest class from a well-formed credential manifest JSON object.
//    * TODO: check that the JSON parameter is valid (yup?)
//    */
//   constructor (credentialManifest: any) {
//     this.credential = credentialManifest.credential;
//     this.language = credentialManifest.language;
//     this.keeper = credentialManifest.keeper;
//     this.version = credentialManifest.version;
//     this.preconditions = credentialManifest.preconditions;
//     this.inputs = credentialManifest.inputs;
//     this.issuerOptions = credentialManifest.issuer_options;
//   }

//   /**
//    * Creates a new instance of the CredentialManifest class.
//    */
//   public static create (credential: string,
//                         language: Array<string>,
//                         keeper: string,
//                         version: string,
//                         preconditions: any,
//                         inputs: Array<CredentialInput | DataInput | OpenIDInput>,
//                         issuerOptions: CredentialManifestIssuerOptions) {
//     const manifest = {
//       '@context': context,
//       '@type': type,
//       'language': language,
//       'credential': credential,
//       'keeper': keeper,
//       'version': version,
//       'preconditions': preconditions,
//       'inputs': inputs,
//       'issuer_options': issuerOptions
//     };
//     return new CredentialManifest(manifest);
//   }

//   /**
//    * serializes the CredentialManifest to JSON.
//    */
//   public toJSON () {
//     const manifest = {
//       '@context': context,
//       '@type': type,
//       'credential': this.credential,
//       'preconditions': this.preconditions,
//       'inputs': this.inputs,
//       'issuer_options': this.issuerOptions
//     };
//     return manifest;
//   }

//   /**
//    * Get the keeper did of the CredentialManifest
//    */
//   public getKeeperDid () {
//     return this.keeper;
//   }

//   /**
//    * Get the input properties of the manifest
//    */
//   public getInputProperties () {
//     return this.inputs;
//   }

//   /**
//    * Get the input style properties of the manifest.
//    */
//   public getInputStyleProperties () {
//     return this.issuerOptions.input.styles;
//   }

//   /**
//    * Get the input label for a specific value
//    */
//   public getInputLabels () {
//     return this.issuerOptions.input.labels;
//   }

//   /**
//    * Get the presentation options used to present a credential in a UA
//    */
//   public getPresentationOptions () {
//     return this.issuerOptions.presentation;
//   }
// }
