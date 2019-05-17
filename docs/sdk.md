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
<dt><a href="#CryptoOptions">CryptoOptions</a></dt>
<dd><p>Class used to model crypto options</p></dd>
<dt><a href="#HubClient">HubClient</a></dt>
<dd><p>Class for doing CRUD operations to Actions, Collections, Permissions, and Profile
In a Hub.</p></dd>
<dt><a href="#Actions">Actions</a></dt>
<dd><p>A Class that represents objects</p></dd>
<dt><a href="#Collections">Collections</a></dt>
<dd><p>A Class that does CRUD operations for storing items as Collections in the Hub</p></dd>
<dt><a href="#HubMethods">HubMethods</a></dt>
<dd><p>An Abstract Class for HubMethods.</p></dd>
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
<dt><a href="#CommitSigner">CommitSigner</a></dt>
<dd><p>Class which can apply a signature to a commit.</p></dd>
<dt><a href="#HubError">HubError</a></dt>
<dd><p>Represents an error returned by an Identity Hub.</p></dd>
<dt><a href="#HubSession">HubSession</a></dt>
<dd><p>Represents a communication session with a particular Hub instance.</p></dd>
<dt><a href="#HubCommitQueryRequest">HubCommitQueryRequest</a></dt>
<dd><p>Represents a request to a Hub for a set of commits.</p></dd>
<dt><a href="#HubCommitWriteRequest">HubCommitWriteRequest</a></dt>
<dd><p>Represents a request to commit the given Commit object to an Identity Hub.</p></dd>
<dt><a href="#HubObjectQueryRequest">HubObjectQueryRequest</a></dt>
<dd><p>Represents a request to a Hub to query the available objects.</p></dd>
<dt><a href="#HubRequest">HubRequest</a></dt>
<dd><p>The base class for all requests to an Identity Hub.</p></dd>
<dt><a href="#HubCommitQueryResponse">HubCommitQueryResponse</a></dt>
<dd><p>Represents the response to a <code>HubCommitQueryRequest</code>.</p></dd>
<dt><a href="#HubObjectQueryResponse">HubObjectQueryResponse</a></dt>
<dd><p>Represents the response to a <code>HubObjectQueryRequest</code>.</p></dd>
<dt><a href="#HubWriteResponse">HubWriteResponse</a></dt>
<dd><p>Represents the response to a <code>HubWriteRequest</code>.</p></dd>
<dt><a href="#SignedCommit">SignedCommit</a></dt>
<dd><p>Class representing a signed commit.</p></dd>
<dt><a href="#Identifier">Identifier</a></dt>
<dd><p>Class for creating and managing identifiers,
retrieving identifier documents.</p></dd>
<dt><a href="#IdentifierDocument">IdentifierDocument</a></dt>
<dd><p>Class for creating and managing identifiers,
retrieving identifier documents.</p></dd>
<dt><a href="#InMemoryKeyStore">InMemoryKeyStore</a></dt>
<dd><p>An encrypted in memory implementation of IKeyStore using PouchDB
and memdown. As soon as the process ends or
the reference to the store is released all data is discarded.</p>
<p>This implementation is intended as a batteries included approach
to allow simple testing and experimentation with the UserAgent SDK.</p></dd>
<dt><a href="#KeyStoreConstants">KeyStoreConstants</a></dt>
<dd><p>Class for key storage constants</p></dd>
<dt><a href="#Protect">Protect</a></dt>
<dd><p>Class to model protection mechanisms</p></dd>
<dt><a href="#Multihash">Multihash</a></dt>
<dd><p>Class that performs hashing operations using the multihash format.</p></dd>
<dt><a href="#SidetreeRegistrar">SidetreeRegistrar</a></dt>
<dd><p>Registrar implementation for the Sidetree (ION) network</p></dd>
<dt><a href="#HttpResolver">HttpResolver</a></dt>
<dd><p>Fetches DID Documents from remote resolvers over http</p></dd>
<dt><a href="#UserAgentError">UserAgentError</a></dt>
<dd><p>Base error class for the UserAgent.</p></dd>
<dt><a href="#UserAgentOptions">UserAgentOptions</a></dt>
<dd><p>Interface defining options for the
User Agent, such as resolver and register.</p></dd>
</dl>

