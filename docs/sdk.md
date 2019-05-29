## Classes

<dl>
<dt><a href="#CredentialIssuer">CredentialIssuer</a></dt>
<dd><p>Class for obtaining
credentials from an issuer.</p></dd>
<dt><a href="#CredentialManifest">CredentialManifest</a></dt>
<dd><p>Class defining methods and properties for a ClaimManifest object.
 based off of the CredentialManifest spec: <a href="https://github.com/decentralized-identity/credential-manifest/blob/master/explainer.md">https://github.com/decentralized-identity/credential-manifest/blob/master/explainer.md</a></p></dd>
<dt><a href="#SelfIssuedCredential">SelfIssuedCredential</a></dt>
<dd><p>Implementation of an OpenID Connect
self-issued id token.</p></dd>
<dt><a href="#VerifiedCredential">VerifiedCredential</a></dt>
<dd><p>Implementation of an OpenID Connect
self-issued id token.</p></dd>
<dt><a href="#CryptoError">CryptoError</a></dt>
<dd><p>Base error class for the crypto.</p></dd>
<dt><a href="#HubClientOptions">HubClientOptions</a></dt>
<dd><p>Interface defining options for the
HubClient, such as hub Identifier and client Identifier.</p></dd>
<dt><a href="#HubClient">HubClient</a></dt>
<dd></dd>
<dt><a href="#HubClientOptions">HubClientOptions</a></dt>
<dd><p>Class for defining options for the
HubClient, such as hub Identifier and client Identifier.</p></dd>
<dt><a href="#HubObject">HubObject</a></dt>
<dd><p>Class that represents an object in a hub.</p></dd>
<dt><a href="#Actions">Actions</a></dt>
<dd><p>A Class that represents objects</p></dd>
<dt><a href="#Collections">Collections</a></dt>
<dd><p>A Class that does CRUD operations for storing items as Collections in the Hub</p></dd>
<dt><a href="#HubInterface">HubInterface</a></dt>
<dd></dd>
<dt><a href="#Permissions">Permissions</a></dt>
<dd><p>A Class that does CRUD operations for storing items as Permissions in the Hub</p></dd>
<dt><a href="#Profile">Profile</a></dt>
<dd><p>A Class that does CRUD operations for storing items as Collections in the Hub</p></dd>
<dt><a href="#Commit">Commit</a></dt>
<dd><p>Represents a new (i.e pending, unsigned) commit which will create, update, or delete an object in
a user&#39;s Identity Hub.</p></dd>
<dt><a href="#CommitStrategyBasic">CommitStrategyBasic</a></dt>
<dd><p>Resolves the final state of an object from the constituent set of commits for that object.</p>
<p>This class works only with objects using the <code>basic</code> commit strategy.</p></dd>
<dt><a href="#HubError">HubError</a></dt>
<dd><p>Represents an error returned by an Identity Hub.</p></dd>
<dt><a href="#HubSession">HubSession</a></dt>
<dd><p>Represents a communication session with a particular Hub instance.</p></dd>
<dt><a href="#SignedCommit">SignedCommit</a></dt>
<dd><p>Class representing a signed commit.</p></dd>
<dt><a href="#KeyStoreConstants">KeyStoreConstants</a></dt>
<dd><p>Class for key storage constants</p></dd>
<dt><a href="#Multihash">Multihash</a></dt>
<dd><p>Class that performs hashing operations using the multihash format.</p></dd>
<dt><a href="#SidetreeRegistrar">SidetreeRegistrar</a></dt>
<dd><p>Registrar implementation for the Sidetree (ION) network</p></dd>
<dt><a href="#HttpResolver">HttpResolver</a></dt>
<dd><p>Fetches DID Documents from remote resolvers over http</p></dd>
<dt><a href="#HostServiceEndpoint">HostServiceEndpoint</a></dt>
<dd><p>class to represent a host service endpoint.</p></dd>
<dt><a href="#ServiceEndpoint">ServiceEndpoint</a></dt>
<dd><p>abstract class to represent a service endpoint.
based on: <a href="https://github.com/decentralized-identity/identity-hub/blob/master/explainer.md">https://github.com/decentralized-identity/identity-hub/blob/master/explainer.md</a>.</p></dd>
<dt><a href="#UserServiceEndpoint">UserServiceEndpoint</a></dt>
<dd><p>Class to represent a host service endpoint.</p></dd>
</dl>

