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
import HubInterface from './hubInterfaces/HubInterface';
import {HubInterfaceOptions, HubInterfaceType, CommitStrategyType } from './hubInterfaces/HubInterface';
import Actions from './hubInterfaces/Actions';
import Collections from './hubInterfaces/Collections';
import Permissions from './hubInterfaces/Permissions';
import Profile from './hubInterfaces/Profile';
export { HubClient, HubInterface, Actions, Collections, Permissions, Profile };
export { HubInterfaceOptions, HubInterfaceType, CommitStrategyType };

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
