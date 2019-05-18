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
<dt><a href="#EcPrivateKey">EcPrivateKey</a> ⇐ <code><a href="#PrivateKey">PrivateKey</a></code></dt>
<dd><p>Represents an Elliptic Curve private key</p></dd>
<dt><a href="#EcPublicKey">EcPublicKey</a> ⇐ <code>PublicKey</code></dt>
<dd><p>Represents an Elliptic Curve public key</p></dd>
<dt><a href="#PrivateKey">PrivateKey</a></dt>
<dd><p>Represents a Private Key in JWK format.</p></dd>
<dt><a href="#KeyOperation">KeyOperation</a></dt>
<dd></dd>
<dt><a href="#RsaPrivateKey">RsaPrivateKey</a> ⇐ <code><a href="#PrivateKey">PrivateKey</a></code></dt>
<dd><p>Represents an Elliptic Curve private key</p></dd>
<dt><a href="#RsaPublicKey">RsaPublicKey</a> ⇐ <code>PublicKey</code></dt>
<dd><p>Represents an RSA public key</p></dd>
<dt><a href="#KeyStoreMem">KeyStoreMem</a></dt>
<dd><p>Class defining methods and properties for a light KeyStore</p></dd>
<dt><a href="#CryptoFactory">CryptoFactory</a></dt>
<dd><p>Utility class to handle all CryptoSuite dependency injection</p></dd>
<dt><a href="#CryptoSuite">CryptoSuite</a></dt>
<dd><p>Interface for the Crypto Algorithms Plugins</p></dd>
<dt><a href="#DefaultCryptoSuite">DefaultCryptoSuite</a></dt>
<dd><p>Default crypto suite implementing the default plugable crypto layer</p></dd>
<dt><a href="#SubtleCryptoDefault">SubtleCryptoDefault</a></dt>
<dd><p>Subtle crypto class.
Provides support for nodejs and browser</p></dd>
<dt><a href="#SubtleCryptoExtension">SubtleCryptoExtension</a></dt>
<dd><p>Default crypto suite</p></dd>
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
<dt><a href="#CryptoHelpers">CryptoHelpers</a></dt>
<dd><p>Crypto helpers support for plugable crypto layer</p></dd>
<dt><a href="#CryptoOptions">CryptoOptions</a></dt>
<dd><p>Class used to model crypto options</p></dd>
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
<dt><a href="#KeyType">KeyType</a></dt>
<dd><p>enum to model key types</p></dd>
<dt><a href="#KeyType">KeyType</a></dt>
<dd><p>Factory class to create KeyType objects</p></dd>
<dt><a href="#KeyUse">KeyUse</a></dt>
<dd><p>enum to model key use</p></dd>
<dt><a href="#KeyUse">KeyUse</a></dt>
<dd><p>Factory class to create KeyUse objects</p></dd>
<dt><a href="#KeyOperation">KeyOperation</a></dt>
<dd><p>JWK key operations</p></dd>
<dt><a href="#ProtectionFormat">ProtectionFormat</a></dt>
<dd><p>Enum to define different protection formats</p></dd>
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

<a name="EcPrivateKey"></a>

## EcPrivateKey ⇐ [<code>PrivateKey</code>](#PrivateKey)
<p>Represents an Elliptic Curve private key</p>