## Members

<dl>
<dt><a href="#CredentialType">CredentialType</a></dt>
<dd><p>Enumeration of the supported credential types.</p></dd>
<dt><a href="#CredentialType">CredentialType</a></dt>
<dd><p>Interface defining common properties and
methods of a credential.</p></dd>
<dt><a href="#HubClientOptions">HubClientOptions</a></dt>
<dd><p>Class for doing CRUD operations to Actions, Collections, Permissions, and Profile
In a Hub.</p></dd>
<dt><a href="#HubClientOptions">HubClientOptions</a></dt>
<dd><p>Interface for HubClient class that manages which hub instance to create hub session with
And commits and queries for objects in the hub session.</p></dd>
<dt><a href="#CommitStrategyType">CommitStrategyType</a></dt>
<dd><p>Constants that represent what type of commit strategy to be used.</p></dd>
<dt><a href="#CommitStrategyType">CommitStrategyType</a></dt>
<dd><p>Constants that represent what interface type the hub request payload will be.</p></dd>
<dt><a href="#HubInterfaceType">HubInterfaceType</a></dt>
<dd><p>Hub Operations</p></dd>
<dt><a href="#Operation">Operation</a></dt>
<dd><p>Interface for defining options for HubMethods such as hubSession, commitSigner, and hubInterface.</p></dd>
<dt><a href="#HubInterfaceOptions">HubInterfaceOptions</a></dt>
<dd><p>An Abstract Class for Hub Interfaces.</p></dd>
</dl>

## Constants

<dl>
<dt><a href="#context">context</a></dt>
<dd><p>context for credentialManifest</p></dd>
<dt><a href="#type">type</a></dt>
<dd><p>type for credentialManifest</p></dd>
</dl>

<a name="CredentialIssuer"></a>

## CredentialIssuer
<p>Class for obtaining
credentials from an issuer.</p>

**Kind**: global class  

