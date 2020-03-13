
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

# Request and Response Models
Here is an example of how to respond with credentials to a signed request.

* Supports JwsTokens only (but extensible to support JweTokens and other token protocols).
* Authentication protocols abstracted out (supports OIDC/SIOP requests only right now).
* Configurable Protectors (only supporting Signers for now).
```kotlin
val serializedRequest: String = "ey12ds3d..."
val request: Request = Request.create(serializedRequest)
if (!request.valid()) {
    // throw some error
}
val credentialRequests: CredentialRequests = request.getCredentialRequests()
val response: Response = Response(request)
// taking credentialRequests and getting corresponding credential,
// out of scope for now.
response.addCredential(credential)
// optional key reference for signing or use default
val signer = Signer(keyReference)
response.addProtector(signer)
val httpResponse = response.send()
```