## Members

<dl>
<dt><a href="#CredentialType">CredentialType</a></dt>
<dd><p>Enumeration of the supported credential types.</p></dd>
<dt><a href="#CredentialType">CredentialType</a></dt>
<dd><p>Interface defining common properties and
methods of a credential.</p></dd>
<dt><a href="#CommitStrategyReference">CommitStrategyReference</a></dt>
<dd><p>Constants that represent what type of commit strategy to be used.</p></dd>
<dt><a href="#CommitStrategyReference">CommitStrategyReference</a></dt>
<dd><p>Constants that represent what interface type the hub request payload will be.</p></dd>
<dt><a href="#HubInterface">HubInterface</a></dt>
<dd><p>Interface for defining options for HubMethods such as hubSession, commitSigner, and hubInterface.</p></dd>
<dt><a href="#SignatureFormat">SignatureFormat</a></dt>
<dd><p>Enum to define different signature formats</p></dd>
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

<a name="CryptoOptions"></a>

## CryptoOptions
<p>Class used to model crypto options</p>

**Kind**: global class  
<a name="HubClient"></a>

## HubClient
<p>Class for doing CRUD operations to Actions, Collections, Permissions, and Profile
In a Hub.</p>

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
<a name="Actions"></a>

## Actions
<p>A Class that represents objects</p>

**Kind**: global class  
<a name="Collections"></a>

## Collections
<p>A Class that does CRUD operations for storing items as Collections in the Hub</p>

**Kind**: global class  
<a name="HubMethods"></a>

## HubMethods
<p>An Abstract Class for HubMethods.</p>

**Kind**: global class  
<a name="new_HubMethods_new"></a>

### new HubMethods([hubMethodOptions])
<p>Creates an instance of HubMethods that will be used to send hub requests and responses.</p>


| Param | Description |
| --- | --- |
| [hubMethodOptions] | <p>for configuring how to form hub requests and responses.</p> |

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

<a name="CommitSigner"></a>

## CommitSigner
<p>Class which can apply a signature to a commit.</p>

**Kind**: global class  
<a name="CommitSigner+sign"></a>

### commitSigner.sign(commit)
<p>Signs the given commit.</p>

