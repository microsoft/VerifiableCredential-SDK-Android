/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Identifier from './Identifier';
import IdentifierDocument from './IdentifierDocument';
import CredentialManifest from './credentials/CredentialManifest';
import CredentialIssuer from './credentials/CredentialIssuer';
import ICredential from './credentials/ICredential';
import SelfIssuedCredential from './credentials/SelfIssuedCredential';
import IDataHandler from './credentials/IDataHandler';
import SidetreeRegistrar from './registrars/SidetreeRegistrar';
import UserAgentOptions from './UserAgentOptions';
import HttpResolver from './resolvers/HttpResolver';
export { Identifier, IdentifierDocument, UserAgentOptions };
export { CredentialManifest, CredentialIssuer, ICredential, SelfIssuedCredential };
export { IDataHandler };
export { SidetreeRegistrar };
export { HttpResolver };