* [CredentialIssuer](#CredentialIssuer)
    * [new CredentialIssuer(identifier, manifest)](#new_CredentialIssuer_new)
    * _instance_
        * [.requestCredential(inputCredential)](#CredentialIssuer+requestCredential)
        * [.handleCredentialRequest(inputCredential, _dataHandler)](#CredentialIssuer+handleCredentialRequest)
        * [.validateCredential(_inputCredential)](#CredentialIssuer+validateCredential)
    * _static_
        * [.create(identifier, manifest)](#CredentialIssuer.create)

<a name="new_CredentialIssuer_new"></a>

### new CredentialIssuer(identifier, manifest)
<p>Constructs an instance of the credential issuer
based on the specified credential manifest.</p>


| Param | Description |
| --- | --- |
| identifier | <p>for the issuer.</p> |
| manifest | <p>credential manifest for specific credential.</p> |

<a name="CredentialIssuer+requestCredential"></a>

### credentialIssuer.requestCredential(inputCredential)
<p>Requests a new credential from the issuer,
providing a self-issued credential with the inputs
specified in the credential manifest.</p>

**Kind**: instance method of [<code>CredentialIssuer</code>](#CredentialIssuer)  

| Param | Description |
| --- | --- |
| inputCredential | <p>containing the inputs as specified in the credential manifest.</p> |

<a name="CredentialIssuer+handleCredentialRequest"></a>

### credentialIssuer.handleCredentialRequest(inputCredential, _dataHandler)
<p>Validate inputCredential with manifest and process and exchange inputCredential wuth Data Handler</p>

**Kind**: instance method of [<code>CredentialIssuer</code>](#CredentialIssuer)  

| Param | Description |
| --- | --- |
| inputCredential | <p>The Self-Issued Credential that with required claims.</p> |
| _dataHandler | <p>Data handler for process and exchanging credentials.</p> |

<a name="CredentialIssuer+validateCredential"></a>

### credentialIssuer.validateCredential(_inputCredential)
<p>Validate whether a credential is valid for the manifest.</p>

**Kind**: instance method of [<code>CredentialIssuer</code>](#CredentialIssuer)  

| Param | Description |
| --- | --- |
| _inputCredential | <p>the Credential to validate against the credential manifest</p> |

<a name="CredentialIssuer.create"></a>

### CredentialIssuer.create(identifier, manifest)
<p>Constructs an instance of the credential issuer
based on the specified credential manifest.</p>

**Kind**: static method of [<code>CredentialIssuer</code>](#CredentialIssuer)  

| Param | Description |
| --- | --- |
| identifier | <p>for the issuer.</p> |
| manifest | <p>credential manifest object or endpoint string of manifest.</p> |

<a name="CredentialManifest"></a>

## CredentialManifest
<p>Class defining methods and properties for a ClaimManifest object.
 based off of the CredentialManifest spec: [https://github.com/decentralized-identity/credential-manifest/blob/master/explainer.md](https://github.com/decentralized-identity/credential-manifest/blob/master/explainer.md)</p>

**Kind**: global class  

* [CredentialManifest](#CredentialManifest)
    * [new CredentialManifest()](#new_CredentialManifest_new)
    * _instance_
        * [.toJSON()](#CredentialManifest+toJSON)
        * [.getKeeperDid()](#CredentialManifest+getKeeperDid)
        * [.getInputProperties()](#CredentialManifest+getInputProperties)
    * _static_
        * [.create()](#CredentialManifest.create)

<a name="new_CredentialManifest_new"></a>

### new CredentialManifest()
<p>Constructs an instance of the CredentialManifest class from a well-formed credential manifest JSON object.</p>

<a name="CredentialManifest+toJSON"></a>

### credentialManifest.toJSON()
<p>serializes the CredentialManifest to JSON.</p>

**Kind**: instance method of [<code>CredentialManifest</code>](#CredentialManifest)  
<a name="CredentialManifest+getKeeperDid"></a>

### credentialManifest.getKeeperDid()
<p>Get the keeper did of the CredentialManifest</p>

**Kind**: instance method of [<code>CredentialManifest</code>](#CredentialManifest)  
<a name="CredentialManifest+getInputProperties"></a>

### credentialManifest.getInputProperties()
<p>Get the input properties of the manifest</p>

**Kind**: instance method of [<code>CredentialManifest</code>](#CredentialManifest)  
<a name="CredentialManifest.create"></a>

### CredentialManifest.create()
<p>Creates a new instance of the CredentialManifest class.</p>

**Kind**: static method of [<code>CredentialManifest</code>](#CredentialManifest)  
<a name="SelfIssuedCredential"></a>

## SelfIssuedCredential
<p>Implementation of an OpenID Connect
self-issued id token.</p>

**Kind**: global class  
**Implements**: <code>ICredential</code>  

* [SelfIssuedCredential](#SelfIssuedCredential)
    * [new SelfIssuedCredential(issuer, recipient)](#new_SelfIssuedCredential_new)
    * [.addClaim(claim)](#SelfIssuedCredential+addClaim)

<a name="new_SelfIssuedCredential_new"></a>

### new SelfIssuedCredential(issuer, recipient)
<p>Constructs a new instance of a self-issued
credential for the specified identifier.</p>


| Param | Description |
| --- | --- |
| issuer | <p>of the credential.</p> |
| recipient | <p>either a string or identifier identifying the intended recipient of the credential.</p> |

<a name="SelfIssuedCredential+addClaim"></a>

### selfIssuedCredential.addClaim(claim)
<p>Adds the specified claim to the credential.</p>

**Kind**: instance method of [<code>SelfIssuedCredential</code>](#SelfIssuedCredential)  

| Param | Description |
| --- | --- |
| claim | <p>claim to add to credential.</p> |

<a name="VerifiedCredential"></a>

## VerifiedCredential
<p>Implementation of an OpenID Connect
self-issued id token.</p>

**Kind**: global class  
**Implements**: <code>ICredential</code>  
<a name="new_VerifiedCredential_new"></a>

### new VerifiedCredential(issuedBy, issuedTo, issuedAt)
<p>Constructs a new instance of a verified
credential for the specified identifier.</p>


| Param | Description |
| --- | --- |
| issuedBy | <p>the specified identifier.</p> |
| issuedTo | <p>the specified identifier.</p> |
| issuedAt | <p>date and time.</p> |

<a name="CryptoError"></a>

## CryptoError
<p>Base error class for the crypto.</p>

**Kind**: global class  
<a name="new_CryptoError_new"></a>

### new CryptoError(protocol, message)
<p>Create instance of @class CryptoProtocolError</p>


| Param | Description |
| --- | --- |
| protocol | <p>name</p> |
| message | <p>for the error</p> |

<a name="HubClientOptions"></a>

## HubClientOptions
<p>Interface defining options for the
HubClient, such as hub Identifier and client Identifier.</p>

**Kind**: global class  
<a name="HubClient"></a>

## HubClient
**Kind**: global class  

* [HubClient](#HubClient)
    * [new HubClient(hubClientOptions)](#new_HubClient_new)
    * [.commit(commit)](#HubClient+commit)
    * [.queryObjects(queryRequest)](#HubClient+queryObjects)
    * [.getHubInstances()](#HubClient+getHubInstances)
    * [.createHubSession()](#HubClient+createHubSession)

<a name="new_HubClient_new"></a>

### new HubClient(hubClientOptions)
<p>Constructs an instance of the Hub Client Class for hub operations</p>


| Param | Description |
| --- | --- |
| hubClientOptions | <p>hub client options used to create instance.</p> |

<a name="HubClient+commit"></a>

### hubClient.commit(commit)
**Kind**: instance method of [<code>HubClient</code>](#HubClient)  

| Param | Description |
| --- | --- |
| commit | <p>Signs and sends a commit to the hub owner's hub.</p> |

<a name="HubClient+queryObjects"></a>

### hubClient.queryObjects(queryRequest)
<p>Query Objects of certain type in Hub.</p>

**Kind**: instance method of [<code>HubClient</code>](#HubClient)  

| Param | Description |
| --- | --- |
| queryRequest | <p>object that tells the hub what objec to get.</p> |

<a name="HubClient+getHubInstances"></a>

### hubClient.getHubInstances()
<p>Get all Hub Instances from hub owner's identifier document.</p>

**Kind**: instance method of [<code>HubClient</code>](#HubClient)  
<a name="HubClient+createHubSession"></a>

### hubClient.createHubSession()
<p>Implement createHubSession method once HubSession is refactored.
creates a hubSession for hub instance that is available/online.</p>

**Kind**: instance method of [<code>HubClient</code>](#HubClient)  
<a name="HubClientOptions"></a>

## HubClientOptions
<p>Class for defining options for the
HubClient, such as hub Identifier and client Identifier.</p>

**Kind**: global class  
<a name="HubObject"></a>

## HubObject
<p>Class that represents an object in a hub.</p>

**Kind**: global class  

* [HubObject](#HubObject)
    * [new HubObject(objectMetadata)](#new_HubObject_new)
    * [.getPayload()](#HubObject+getPayload)

<a name="new_HubObject_new"></a>

### new HubObject(objectMetadata)
<p>Create an instance for Hub Object using hub object's metadata.</p>


| Param | Description |
| --- | --- |
| objectMetadata | <p>object metadata that represents an object in a hub.</p> |

<a name="HubObject+getPayload"></a>

### hubObject.getPayload()
<p>If payload is not defined, get the payload from hub session using metadata.</p>

**Kind**: instance method of [<code>HubObject</code>](#HubObject)  
<a name="Actions"></a>

## Actions
<p>A Class that represents objects</p>

**Kind**: global class  
<a name="Collections"></a>

## Collections
<p>A Class that does CRUD operations for storing items as Collections in the Hub</p>

**Kind**: global class  
<a name="HubInterface"></a>

## HubInterface
**Kind**: global class  
<a name="new_HubInterface_new"></a>

### new HubInterface([hubInterfaceOptions])
<p>Creates an instance of HubMethods that will be used to send hub requests and responses.</p>


| Param | Description |
| --- | --- |
| [hubInterfaceOptions] | <p>for configuring how to form hub requests and responses.</p> |

<a name="Permissions"></a>

## Permissions
<p>A Class that does CRUD operations for storing items as Permissions in the Hub</p>

**Kind**: global class  
<a name="Profile"></a>

## Profile
<p>A Class that does CRUD operations for storing items as Collections in the Hub</p>

**Kind**: global class  
<a name="Commit"></a>

## Commit
<p>Represents a new (i.e pending, unsigned) commit which will create, update, or delete an object in
a user's Identity Hub.</p>

**Kind**: global class  

* [Commit](#Commit)
    * [.validate()](#Commit+validate)
    * [.isValid()](#Commit+isValid)
    * [.getProtectedHeaders()](#Commit+getProtectedHeaders)
    * [.getUnprotectedHeaders()](#Commit+getUnprotectedHeaders)
    * [.getPayload()](#Commit+getPayload)
    * [.sign(signer)](#Commit+sign)

<a name="Commit+validate"></a>

### commit.validate()
<p>Verifies whether the currently set fields constitute a valid commit which can be
signed/encrypted and stored in an Identity Hub.</p>
<p>Throws an error if the commit is not valid.</p>
<p>need: Move validation logic to hub-common-js repository to be shared with hub-node-core.</p>

**Kind**: instance method of [<code>Commit</code>](#Commit)  
<a name="Commit+isValid"></a>

### commit.isValid()
<p>Returns true if the validate() method would pass without error.</p>

**Kind**: instance method of [<code>Commit</code>](#Commit)  
<a name="Commit+getProtectedHeaders"></a>

### commit.getProtectedHeaders()
<p>Returns the headers which will be signed/encrypted.</p>

**Kind**: instance method of [<code>Commit</code>](#Commit)  
<a name="Commit+getUnprotectedHeaders"></a>

### commit.getUnprotectedHeaders()
<p>Returns the (optional) headers which will not be signed/encrypted.</p>

**Kind**: instance method of [<code>Commit</code>](#Commit)  
<a name="Commit+getPayload"></a>

### commit.getPayload()
<p>Returns the application-specific payload for this commit.</p>

**Kind**: instance method of [<code>Commit</code>](#Commit)  
<a name="Commit+sign"></a>

### commit.sign(signer)
<p>Returns a copy of this commit signed with the given signer.</p>

**Kind**: instance method of [<code>Commit</code>](#Commit)  

| Param | Description |
| --- | --- |
| signer | <p>The signer to use to sign the commit.</p> |

<a name="CommitStrategyBasic"></a>

## CommitStrategyBasic
<p>Resolves the final state of an object from the constituent set of commits for that object.</p>
<p>This class works only with objects using the <code>basic</code> commit strategy.</p>

**Kind**: global class  

* [CommitStrategyBasic](#CommitStrategyBasic)
    * [.resolveObject(commits)](#CommitStrategyBasic+resolveObject)
    * [.compareCommits(a, b)](#CommitStrategyBasic+compareCommits)

<a name="CommitStrategyBasic+resolveObject"></a>

### commitStrategyBasic.resolveObject(commits)
<p>Resolves the current state of an object with the <code>basic</code> commit strategy.</p>
<p>need: This class currently returns only the raw object payload. Once we add an object instance
class to the SDK (e.g. <code>HubObject</code>), this method will no longer be called directly, and will
also need to return the app-readable object metadata.</p>
<p>Currently returns <code>null</code> if the object was deleted, otherwise returns the most recent payload.</p>

**Kind**: instance method of [<code>CommitStrategyBasic</code>](#CommitStrategyBasic)  

| Param | Description |
| --- | --- |
| commits | <p>The entire known set of commits for the object.</p> |

<a name="CommitStrategyBasic+compareCommits"></a>

### commitStrategyBasic.compareCommits(a, b)
<p>Compares two commits (which must belong to the same object) in order to evaulate which one is
more recent.</p>
<p>Follows the conventions of the JavaScript sort() method:</p>
<ul>
<li><code>-1</code> indicates that a comes before (i.e. is older than b)</li>
<li><code>1</code> indicates that a comes after (i.e. is newer than b)</li>
</ul>

**Kind**: instance method of [<code>CommitStrategyBasic</code>](#CommitStrategyBasic)  

| Param | Description |
| --- | --- |
| a | <p>The first commit to compare.</p> |
| b | <p>The second commit to compare.</p> |

<a name="HubError"></a>

## HubError
<p>Represents an error returned by an Identity Hub.</p>

**Kind**: global class  

* [HubError](#HubError)
    * _instance_
        * [.getErrorCode()](#HubError+getErrorCode)
        * [.getTarget()](#HubError+getTarget)
        * [.getRawError()](#HubError+getRawError)
    * _static_
        * [.is()](#HubError.is)

<a name="HubError+getErrorCode"></a>

### hubError.getErrorCode()
<p>Returns the error code given by the Hub.</p>

**Kind**: instance method of [<code>HubError</code>](#HubError)  
<a name="HubError+getTarget"></a>

### hubError.getTarget()
<p>Returns the error target (e.g. the property which is invalid).</p>

**Kind**: instance method of [<code>HubError</code>](#HubError)  
<a name="HubError+getRawError"></a>

### hubError.getRawError()
<p>Returns the raw error JSON as provided by the Hub.</p>

**Kind**: instance method of [<code>HubError</code>](#HubError)  
<a name="HubError.is"></a>

### HubError.is()
<p>Indicates whether the passed-in object is a HubError instance.</p>

**Kind**: static method of [<code>HubError</code>](#HubError)  
<a name="HubSession"></a>

## HubSession
<p>Represents a communication session with a particular Hub instance.</p>

**Kind**: global class  

* [HubSession](#HubSession)
    * _instance_
        * [.send(request)](#HubSession+send)
        * [.makeRequest(message, accessToken)](#HubSession+makeRequest)
        * [.callFetch(url, init)](#HubSession+callFetch)
        * [.getAccessToken()](#HubSession+getAccessToken)
        * [.refreshAccessToken()](#HubSession+refreshAccessToken)
    * _static_
        * [.mapResponseToObject(response)](#HubSession.mapResponseToObject)

<a name="HubSession+send"></a>

### hubSession.send(request)
<p>Sends the given request to the Hub instance, and returns the associated response.</p>

**Kind**: instance method of [<code>HubSession</code>](#HubSession)  

| Param | Description |
| --- | --- |
| request | <p>An instance or subclass of HubRequest to be sent.</p> |

<a name="HubSession+makeRequest"></a>

### hubSession.makeRequest(message, accessToken)
<p>Sends a raw (string) request body to the Hub and receives a response.</p>

**Kind**: instance method of [<code>HubSession</code>](#HubSession)  

| Param | Description |
| --- | --- |
| message | <p>The raw request body to send.</p> |
| accessToken | <p>The access token to include in the request, if any.</p> |

<a name="HubSession+callFetch"></a>

### hubSession.callFetch(url, init)
<p>Fetch API wrapper, to allow unit testing.</p>

**Kind**: instance method of [<code>HubSession</code>](#HubSession)  

| Param | Description |
| --- | --- |
| url | <p>The URL to make a request to.</p> |
| init | <p>Request initialization details.</p> |

<a name="HubSession+getAccessToken"></a>

### hubSession.getAccessToken()
<p>Returns the current access token for the Hub, requesting one if necessary.</p>

**Kind**: instance method of [<code>HubSession</code>](#HubSession)  
<a name="HubSession+refreshAccessToken"></a>

### hubSession.refreshAccessToken()
<p>Requests an updated access token from the Hub.</p>

**Kind**: instance method of [<code>HubSession</code>](#HubSession)  
<a name="HubSession.mapResponseToObject"></a>

### HubSession.mapResponseToObject(response)
<p>Transforms a JSON blob returned by the Hub into a subclass of HubResponse, based on the <code>@type</code>
field of the response.</p>

**Kind**: static method of [<code>HubSession</code>](#HubSession)  

| Param | Description |
| --- | --- |
| response | <p>The Hub response to be transformed.</p> |

<a name="SignedCommit"></a>

## SignedCommit
<p>Class representing a signed commit.</p>

**Kind**: global class  

* [SignedCommit](#SignedCommit)
    * [.toFlattenedJson()](#SignedCommit+toFlattenedJson)
    * [.getProtectedHeaders()](#SignedCommit+getProtectedHeaders)
    * [.getPayload()](#SignedCommit+getPayload)
    * [.getRevision()](#SignedCommit+getRevision)
    * [.getObjectId()](#SignedCommit+getObjectId)

<a name="SignedCommit+toFlattenedJson"></a>

### signedCommit.toFlattenedJson()
<p>Returns the signed commit data in the Flattened JWS JSON Serialization.</p>

**Kind**: instance method of [<code>SignedCommit</code>](#SignedCommit)  
<a name="SignedCommit+getProtectedHeaders"></a>

### signedCommit.getProtectedHeaders()
<p>Returns the decoded protected headers for this commit.</p>

**Kind**: instance method of [<code>SignedCommit</code>](#SignedCommit)  
<a name="SignedCommit+getPayload"></a>

### signedCommit.getPayload()
<p>Returns the decoded payload for this commit.</p>

**Kind**: instance method of [<code>SignedCommit</code>](#SignedCommit)  
<a name="SignedCommit+getRevision"></a>

### signedCommit.getRevision()
<p>Retrieves the revision ID for this commit.</p>

**Kind**: instance method of [<code>SignedCommit</code>](#SignedCommit)  
<a name="SignedCommit+getObjectId"></a>

### signedCommit.getObjectId()
<p>Retrieves the ID of the object to which this commit belongs.</p>

**Kind**: instance method of [<code>SignedCommit</code>](#SignedCommit)  
<a name="KeyStoreConstants"></a>

## KeyStoreConstants
<p>Class for key storage constants</p>

**Kind**: global class  
<a name="Multihash"></a>

## Multihash
<p>Class that performs hashing operations using the multihash format.</p>

**Kind**: global class  
<a name="Multihash.hash"></a>

### Multihash.hash()
<p>Hashes the content using the hashing algorithm specified by the latest protocol version.</p>

**Kind**: static method of [<code>Multihash</code>](#Multihash)  
<a name="SidetreeRegistrar"></a>

## SidetreeRegistrar
<p>Registrar implementation for the Sidetree (ION) network</p>

**Kind**: global class  

* [SidetreeRegistrar](#SidetreeRegistrar)
    * [new SidetreeRegistrar(url, options)](#new_SidetreeRegistrar_new)
    * [.prepareDocForRegistration(document)](#SidetreeRegistrar+prepareDocForRegistration)
    * [.register(identifierDocument, keyReference)](#SidetreeRegistrar+register)
    * [.generateIdentifier(identifierDocument)](#SidetreeRegistrar+generateIdentifier)

<a name="new_SidetreeRegistrar_new"></a>

### new SidetreeRegistrar(url, options)
<p>Constructs a new instance of the Sidetree registrar</p>


| Param | Description |
| --- | --- |
| url | <p>to the registration endpoint at the registrar</p> |
| options | <p>to configure the registrar.</p> |

<a name="SidetreeRegistrar+prepareDocForRegistration"></a>

### sidetreeRegistrar.prepareDocForRegistration(document)
<p>Prepare the document for registration</p>

**Kind**: instance method of [<code>SidetreeRegistrar</code>](#SidetreeRegistrar)  

| Param | Description |
| --- | --- |
| document | <p>Document to format</p> |

<a name="SidetreeRegistrar+register"></a>

### sidetreeRegistrar.register(identifierDocument, keyReference)
<p>Registers the identifier document on the ledger
returning the identifier generated by the registrar.</p>

**Kind**: instance method of [<code>SidetreeRegistrar</code>](#SidetreeRegistrar)  

| Param | Description |
| --- | --- |
| identifierDocument | <p>to register.</p> |
| keyReference | <p>Reference to the identifier for the signing key.</p> |

<a name="SidetreeRegistrar+generateIdentifier"></a>

### sidetreeRegistrar.generateIdentifier(identifierDocument)
<p>Uses the specified input to create a basic Sidetree
compliant identifier document and then hashes the document
in accordance with the Sidetree protocol specification
to generate and return the identifier.</p>

**Kind**: instance method of [<code>SidetreeRegistrar</code>](#SidetreeRegistrar)  

| Param | Description |
| --- | --- |
| identifierDocument | <p>for which to generate the identifier.</p> |

<a name="HttpResolver"></a>

## HttpResolver
<p>Fetches DID Documents from remote resolvers over http</p>

**Kind**: global class  
**Implements**: <code>Resolver</code>  

* [HttpResolver](#HttpResolver)
    * [new HttpResolver(url, [options])](#new_HttpResolver_new)
    * [.resolve(identifier)](#HttpResolver+resolve)

<a name="new_HttpResolver_new"></a>

### new HttpResolver(url, [options])
<p>Constructs an instance of the HttpResolver class.</p>


| Param | Description |
| --- | --- |
| url | <p>of the remote resolver.</p> |
| [options] | <p>for configuring the resolver.</p> |

<a name="HttpResolver+resolve"></a>

### httpResolver.resolve(identifier)
<p>Sends a fetch request to the resolver URL including the
specified identifier.</p>

**Kind**: instance method of [<code>HttpResolver</code>](#HttpResolver)  

| Param | Description |
| --- | --- |
| identifier | <p>to resolve.</p> |

<a name="HostServiceEndpoint"></a>

## HostServiceEndpoint
<p>class to represent a host service endpoint.</p>

**Kind**: global class  

* [HostServiceEndpoint](#HostServiceEndpoint)
    * [new HostServiceEndpoint()](#new_HostServiceEndpoint_new)
    * _instance_
        * [.toJSON()](#HostServiceEndpoint+toJSON)
    * _static_
        * [.fromJSON()](#HostServiceEndpoint.fromJSON)

<a name="new_HostServiceEndpoint_new"></a>

### new HostServiceEndpoint()
<p>locations of the hubs.</p>

<a name="HostServiceEndpoint+toJSON"></a>

### hostServiceEndpoint.toJSON()
<p>Used to control the the properties that are
output by JSON.stringify.</p>

**Kind**: instance method of [<code>HostServiceEndpoint</code>](#HostServiceEndpoint)  
<a name="HostServiceEndpoint.fromJSON"></a>

### HostServiceEndpoint.fromJSON()
<p>Used to control the the properties that are
output by JSON.parse.</p>

**Kind**: static method of [<code>HostServiceEndpoint</code>](#HostServiceEndpoint)  
<a name="ServiceEndpoint"></a>

## ServiceEndpoint
<p>abstract class to represent a service endpoint.
based on: https://github.com/decentralized-identity/identity-hub/blob/master/explainer.md.</p>

**Kind**: global class  
<a name="new_ServiceEndpoint_new"></a>

### new ServiceEndpoint()
<p>The type of the service reference.</p>

<a name="UserServiceEndpoint"></a>

## UserServiceEndpoint
<p>Class to represent a host service endpoint.</p>

**Kind**: global class  

* [UserServiceEndpoint](#UserServiceEndpoint)
    * [new UserServiceEndpoint()](#new_UserServiceEndpoint_new)
    * _instance_
        * [.toJSON()](#UserServiceEndpoint+toJSON)
    * _static_
        * [.fromJSON()](#UserServiceEndpoint.fromJSON)

<a name="new_UserServiceEndpoint_new"></a>

### new UserServiceEndpoint()
<p>locations of the hubs.</p>

<a name="UserServiceEndpoint+toJSON"></a>

### userServiceEndpoint.toJSON()
<p>Used to control the the properties that are
output by JSON.stringify.</p>

**Kind**: instance method of [<code>UserServiceEndpoint</code>](#UserServiceEndpoint)  
<a name="UserServiceEndpoint.fromJSON"></a>

### UserServiceEndpoint.fromJSON()
<p>Used to control the the properties that are
output by JSON.parse.</p>

**Kind**: static method of [<code>UserServiceEndpoint</code>](#UserServiceEndpoint)  
<a name="CredentialType"></a>

## CredentialType
<p>Enumeration of the supported credential types.</p>

**Kind**: global variable  
<a name="CredentialType"></a>

## CredentialType
<p>Interface defining common properties and
methods of a credential.</p>

**Kind**: global variable  
<a name="HubClientOptions"></a>

## HubClientOptions
<p>Class for doing CRUD operations to Actions, Collections, Permissions, and Profile
In a Hub.</p>

**Kind**: global variable  
<a name="HubClientOptions"></a>

## HubClientOptions
<p>Interface for HubClient class that manages which hub instance to create hub session with
And commits and queries for objects in the hub session.</p>

**Kind**: global variable  
<a name="CommitStrategyType"></a>

## CommitStrategyType
<p>Constants that represent what type of commit strategy to be used.</p>

**Kind**: global variable  
<a name="CommitStrategyType"></a>

## CommitStrategyType
<p>Constants that represent what interface type the hub request payload will be.</p>

**Kind**: global variable  
<a name="HubInterfaceType"></a>

## HubInterfaceType
<p>Hub Operations</p>

**Kind**: global variable  
<a name="Operation"></a>

## Operation
<p>Interface for defining options for HubMethods such as hubSession, commitSigner, and hubInterface.</p>

**Kind**: global variable  
<a name="HubInterfaceOptions"></a>

## HubInterfaceOptions
<p>An Abstract Class for Hub Interfaces.</p>

**Kind**: global variable  
<a name="context"></a>

## context
<p>context for credentialManifest</p>

**Kind**: global constant  
<a name="type"></a>

## type
<p>type for credentialManifest</p>

**Kind**: global constant  
