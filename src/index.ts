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
import CryptoOptions from './CryptoOptions';
export { CryptoOptions };

// Hub Client and Methods
import HubClient from './HubClient';
import HubMethods from './hubMethods/HubMethods';
import {HubMethodsOptions, HubInterface, CommitStrategyReference } from './hubMethods/HubMethods';
import Actions from './hubMethods/Actions';
import Collections from './hubMethods/Collections';
import Permissions from './hubMethods/Permissions';
import Profile from './hubMethods/Profile';
export { HubClient, HubMethods, Actions, Collections, Permissions, Profile };
export { HubMethodsOptions, HubInterface, CommitStrategyReference };

// CommitSigners
export { default as CommitSigner } from './hubSession/crypto/CommitSigner';
export { default as ICommitSigner } from './hubSession/crypto/ICommitSigner';

// Hub Requests
export { default as HubRequest } from './hubSession/requests/HubRequest';
export { default as HubObjectQueryRequest } from './hubSession/requests/HubObjectQueryRequest';
export { default as HubCommitQueryRequest } from './hubSession/requests/HubCommitQueryRequest';
export { default as HubWriteRequest } from './hubSession/requests/HubCommitWriteRequest';

// Hub Responses
export { default as HubObjectQueryResponse } from './hubSession/responses/HubObjectQueryResponse';
export { default as HubCommitQueryResponse } from './hubSession/responses/HubCommitQueryResponse';
export { default as HubWriteResponse } from './hubSession/responses/HubWriteResponse';

// Hub Session
export { default as Commit } from './hubSession/Commit';
export { default as CommitStrategyBasic } from './hubSession/CommitStrategyBasic';
export { default as HubError } from './hubSession/HubError';
export { default as HubSession } from './hubSession/HubSession';
export { default as SignedCommit } from './hubSession/SignedCommit';

