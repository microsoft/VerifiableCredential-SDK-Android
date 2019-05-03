## Classes

<dl>
<dt><a href="#Claim">Claim</a></dt>
<dd><p>Interface defining methods and properties for a Claim object.
The properties such as issuer, logo, name, and descriptions are what are meant to be rendered on the claim UI.
TODO: figure out what properties exactly we want on a claim.</p></dd>
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
<dt><a href="#CredentialStore">CredentialStore</a></dt>
<dd><p>Interface defining methods and properties to
be implemented by specific credential stores.</p></dd>
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
<dt><a href="#Jwt">Jwt</a></dt>
<dd><p>Class for creating and managing a JWT-formed claims</p></dd>
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
<dt><a href="#SignatureFormat">SignatureFormat</a></dt>
<dd><p>Enum to define different signature formats</p></dd>
<dt><a href="#ClaimDetailsType">ClaimDetailsType</a></dt>
<dd><p>ClaimDetails Types</p></dd>
<dt><a href="#ClaimDetailsType">ClaimDetailsType</a></dt>
<dd><p>Interface defining methods and properties for a Token object</p></dd>
</dl>

## Constants

<dl>
<dt><a href="#context">context</a></dt>
<dd><p>context for credentialManifest</p></dd>
<dt><a href="#type">type</a></dt>
<dd><p>type for credentialManifest</p></dd>
</dl>

<a name="Claim"></a>

## Claim
<p>Interface defining methods and properties for a Claim object.
The properties such as issuer, logo, name, and descriptions are what are meant to be rendered on the claim UI.
TODO: figure out what properties exactly we want on a claim.</p>

**Kind**: global class  

* [Claim](#Claim)
    * [new Claim()](#new_Claim_new)
    * [.getUIProperties()](#Claim+getUIProperties)

<a name="new_Claim_new"></a>

### new Claim()
<p>Contructs an instance of the Claim class</p>

<a name="Claim+getUIProperties"></a>

### claim.getUIProperties()
<p>Get the UI properties in order to render claim correctly
TODO: figure out exactly what properties we want to render on the claim.</p>

**Kind**: instance method of [<code>Claim</code>](#Claim)  
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
<p>Validate whether a credential is valid for the manifest.
TODO: implement method to validate that credential matches the manifest.</p>

**Kind**: instance method of [<code>CredentialIssuer</code>](#CredentialIssuer)  

| Param | Description |
| --- | --- |
| _inputCredential | <p>the Credential to validate against the credential manifest</p> |

<a name="CredentialIssuer.create"></a>

### CredentialIssuer.create(identifier, manifest)
<p>Constructs an instance of the credential issuer
based on the specified credential manifest.
TODO: check if manifest param is id in hub of credential manifest.</p>

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
<p>Constructs an instance of the CredentialManifest class from a well-formed credential manifest JSON object.
TODO: check that the JSON parameter is valid (yup?)</p>

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
<a name="CredentialStore"></a>

## CredentialStore
<p>Interface defining methods and properties to
be implemented by specific credential stores.</p>

**Kind**: global class  

* [CredentialStore](#CredentialStore)
    * [new CredentialStore(hubSession)](#new_CredentialStore_new)
    * [.create(identifier, publicKeyReference, hubInstance)](#CredentialStore+create)

<a name="new_CredentialStore_new"></a>

### new CredentialStore(hubSession)
<p>Creates a Credential Store from a Hub Session.</p>


| Param | Description |
| --- | --- |
| hubSession | <p>hub session for the credential store.</p> |

<a name="CredentialStore+create"></a>

### credentialStore.create(identifier, publicKeyReference, hubInstance)
<p>Creates a Credential Store from identifier.</p>

**Kind**: instance method of [<code>CredentialStore</code>](#CredentialStore)  

| Param | Description |
| --- | --- |
| identifier | <p>Identifier of Hub or User of Hub.</p> |
| publicKeyReference | <p>Reference to the Public Key for Hub.</p> |
| hubInstance | <p>Hub Instance Reference if Identifier is a User Identifier.</p> |

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
    * [.get(keyIdentifier)](#InMemoryKeyStore+get)
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

<a name="InMemoryKeyStore+get"></a>

### inMemoryKeyStore.get(keyIdentifier)
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

<a name="Jwt"></a>

## Jwt
<p>Class for creating and managing a JWT-formed claims</p>

**Kind**: global class  

* [Jwt](#Jwt)
    * [new Jwt(jwsToken)](#new_Jwt_new)
    * _instance_
        * [.extractContents()](#Jwt+extractContents)
        * [.sign()](#Jwt+sign)
        * [.verify()](#Jwt+verify) ⇒
    * _static_
        * [.create(content, options)](#Jwt.create)

<a name="new_Jwt_new"></a>

### new Jwt(jwsToken)
<p>Constructs an instance of the JWT Class</p>


| Param | Description |
| --- | --- |
| jwsToken | <p>a jwsToken Object.</p> |

<a name="Jwt+extractContents"></a>

### jwt.extractContents()
<p>Returns the extracted contents of the token.</p>

**Kind**: instance method of [<code>Jwt</code>](#Jwt)  
<a name="Jwt+sign"></a>

### jwt.sign()
<p>Sign the claim and return a JWT</p>

**Kind**: instance method of [<code>Jwt</code>](#Jwt)  
<a name="Jwt+verify"></a>

### jwt.verify() ⇒
<p>Verify the claim and return the contents</p>

**Kind**: instance method of [<code>Jwt</code>](#Jwt)  
**Returns**: <p>the stringified contents of the token.</p>  
<a name="Jwt.create"></a>

### Jwt.create(content, options)
<p>Create a new instance of JWT Class.</p>

**Kind**: static method of [<code>Jwt</code>](#Jwt)  

| Param | Description |
| --- | --- |
| content | <p>either the signed payload represented as a string or the payload object to be signed.</p> |
| options | <p>optional options such as the cryptoSuites used to sign/verify JwsToken. TODO: decide if cryptofactory is an advanced option and have defaults instead (RSA and EC)</p> |

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
<a name="SignatureFormat"></a>

## SignatureFormat
<p>Enum to define different signature formats</p>

**Kind**: global variable  
<a name="ClaimDetailsType"></a>

## ClaimDetailsType
<p>ClaimDetails Types</p>

**Kind**: global variable  
<a name="ClaimDetailsType"></a>

## ClaimDetailsType
<p>Interface defining methods and properties for a Token object</p>

**Kind**: global variable  
<a name="context"></a>

## context
<p>context for credentialManifest</p>

**Kind**: global constant  
<a name="type"></a>

## type
<p>type for credentialManifest</p>

**Kind**: global constant  
