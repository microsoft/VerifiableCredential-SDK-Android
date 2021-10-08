❗ **From October 31, 2021, some Microsoft Azure AD Verifiable Credential SDK functionality will stop working in Microsoft Authenticator. Applications and services that currently use the Microsoft Azure AD Verifiable Credential SDK should migrate to the Microsoft [Request Service REST API](https://aka.ms/vcapi).**

-----

[![Build Status](https://dev.azure.com/verifiable-credentials/VerifiableCredential/_apis/build/status/microsoft.VerifiableCredential-SDK-Android%20(1)?branchName=master)](https://dev.azure.com/verifiable-credentials/VerifiableCredential/_build/latest?definitionId=3&branchName=master)
![Test Results](https://img.shields.io/azure-devops/coverage/verifiable-credentials/VerifiableCredential/3)
![Tests Passing](https://img.shields.io/azure-devops/tests/verifiable-credentials/VerifiableCredential/3)
![Open Issued](https://img.shields.io/github/issues/microsoft/VerifiableCredential-SDK-Android)

This SDK is used in the [Microsoft Authenticator app](https://www.microsoft.com/en-us/account/authenticator) in order to interact with [verifiable credentials](https://www.w3.org/TR/vc-data-model/) and [Decentralized Identifiers (DIDs)](https://www.w3.org/TR/did-core/) on the [ION network](https://github.com/decentralized-identity/ion). It can be integrated with any app to provide interactions using verifiable credentials.
 
# Verifiable Credentials 
 
Verifiable credentials is a [W3C standard](https://www.w3.org/TR/vc-data-model/) that can be used to validate information about people, organizations, and more. Verifiable credentials put people in control of their personal information, enabling more trustworthy digital experiences while respecting people's privacy. 
 
To learn more about verifiable credentials, please review our [documentation.](https://didproject.azurewebsites.net/docs/verifiable-credentials.html)

# How to use SDK

## Initializing SDK
`VerifiableCredentialSdk` - this class is used to initialize the SDK. You may want to call it in your apps `Application` `onCreate()` or during your dependency injection initialization:
```kotlin
VerifiableCredentialSdk.init(getApplicationContext());
```

After initialization you can access various services from this singleton directly, but we recommend to access it through your dependency injection framework (like dagger). We currently support the following services:

```kotlin
VerifiableCredentialSdk.issuanceService
VerifiableCredentialSdk.presentationService
VerifiableCredentialSdk.revocationService
```

## APIs

Our APIs use Kotlin coroutines. Read more about coroutines [here](https://kotlinlang.org/docs/coroutines-overview.html). To be able to call into `suspend` functions you need to run the method within a Coroutine Context. Usually you can start one in your Fragment as such:

```kotlin
lifecycleScope.launchWhenStarted {
    // call suspend functions
}
```

All our public APIs return `Result<T>` objects. This forces explicit error handling. Always make sure to use the following import statement.
```kotlin
import com.microsoft.did.sdk.util.controlflow.Result
```

You can unpack and handle these results easily in Kotlin with the `when` statement

```kotlin
when (result) {
    is Result.Success -> handleRequestSuccess(result.payload) // will be smartcast into <T>
    is Result.Failure -> handleRequestFailure(result.payload) // will be smartcast into SdkException
}
```

## Receive a Verifiable Credential (IssuanceService)

To receive a verifiable credential you need a service endpoint providing an issuance contract. You can either get it from someone or create your own. See [How to customize your credentials](https://docs.microsoft.com/en-us/azure/active-directory/verifiable-credentials/credential-design) for more information or use an existing provider. In the future, we plan to support the Decentralized Identity Foundation (DIF) standard [Credential Manifest](https://identity.foundation/credential-manifest/).

```kotlin
suspend fun issuanceSample() {
    when (val result = VerifiableCredentialSdk.issuanceService.getRequest("<issuance request url>")) {
        is Result.Success -> handleRequestSuccess(result.payload)
        is Result.Failure -> handleRequestFailure(result.payload)
    }
}

private suspend fun handleRequestSuccess(request: IssuanceRequest) {
    val response = IssuanceResponse(request)
    addRequestedData(response)
    when (val result = VerifiableCredentialSdk.issuanceService.sendResponse(response)) {
        is Result.Success -> handleResponseSuccess(result.payload)
        is Result.Failure -> handleResponseFailure(result.payload)
    }
}
```

Most issuance requests will ask you for attestations that the user might need to provide. Provide them by filling the values for the existing keys in the three available maps for self attested claims, idtokens and vcs.

```kotlin
private fun addRequestedData(response: IssuanceResponse) {
    for (requestedClaim in response.requestedSelfAttestedClaimMap) {
        requestedClaim.setValue("your data") 
    }
    for (requestedIdToken in response.requestedIdTokenMap) {
        requestedIdToken.setValue("your idToken") 
    }
    for (requestedVc in response.requestedVcMap) {
        requestedVc.setValue(yourVc) 
    }
}
```

See the [full sample](https://github.com/microsoft/VerifiableCredential-SDK-Android/blob/master/sdk/src/samples/java/com/microsoft/did/sdk/IssuanceSample.kt).

## Present Verifiable Credentials (PresentationService)

A presentation request can ask for multiple VCs and follows the [Presentation Exchange](https://identity.foundation/presentation-exchange/) of the Decentralized Identity Foundation. In code, presenting follows an almost identical pattern as issuance.

```kotlin
suspend fun presentationSample() {
    when (val result = VerifiableCredentialSdk.presentationService.getRequest("<presentation request url>")) {
        is Result.Success -> handleRequestSuccess(result.payload)
        is Result.Failure -> handleRequestFailure(result.payload)
    }
}

private suspend fun handleRequestSuccess(request: PresentationRequest) {
    val response = PresentationResponse(request)
    addRequestedData(response)
    when (val result = VerifiableCredentialSdk.presentationService.sendResponse(response)) {
        is Result.Success -> handleResponseSuccess()
        is Result.Failure -> handleResponseFailure(result.payload)
    }
}
```

You can only present VCs in a presentation request. Add the requested VCs:

```kotlin
private fun addRequestedData(response: PresentationResponse) {
    for (requestedVc in response.requestedVcPresentationSubmissionMap) {
        requestedVc.setValue(yourVc) // Set values here
    }
}
```

See the [full sample](https://github.com/microsoft/VerifiableCredential-SDK-Android/blob/master/sdk/src/samples/java/com/microsoft/did/sdk/PresentationSample.kt).

## Revoke a Verifiable Presentation (RevocationService)

A Verifiable Presentation can be revoked from a relying party (RP).

```kotlin
suspend fun revocationSample(verifiableCredential: VerifiableCredential) {
    val rpList = listOf("did:ion:12345") // provide a list of DIDs that the VC is revoked from
    when (val result = VerifiableCredentialSdk.revocationService.revokeVerifiablePresentation(verifiableCredential, rpList)) {
        is Result.Success -> handleRevokeSuccess(result.payload)
        is Result.Failure -> handleRevokeFailure(result.payload)
    }
}
```

See the [full sample](https://github.com/microsoft/VerifiableCredential-SDK-Android/blob/master/sdk/src/samples/java/com/microsoft/did/sdk/RevocationSample.kt).

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
