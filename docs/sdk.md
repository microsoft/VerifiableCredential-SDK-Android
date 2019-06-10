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
<dt><a href="#EcPairwiseKey">EcPairwiseKey</a></dt>
<dd><p>Class to model EC pairwise keys</p></dd>
<dt><a href="#EcPrivateKey">EcPrivateKey</a> ⇐ <code><a href="#PrivateKey">PrivateKey</a></code></dt>
<dd><p>Represents an Elliptic Curve private key</p></dd>
<dt><a href="#EcPublicKey">EcPublicKey</a> ⇐ <code><a href="#PublicKey">PublicKey</a></code></dt>
<dd><p>Represents an Elliptic Curve public key</p></dd>
<dt><a href="#PairwiseKey">PairwiseKey</a></dt>
<dd><p>Class to model pairwise keys</p></dd>
<dt><a href="#PrivateKey">PrivateKey</a></dt>
<dd><p>Represents a Private Key in JWK format.</p></dd>
<dt><a href="#KeyOperation">KeyOperation</a></dt>
<dd></dd>
<dt><a href="#PublicKey">PublicKey</a></dt>
<dd></dd>
<dt><a href="#RsaPairwiseKey">RsaPairwiseKey</a></dt>
<dd><p>Class to model RSA pairwise keys</p></dd>
<dt><a href="#RsaPrivateKey">RsaPrivateKey</a> ⇐ <code><a href="#PrivateKey">PrivateKey</a></code></dt>
<dd><p>Represents an Elliptic Curve private key</p></dd>
<dt><a href="#RsaPublicKey">RsaPublicKey</a> ⇐ <code><a href="#PublicKey">PublicKey</a></code></dt>
<dd><p>Represents an RSA public key</p></dd>
<dt><a href="#KeyStoreInMemory">KeyStoreInMemory</a></dt>
<dd><p>Class defining methods and properties for a light KeyStore</p></dd>
<dt><a href="#CryptoFactory">CryptoFactory</a></dt>
<dd><p>Utility class to handle all CryptoSuite dependency injection</p></dd>
<dt><a href="#CryptoOperations">CryptoOperations</a></dt>
<dd><p>Interface for the Crypto Algorithms Plugins</p></dd>
<dt><a href="#SubtleCryptoExtension">SubtleCryptoExtension</a></dt>
<dd><p>The class extends the @class SubtleCrypto with addtional methods.
 Adds methods to work with key references.
 Extends SubtleCrypto to work with JWK keys.</p></dd>
<dt><a href="#SubtleCryptoOperations">SubtleCryptoOperations</a></dt>
<dd><p>Default crypto suite implementing the default plugable crypto layer</p></dd>
<dt><a href="#CryptoProtocolError">CryptoProtocolError</a></dt>
<dd><p>Base error class for the crypto protocols.</p></dd>
<dt><a href="#DidProtocol">DidProtocol</a></dt>
<dd><p>Hub Protocol for decrypting/verifying and encrypting/signing payloads</p></dd>
<dt><a href="#JoseConstants">JoseConstants</a></dt>
<dd><p>Class for JOSE constants</p></dd>
<dt><a href="#JoseHelpers">JoseHelpers</a></dt>
<dd><p>Crypto helpers support for plugable crypto layer</p></dd>
<dt><a href="#JweRecipient">JweRecipient</a></dt>
<dd><p>JWS signature used by the general JSON</p></dd>
<dt><a href="#JweToken">JweToken</a></dt>
<dd><p>Class for containing Jwe token operations.
This class hides the JOSE and crypto library dependencies to allow support for additional crypto algorithms.
Crypto calls always happen via CryptoFactory</p></dd>
<dt><a href="#JwsSignature">JwsSignature</a></dt>
<dd><p>JWS signature used by the general JSON</p></dd>
<dt><a href="#JwsToken">JwsToken</a></dt>
<dd><p>Class for containing JWS token operations.
This class hides the JOSE and crypto library dependencies to allow support for additional crypto algorithms.
Crypto calls always happen via CryptoFactory</p></dd>
<dt><a href="#EncryptionStrategy">EncryptionStrategy</a></dt>
<dd><p>Class used to model encryption strategies</p></dd>
<dt><a href="#MessageSigningStrategy">MessageSigningStrategy</a></dt>
<dd><p>Class used to model the message signing strategy</p></dd>
<dt><a href="#CryptoHelpers">CryptoHelpers</a></dt>
<dd><p>Crypto helpers support for plugable crypto layer</p></dd>
<dt><a href="#W3cCryptoApiConstants">W3cCryptoApiConstants</a></dt>
<dd><p>Class for W3C Crypto API constants</p></dd>
<dt><a href="#CryptoOptions">CryptoOptions</a></dt>
<dd><p>Class used to model crypto options</p></dd>
<dt><a href="#HubClient">HubClient</a></dt>
<dd><p>Class for doing CRUD operations to Actions, Collections, Permissions, and Profile
In a Hub.</p></dd>
<dt><a href="#HubObject">HubObject</a></dt>
<dd><p>Class that represents an object in a hub.</p></dd>
<dt><a href="#HubClientOptions">HubClientOptions</a></dt>
<dd><p>Class for defining options for the
HubClient, such as hub Identifier and client Identifier.</p></dd>
<dt><a href="#Actions">Actions</a></dt>
<dd><p>A Class that does CRUD operations for storing objects as Actions in the Hub</p></dd>
<dt><a href="#Collections">Collections</a></dt>
<dd><p>A Class that does CRUD operations for storing objects as Collections in the Hub</p></dd>
<dt><a href="#HubInterface">HubInterface</a></dt>
<dd></dd>
<dt><a href="#Permissions">Permissions</a></dt>
<dd><p>A Class that does CRUD operations for storing items as Permissions in the Hub</p></dd>
<dt><a href="#Profile">Profile</a></dt>
<dd><p>A Class that does CRUD operations for storing objects as Profile in the Hub.</p></dd>
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
<dt><a href="#HubSessionOptions">HubSessionOptions</a></dt>
<dd><p>Options for instantiating a new Hub session.</p></dd>
<dt><a href="#KeyStoreConstants">KeyStoreConstants</a></dt>
<dd><p>Class for key storage constants</p></dd>
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
<dt><a href="#UserAgentError">UserAgentError</a></dt>
<dd><p>Base error class for the UserAgent.</p></dd>
<dt><a href="#UserAgentOptions">UserAgentOptions</a></dt>
<dd><p>Interface defining options for the
User Agent, such as resolver and register.</p></dd>
<dt><a href="#UserAgentSession">UserAgentSession</a></dt>
<dd><p>Class for creating a User Agent Session for sending and verifying
Authentication Requests and Responses.</p></dd>
</dl>

## Members

<dl>
<dt><a href="#CredentialType">CredentialType</a></dt>
<dd><p>Enumeration of the supported credential types.</p></dd>
<dt><a href="#CredentialType">CredentialType</a></dt>
<dd><p>Interface defining common properties and
methods of a credential.</p></dd>
<dt><a href="#KeyType">KeyType</a></dt>
<dd><p>Enumeration to model key types.</p></dd>
<dt><a href="#KeyType">KeyType</a></dt>
<dd><p>Factory class to create @enum KeyType objects</p></dd>
<dt><a href="#KeyUse">KeyUse</a></dt>
<dd><p>Enumeration to model key use.</p></dd>
<dt><a href="#KeyUse">KeyUse</a></dt>
<dd><p>Factory class to create @enum KeyUse objects.</p></dd>
<dt><a href="#KeyOperation">KeyOperation</a></dt>
<dd><p>JWK key operations</p></dd>
<dt><a href="#ProtectionFormat">ProtectionFormat</a></dt>
<dd><p>Enum to define different protection formats</p></dd>
<dt><a href="#ProtectionStrategyScope">ProtectionStrategyScope</a></dt>
<dd><p>Class used to model protection strategy</p></dd>
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
<dd><p>Interface for defining options for HubInterface.</p></dd>
<dt><a href="#HubInterfaceOptions">HubInterfaceOptions</a></dt>
<dd><p>An Abstract Class for Hub Interfaces.</p></dd>
<dt><a href="#HubSessionOptions">HubSessionOptions</a></dt>
<dd><p>Represents a communication session with a particular Hub instance.</p></dd>
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

<a name="EcPairwiseKey"></a>

## EcPairwiseKey
<p>Class to model EC pairwise keys</p>

**Kind**: global class  
<a name="EcPairwiseKey.generate"></a>

### EcPairwiseKey.generate(cryptoFactory, personaMasterKey, algorithm, peerId, extractable)
<p>Generate a pairwise key for the specified algorithms</p>