**Kind**: global class  
**Extends**: [<code>PrivateKey</code>](#PrivateKey)  
<a name="EcPublicKey"></a>

## EcPublicKey ⇐ <code>PublicKey</code>
<p>Represents an Elliptic Curve public key</p>

**Kind**: global class  
**Extends**: <code>PublicKey</code>  
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

<a name="RsaPrivateKey"></a>

## RsaPrivateKey ⇐ [<code>PrivateKey</code>](#PrivateKey)
<p>Represents an Elliptic Curve private key</p>

**Kind**: global class  
**Extends**: [<code>PrivateKey</code>](#PrivateKey)  
<a name="RsaPrivateKey+getPublicKey"></a>

### rsaPrivateKey.getPublicKey()
<p>Get the RSA public key</p>

**Kind**: instance method of [<code>RsaPrivateKey</code>](#RsaPrivateKey)  
<a name="RsaPublicKey"></a>

## RsaPublicKey ⇐ <code>PublicKey</code>
<p>Represents an RSA public key</p>

**Kind**: global class  
**Extends**: <code>PublicKey</code>  
<a name="KeyStoreMem"></a>

## KeyStoreMem
<p>Class defining methods and properties for a light KeyStore</p>

**Kind**: global class  

* [KeyStoreMem](#KeyStoreMem)
    * [.get(keyReference, publicKeyOnly)](#KeyStoreMem+get)
    * [.list()](#KeyStoreMem+list)
    * [.save(keyIdentifier, key)](#KeyStoreMem+save)

<a name="KeyStoreMem+get"></a>

### keyStoreMem.get(keyReference, publicKeyOnly)
<p>Returns the key associated with the specified
key identifier.</p>

**Kind**: instance method of [<code>KeyStoreMem</code>](#KeyStoreMem)  

| Param | Description |
| --- | --- |
| keyReference | <p>for which to return the key.</p> |
| publicKeyOnly | <p>True if only the public key is needed.</p> |

<a name="KeyStoreMem+list"></a>

### keyStoreMem.list()
<p>Lists all keys with their corresponding key ids</p>

**Kind**: instance method of [<code>KeyStoreMem</code>](#KeyStoreMem)  
<a name="KeyStoreMem+save"></a>

### keyStoreMem.save(keyIdentifier, key)
<p>Saves the specified key to the key store using
the key identifier.</p>

**Kind**: instance method of [<code>KeyStoreMem</code>](#KeyStoreMem)  

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
    * [.getMacSigner(name)](#CryptoFactory+getMacSigner) ⇒
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

<a name="CryptoFactory+getMacSigner"></a>

### cryptoFactory.getMacSigner(name) ⇒
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

<a name="CryptoSuite"></a>

## CryptoSuite
<p>Interface for the Crypto Algorithms Plugins</p>

**Kind**: global class  
<a name="DefaultCryptoSuite"></a>

## DefaultCryptoSuite
<p>Default crypto suite implementing the default plugable crypto layer</p>

**Kind**: global class  

* [DefaultCryptoSuite](#DefaultCryptoSuite)
    * [.getKekEncrypters()](#DefaultCryptoSuite+getKekEncrypters) ⇒
    * [.getSharedKeyEncrypters()](#DefaultCryptoSuite+getSharedKeyEncrypters) ⇒
    * [.getSymmetricEncrypters()](#DefaultCryptoSuite+getSymmetricEncrypters) ⇒
    * [.getMessageSigners()](#DefaultCryptoSuite+getMessageSigners) ⇒
    * [.getMacSigners()](#DefaultCryptoSuite+getMacSigners) ⇒
    * [.getMessageDigests()](#DefaultCryptoSuite+getMessageDigests) ⇒

<a name="DefaultCryptoSuite+getKekEncrypters"></a>

### defaultCryptoSuite.getKekEncrypters() ⇒
<p>Gets all of the key encryption Algorithms from the plugin</p>

**Kind**: instance method of [<code>DefaultCryptoSuite</code>](#DefaultCryptoSuite)  
**Returns**: <p>a subtle crypto object for key encryption/decryption</p>  
<a name="DefaultCryptoSuite+getSharedKeyEncrypters"></a>

### defaultCryptoSuite.getSharedKeyEncrypters() ⇒
<p>Gets all of the key sharing encryption Algorithms from the plugin</p>

**Kind**: instance method of [<code>DefaultCryptoSuite</code>](#DefaultCryptoSuite)  
**Returns**: <p>a subtle crypto object for key sharing encryption/decryption</p>  
<a name="DefaultCryptoSuite+getSymmetricEncrypters"></a>

### defaultCryptoSuite.getSymmetricEncrypters() ⇒
<p>Get all of the symmetric encrypter algorithms from the plugin</p>

**Kind**: instance method of [<code>DefaultCryptoSuite</code>](#DefaultCryptoSuite)  
**Returns**: <p>a subtle crypto object for symmetric encryption/decryption</p>  
<a name="DefaultCryptoSuite+getMessageSigners"></a>

### defaultCryptoSuite.getMessageSigners() ⇒
<p>Gets all of the message signing Algorithms from the plugin</p>

**Kind**: instance method of [<code>DefaultCryptoSuite</code>](#DefaultCryptoSuite)  
**Returns**: <p>a subtle crypto object for message signing</p>  
<a name="DefaultCryptoSuite+getMacSigners"></a>

### defaultCryptoSuite.getMacSigners() ⇒
<p>Gets all of the MAC signing Algorithms from the plugin. 
Will be used for primitive operations such as key generation.</p>

**Kind**: instance method of [<code>DefaultCryptoSuite</code>](#DefaultCryptoSuite)  
**Returns**: <p>a subtle crypto object for message signing</p>  
<a name="DefaultCryptoSuite+getMessageDigests"></a>

### defaultCryptoSuite.getMessageDigests() ⇒
<p>Gets all of the message digest Algorithms from the plugin.</p>

**Kind**: instance method of [<code>DefaultCryptoSuite</code>](#DefaultCryptoSuite)  
**Returns**: <p>a subtle crypto object for message digests</p>  
<a name="SubtleCryptoDefault"></a>

## SubtleCryptoDefault
<p>Subtle crypto class.
Provides support for nodejs and browser</p>

**Kind**: global class  
<a name="new_SubtleCryptoDefault_new"></a>

### new SubtleCryptoDefault()
<p>Constructs a new instance of the class.</p>

<a name="SubtleCryptoExtension"></a>

## SubtleCryptoExtension
<p>Default crypto suite</p>

**Kind**: global class  
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

<a name="JoseHelpers"></a>

## JoseHelpers
<p>Crypto helpers support for plugable crypto layer</p>

**Kind**: global class  

* [JoseHelpers](#JoseHelpers)
    * [.headerHasElements(header)](#JoseHelpers.headerHasElements)
    * [.encodeHeader(header, toBase64Url)](#JoseHelpers.encodeHeader)

<a name="JoseHelpers.headerHasElements"></a>

### JoseHelpers.headerHasElements(header)
<p>Return true if the header has elements</p>

**Kind**: static method of [<code>JoseHelpers</code>](#JoseHelpers)  

| Param | Description |
| --- | --- |
| header | <p>to test</p> |

<a name="JoseHelpers.encodeHeader"></a>

### JoseHelpers.encodeHeader(header, toBase64Url)
<p>Encode the header to JSON and base 64 url</p>

**Kind**: static method of [<code>JoseHelpers</code>](#JoseHelpers)  

| Param | Default | Description |
| --- | --- | --- |
| header |  | <p>to encode</p> |
| toBase64Url | <code>true</code> | <p>is true when result needs to be base 64 url</p> |

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
        * [.setGeneralParts(content)](#JweToken+setGeneralParts) ⇒
        * [.setFlatParts(content)](#JweToken+setFlatParts) ⇒
        * [.setCompactParts(content)](#JweToken+setCompactParts) ⇒
        * [.setProtected(protectedHeader)](#JweToken+setProtected)
        * [.isValidToken()](#JweToken+isValidToken)
        * [.getKeyStore(newOptions, manadatory)](#JweToken+getKeyStore)
        * [.getCryptoFactory(newOptions, manadatory)](#JweToken+getCryptoFactory)
        * [.getProtected(newOptions, manadatory)](#JweToken+getProtected)
        * [.getHeader(newOptions, manadatory)](#JweToken+getHeader)
        * [.getAlgorithm(newOptions, manadatory)](#JweToken+getAlgorithm)
        * [.getOptionsProperty(propertyName, newOptions, manadatory)](#JweToken+getOptionsProperty)
        * [.encrypt(signingKeyReference, payload, format, options)](#JweToken+encrypt) ⇒
        * [.getPayload()](#JweToken+getPayload)
    * _static_
        * [.serializeJweGeneralJson(token)](#JweToken.serializeJweGeneralJson)
        * [.serializeJweFlatJson(token)](#JweToken.serializeJweFlatJson)
        * [.serializeJweCompact(token)](#JweToken.serializeJweCompact)
        * [.create(token, options)](#JweToken.create)

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

<a name="JweToken+setGeneralParts"></a>

### jweToken.setGeneralParts(content) ⇒
<p>Try to parse the input token and set the properties of this JswToken</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  
**Returns**: <p>true if valid token was parsed</p>  

| Param | Description |
| --- | --- |
| content | <p>Alledged IJweGeneralJSon token</p> |

<a name="JweToken+setFlatParts"></a>

### jweToken.setFlatParts(content) ⇒
<p>Try to parse the input token and set the properties of this JswToken</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  
**Returns**: <p>true if valid token was parsed</p>  

| Param | Description |
| --- | --- |
| content | <p>Alledged IJweFlatJson token</p> |

<a name="JweToken+setCompactParts"></a>

### jweToken.setCompactParts(content) ⇒
<p>Try to parse the input token and set the properties of this JswToken</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  
**Returns**: <p>true if valid token was parsed</p>  

| Param | Description |
| --- | --- |
| content | <p>Alledged IJweCompact token</p> |

<a name="JweToken+setProtected"></a>

### jweToken.setProtected(protectedHeader)
<p>Set the protected header</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  

| Param | Description |
| --- | --- |
| protectedHeader | <p>to set on the JweToken object</p> |

<a name="JweToken+isValidToken"></a>

### jweToken.isValidToken()
<p>Check if a valid token was found after decoding</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  
<a name="JweToken+getKeyStore"></a>

### jweToken.getKeyStore(newOptions, manadatory)
<p>Get the keyStore to be used</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  

| Param | Default | Description |
| --- | --- | --- |
| newOptions |  | <p>Options passed in after the constructure</p> |
| manadatory | <code>true</code> | <p>True if property needs to be defined</p> |

<a name="JweToken+getCryptoFactory"></a>

### jweToken.getCryptoFactory(newOptions, manadatory)
<p>Get the CryptoFactory to be used</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  

| Param | Default | Description |
| --- | --- | --- |
| newOptions |  | <p>Options passed in after the constructure</p> |
| manadatory | <code>true</code> | <p>True if property needs to be defined</p> |

<a name="JweToken+getProtected"></a>

### jweToken.getProtected(newOptions, manadatory)
<p>Get the default protected header to be used from the options</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  

| Param | Default | Description |
| --- | --- | --- |
| newOptions |  | <p>Options passed in after the constructure</p> |
| manadatory | <code>false</code> | <p>True if property needs to be defined</p> |

<a name="JweToken+getHeader"></a>

### jweToken.getHeader(newOptions, manadatory)
<p>Get the default header to be used from the options</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  

| Param | Default | Description |
| --- | --- | --- |
| newOptions |  | <p>Options passed in after the constructure</p> |
| manadatory | <code>false</code> | <p>True if property needs to be defined</p> |

<a name="JweToken+getAlgorithm"></a>

### jweToken.getAlgorithm(newOptions, manadatory)
<p>Get the algorithm from the options</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  

| Param | Default | Description |
| --- | --- | --- |
| newOptions |  | <p>Options passed in after the constructure</p> |
| manadatory | <code>true</code> | <p>True if property needs to be defined</p> |

<a name="JweToken+getOptionsProperty"></a>

### jweToken.getOptionsProperty(propertyName, newOptions, manadatory)
<p>Get the Protected to be used from the options</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  

| Param | Default | Description |
| --- | --- | --- |
| propertyName |  | <p>Property name in options</p> |
| newOptions |  | <p>Options passed in after the constructure</p> |
| manadatory | <code>true</code> | <p>True if property needs to be defined</p> |

<a name="JweToken+encrypt"></a>

### jweToken.encrypt(signingKeyReference, payload, format, options) ⇒
<p>Encrypt content using the given public keys in JWK format.</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  
**Returns**: <p>Signed payload in compact Jwe format.</p>  

| Param | Description |
| --- | --- |
| signingKeyReference | <p>Reference to the signing key.</p> |
| payload | <p>to sign.</p> |
| format | <p>of the final signature.</p> |
| options | <p>used for the signature. These options override the options provided in the constructor.</p> |

<a name="JweToken+getPayload"></a>

### jweToken.getPayload()
<p>Gets the base64 URL decrypted payload.</p>

**Kind**: instance method of [<code>JweToken</code>](#JweToken)  
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

<a name="JweToken.create"></a>

### JweToken.create(token, options)
<p>Create an Jwe token object from a token</p>

**Kind**: static method of [<code>JweToken</code>](#JweToken)  

| Param | Description |
| --- | --- |
| token | <p>Base object used to create this token</p> |
| options | <p>Set of Jwe token options</p> |

<a name="JwsSignature"></a>

## JwsSignature
<p>JWS signature used by the general JSON</p>

**Kind**: global class  
<a name="new_JwsSignature_new"></a>

### new JwsSignature()
<p>The JWS signature.</p>

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
        * [.setCompactParts(content)](#JwsToken+setCompactParts) ⇒
        * [.setProtected(protectedHeader)](#JwsToken+setProtected)
        * [.isValidToken()](#JwsToken+isValidToken)
        * [.getKeyStore(newOptions, manadatory)](#JwsToken+getKeyStore)
        * [.getCryptoFactory(newOptions, manadatory)](#JwsToken+getCryptoFactory)
        * [.getProtected(newOptions, manadatory)](#JwsToken+getProtected)
        * [.getHeader(newOptions, manadatory)](#JwsToken+getHeader)
        * [.getAlgorithm(newOptions, manadatory)](#JwsToken+getAlgorithm)
        * [.getOptionsProperty(propertyName, newOptions, manadatory)](#JwsToken+getOptionsProperty)
        * [.sign(signingKeyReference, payload, format, options)](#JwsToken+sign) ⇒
        * [.getPayload()](#JwsToken+getPayload)
    * _static_
        * [.serializeJwsGeneralJson(token)](#JwsToken.serializeJwsGeneralJson)
        * [.serializeJwsFlatJson(token)](#JwsToken.serializeJwsFlatJson)
        * [.serializeJwsCompact(token)](#JwsToken.serializeJwsCompact)
        * [.create(token, options)](#JwsToken.create)
        * [.headerHasElements(header)](#JwsToken.headerHasElements)
        * [.encodeHeader(header, toBase64Url)](#JwsToken.encodeHeader)

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

<a name="JwsToken+setCompactParts"></a>

### jwsToken.setCompactParts(content) ⇒
<p>Try to parse the input token and set the properties of this JswToken</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  
**Returns**: <p>true if valid token was parsed</p>  

| Param | Description |
| --- | --- |
| content | <p>Alledged IJwsCompact token</p> |

<a name="JwsToken+setProtected"></a>

### jwsToken.setProtected(protectedHeader)
<p>Set the protected header</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  

| Param | Description |
| --- | --- |
| protectedHeader | <p>to set on the JwsToken object</p> |

<a name="JwsToken+isValidToken"></a>

### jwsToken.isValidToken()
<p>Check if a valid token was found after decoding</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  
<a name="JwsToken+getKeyStore"></a>

### jwsToken.getKeyStore(newOptions, manadatory)
<p>Get the keyStore to be used</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  

| Param | Default | Description |
| --- | --- | --- |
| newOptions |  | <p>Options passed in after the constructure</p> |
| manadatory | <code>true</code> | <p>True if property needs to be defined</p> |

<a name="JwsToken+getCryptoFactory"></a>

### jwsToken.getCryptoFactory(newOptions, manadatory)
<p>Get the CryptoFactory to be used</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  

| Param | Default | Description |
| --- | --- | --- |
| newOptions |  | <p>Options passed in after the constructure</p> |
| manadatory | <code>true</code> | <p>True if property needs to be defined</p> |

<a name="JwsToken+getProtected"></a>

### jwsToken.getProtected(newOptions, manadatory)
<p>Get the default protected header to be used from the options</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  

| Param | Default | Description |
| --- | --- | --- |
| newOptions |  | <p>Options passed in after the constructure</p> |
| manadatory | <code>false</code> | <p>True if property needs to be defined</p> |

<a name="JwsToken+getHeader"></a>

### jwsToken.getHeader(newOptions, manadatory)
<p>Get the default header to be used from the options</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  

| Param | Default | Description |
| --- | --- | --- |
| newOptions |  | <p>Options passed in after the constructure</p> |
| manadatory | <code>false</code> | <p>True if property needs to be defined</p> |

<a name="JwsToken+getAlgorithm"></a>

### jwsToken.getAlgorithm(newOptions, manadatory)
<p>Get the algorithm from the options</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  

| Param | Default | Description |
| --- | --- | --- |
| newOptions |  | <p>Options passed in after the constructure</p> |
| manadatory | <code>true</code> | <p>True if property needs to be defined</p> |

<a name="JwsToken+getOptionsProperty"></a>

### jwsToken.getOptionsProperty(propertyName, newOptions, manadatory)
<p>Get the Protected to be used from the options</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  

| Param | Default | Description |
| --- | --- | --- |
| propertyName |  | <p>Property name in options</p> |
| newOptions |  | <p>Options passed in after the constructure</p> |
| manadatory | <code>true</code> | <p>True if property needs to be defined</p> |

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

<a name="JwsToken+getPayload"></a>

### jwsToken.getPayload()
<p>Gets the base64 URL decrypted payload.</p>

**Kind**: instance method of [<code>JwsToken</code>](#JwsToken)  
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

<a name="JwsToken.create"></a>

### JwsToken.create(token, options)
<p>Create an Jws token object from a token</p>

**Kind**: static method of [<code>JwsToken</code>](#JwsToken)  

| Param | Description |
| --- | --- |
| token | <p>Base object used to create this token</p> |
| options | <p>Set of jws token options</p> |

<a name="JwsToken.headerHasElements"></a>

### JwsToken.headerHasElements(header)
<p>Return true if the header has elements</p>

**Kind**: static method of [<code>JwsToken</code>](#JwsToken)  

| Param | Description |
| --- | --- |
| header | <p>to test</p> |

<a name="JwsToken.encodeHeader"></a>

### JwsToken.encodeHeader(header, toBase64Url)
<p>Encode the header to JSON and base 64 url</p>

**Kind**: static method of [<code>JwsToken</code>](#JwsToken)  

| Param | Default | Description |
| --- | --- | --- |
| header |  | <p>to encode</p> |
| toBase64Url | <code>true</code> | <p>is true when result needs to be base 64 url</p> |

<a name="CryptoHelpers"></a>

## CryptoHelpers
<p>Crypto helpers support for plugable crypto layer</p>

**Kind**: global class  

* [CryptoHelpers](#CryptoHelpers)
    * [.getSubtleCryptoForTheAlgorithm(cryptoFactory, algorithmName, hash)](#CryptoHelpers.getSubtleCryptoForTheAlgorithm)
    * [.toJwa(algorithmName, hash)](#CryptoHelpers.toJwa)
    * [.getKeyImportAlgorithm(algorithm)](#CryptoHelpers.getKeyImportAlgorithm)

<a name="CryptoHelpers.getSubtleCryptoForTheAlgorithm"></a>

### CryptoHelpers.getSubtleCryptoForTheAlgorithm(cryptoFactory, algorithmName, hash)
<p>The API which implements the requested algorithm</p>

**Kind**: static method of [<code>CryptoHelpers</code>](#CryptoHelpers)  

| Param | Description |
| --- | --- |
| cryptoFactory | <p>Crypto suite</p> |
| algorithmName | <p>Requested algorithm</p> |
| hash | <p>Optional hash for the algorithm</p> |

<a name="CryptoHelpers.toJwa"></a>

### CryptoHelpers.toJwa(algorithmName, hash)
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

<a name="CryptoOptions"></a>

## CryptoOptions
<p>Class used to model crypto options</p>

**Kind**: global class  
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
<a name="KeyType"></a>

## KeyType
<p>enum to model key types</p>

**Kind**: global variable  
<a name="KeyType"></a>

## KeyType
<p>Factory class to create KeyType objects</p>

**Kind**: global variable  
<a name="KeyUse"></a>

## KeyUse
<p>enum to model key use</p>

**Kind**: global variable  
<a name="KeyUse"></a>

## KeyUse
<p>Factory class to create KeyUse objects</p>

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
