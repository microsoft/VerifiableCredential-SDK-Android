[![Build status](https://dev.azure.com/decentralized-identity/Core/_apis/build/status/PICS-client-sdk-android)](https://dev.azure.com/decentralized-identity/Core/_build/latest?definitionId=29)

This SDK is used in the [Microsoft Authenticator app](https://www.microsoft.com/en-us/account/authenticator) in order to interact with [verifiable credentials](https://www.w3.org/TR/vc-data-model/) and DIDs on the [ION network.](https://github.com/decentralized-identity/ion)
 
# Verifiable Credentials 
 
Verifiable credentials are a [W3C standard format](https://www.w3.org/TR/vc-data-model/) that can be used to validate information about people, organizations, and more. Verifiable credentials put people in control of their personal information, enabling more trustworthy digital experiences while respecting people's privacy. 
 
To learn more about verifiable credentials, please review our [documentation.](https://didproject.azurewebsites.net/docs/verifiable-credentials.html)

# How to use SDK

## Initializing SDK
`VerifiableCredentialSdk` - this class is used to initialize the SDK inside of the app with these init method parameters:
```kotlin
fun init(
    context: Context,
    logConsumer: SdkLog.Consumer = DefaultLogConsumer(), // Optional: Provide your app logger
    registrationUrl: String = "", // Not supported in MVP
    resolverUrl: String = "https://beta.discover.did.microsoft.com/1.0/identifiers" // Optional: Provide identifier resolution service
)
```

Example of SDK initialization within app (call in your `Application` `onCreate()`):
```kotlin
VerifiableCredentialSdk.init(getApplicationContext(), new VerifiableCredentialSdkLogConsumer());
```

After initialization you can access `identifierManager` and `verifiableCredentialManager` from this singleton directly, but it you may want to access it through your dependency injection framework (like dagger).
```kotlin
VerifiableCredentialSdk.verifiableCredentialManager
VerifiableCredentialSdk.identifierManager
```

## External facing APIs
There two classes that are external to our SDK.

### Identifier Manager
`IdentifierManager` - this class deals with any logic related to Identifiers such as creating Identifiers, creating Pairwise Identifiers, and resolving Identifiers through the Resolver.

Creating and saving Identifier Example:
```kotlin
val identifier = identifierManager.initLongFormIdentifier()
```

To get master Identifier
```kotlin
val identifier = identifierManager.getIdentifier()
```

> note: Personas Identifiers to come.

#### Pairwise Identifiers
Pairwise Identifier are Identifiers created from four things:
* An Identifier (in our case, the Master Identifier)
* A target string (in our case, the relying party DID)
* The algorithm of the keys that will be generated for the Identifier
* The key type of the keys that will be generated for the Identifier

Pairwise Identifiers are created for every interaction with a relying party to prevent to Relying Parties from correlating users based off of Identifiers. 

### Verifiable Credential Manager
`VerifiableCredentialManager` - this class deals with any logic related to Verifiable Credentials such as requesting vcs through the Issuance service, presenting vcs back to relying parties, and saving them.

Issuance Flow Example:
```kotlin
// to get a new issuance request from a contract url
val request = verifiableCredentialManager.getIssuanceRequest(url)

// create issuance response.
val response = verifiableCredentialManager.createIssuanceResponse(request)
// add requested verifiable credentials to response.
addCollectedRequirementsToResponse(response, requirementList)
// get Master Identifier.
val identifier = identifierManager.getMasterIdentifier()
// create a pairwise identifier for connection from master identifier and requester's identifier.
val pairwiseIdentifier = identifierManager.createPairwiseIdentifier(identifier, request.entityIdentifier)
// send issuance response in order to get a verifiable credential, signed by pairwise identifier. 
val vch = verifiableCredentialManager.sendIssuanceResponse(response, pairwiseIdentifier)
// save vc to database.
verifiableCredentialManager.saveVch(vch)
```

Presentation Flow Example:
```kotlin
// to get a new presentation request from a openid:// scanned through QRCode or deeplink
val request = verifiableCredentialManager.getPresentationRequest(url)

// create and send presentation response.
val response = verifiableCredentialManager.createPresentationResponse(request)
// add requested verifiable credentials to response.
addCollectedRequirementsToResponse(response, requirementList)
// get Master Identifier.
val identifier = identifierManager.getMasterIdentifier()
// create a pairwise identifier for connection from master identifier and requester's identifier.
val pairwiseIdentifier = identifierManager.createPairwiseIdentifier(identifier, request.entityIdentifier)
// send response to relying party the initiated request, signed by pairwise identifier.
// if successful, create and store receipts of interaction with the relying party for each verifiable credential that was presented.
verifiableCredentialManager.sendPresentationResponse(response, pairwiseIdentifier)
```

Get all saved VCs
```kotlin
val verifiableCredentials = verifiableCredentialManager.getVerifiableCredentials()
```

> note: Every method is wrapped in a Result object. Unwrapping these returns is not included in these examples to simplify things a bit. (see [Result Class Section](#Result-Class) for more details)

### OIDC Formatter and Validator

#### OIDC Response Formatter
A `OidcResponseFormatter` object creates a token payload based on the OpenID Connect Protocol (Self-Issued token) with an addition of a `attestations` claim that is not yet standard. This `attestations` claim contains all verifiable credentials, id-tokens, or self-attested claims that the initial request asked for. Then the payload is signed with the keys owned by the responder `Identifier`.

> note: [OIDC Self-Issued Protocol](https://openid.net/specs/openid-connect-core-1_0.html#SelfIssued) is the only protocol we support as of now in the `OidcResponseFormatter` class.

#### Presentation Request Validator 
`PresentationRequestValidator` object takes in a `PresentationRequest` and validates the request based on protocol.
```kotlin
interface PresentationRequestValidator {
    suspend fun validate(request: PresentationRequest)
}
```

> note: [OIDC Self-Issued Protocol](https://openid.net/specs/openid-connect-core-1_0.html#SelfIssued) is the only protocol we support as of now in `OidcRequestValidator` class.


### VerifiableCredential Data Model
VerifiableCredentialHolder is comprised of:
* A unique ID
* A [Verifiable Credential](https://www.w3.org/TR/vc-data-model/)
* Display information in order to render the vc.
* History and other meta data

```kotlin
data class VerifiableCredentialHolder (

    val id: String,

    val verifiableCredential: VerifiableCredential,

    val displayContract: DisplayContract
)
```

> note: this data model will change when pairwise feature is implemented.

### Receipts
A `Receipt` is created for every verifiable credential that is presented. The purpose of a `Receipt` is to keep track of when a verifiable credential was presented and who that vc was presented to.

```kotlin
data class Receipt (
    val id: Int,

    // Issuance or Presentation
    val action: ReceiptAction,

    // did of the verifier/issuer
    val entityIdentifier: String,

    // date action occurred
    val activityDate: Long,

    //Name of the verifier/issuer
    val entityName: String,

    val vcId: String
)
```

### Repository Layer
The repository is an abstraction layer that is consumed by business logic and abstracts away the various data sources that an app can have. There are two datasources in our SDK: network and database.

`VerifiableCredentialRepository` - this class saves Verifiable Credentials and Receipts to the database, retrieves vcs and receipts from the database, `GET`s presentation and issuance requests, and `POST`s presentation and issuance responses.

`IdentifierRepository` - this class save Identifiers to database, retrieves Identifiers from database, and resolves Identifiers.

> note: we are using [Room](https://developer.android.com/topic/libraries/architecture/room) for database access and [Retrofit](https://square.github.io/retrofit/) for network calls.

### Crypto Layer
`CryptoOperations` - this class is the top layer of our crypto abstractions. 

Initialized like so:
```kotlin
class CryptoOperations (
    subtleCrypto: SubtleCrypto,
    val keyStore: KeyStore
)
```
* SubtleCrypto - interface of the [Web Crypto API](https://developer.mozilla.org/en-US/docs/Web/API/SubtleCrypto) that provides a number of low-level cryptographic functions
* KeyStore - where keys are stored, default implementation is AndroidKeyStore.

Crypto methods exposed in cryptoOperations layer
```kotlin
fun sign(payload: ByteArray, signingKeyReference: String, algorithm: Algorithm? = null)
fun verify(payload: ByteArray, signature: ByteArray, signingKeyReference: String, algorithm: Algorithm? = null)
fun encrypt() // TODO
fun decrypt() // TODO
fun generateKeyPair(keyReference: String, keyType: KeyType)
fun generatePairwise(seed: String)
fun generateSeed(): String
```

### Result Class
Every external method returns a `Result` for error handling simplicity.

```kotlin
sealed class Result<out S> {
    class Success<out S>(val payload: S) : Result<S>()
    class Failure(val payload: SdkException) : Result<Nothing>()
}
```

If the method was successful, `Result.Success(ReturnType)` is returned.
If an exception occurred, `Result.Failure(SdkException)` is returned.

# Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.opensource.microsoft.com.

When you submit a pull request, a CLA bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., status check, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.