**Kind**: instance method of [<code>CommitSigner</code>](#CommitSigner)  

| Param | Description |
| --- | --- |
| commit | <p>The commit to sign.</p> |

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

<a name="HubCommitQueryRequest"></a>

## HubCommitQueryRequest
<p>Represents a request to a Hub for a set of commits.</p>

**Kind**: global class  
<a name="HubCommitWriteRequest"></a>

## HubCommitWriteRequest
<p>Represents a request to commit the given Commit object to an Identity Hub.</p>

**Kind**: global class  
<a name="HubObjectQueryRequest"></a>

## HubObjectQueryRequest
<p>Represents a request to a Hub to query the available objects.</p>

**Kind**: global class  
<a name="HubRequest"></a>

## HubRequest
<p>The base class for all requests to an Identity Hub.</p>

**Kind**: global class  
<a name="HubRequest+getRequestJson"></a>

### hubRequest.getRequestJson()
<p>Returns the raw request JSON which will be sent to the Hub.</p>

**Kind**: instance method of [<code>HubRequest</code>](#HubRequest)  
<a name="HubCommitQueryResponse"></a>

## HubCommitQueryResponse
<p>Represents the response to a <code>HubCommitQueryRequest</code>.</p>

**Kind**: global class  

* [HubCommitQueryResponse](#HubCommitQueryResponse)
    * [.getCommits()](#HubCommitQueryResponse+getCommits)
    * [.hasSkipToken()](#HubCommitQueryResponse+hasSkipToken)
    * [.getSkipToken()](#HubCommitQueryResponse+getSkipToken)

<a name="HubCommitQueryResponse+getCommits"></a>

### hubCommitQueryResponse.getCommits()
<p>Returns the set of commits returned by the Hub.</p>

**Kind**: instance method of [<code>HubCommitQueryResponse</code>](#HubCommitQueryResponse)  
<a name="HubCommitQueryResponse+hasSkipToken"></a>

### hubCommitQueryResponse.hasSkipToken()
<p>Indicates whether additional pages of results are available.</p>

**Kind**: instance method of [<code>HubCommitQueryResponse</code>](#HubCommitQueryResponse)  
<a name="HubCommitQueryResponse+getSkipToken"></a>

### hubCommitQueryResponse.getSkipToken()
<p>Retrieves a token which can be used to fetch subsequent result pages.</p>

**Kind**: instance method of [<code>HubCommitQueryResponse</code>](#HubCommitQueryResponse)  
<a name="HubObjectQueryResponse"></a>

## HubObjectQueryResponse
<p>Represents the response to a <code>HubObjectQueryRequest</code>.</p>

**Kind**: global class  

* [HubObjectQueryResponse](#HubObjectQueryResponse)
    * [.getObjects()](#HubObjectQueryResponse+getObjects)
    * [.hasSkipToken()](#HubObjectQueryResponse+hasSkipToken)
    * [.getSkipToken()](#HubObjectQueryResponse+getSkipToken)

<a name="HubObjectQueryResponse+getObjects"></a>

### hubObjectQueryResponse.getObjects()
<p>Returns the set of objects returned by the Hub.</p>
<p>NEED TO Map JSON into useful objects, as done for commits.</p>

**Kind**: instance method of [<code>HubObjectQueryResponse</code>](#HubObjectQueryResponse)  
<a name="HubObjectQueryResponse+hasSkipToken"></a>

### hubObjectQueryResponse.hasSkipToken()
<p>Indicates whether additional pages of results are available.</p>

**Kind**: instance method of [<code>HubObjectQueryResponse</code>](#HubObjectQueryResponse)  
<a name="HubObjectQueryResponse+getSkipToken"></a>

### hubObjectQueryResponse.getSkipToken()
<p>Retrieves a token which can be used to fetch subsequent result pages.</p>

**Kind**: instance method of [<code>HubObjectQueryResponse</code>](#HubObjectQueryResponse)  
<a name="HubWriteResponse"></a>

## HubWriteResponse
<p>Represents the response to a <code>HubWriteRequest</code>.</p>

**Kind**: global class  
<a name="HubWriteResponse+getRevisions"></a>

### hubWriteResponse.getRevisions()
<p>Returns the list of known revisions for the object which was created/modified.</p>

**Kind**: instance method of [<code>HubWriteResponse</code>](#HubWriteResponse)  
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
<a name="Identifier"></a>

## Identifier
<p>Class for creating and managing identifiers,
retrieving identifier documents.</p>

**Kind**: global class  

* [Identifier](#Identifier)
    * [new Identifier(identifier, [options])](#new_Identifier_new)
    * _instance_
        * [.createLinkedIdentifier(target, register)](#Identifier+createLinkedIdentifier)
        * [.getDocument()](#Identifier+getDocument)
        * [.getPublicKey(keyIdentifier)](#Identifier+getPublicKey)
        * [.sign(payload, keyStorageIdentifier)](#Identifier+sign)
        * [.verify(jws)](#Identifier+verify)
    * _static_
        * [.create([options])](#Identifier.create)
        * [.keyStorageIdentifier(personaId, target, keyUse, keyType)](#Identifier.keyStorageIdentifier)

<a name="new_Identifier_new"></a>

### new Identifier(identifier, [options])
<p>Constructs an instance of the Identifier
class using the provided identifier or identifier document.</p>


| Param | Description |
| --- | --- |
| identifier | <p>either the string representation of an identifier or a identifier document.</p> |
| [options] | <p>for configuring how to register and resolve identifiers.</p> |

<a name="Identifier+createLinkedIdentifier"></a>

### identifier.createLinkedIdentifier(target, register)
<p>Creates a new decentralized identifier, using the current identifier
and the specified target. If the registar flag is true, the newly created
identifier will be registered using the</p>

**Kind**: instance method of [<code>Identifier</code>](#Identifier)  

| Param | Default | Description |
| --- | --- | --- |
| target |  | <p>entity for which to create the linked identifier</p> |
| register | <code>false</code> | <p>flag indicating whether the new identifier should be registered with a ledger.</p> |

<a name="Identifier+getDocument"></a>

### identifier.getDocument()
<p>Gets the IdentifierDocument for the identifier
instance, throwing if no identifier has been
created.</p>

**Kind**: instance method of [<code>Identifier</code>](#Identifier)  
<a name="Identifier+getPublicKey"></a>

### identifier.getPublicKey(keyIdentifier)
<p>Performs a public key lookup using the
specified key identifier, returning the
key defined in document.</p>

**Kind**: instance method of [<code>Identifier</code>](#Identifier)  

| Param | Description |
| --- | --- |
| keyIdentifier | <p>the identifier of the public key.</p> |

<a name="Identifier+sign"></a>

### identifier.sign(payload, keyStorageIdentifier)
<p>Sign payload with key specified by keyStorageIdentifier in options.keyStore</p>

**Kind**: instance method of [<code>Identifier</code>](#Identifier)  

| Param | Description |
| --- | --- |
| payload | <p>object to be signed</p> |
| keyStorageIdentifier | <p>the identifier for the key used to sign payload.</p> |

<a name="Identifier+verify"></a>

### identifier.verify(jws)
<p>Verify the payload with public key from the Identifier Document.</p>

**Kind**: instance method of [<code>Identifier</code>](#Identifier)  

| Param | Description |
| --- | --- |
| jws | <p>the signed token to be verified.</p> |

<a name="Identifier.create"></a>

### Identifier.create([options])
<p>Creates a new decentralized identifier.</p>

**Kind**: static method of [<code>Identifier</code>](#Identifier)  

| Param | Description |
| --- | --- |
| [options] | <p>for configuring how to register and resolve identifiers.</p> |

<a name="Identifier.keyStorageIdentifier"></a>

### Identifier.keyStorageIdentifier(personaId, target, keyUse, keyType)
<p>Generate a storage identifier to store a key</p>

**Kind**: static method of [<code>Identifier</code>](#Identifier)  

| Param | Description |
| --- | --- |
| personaId | <p>The identifier for the persona</p> |
| target | <p>The identifier for the peer. Will be persona for non-pairwise keys</p> |
| keyUse | <p>Key usage</p> |
| keyType | <p>Key type</p> |

<a name="IdentifierDocument"></a>

## IdentifierDocument
<p>Class for creating and managing identifiers,
retrieving identifier documents.</p>

**Kind**: global class  

* [IdentifierDocument](#IdentifierDocument)
    * [new IdentifierDocument(document, options)](#new_IdentifierDocument_new)
    * _instance_
        * [.addAuthenticationReference(authenticationReference)](#IdentifierDocument+addAuthenticationReference)
        * [.addServiceReference(serviceReference)](#IdentifierDocument+addServiceReference)
        * [.getHubInstances()](#IdentifierDocument+getHubInstances)
        * [.getHubLocations()](#IdentifierDocument+getHubLocations)
        * [.toJSON()](#IdentifierDocument+toJSON)
    * _static_
        * [.create(publicKeys)](#IdentifierDocument.create)
        * [.createAndGenerateId(idBase, publicKeys, options)](#IdentifierDocument.createAndGenerateId)
        * [.fromJSON()](#IdentifierDocument.fromJSON)

<a name="new_IdentifierDocument_new"></a>

### new IdentifierDocument(document, options)
<p>Constructs an instance of the identifier
document.</p>


| Param | Description |
| --- | --- |
| document | <p>from which to create the identifier document.</p> |
| options | <p>for configuring how to register and resolve identifiers.</p> |

<a name="IdentifierDocument+addAuthenticationReference"></a>

### identifierDocument.addAuthenticationReference(authenticationReference)
<p>Adds an authentication reference to the document.</p>

**Kind**: instance method of [<code>IdentifierDocument</code>](#IdentifierDocument)  

| Param | Description |
| --- | --- |
| authenticationReference | <p>to add to the document.</p> |

<a name="IdentifierDocument+addServiceReference"></a>

### identifierDocument.addServiceReference(serviceReference)
<p>Adds a service reference to the document.</p>

**Kind**: instance method of [<code>IdentifierDocument</code>](#IdentifierDocument)  

| Param | Description |
| --- | --- |
| serviceReference | <p>to add to the document.</p> |

<a name="IdentifierDocument+getHubInstances"></a>

### identifierDocument.getHubInstances()
<p>Get Hub Instances from Identity Service Reference.</p>

**Kind**: instance method of [<code>IdentifierDocument</code>](#IdentifierDocument)  
<a name="IdentifierDocument+getHubLocations"></a>

### identifierDocument.getHubLocations()
<p>Get Hub Locations from Identity Service Reference.</p>

**Kind**: instance method of [<code>IdentifierDocument</code>](#IdentifierDocument)  
<a name="IdentifierDocument+toJSON"></a>

### identifierDocument.toJSON()
<p>Used to control the the properties that are
output by JSON.stringify.</p>

**Kind**: instance method of [<code>IdentifierDocument</code>](#IdentifierDocument)  
<a name="IdentifierDocument.create"></a>

### IdentifierDocument.create(publicKeys)
<p>Creates a new instance of an identifier document using the
provided public keys.</p>

**Kind**: static method of [<code>IdentifierDocument</code>](#IdentifierDocument)  

| Param | Description |
| --- | --- |
| publicKeys | <p>to include in the document.</p> |

<a name="IdentifierDocument.createAndGenerateId"></a>

### IdentifierDocument.createAndGenerateId(idBase, publicKeys, options)
<p>Creates a new instance of an identifier document using the
provided public keys.
The id is generated.</p>

**Kind**: static method of [<code>IdentifierDocument</code>](#IdentifierDocument)  

| Param | Type | Description |
| --- | --- | --- |
| idBase | <code>method</code> | <p>The base id in format did::{id}. {id} will be filled in by this method</p> |
| publicKeys |  | <p>to include in the document.</p> |
| options |  | <p>User agent options containing the crypto Api</p> |

<a name="IdentifierDocument.fromJSON"></a>

### IdentifierDocument.fromJSON()
<p>Used to control the the properties that are
output by JSON.parse.</p>

**Kind**: static method of [<code>IdentifierDocument</code>](#IdentifierDocument)  
<a name="InMemoryKeyStore"></a>

## InMemoryKeyStore
<p>An encrypted in memory implementation of IKeyStore using PouchDB
and memdown. As soon as the process ends or
the reference to the store is released all data is discarded.</p>
<p>This implementation is intended as a batteries included approach
to allow simple testing and experimentation with the UserAgent SDK.</p>

**Kind**: global class  

* [InMemoryKeyStore](#InMemoryKeyStore)
    * [new InMemoryKeyStore([encryptionKey])](#new_InMemoryKeyStore_new)
    * [.getKey(keyIdentifier)](#InMemoryKeyStore+getKey)
    * [.save(keyIdentifier, key)](#InMemoryKeyStore+save)
    * [.sign(keyIdentifier, data, format)](#InMemoryKeyStore+sign)

<a name="new_InMemoryKeyStore_new"></a>

### new InMemoryKeyStore([encryptionKey])
<p>Constructs an instance of the in memory key store
optionally encrypting the contents of the store
using the specified encryption key.</p>


| Param | Description |
| --- | --- |
| [encryptionKey] | <p>a 32 byte buffer that will be used as the key or a string which will be used to generate one.</p> |

<a name="InMemoryKeyStore+getKey"></a>

### inMemoryKeyStore.getKey(keyIdentifier)
<p>Gets the key from the store using the specified identifier.</p>

**Kind**: instance method of [<code>InMemoryKeyStore</code>](#InMemoryKeyStore)  

| Param | Description |
| --- | --- |
| keyIdentifier | <p>for which to return the key.</p> |

<a name="InMemoryKeyStore+save"></a>

### inMemoryKeyStore.save(keyIdentifier, key)
<p>Saves the specified key to the store using the key identifier.</p>

**Kind**: instance method of [<code>InMemoryKeyStore</code>](#InMemoryKeyStore)  

| Param | Description |
| --- | --- |
| keyIdentifier | <p>to store the key against</p> |
| key | <p>the key to store.</p> |

<a name="InMemoryKeyStore+sign"></a>

### inMemoryKeyStore.sign(keyIdentifier, data, format)
<p>Sign the data with the key referenced by keyIdentifier.</p>

**Kind**: instance method of [<code>InMemoryKeyStore</code>](#InMemoryKeyStore)  

| Param | Description |
| --- | --- |
| keyIdentifier | <p>for the key used for signature.</p> |
| data | <p>Data to sign</p> |
| format | <p>Signature format</p> |

<a name="KeyStoreConstants"></a>

## KeyStoreConstants
<p>Class for key storage constants</p>

**Kind**: global class  
<a name="Protect"></a>

## Protect
<p>Class to model protection mechanisms</p>

**Kind**: global class  

* [Protect](#Protect)
    * [.sign(body)](#Protect.sign)
    * [.verify(jws, jwk)](#Protect.verify)

<a name="Protect.sign"></a>

### Protect.sign(body)
<p>Sign the body for the registar</p>

**Kind**: static method of [<code>Protect</code>](#Protect)  

| Param | Description |
| --- | --- |
| body | <p>Body to sign</p> |

<a name="Protect.verify"></a>

### Protect.verify(jws, jwk)
<p>Verify the jws</p>

**Kind**: static method of [<code>Protect</code>](#Protect)  

| Param | Description |
| --- | --- |
| jws | <p>token to be verified</p> |
| jwk | <p>Public Key to be used to verify</p> |

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

<a name="UserAgentError"></a>

## UserAgentError
<p>Base error class for the UserAgent.</p>

**Kind**: global class  
<a name="UserAgentOptions"></a>

## UserAgentOptions
<p>Interface defining options for the
User Agent, such as resolver and register.</p>

**Kind**: global class  
<a name="CredentialType"></a>

## CredentialType
<p>Enumeration of the supported credential types.</p>

**Kind**: global variable  
<a name="CredentialType"></a>

## CredentialType
<p>Interface defining common properties and
methods of a credential.</p>

**Kind**: global variable  
<a name="CommitStrategyReference"></a>

## CommitStrategyReference
<p>Constants that represent what type of commit strategy to be used.</p>

**Kind**: global variable  
<a name="CommitStrategyReference"></a>

## CommitStrategyReference
<p>Constants that represent what interface type the hub request payload will be.</p>

**Kind**: global variable  
<a name="HubInterface"></a>

## HubInterface
<p>Interface for defining options for HubMethods such as hubSession, commitSigner, and hubInterface.</p>

**Kind**: global variable  
<a name="SignatureFormat"></a>

## SignatureFormat
<p>Enum to define different signature formats</p>

**Kind**: global variable  
<a name="context"></a>

## context
<p>context for credentialManifest</p>

**Kind**: global constant  
<a name="type"></a>

## type
<p>type for credentialManifest</p>

**Kind**: global constant  
