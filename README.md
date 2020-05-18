[![Build Status](https://dev.azure.com/decentralized-identity/Core/_apis/build/status/VerifiableCredential-Android-SDK?branchName=master&jobName=Build)](https://dev.azure.com/decentralized-identity/Core/_build/latest?definitionId=29&branchName=master)
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

# How to use SDK

## Initializing SDK
`PortableIdentitySdk` - this class is used to initialize the SDK inside of the app with these init method parameters:
```kotlin
init(
        context: Context, // App Context.
        logConsumerBridge: SdkLog.ConsumerBridge = DefaultLogConsumerBridge(), // Bridge for logging.
        registrationUrl: String = "", // Registration url for registering Identifier (not needed for MVP)
        resolverUrl: String = "https://dev.discover.did.msidentity.com/1.0/identifiers" // Resolver url for resolving Identifiers.
    )
```

Example of SDK initialization within app:
```kotlin
val piSdk = PortableIdentitySdk.init(getApplicationContext(), new PortableIdentitySdkLogConsumerBridge());
```

> note: Dependency Injection is configured through [Dagger](https://github.com/google/dagger) in our SDK.

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

> note: Personas and Pairwise Identifiers to come.

### Card Manager
`CardManager` - this class deals with any logic related to Portable Identity Cards such as requesting a card through the Issuance service, presenting Verifiable Credentials back to relying parties, and saving cards.

Issuance Flow Example:
```kotlin
// to get a new issuance request from a contract url
val request = cardManager.getIssuanceRequest(url)

// create and send issuance response.
val response = cardManager.createIssuanceResponse(request)
response.addSelfIssuedClaim(claim_field, value)
response.addIdToken(id_token_configuration_url, collected_idtoken)
response.addCard(card_type, collected_portable_identity_card)
val issuedCard = cardManager.sendIssuanceResponse(response, identifier) // identifier to sign response with
cardManager.saveCard(issuedCard)
```

Presentation Flow Example:
```kotlin
// to get a new presentation request from a openid:// scanned through QRCode or deeplink
val request = cardManager.getPresentationRequest(url)
if (cardManager.isValid(request)) {
    // create and send presentation response.
    val response = cardManager.createPresentationResponse(request)
    response.addSelfIssuedClaim(claim_field, value)
    response.addCard(card_type, collected_portable_identity_card)
    cardManager.sendPresentationResponse(response, identifier) // identifier to sign response with
    cardManager.saveCard(issuedCard)
}
```

Get all saved Portable Identity Cards
```kotlin
val cards = cardManager.getCards()
```

> note: Every method is wrapped in a Result object. Unwrapping these returns is not included in these examples to simplify things a bit. (see [Result Class Section](#Result-Class) for more details)

### Formatters and Validators

#### Formatter
A `Formatter` object takes in a `Response` and creates a token payload based on protocol defined in the `Response`. Then signs the payload with the keys owned by the responder `Identifier`.
```kotlin
interface Formatter {
    fun formAndSignResponse(response: Response, responder: Identifier, expiresIn: Int): Result<String>
}
```

> note: [OIDC Self-Issued Protocol](https://openid.net/specs/openid-connect-core-1_0.html#SelfIssued) is the only protocol we support as of now in the `OidcResponseFormatter` class.

#### Validator 
`Validator` object takes in a `Request` and validates the request based on protocol.
```kotlin
interface Validator {
    suspend fun validate(request: Request): Result<Unit>
}
```

> note: [OIDC Self-Issued Protocol](https://openid.net/specs/openid-connect-core-1_0.html#SelfIssued) is the only protocol we support as of now in `OidcRequestValidator` class.


### Portable Identity Card Data Model
Portable Identity Cards are comprised of:
* A unique ID
* A [Verifiable Credential](https://www.w3.org/TR/vc-data-model/)
* Display information in order to render cards inside of the app.

```kotlin
data class PortableIdentityCard (

    val id: String,

    val verifiableCredential: VerifiableCredential,

    val displayContract: DisplayContract
)
```

> note: this data model will change when pairwise feature is implemented.

### Receipts
A `Receipt` is created for every card that is presented in a Presentation Flow. The purpose of a `Receipt` is to keep track of when a Portable Identity Card was presented and who that card was presented to.

```kotlin
data class Receipt (
    val id: Int,

    // Issuance or Presentation
    val action: ReceiptAction,

    val token: String,

    // did of the verifier/issuer
    val entityIdentifier: String,

    // date action occurred
    val activityDate: Long,

    // Host name of verifier/issuer
    val entityHostName: String,

    //Name of the verifier/issuer
    val entityName: String,

    val cardId: String
)
```

### Repository Layer
The repository is an abstraction layer that is consumed by business logic and abstracts away the various data sources that an app can have. There are two datasources in our SDK: network and database.

`CardRepository` - this class saves Portable Identity Cards and Receipts to the database, retrieves cards and receipts from the database, `GET`s presentation and issuance requests, and `POST`s presentation and issuance responses.

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
    class Failure(val payload: PortableIdentitySdkException) : Result<Nothing>()
}
```

If the method was successful, `Result.Success(ReturnType)` is returned.
If an exception occurred, `Result.Failure(PortableIdentityCardException)` is returned.


