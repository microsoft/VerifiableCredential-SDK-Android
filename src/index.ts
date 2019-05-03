/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Identifier from 'src/Identifier';
import IdentifierDocument from 'src/IdentifierDocument';
import CredentialManifest from 'src/credentials/CredentialManifest';
import CredentialIssuer from 'src/credentials/CredentialIssuer';
import ICredential from 'src/credentials/ICredential';
import SelfIssuedCredential from 'src/credentials/SelfIssuedCredential';
import IDataHandler from 'src/credentials/IDataHandler';
import SidetreeRegistrar from 'src/registrars/SidetreeRegistrar';
import InMemoryKeyStore from 'src/keystores/InMemoryKeyStore';
import UserAgentOptions from 'src/UserAgentOptions';
import HttpResolver from 'src/resolvers/HttpResolver';
export { Identifier, IdentifierDocument, UserAgentOptions };
export { CredentialManifest, CredentialIssuer, ICredential, SelfIssuedCredential };
export { IDataHandler };
export { SidetreeRegistrar };
export { InMemoryKeyStore };
export { HttpResolver };