**Kind**: static method of [<code>EcPairwiseKey</code>](#EcPairwiseKey)  

| Param | Description |
| --- | --- |
| cryptoFactory | <p>defining the key store and the used crypto api</p> |
| personaMasterKey | <p>Master key for the current selected persona</p> |
| algorithm | <p>for the key</p> |
| peerId | <p>Id for the peer</p> |
| extractable | <p>True if key is exportable</p> |

<a name="EcPrivateKey"></a>

## EcPrivateKey ⇐ [<code>PrivateKey</code>](#PrivateKey)
<p>Represents an Elliptic Curve private key</p>

**Kind**: global class  
**Extends**: [<code>PrivateKey</code>](#PrivateKey)  

* [EcPrivateKey](#EcPrivateKey) ⇐ [<code>PrivateKey</code>](#PrivateKey)
    * [new EcPrivateKey()](#new_EcPrivateKey_new)
    * [.getPublicKey()](#EcPrivateKey+getPublicKey) ⇒

<a name="new_EcPrivateKey_new"></a>

### new EcPrivateKey()
<p>Create instance of @class EcPrivateKey</p>

<a name="EcPrivateKey+getPublicKey"></a>

### ecPrivateKey.getPublicKey() ⇒
<p>Gets the corresponding public key</p>

**Kind**: instance method of [<code>EcPrivateKey</code>](#EcPrivateKey)  
**Returns**: <p>The corresponding [PublicKey](#PublicKey)</p>  
<a name="EcPublicKey"></a>

## EcPublicKey ⇐ [<code>PublicKey</code>](#PublicKey)
<p>Represents an Elliptic Curve public key</p>

**Kind**: global class  
**Extends**: [<code>PublicKey</code>](#PublicKey)  
<a name="new_EcPublicKey_new"></a>

### new EcPublicKey()
<p>Create instance of @class EcPublicKey</p>

<a name="PairwiseKey"></a>

## PairwiseKey
<p>Class to model pairwise keys</p>

**Kind**: global class  

* [PairwiseKey](#PairwiseKey)
    * [new PairwiseKey(cryptoFactory)](#new_PairwiseKey_new)
    * [.generatePairwiseKey(algorithm, seedReference, personaId, peerId)](#PairwiseKey+generatePairwiseKey)
    * [.generatePersonaMasterKey(seedReference, personaId)](#PairwiseKey+generatePersonaMasterKey)

<a name="new_PairwiseKey_new"></a>

### new PairwiseKey(cryptoFactory)
<p>Create an instance of @class PairwiseKey.</p>


| Param | Description |
| --- | --- |
| cryptoFactory | <p>The crypto factory object.</p> |

<a name="PairwiseKey+generatePairwiseKey"></a>

### pairwiseKey.generatePairwiseKey(algorithm, seedReference, personaId, peerId)
<p>Generate a pairwise key for the specified algorithms</p>

**Kind**: instance method of [<code>PairwiseKey</code>](#PairwiseKey)  

| Param | Description |
| --- | --- |
| algorithm | <p>for the key</p> |
| seedReference | <p>Reference to the seed</p> |
| personaId | <p>Id for the persona</p> |
| peerId | <p>Id for the peer</p> |

<a name="PairwiseKey+generatePersonaMasterKey"></a>

### pairwiseKey.generatePersonaMasterKey(seedReference, personaId)
<p>Generate a pairwise master key.</p>

**Kind**: instance method of [<code>PairwiseKey</code>](#PairwiseKey)  

| Param | Description |
| --- | --- |
| seedReference | <p>The master seed for generating pairwise keys</p> |
| personaId | <p>The owner DID</p> |

<a name="PrivateKey"></a>

## *PrivateKey*
<p>Represents a Private Key in JWK format.</p>

**Kind**: global abstract class  
<a name="KeyOperation"></a>

## *KeyOperation*
**Kind**: global abstract class  
<a name="new_KeyOperation_new"></a>

### *new exports.KeyOperation()*
<p>Represents a Public Key in JWK format.</p>

<a name="PublicKey"></a>

## PublicKey
**Kind**: global class  

* [PublicKey](#PublicKey)
    * [new PublicKey()](#new_PublicKey_new)
    * [.getThumbprint(jwk)](#PublicKey.getThumbprint)

<a name="new_PublicKey_new"></a>

### new PublicKey()
<p>Create instance of @class PublicKey</p>

<a name="PublicKey.getThumbprint"></a>

### PublicKey.getThumbprint(jwk)
<p>Obtains the thumbprint for the jwk parameter</p>

**Kind**: static method of [<code>PublicKey</code>](#PublicKey)  

| Param | Description |
| --- | --- |
| jwk | <p>JSON object representation of a JWK</p> |

<a name="RsaPairwiseKey"></a>

## RsaPairwiseKey
<p>Class to model RSA pairwise keys</p>

**Kind**: global class  

* [RsaPairwiseKey](#RsaPairwiseKey)
    * [.generate(cryptoFactory, personaMasterKey, algorithm, peerId)](#RsaPairwiseKey.generate)
    * [.getPrime()](#RsaPairwiseKey.getPrime)
    * [.generateDeterministicNumberForPrime(cryptoFactory, keySize, personaMasterKey, peerId)](#RsaPairwiseKey.generateDeterministicNumberForPrime)
    * [.generateHashForPrime(crypto, inx, key, data)](#RsaPairwiseKey.generateHashForPrime)
    * [.executeRounds(rounds, inx, key, data)](#RsaPairwiseKey.executeRounds)
    * [.generatePrime(primeSeed)](#RsaPairwiseKey.generatePrime)
    * [.toBase(bigNumber)](#RsaPairwiseKey.toBase)

<a name="RsaPairwiseKey.generate"></a>

### RsaPairwiseKey.generate(cryptoFactory, personaMasterKey, algorithm, peerId)
<p>Generate a pairwise key for the specified algorithms</p>

**Kind**: static method of [<code>RsaPairwiseKey</code>](#RsaPairwiseKey)  

| Param | Description |
| --- | --- |
| cryptoFactory | <p>defining the key store and the used crypto api</p> |
| personaMasterKey | <p>Master key for the current selected persona</p> |
| algorithm | <p>for the key</p> |
| peerId | <p>Id for the peer</p> |

<a name="RsaPairwiseKey.getPrime"></a>

### RsaPairwiseKey.getPrime()
<p>Uses primeBase as reference and generate the closest prime number</p>

**Kind**: static method of [<code>RsaPairwiseKey</code>](#RsaPairwiseKey)  
<a name="RsaPairwiseKey.generateDeterministicNumberForPrime"></a>

### RsaPairwiseKey.generateDeterministicNumberForPrime(cryptoFactory, keySize, personaMasterKey, peerId)
<p>Generate a deterministic number that can be used as prime</p>

**Kind**: static method of [<code>RsaPairwiseKey</code>](#RsaPairwiseKey)  

| Param | Description |
| --- | --- |
| cryptoFactory | <p>The crypto factory.</p> |
| keySize | <p>Desired key size</p> |
| personaMasterKey | <p>The persona master key</p> |
| peerId | <p>The peer id</p> |

<a name="RsaPairwiseKey.generateHashForPrime"></a>

### RsaPairwiseKey.generateHashForPrime(crypto, inx, key, data)
<p>Generate a hash used as component for prime number</p>

**Kind**: static method of [<code>RsaPairwiseKey</code>](#RsaPairwiseKey)  

| Param | Description |
| --- | --- |
| crypto | <p>The crypto object.</p> |
| inx | <p>Round number</p> |
| key | <p>Signature key</p> |
| data | <p>Data to sign</p> |

<a name="RsaPairwiseKey.executeRounds"></a>

### RsaPairwiseKey.executeRounds(rounds, inx, key, data)
<p>Execute all rounds</p>

**Kind**: static method of [<code>RsaPairwiseKey</code>](#RsaPairwiseKey)  

| Param | Description |
| --- | --- |
| rounds | <p>Array of functions to execute</p> |
| inx | <p>Current step</p> |
| key | <p>Key to sign</p> |
| data | <p>Data to sign</p> |

<a name="RsaPairwiseKey.generatePrime"></a>

### RsaPairwiseKey.generatePrime(primeSeed)
<p>Generate a prime number from the seed.
isProbablyPrime is based on the Miller-Rabin prime test.</p>

**Kind**: static method of [<code>RsaPairwiseKey</code>](#RsaPairwiseKey)  

| Param | Description |
| --- | --- |
| primeSeed | <p>seed for prime generator</p> |

<a name="RsaPairwiseKey.toBase"></a>

### RsaPairwiseKey.toBase(bigNumber)
<p>Convert big number to base64 url.</p>

**Kind**: static method of [<code>RsaPairwiseKey</code>](#RsaPairwiseKey)  

| Param | Description |
| --- | --- |
| bigNumber | <p>Number to convert</p> |

<a name="RsaPrivateKey"></a>

## RsaPrivateKey ⇐ [<code>PrivateKey</code>](#PrivateKey)
<p>Represents an Elliptic Curve private key</p>

**Kind**: global class  
**Extends**: [<code>PrivateKey</code>](#PrivateKey)  

* [RsaPrivateKey](#RsaPrivateKey) ⇐ [<code>PrivateKey</code>](#PrivateKey)
    * [new RsaPrivateKey()](#new_RsaPrivateKey_new)
    * [.getPublicKey()](#RsaPrivateKey+getPublicKey) ⇒

<a name="new_RsaPrivateKey_new"></a>

### new RsaPrivateKey()
<p>Create instance of @class RsaPrivateKey</p>

<a name="RsaPrivateKey+getPublicKey"></a>

### rsaPrivateKey.getPublicKey() ⇒
<p>Gets the corresponding public key</p>

**Kind**: instance method of [<code>RsaPrivateKey</code>](#RsaPrivateKey)  
**Returns**: <p>The corresponding [PublicKey](#PublicKey)</p>  
<a name="RsaPublicKey"></a>

## RsaPublicKey ⇐ [<code>PublicKey</code>](#PublicKey)
<p>Represents an RSA public key</p>

**Kind**: global class  
**Extends**: [<code>PublicKey</code>](#PublicKey)  
<a name="new_RsaPublicKey_new"></a>

### new RsaPublicKey()
<p>Create instance of @class RsaPublicKey</p>

<a name="KeyStoreInMemory"></a>

## KeyStoreInMemory
<p>Class defining methods and properties for a light KeyStore</p>

**Kind**: global class  

* [KeyStoreInMemory](#KeyStoreInMemory)
    * [.get(keyReference, [publicKeyOnly])](#KeyStoreInMemory+get)
    * [.list()](#KeyStoreInMemory+list)
    * [.save(keyIdentifier, key)](#KeyStoreInMemory+save)

<a name="KeyStoreInMemory+get"></a>

### keyStoreInMemory.get(keyReference, [publicKeyOnly])
<p>Returns the key associated with the specified
key identifier.</p>

**Kind**: instance method of [<code>KeyStoreInMemory</code>](#KeyStoreInMemory)  

| Param | Default | Description |
| --- | --- | --- |
| keyReference |  | <p>for which to return the key.</p> |
| [publicKeyOnly] | <code>true</code> | <p>True if only the public key is needed.</p> |

<a name="KeyStoreInMemory+list"></a>

### keyStoreInMemory.list()
<p>Lists all keys with their corresponding key ids</p>

**Kind**: instance method of [<code>KeyStoreInMemory</code>](#KeyStoreInMemory)  
<a name="KeyStoreInMemory+save"></a>

### keyStoreInMemory.save(keyIdentifier, key)
<p>Saves the specified key to the key store using
the key identifier.</p>

**Kind**: instance method of [<code>KeyStoreInMemory</code>](#KeyStoreInMemory)  

| Param | Description |
| --- | --- |
| keyIdentifier | <p>for the key being saved.</p> |
| key | <p>being saved to the key store.</p> |

<a name="CryptoFactory"></a>

## CryptoFactory
<p>Utility class to handle all CryptoSuite dependency injection</p>

**Kind**: global class  

* [CryptoFactory](#CryptoFactory)
    * [new CryptoFactory(keyStore, suite)](#new_CryptoFactory_new)
    * [.getKeyEncrypter(name)](#CryptoFactory+getKeyEncrypter) ⇒
    * [.getSharedKeyEncrypter(name)](#CryptoFactory+getSharedKeyEncrypter) ⇒
    * [.getSymmetricEncrypter(name)](#CryptoFactory+getSymmetricEncrypter) ⇒
    * [.getMessageSigner(name)](#CryptoFactory+getMessageSigner) ⇒
    * [.getMessageAuthenticationCodeSigners(name)](#CryptoFactory+getMessageAuthenticationCodeSigners) ⇒
    * [.getMessageDigest(name)](#CryptoFactory+getMessageDigest) ⇒

<a name="new_CryptoFactory_new"></a>

### new CryptoFactory(keyStore, suite)
<p>Constructs a new CryptoRegistry</p>


| Param | Description |
| --- | --- |
| keyStore | <p>used to store private jeys</p> |
| suite | <p>The suite to use for dependency injection</p> |

<a name="CryptoFactory+getKeyEncrypter"></a>

### cryptoFactory.getKeyEncrypter(name) ⇒
<p>Gets the key encrypter object given the encryption algorithm's name</p>

**Kind**: instance method of [<code>CryptoFactory</code>](#CryptoFactory)  
**Returns**: <p>The corresponding crypto API</p>  

| Param | Description |
| --- | --- |
| name | <p>The name of the algorithm</p> |

<a name="CryptoFactory+getSharedKeyEncrypter"></a>

### cryptoFactory.getSharedKeyEncrypter(name) ⇒
<p>Gets the shared key encrypter object given the encryption algorithm's name
Used for DH algorithms</p>

**Kind**: instance method of [<code>CryptoFactory</code>](#CryptoFactory)  
**Returns**: <p>The corresponding crypto API</p>  

| Param | Description |
| --- | --- |
| name | <p>The name of the algorithm</p> |

<a name="CryptoFactory+getSymmetricEncrypter"></a>

### cryptoFactory.getSymmetricEncrypter(name) ⇒
<p>Gets the SymmetricEncrypter object given the symmetric encryption algorithm's name</p>

**Kind**: instance method of [<code>CryptoFactory</code>](#CryptoFactory)  
**Returns**: <p>The corresponding crypto API</p>  

| Param | Description |
| --- | --- |
| name | <p>The name of the algorithm</p> |

<a name="CryptoFactory+getMessageSigner"></a>

### cryptoFactory.getMessageSigner(name) ⇒
<p>Gets the message signer object given the signing algorithm's name</p>

**Kind**: instance method of [<code>CryptoFactory</code>](#CryptoFactory)  
**Returns**: <p>The corresponding crypto API</p>  

| Param | Description |
| --- | --- |
| name | <p>The name of the algorithm</p> |

<a name="CryptoFactory+getMessageAuthenticationCodeSigners"></a>

### cryptoFactory.getMessageAuthenticationCodeSigners(name) ⇒
<p>Gets the mac signer object given the signing algorithm's name</p>

**Kind**: instance method of [<code>CryptoFactory</code>](#CryptoFactory)  
**Returns**: <p>The corresponding crypto API</p>  

| Param | Description |
| --- | --- |
| name | <p>The name of the algorithm</p> |

<a name="CryptoFactory+getMessageDigest"></a>

### cryptoFactory.getMessageDigest(name) ⇒
<p>Gets the message digest object given the digest algorithm's name</p>

**Kind**: instance method of [<code>CryptoFactory</code>](#CryptoFactory)  
**Returns**: <p>The corresponding crypto API</p>  

| Param | Description |
| --- | --- |
| name | <p>The name of the algorithm</p> |

<a name="CryptoOperations"></a>

## CryptoOperations
<p>Interface for the Crypto Algorithms Plugins</p>

**Kind**: global class  
<a name="SubtleCryptoExtension"></a>

## SubtleCryptoExtension
<p>The class extends the @class SubtleCrypto with addtional methods.
 Adds methods to work with key references.
 Extends SubtleCrypto to work with JWK keys.</p>

**Kind**: global class  

* [SubtleCryptoExtension](#SubtleCryptoExtension)
    * _instance_
        * [.generatePairwiseKey(algorithm, seedReference, personaId, peerId, extractable, keyops)](#SubtleCryptoExtension+generatePairwiseKey)
        * [.signByKeyStore(algorithm, keyReference, data)](#SubtleCryptoExtension+signByKeyStore) ⇒
        * [.verifyByJwk(algorithm, jwk, signature, payload)](#SubtleCryptoExtension+verifyByJwk)
        * [.decryptByKeyStore(algorithm, keyReference, cipher)](#SubtleCryptoExtension+decryptByKeyStore)
        * [.decryptByJwk(algorithm, jwk, cipher)](#SubtleCryptoExtension+decryptByJwk)
        * [.encryptByJwk(algorithm, jwk, data)](#SubtleCryptoExtension+encryptByJwk)
    * _static_
        * [.toDer(elements)](#SubtleCryptoExtension.toDer)
        * [.normalizeAlgorithm(algorithm)](#SubtleCryptoExtension.normalizeAlgorithm)
        * [.normalizeJwk(jwk)](#SubtleCryptoExtension.normalizeJwk)

<a name="SubtleCryptoExtension+generatePairwiseKey"></a>

### subtleCryptoExtension.generatePairwiseKey(algorithm, seedReference, personaId, peerId, extractable, keyops)
<p>Generate a pairwise key for the algorithm</p>

**Kind**: instance method of [<code>SubtleCryptoExtension</code>](#SubtleCryptoExtension)  

| Param | Description |
| --- | --- |
| algorithm | <p>for the key</p> |
| seedReference | <p>Reference to the seed</p> |
| personaId | <p>Id for the persona</p> |
| peerId | <p>Id for the peer</p> |
| extractable | <p>True if key is exportable</p> |
| keyops | <p>Key operations</p> |

<a name="SubtleCryptoExtension+signByKeyStore"></a>

### subtleCryptoExtension.signByKeyStore(algorithm, keyReference, data) ⇒
<p>Sign with a key referenced in the key store</p>

**Kind**: instance method of [<code>SubtleCryptoExtension</code>](#SubtleCryptoExtension)  
**Returns**: <p>The signature in the requested algorithm</p>  

| Param | Description |
| --- | --- |
| algorithm | <p>used for signature</p> |
| keyReference | <p>points to key in the key store</p> |
| data | <p>to sign</p> |

<a name="SubtleCryptoExtension+verifyByJwk"></a>

### subtleCryptoExtension.verifyByJwk(algorithm, jwk, signature, payload)
<p>Verify with JWK.</p>

**Kind**: instance method of [<code>SubtleCryptoExtension</code>](#SubtleCryptoExtension)  

| Param | Description |
| --- | --- |
| algorithm | <p>used for verification</p> |
| jwk | <p>Json web key used to verify</p> |
| signature | <p>to verify</p> |
| payload | <p>which was signed</p> |

<a name="SubtleCryptoExtension+decryptByKeyStore"></a>

### subtleCryptoExtension.decryptByKeyStore(algorithm, keyReference, cipher)
<p>Decrypt with a key referenced in the key store.
The referenced key must be a jwk key.</p>

**Kind**: instance method of [<code>SubtleCryptoExtension</code>](#SubtleCryptoExtension)  

| Param | Description |
| --- | --- |
| algorithm | <p>used for signature</p> |
| keyReference | <p>points to key in the key store</p> |
| cipher | <p>to decrypt</p> |

<a name="SubtleCryptoExtension+decryptByJwk"></a>

### subtleCryptoExtension.decryptByJwk(algorithm, jwk, cipher)
<p>Decrypt with JWK.</p>

**Kind**: instance method of [<code>SubtleCryptoExtension</code>](#SubtleCryptoExtension)  

| Param | Description |
| --- | --- |
| algorithm | <p>used for decryption</p> |
| jwk | <p>Json web key to decrypt</p> |
| cipher | <p>to decrypt</p> |

<a name="SubtleCryptoExtension+encryptByJwk"></a>

### subtleCryptoExtension.encryptByJwk(algorithm, jwk, data)
<p>Encrypt with a jwk key referenced in the key store</p>

**Kind**: instance method of [<code>SubtleCryptoExtension</code>](#SubtleCryptoExtension)  

| Param | Description |
| --- | --- |
| algorithm | <p>used for encryption</p> |
| jwk | <p>Json web key public key</p> |
| data | <p>to encrypt</p> |

<a name="SubtleCryptoExtension.toDer"></a>

### SubtleCryptoExtension.toDer(elements)
<p>format the signature output to DER format</p>

**Kind**: static method of [<code>SubtleCryptoExtension</code>](#SubtleCryptoExtension)  

| Param | Description |
| --- | --- |
| elements | <p>Array of elements to encode in DER</p> |

<a name="SubtleCryptoExtension.normalizeAlgorithm"></a>

### SubtleCryptoExtension.normalizeAlgorithm(algorithm)
<p>Normalize the algorithm so it can be used by underlying crypto.</p>

**Kind**: static method of [<code>SubtleCryptoExtension</code>](#SubtleCryptoExtension)  

| Param | Description |
| --- | --- |
| algorithm | <p>Algorithm to be normalized</p> |

<a name="SubtleCryptoExtension.normalizeJwk"></a>

### SubtleCryptoExtension.normalizeJwk(jwk)
<p>Normalize the JWK parameters so it can be used by underlying crypto.</p>

**Kind**: static method of [<code>SubtleCryptoExtension</code>](#SubtleCryptoExtension)  

| Param | Description |
| --- | --- |
| jwk | <p>Json web key to be normalized</p> |

<a name="SubtleCryptoOperations"></a>

## SubtleCryptoOperations
<p>Default crypto suite implementing the default plugable crypto layer</p>

**Kind**: global class  

* [SubtleCryptoOperations](#SubtleCryptoOperations)
    * [.getKeyEncrypters()](#SubtleCryptoOperations+getKeyEncrypters) ⇒
    * [.getSharedKeyEncrypters()](#SubtleCryptoOperations+getSharedKeyEncrypters) ⇒
    * [.getSymmetricEncrypters()](#SubtleCryptoOperations+getSymmetricEncrypters) ⇒
    * [.getMessageSigners()](#SubtleCryptoOperations+getMessageSigners) ⇒
    * [.messageAuthenticationCodeSigners()](#SubtleCryptoOperations+messageAuthenticationCodeSigners) ⇒
    * [.getMessageDigests()](#SubtleCryptoOperations+getMessageDigests) ⇒
    * [.getSubtleCrypto()](#SubtleCryptoOperations+getSubtleCrypto)

<a name="SubtleCryptoOperations+getKeyEncrypters"></a>

### subtleCryptoOperations.getKeyEncrypters() ⇒
<p>Gets all of the key encryption Algorithms from the plugin</p>

**Kind**: instance method of [<code>SubtleCryptoOperations</code>](#SubtleCryptoOperations)  
**Returns**: <p>a subtle crypto object for key encryption/decryption</p>  
<a name="SubtleCryptoOperations+getSharedKeyEncrypters"></a>

### subtleCryptoOperations.getSharedKeyEncrypters() ⇒
<p>Gets all of the key sharing encryption Algorithms from the plugin</p>

**Kind**: instance method of [<code>SubtleCryptoOperations</code>](#SubtleCryptoOperations)  
**Returns**: <p>a subtle crypto object for key sharing encryption/decryption</p>  
<a name="SubtleCryptoOperations+getSymmetricEncrypters"></a>

### subtleCryptoOperations.getSymmetricEncrypters() ⇒
<p>Get all of the symmetric encrypter algorithms from the plugin</p>

**Kind**: instance method of [<code>SubtleCryptoOperations</code>](#SubtleCryptoOperations)  
**Returns**: <p>a subtle crypto object for symmetric encryption/decryption</p>  
<a name="SubtleCryptoOperations+getMessageSigners"></a>

### subtleCryptoOperations.getMessageSigners() ⇒
<p>Gets all of the message signing Algorithms from the plugin</p>

**Kind**: instance method of [<code>SubtleCryptoOperations</code>](#SubtleCryptoOperations)  
**Returns**: <p>a subtle crypto object for message signing</p>  
<a name="SubtleCryptoOperations+messageAuthenticationCodeSigners"></a>

### subtleCryptoOperations.messageAuthenticationCodeSigners() ⇒
<p>Gets all of the MAC signing Algorithms from the plugin. 
Will be used for primitive operations such as key generation.</p>

**Kind**: instance method of [<code>SubtleCryptoOperations</code>](#SubtleCryptoOperations)  
**Returns**: <p>a subtle crypto object for message signing</p>  
<a name="SubtleCryptoOperations+getMessageDigests"></a>

### subtleCryptoOperations.getMessageDigests() ⇒
<p>Gets all of the message digest Algorithms from the plugin.</p>

**Kind**: instance method of [<code>SubtleCryptoOperations</code>](#SubtleCryptoOperations)  
**Returns**: <p>a subtle crypto object for message digests</p>  
<a name="SubtleCryptoOperations+getSubtleCrypto"></a>

### subtleCryptoOperations.getSubtleCrypto()
<p>Returns the @class SubtleCrypto ipmplementation for the current environment</p>

**Kind**: instance method of [<code>SubtleCryptoOperations</code>](#SubtleCryptoOperations)  
<a name="CryptoProtocolError"></a>

## CryptoProtocolError
<p>Base error class for the crypto protocols.</p>

**Kind**: global class  
<a name="new_CryptoProtocolError_new"></a>

### new CryptoProtocolError(protocol, message)
<p>Create instance of @class CryptoProtocolError</p>


| Param | Description |
| --- | --- |
| protocol | <p>name</p> |
| message | <p>for the error</p> |

<a name="DidProtocol"></a>

## DidProtocol
<p>Hub Protocol for decrypting/verifying and encrypting/signing payloads</p>

**Kind**: global class  

* [DidProtocol](#DidProtocol)
    * [new DidProtocol(options)](#new_DidProtocol_new)
    * [.decryptAndVerify(keyReference, cipher)](#DidProtocol+decryptAndVerify)
    * [.signAndEncrypt(payload)](#DidProtocol+signAndEncrypt)

<a name="new_DidProtocol_new"></a>

### new DidProtocol(options)
<p>Authentication constructor</p>


| Param | Description |
| --- | --- |
| options | <p>Arguments to a constructor in a named object</p> |

<a name="DidProtocol+decryptAndVerify"></a>

### didProtocol.decryptAndVerify(keyReference, cipher)
<p>Unwrapping method for unwrapping requests/responses.
Decrypt a cipher using [PrivateKey] of Client Identifier and then verify payload.</p>

**Kind**: instance method of [<code>DidProtocol</code>](#DidProtocol)  

| Param | Description |
| --- | --- |
| keyReference | <p>key reference of private key in keystore used to decrypt.</p> |
| cipher | <p>the cipher to be decrypted and verified by client Identifier.</p> |

<a name="DidProtocol+signAndEncrypt"></a>

### didProtocol.signAndEncrypt(payload)
<p>Wrapping method for wrapping requests/responses.
Sign a payload using [PrivateKey] in client's keystore and encrypt payload using [PublicKey]</p>

**Kind**: instance method of [<code>DidProtocol</code>](#DidProtocol)  

| Param | Description |
| --- | --- |
| payload | <p>the payload to be signed and encrypted by client Identifier.</p> |

<a name="JoseConstants"></a>

## JoseConstants
<p>Class for JOSE constants</p>

**Kind**: global class  
<a name="JoseHelpers"></a>

## JoseHelpers
<p>Crypto helpers support for plugable crypto layer</p>

**Kind**: global class  

* [JoseHelpers](#JoseHelpers)
    * [.headerHasElements(header)](#JoseHelpers.headerHasElements)
    * [.encodeHeader(header, toBase64Url)](#JoseHelpers.encodeHeader)
    * [.getOptionsProperty(propertyName, [initialOptions], [overrideOptions], [mandatory])](#JoseHelpers.getOptionsProperty)

<a name="JoseHelpers.headerHasElements"></a>

### JoseHelpers.headerHasElements(header)
<p>Return true if the header has elements</p>

**Kind**: static method of [<code>JoseHelpers</code>](#JoseHelpers)  

| Param | Description |
| --- | --- |
| header | <p>to test</p> |

<a name="JoseHelpers.encodeHeader"></a>

### JoseHelpers.encodeHeader(header, toBase64Url)
<p>Encode the header to JSON and base 64 url.
The Typescript Map construct does not allow for JSON.stringify returning {}.
TSMap.toJSON prepares a map so it can be serialized as a dictionary.</p>

**Kind**: static method of [<code>JoseHelpers</code>](#JoseHelpers)  

| Param | Default | Description |
| --- | --- | --- |
| header |  | <p>to encode</p> |
| toBase64Url | <code>true</code> | <p>is true when result needs to be base 64 url</p> |

<a name="JoseHelpers.getOptionsProperty"></a>

### JoseHelpers.getOptionsProperty(propertyName, [initialOptions], [overrideOptions], [mandatory])
<p>Get the Protected to be used from the options</p>

**Kind**: static method of [<code>JoseHelpers</code>](#JoseHelpers)  

| Param | Default | Description |
| --- | --- | --- |
| propertyName |  | <p>Property name in options</p> |
| [initialOptions] |  | <p>The initial set of options</p> |
| [overrideOptions] |  | <p>Options passed in after the constructure</p> |
| [mandatory] | <code>true</code> | <p>True if property is required</p> |

<a name="JweRecipient"></a>

## JweRecipient
<p>JWS signature used by the general JSON</p>

**Kind**: global class  
<a name="new_JweRecipient_new"></a>

### new JweRecipient()
<p>The JWE signature.</p>

<a name="JweToken"></a>

## JweToken
<p>Class for containing Jwe token operations.
This class hides the JOSE and crypto library dependencies to allow support for additional crypto algorithms.
Crypto calls always happen via CryptoFactory</p>

**Kind**: global class  

* [JweToken](#JweToken)
    * [new JweToken(options)](#new_JweToken_new)
    * _instance_
        * [.serialize(format)](#JweToken+serialize)
        * [.getCryptoFactory(newOptions, manadatory)](#JweToken+getCryptoFactory)
        * [.getContentEncryptionKey(newOptions, manadatory)](#JweToken+getContentEncryptionKey)
        * [.getInitialVector(newOptions, manadatory)](#JweToken+getInitialVector)
        * [.getContentEncryptionAlgorithm(newOptions, manadatory)](#JweToken+getContentEncryptionAlgorithm)
        * [.encrypt(recipients, payload, format, options)](#JweToken+encrypt) ⇒
        * [.decrypt(decryptionKeyReference, options)](#JweToken+decrypt) ⇒
    * _static_
        * [.serializeJweGeneralJson(token)](#JweToken.serializeJweGeneralJson)
        * [.serializeJweFlatJson(token)](#JweToken.serializeJweFlatJson)
        * [.serializeJweCompact(token)](#JweToken.serializeJweCompact)

<a name="new_JweToken_new"></a>

### new JweToken(options)
<p>Create an Jwe token object</p>


| Param | Description |
| --- | --- |
| options | <p>Set of Jwe token options</p> |

<a name="JweToken+serialize"></a>

### jweToken.serialize(format)
<p>Serialize a Jwe token object from a token</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  

| Param | Description |
| --- | --- |
| format | <p>Optional specify the serialization format. If not specified, use default format.</p> |

<a name="JweToken+getCryptoFactory"></a>

### jweToken.getCryptoFactory(newOptions, manadatory)
<p>Get the CryptoFactory to be used</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  

| Param | Default | Description |
| --- | --- | --- |
| newOptions |  | <p>Options passed in after the constructure</p> |
| manadatory | <code>true</code> | <p>True if property needs to be defined</p> |

<a name="JweToken+getContentEncryptionKey"></a>

### jweToken.getContentEncryptionKey(newOptions, manadatory)
<p>Get the key encryption key for testing</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  

| Param | Default | Description |
| --- | --- | --- |
| newOptions |  | <p>Options passed in after the constructure</p> |
| manadatory | <code>true</code> | <p>True if property needs to be defined</p> |

<a name="JweToken+getInitialVector"></a>

### jweToken.getInitialVector(newOptions, manadatory)
<p>Get the initial vector for testing</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  

| Param | Default | Description |
| --- | --- | --- |
| newOptions |  | <p>Options passed in after the constructure</p> |
| manadatory | <code>true</code> | <p>True if property needs to be defined</p> |

<a name="JweToken+getContentEncryptionAlgorithm"></a>

### jweToken.getContentEncryptionAlgorithm(newOptions, manadatory)
<p>Get the content encryption algorithm from the options</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  

| Param | Default | Description |
| --- | --- | --- |
| newOptions |  | <p>Options passed in after the constructure</p> |
| manadatory | <code>true</code> | <p>True if property needs to be defined</p> |

<a name="JweToken+encrypt"></a>

### jweToken.encrypt(recipients, payload, format, options) ⇒
<p>Encrypt content using the given public keys in JWK format.
The key type enforces the key encryption algorithm.
The options can override certain algorithm choices.</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  
**Returns**: <p>JweToken with encrypted payload.</p>  

| Param | Description |
| --- | --- |
| recipients | <p>List of recipients' public keys.</p> |
| payload | <p>to encrypt.</p> |
| format | <p>of the final serialization.</p> |
| options | <p>used for the signature. These options override the options provided in the constructor.</p> |

<a name="JweToken+decrypt"></a>

### jweToken.decrypt(decryptionKeyReference, options) ⇒
<p>Decrypt the content.</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  
**Returns**: <p>Signed payload in compact Jwe format.</p>  

| Param | Description |
| --- | --- |
| decryptionKeyReference | <p>Reference to the decryption key.</p> |
| options | <p>used for the signature. These options override the options provided in the constructor.</p> |

<a name="JweToken.serializeJweGeneralJson"></a>

### JweToken.serializeJweGeneralJson(token)
<p>Serialize a Jwe token object from a token in General Json format</p>

**Kind**: static method of [<code>JweToken</code>](#JweToken)  

| Param | Description |
| --- | --- |
| token | <p>Jwe base object</p> |

<a name="JweToken.serializeJweFlatJson"></a>

### JweToken.serializeJweFlatJson(token)
<p>Serialize a Jwe token object from a token in Flat Json format</p>

**Kind**: static method of [<code>JweToken</code>](#JweToken)  

| Param | Description |
| --- | --- |
| token | <p>Jwe base object</p> |

<a name="JweToken.serializeJweCompact"></a>

### JweToken.serializeJweCompact(token)
<p>Serialize a Jwe token object from a token in Compact format</p>

**Kind**: static method of [<code>JweToken</code>](#JweToken)  

| Param | Description |
| --- | --- |
| token | <p>Jwe base object</p> |

<a name="JwsSignature"></a>

## JwsSignature
<p>JWS signature used by the general JSON</p>

**Kind**: global class  
<a name="new_JwsSignature_new"></a>

### new JwsSignature()
<p>Creates instance of @class JwsSignature</p>

<a name="JwsToken"></a>

## JwsToken
<p>Class for containing JWS token operations.
This class hides the JOSE and crypto library dependencies to allow support for additional crypto algorithms.
Crypto calls always happen via CryptoFactory</p>

**Kind**: global class  

* [JwsToken](#JwsToken)
    * [new JwsToken(options)](#new_JwsToken_new)
    * _instance_
        * [.serialize(format)](#JwsToken+serialize)
        * [.setGeneralParts(content)](#JwsToken+setGeneralParts) ⇒
        * [.setFlatParts(content)](#JwsToken+setFlatParts) ⇒
        * [.isValidToken()](#JwsToken+isValidToken)
        * [.getKeyStore(newOptions, mandatory)](#JwsToken+getKeyStore)
        * [.getCryptoFactory(newOptions, mandatory)](#JwsToken+getCryptoFactory)
        * [.getProtected(newOptions, mandatory)](#JwsToken+getProtected)
        * [.getHeader(newOptions, mandatory)](#JwsToken+getHeader)
        * [.sign(signingKeyReference, payload, format, options)](#JwsToken+sign) ⇒
        * [.verify(validationKeys, options)](#JwsToken+verify) ⇒
        * [.getPayload()](#JwsToken+getPayload)
        * [.setProtected(protectedHeader)](#JwsToken+setProtected)
    * _static_
        * [.serializeJwsGeneralJson(token)](#JwsToken.serializeJwsGeneralJson)
        * [.serializeJwsFlatJson(token)](#JwsToken.serializeJwsFlatJson)
        * [.serializeJwsCompact(token)](#JwsToken.serializeJwsCompact)
        * [.deserialize()](#JwsToken.deserialize)

<a name="new_JwsToken_new"></a>

### new JwsToken(options)
<p>Create an Jws token object</p>


| Param | Description |
| --- | --- |
| options | <p>Set of jws token options</p> |

<a name="JwsToken+serialize"></a>

### jwsToken.serialize(format)
<p>Serialize a Jws token object from a token</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  

| Param | Description |
| --- | --- |
| format | <p>Optional specify the serialization format. If not specified, use default format.</p> |

<a name="JwsToken+setGeneralParts"></a>

### jwsToken.setGeneralParts(content) ⇒
<p>Try to parse the input token and set the properties of this JswToken</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  
**Returns**: <p>true if valid token was parsed</p>  

| Param | Description |
| --- | --- |
| content | <p>Alledged IJwsGeneralJSon token</p> |

<a name="JwsToken+setFlatParts"></a>

### jwsToken.setFlatParts(content) ⇒
<p>Try to parse the input token and set the properties of this JswToken</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  
**Returns**: <p>true if valid token was parsed</p>  

| Param | Description |
| --- | --- |
| content | <p>Alledged IJwsFlatJson token</p> |

<a name="JwsToken+isValidToken"></a>

### jwsToken.isValidToken()
<p>Check if a valid token was found after decoding</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  
<a name="JwsToken+getKeyStore"></a>

### jwsToken.getKeyStore(newOptions, mandatory)
<p>Get the keyStore to be used</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  

| Param | Default | Description |
| --- | --- | --- |
| newOptions |  | <p>Options passed in after the constructure</p> |
| mandatory | <code>true</code> | <p>True if property needs to be defined</p> |

<a name="JwsToken+getCryptoFactory"></a>

### jwsToken.getCryptoFactory(newOptions, mandatory)
<p>Get the CryptoFactory to be used</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  

| Param | Default | Description |
| --- | --- | --- |
| newOptions |  | <p>Options passed in after the constructure</p> |
| mandatory | <code>true</code> | <p>True if property needs to be defined</p> |

<a name="JwsToken+getProtected"></a>

### jwsToken.getProtected(newOptions, mandatory)
<p>Get the default protected header to be used from the options</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  

| Param | Default | Description |
| --- | --- | --- |
| newOptions |  | <p>Options passed in after the constructure</p> |
| mandatory | <code>false</code> | <p>True if property needs to be defined</p> |

<a name="JwsToken+getHeader"></a>

### jwsToken.getHeader(newOptions, mandatory)
<p>Get the default header to be used from the options</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  

| Param | Default | Description |
| --- | --- | --- |
| newOptions |  | <p>Options passed in after the constructure</p> |
| mandatory | <code>false</code> | <p>True if property needs to be defined</p> |

<a name="JwsToken+sign"></a>

### jwsToken.sign(signingKeyReference, payload, format, options) ⇒
<p>Signs contents using the given private key in JWK format.</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  
**Returns**: <p>Signed payload in compact JWS format.</p>  

| Param | Description |
| --- | --- |
| signingKeyReference | <p>Reference to the signing key.</p> |
| payload | <p>to sign.</p> |
| format | <p>of the final signature.</p> |
| options | <p>used for the signature. These options override the options provided in the constructor.</p> |

<a name="JwsToken+verify"></a>

### jwsToken.verify(validationKeys, options) ⇒
<p>Verify the JWS signature.</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  
**Returns**: <p>True if signature validated.</p>  

| Param | Description |
| --- | --- |
| validationKeys | <p>Public JWK key to validate the signature.</p> |
| options | <p>used for the signature. These options override the options provided in the constructor.</p> |

<a name="JwsToken+getPayload"></a>

### jwsToken.getPayload()
<p>Gets the base64 URL decrypted payload.</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  
<a name="JwsToken+setProtected"></a>

### jwsToken.setProtected(protectedHeader)
<p>Set the protected header</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  

| Param | Description |
| --- | --- |
| protectedHeader | <p>to set on the JwsToken object</p> |

<a name="JwsToken.serializeJwsGeneralJson"></a>

### JwsToken.serializeJwsGeneralJson(token)
<p>Serialize a Jws token object from a token in General Json format</p>

**Kind**: static method of [<code>JwsToken</code>](#JwsToken)  

| Param | Description |
| --- | --- |
| token | <p>JWS base object</p> |

<a name="JwsToken.serializeJwsFlatJson"></a>

### JwsToken.serializeJwsFlatJson(token)
<p>Serialize a Jws token object from a token in Flat Json format</p>

**Kind**: static method of [<code>JwsToken</code>](#JwsToken)  

| Param | Description |
| --- | --- |
| token | <p>JWS base object</p> |

<a name="JwsToken.serializeJwsCompact"></a>

### JwsToken.serializeJwsCompact(token)
<p>Serialize a Jws token object from a token in Compact format</p>

**Kind**: static method of [<code>JwsToken</code>](#JwsToken)  

| Param | Description |
| --- | --- |
| token | <p>JWS base object</p> |

<a name="JwsToken.deserialize"></a>

### JwsToken.deserialize()
<p>Deserialize a Jws token object</p>

**Kind**: static method of [<code>JwsToken</code>](#JwsToken)  
<a name="EncryptionStrategy"></a>

## EncryptionStrategy
<p>Class used to model encryption strategies</p>

**Kind**: global class  
<a name="MessageSigningStrategy"></a>

## MessageSigningStrategy
<p>Class used to model the message signing strategy</p>

**Kind**: global class  
<a name="CryptoHelpers"></a>

## CryptoHelpers
<p>Crypto helpers support for plugable crypto layer</p>

**Kind**: global class  

* [CryptoHelpers](#CryptoHelpers)
    * [.getSubtleCryptoForAlgorithm(cryptoFactory, algorithmName, hash)](#CryptoHelpers.getSubtleCryptoForAlgorithm)
    * [.jwaToWebCrypto(jwaAlgorithmName)](#CryptoHelpers.jwaToWebCrypto)
    * [.webCryptoToJwa(algorithmName, hash)](#CryptoHelpers.webCryptoToJwa)
    * [.getKeyImportAlgorithm(algorithm)](#CryptoHelpers.getKeyImportAlgorithm)

<a name="CryptoHelpers.getSubtleCryptoForAlgorithm"></a>

### CryptoHelpers.getSubtleCryptoForAlgorithm(cryptoFactory, algorithmName, hash)
<p>The API which implements the requested algorithm</p>

**Kind**: static method of [<code>CryptoHelpers</code>](#CryptoHelpers)  

| Param | Description |
| --- | --- |
| cryptoFactory | <p>Crypto suite</p> |
| algorithmName | <p>Requested algorithm</p> |
| hash | <p>Optional hash for the algorithm</p> |

<a name="CryptoHelpers.jwaToWebCrypto"></a>

### CryptoHelpers.jwaToWebCrypto(jwaAlgorithmName)
<p>Map the JWA algorithm to the W3C crypto API algorithm.
The method restricts the supported algorithms. This can easily be extended.
Based on https://<a href="http://www.w3.org/TR/WebCryptoAPI/">www.w3.org/TR/WebCryptoAPI/</a> A. Mapping between JSON Web Key / JSON Web Algorithm</p>

**Kind**: static method of [<code>CryptoHelpers</code>](#CryptoHelpers)  

| Param | Description |
| --- | --- |
| jwaAlgorithmName | <p>Requested algorithm</p> |

<a name="CryptoHelpers.webCryptoToJwa"></a>

### CryptoHelpers.webCryptoToJwa(algorithmName, hash)
<p>Maps the subtle crypto algorithm name to the JWA name</p>

**Kind**: static method of [<code>CryptoHelpers</code>](#CryptoHelpers)  

| Param | Description |
| --- | --- |
| algorithmName | <p>Requested algorithm</p> |
| hash | <p>Optional hash for the algorithm</p> |

<a name="CryptoHelpers.getKeyImportAlgorithm"></a>

### CryptoHelpers.getKeyImportAlgorithm(algorithm)
<p>Derive the key import algorithm</p>

**Kind**: static method of [<code>CryptoHelpers</code>](#CryptoHelpers)  

| Param | Description |
| --- | --- |
| algorithm | <p>used for signature</p> |

<a name="W3cCryptoApiConstants"></a>

## W3cCryptoApiConstants
<p>Class for W3C Crypto API constants</p>

**Kind**: global class  
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
    * [.queryObject(commitQueryRequest, hubObject)](#HubClient+queryObject)
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
<p>Signs and sends a commit to the hub owner's hub.</p>

**Kind**: instance method of [<code>HubClient</code>](#HubClient)  

| Param | Description |
| --- | --- |
| commit | <p>commit to be sent to hub owner's hub.</p> |

<a name="HubClient+queryObjects"></a>

### hubClient.queryObjects(queryRequest)
<p>Query Objects of certain type in Hub.</p>

**Kind**: instance method of [<code>HubClient</code>](#HubClient)  

| Param | Description |
| --- | --- |
| queryRequest | <p>object that tells the hub what object to get.</p> |

<a name="HubClient+queryObject"></a>

### hubClient.queryObject(commitQueryRequest, hubObject)
<p>Query Object specified by certain id</p>

**Kind**: instance method of [<code>HubClient</code>](#HubClient)  

| Param | Description |
| --- | --- |
| commitQueryRequest | <p>HubCommitQueryRequest object to request object of specific id.</p> |
| hubObject | <p>a HubObject containing metadata such as object id.</p> |

<a name="HubClient+getHubInstances"></a>

### hubClient.getHubInstances()
<p>Get all Hub Instances from hub owner's identifier document.</p>

**Kind**: instance method of [<code>HubClient</code>](#HubClient)  
<a name="HubClient+createHubSession"></a>

### hubClient.createHubSession()
<p>Implement createHubSession method once HubSession is refactored.
creates a hubSession for hub instance that is available/online.</p>

**Kind**: instance method of [<code>HubClient</code>](#HubClient)  
<a name="HubObject"></a>

## HubObject
<p>Class that represents an object in a hub.</p>

**Kind**: global class  

* [HubObject](#HubObject)
    * [new HubObject(objectMetadata)](#new_HubObject_new)
    * [.hydrate(hubSession, commitQueryRequest)](#HubObject+hydrate)
    * [.getPayload()](#HubObject+getPayload)
    * [.getMetadata()](#HubObject+getMetadata)

<a name="new_HubObject_new"></a>

### new HubObject(objectMetadata)
<p>Create an instance for Hub Object using hub object's metadata.</p>


| Param | Description |
| --- | --- |
| objectMetadata | <p>object metadata that represents an object in a hub.</p> |

<a name="HubObject+hydrate"></a>

### hubObject.hydrate(hubSession, commitQueryRequest)
<p>If payload is not defined, get the payload from hub session using metadata.</p>

**Kind**: instance method of [<code>HubObject</code>](#HubObject)  

| Param | Description |
| --- | --- |
| hubSession | <p>the hub session that will be used to query object</p> |
| commitQueryRequest | <p>the commit query requests for getting all commits for certain object.</p> |

<a name="HubObject+getPayload"></a>

### hubObject.getPayload()
<p>Get the Payload of the Hub Object if object is hydrated.
Throws an Error if object is not hydrated.</p>

**Kind**: instance method of [<code>HubObject</code>](#HubObject)  
<a name="HubObject+getMetadata"></a>

### hubObject.getMetadata()
<p>Get The Metadata of the Hub Object.</p>

**Kind**: instance method of [<code>HubObject</code>](#HubObject)  
<a name="HubClientOptions"></a>

## HubClientOptions
<p>Class for defining options for the
HubClient, such as hub Identifier and client Identifier.</p>

**Kind**: global class  
<a name="Actions"></a>

## Actions
<p>A Class that does CRUD operations for storing objects as Actions in the Hub</p>

**Kind**: global class  
<a name="Collections"></a>

## Collections
<p>A Class that does CRUD operations for storing objects as Collections in the Hub</p>

**Kind**: global class  
<a name="HubInterface"></a>

## HubInterface
**Kind**: global class  

* [HubInterface](#HubInterface)
    * [new HubInterface([hubInterfaceOptions])](#new_HubInterface_new)
    * [.addObject(object)](#HubInterface+addObject)
    * [.getUnHydratedObjects()](#HubInterface+getUnHydratedObjects)
    * [.getObject(hubObject)](#HubInterface+getObject)
    * [.getObjects()](#HubInterface+getObjects)
    * [.updateObject()](#HubInterface+updateObject)
    * [.deleteObject()](#HubInterface+deleteObject)

<a name="new_HubInterface_new"></a>

### new HubInterface([hubInterfaceOptions])
<p>Creates an instance of HubMethods that will be used to send hub requests and responses.</p>


| Param | Description |
| --- | --- |
| [hubInterfaceOptions] | <p>for configuring how to form hub requests and responses.</p> |

<a name="HubInterface+addObject"></a>

### hubInterface.addObject(object)
<p>Add object to Hub Owner's hub.</p>

**Kind**: instance method of [<code>HubInterface</code>](#HubInterface)  

| Param | Description |
| --- | --- |
| object | <p>object to be added to hub owned by hub owner.</p> |

<a name="HubInterface+getUnHydratedObjects"></a>

### hubInterface.getUnHydratedObjects()
<p>Get all unhydrated hubObjects of specific type.</p>

**Kind**: instance method of [<code>HubInterface</code>](#HubInterface)  
<a name="HubInterface+getObject"></a>

### hubInterface.getObject(hubObject)
<p>create and return hydrated hubObject.</p>

**Kind**: instance method of [<code>HubInterface</code>](#HubInterface)  

| Param | Description |
| --- | --- |
| hubObject | <p>unhydrated hubObject containing on object metadata.</p> |

<a name="HubInterface+getObjects"></a>

### hubInterface.getObjects()
<p>Get a list of all hydrated HubObjects containing both metadata and payload.</p>

**Kind**: instance method of [<code>HubInterface</code>](#HubInterface)  
<a name="HubInterface+updateObject"></a>

### hubInterface.updateObject()
<p>Update Hub Object in hub owner's hub.</p>

**Kind**: instance method of [<code>HubInterface</code>](#HubInterface)  
<a name="HubInterface+deleteObject"></a>

### hubInterface.deleteObject()
<p>Update Hub Object in hub owner's hub.</p>

**Kind**: instance method of [<code>HubInterface</code>](#HubInterface)  
<a name="Permissions"></a>

## Permissions
<p>A Class that does CRUD operations for storing items as Permissions in the Hub</p>

**Kind**: global class  
<a name="Profile"></a>

## Profile
<p>A Class that does CRUD operations for storing objects as Profile in the Hub.</p>

**Kind**: global class  
<a name="Commit"></a>

## Commit
<p>Represents a new (i.e pending, unsigned) commit which will create, update, or delete an object in
a user's Identity Hub.</p>

**Kind**: global class  

* [Commit](#Commit)
    * [.validate()](#Commit+validate)
    * [.isValid()](#Commit+isValid)
    * [.getCommitFields()](#Commit+getCommitFields)
    * [.getPayload()](#Commit+getPayload)

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
<a name="Commit+getCommitFields"></a>

### commit.getCommitFields()
<p>Returns the fields of the commit.</p>

**Kind**: instance method of [<code>Commit</code>](#Commit)  
<a name="Commit+getPayload"></a>

### commit.getPayload()
<p>Returns the application-specific payload for this commit.</p>

**Kind**: instance method of [<code>Commit</code>](#Commit)  
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
<a name="HubSessionOptions"></a>

## HubSessionOptions
<p>Options for instantiating a new Hub session.</p>

**Kind**: global class  
<a name="KeyStoreConstants"></a>

## KeyStoreConstants
<p>Class for key storage constants</p>

**Kind**: global class  
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
        * [.encrypt(payload)](#Identifier+encrypt)
        * [.decrypt(cipher, keyReference)](#Identifier+decrypt)
    * _static_
        * [.create([options])](#Identifier.create)
        * [.keyStorageIdentifier(personaId, target, algorithm, keyType)](#Identifier.keyStorageIdentifier)

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

<a name="Identifier+encrypt"></a>

### identifier.encrypt(payload)
<p>Encrypt payload using Public Key registered on Identifier Document.</p>

**Kind**: instance method of [<code>Identifier</code>](#Identifier)  

| Param | Description |
| --- | --- |
| payload | <p>object that will be encrypted.</p> |

<a name="Identifier+decrypt"></a>

### identifier.decrypt(cipher, keyReference)
<p>Decrypt cipher using key referenced in keystore.</p>

**Kind**: instance method of [<code>Identifier</code>](#Identifier)  

| Param | Description |
| --- | --- |
| cipher | <p>cipher to be decrypted.</p> |
| keyReference | <p>string that references what key to use from keystore.</p> |

<a name="Identifier.create"></a>

### Identifier.create([options])
<p>Creates a new decentralized identifier.</p>

**Kind**: static method of [<code>Identifier</code>](#Identifier)  

| Param | Description |
| --- | --- |
| [options] | <p>for configuring how to register and resolve identifiers.</p> |

<a name="Identifier.keyStorageIdentifier"></a>

### Identifier.keyStorageIdentifier(personaId, target, algorithm, keyType)
<p>Generate a storage identifier to store a key</p>

**Kind**: static method of [<code>Identifier</code>](#Identifier)  

| Param | Description |
| --- | --- |
| personaId | <p>The identifier for the persona</p> |
| target | <p>The identifier for the peer. Will be persona for non-pairwise keys</p> |
| algorithm | <p>Key algorithm</p> |
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
<a name="UserAgentError"></a>

## UserAgentError
<p>Base error class for the UserAgent.</p>

**Kind**: global class  
<a name="UserAgentOptions"></a>

## UserAgentOptions
<p>Interface defining options for the
User Agent, such as resolver and register.</p>

**Kind**: global class  

* [UserAgentOptions](#UserAgentOptions)
    * [.keyStore](#UserAgentOptions+keyStore)
    * [.keyStore](#UserAgentOptions+keyStore)
    * [.cryptoFactory](#UserAgentOptions+cryptoFactory)
    * [.cryptoFactory](#UserAgentOptions+cryptoFactory)

<a name="UserAgentOptions+keyStore"></a>

### userAgentOptions.keyStore
<p>Get the key store</p>

**Kind**: instance property of [<code>UserAgentOptions</code>](#UserAgentOptions)  
<a name="UserAgentOptions+keyStore"></a>

### userAgentOptions.keyStore
<p>Set the key store</p>

**Kind**: instance property of [<code>UserAgentOptions</code>](#UserAgentOptions)  
<a name="UserAgentOptions+cryptoFactory"></a>

### userAgentOptions.cryptoFactory
<p>Get the crypto operations</p>

**Kind**: instance property of [<code>UserAgentOptions</code>](#UserAgentOptions)  
<a name="UserAgentOptions+cryptoFactory"></a>

### userAgentOptions.cryptoFactory
<p>Set the key store</p>

**Kind**: instance property of [<code>UserAgentOptions</code>](#UserAgentOptions)  
<a name="UserAgentSession"></a>

## UserAgentSession
<p>Class for creating a User Agent Session for sending and verifying
Authentication Requests and Responses.</p>

**Kind**: global class  

* [UserAgentSession](#UserAgentSession)
    * [.signRequest(redirectUrl, nonce, claimRequests, state)](#UserAgentSession+signRequest)
    * [.signResponse(redirectUrl, nonce, state, claims)](#UserAgentSession+signResponse)
    * [.verify(jws)](#UserAgentSession+verify)

<a name="UserAgentSession+signRequest"></a>

### userAgentSession.signRequest(redirectUrl, nonce, claimRequests, state)
<p>Sign a User Agent Request.</p>

**Kind**: instance method of [<code>UserAgentSession</code>](#UserAgentSession)  

| Param | Description |
| --- | --- |
| redirectUrl | <p>url that recipient should send response back to.</p> |
| nonce | <p>nonce that will come back in response.</p> |
| claimRequests | <p>any claims that sender is requesting from the recipient.</p> |
| state | <p>optional stringified JSON state opaque object that will come back in response.</p> |

<a name="UserAgentSession+signResponse"></a>

### userAgentSession.signResponse(redirectUrl, nonce, state, claims)
<p>Sign a User Agent Response.</p>

**Kind**: instance method of [<code>UserAgentSession</code>](#UserAgentSession)  

| Param | Description |
| --- | --- |
| redirectUrl | <p>url that request was sent to.</p> |
| nonce | <p>nonce to return to sender of the request.</p> |
| state | <p>opaque object to return to sender of the request.</p> |
| claims | <p>any claims that request asked for.</p> |

<a name="UserAgentSession+verify"></a>

### userAgentSession.verify(jws)
<p>Verify a request was signed and sent by Identifier.</p>

**Kind**: instance method of [<code>UserAgentSession</code>](#UserAgentSession)  

| Param | Description |
| --- | --- |
| jws | <p>Signed Payload</p> |

<a name="CredentialType"></a>

## CredentialType
<p>Enumeration of the supported credential types.</p>

**Kind**: global variable  
<a name="CredentialType"></a>

## CredentialType
<p>Interface defining common properties and
methods of a credential.</p>

**Kind**: global variable  
<a name="KeyType"></a>

## KeyType
<p>Enumeration to model key types.</p>

**Kind**: global variable  
<a name="KeyType"></a>

## KeyType
<p>Factory class to create @enum KeyType objects</p>

**Kind**: global variable  
<a name="KeyUse"></a>

## KeyUse
<p>Enumeration to model key use.</p>

**Kind**: global variable  
<a name="KeyUse"></a>

## KeyUse
<p>Factory class to create @enum KeyUse objects.</p>

**Kind**: global variable  
<a name="KeyOperation"></a>

## KeyOperation
<p>JWK key operations</p>

**Kind**: global variable  
<a name="new_KeyOperation_new"></a>

### *new exports.KeyOperation()*
<p>Represents a Public Key in JWK format.</p>

<a name="ProtectionFormat"></a>

## ProtectionFormat
<p>Enum to define different protection formats</p>

**Kind**: global variable  
<a name="ProtectionStrategyScope"></a>

## ProtectionStrategyScope
<p>Class used to model protection strategy</p>

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
<p>Interface for defining options for HubInterface.</p>

**Kind**: global variable  
<a name="HubInterfaceOptions"></a>

## HubInterfaceOptions
<p>An Abstract Class for Hub Interfaces.</p>

**Kind**: global variable  
<a name="HubSessionOptions"></a>

## HubSessionOptions
<p>Represents a communication session with a particular Hub instance.</p>

**Kind**: global variable  
<a name="context"></a>

## context
<p>context for credentialManifest</p>

**Kind**: global constant  
<a name="type"></a>

## type
<p>type for credentialManifest</p>

**Kind**: global constant  
