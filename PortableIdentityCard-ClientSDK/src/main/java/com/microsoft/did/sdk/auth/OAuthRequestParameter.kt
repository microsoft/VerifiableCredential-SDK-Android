package com.microsoft.did.sdk.auth

enum class OAuthRequestParameter(val value: String) {
    // Required
    Scope("scope"),
    ResponseType("response_type"),
    ClientId("client_id"),
    RedirectUri("redirect_uri"),

    // Recommended
    State("state"),

    // Optional
    ResponseMode("response_mode"),
    Nonce("nonce"),
    MaxAge("max_age"),
    UiLocales("ui_locales"),
    IdTokenHint("id_token_hint"),

    // Self-issued parameters (optional)
    Registration("registration"),
    Request("request"),
    RequestUri("request_uri"),
    Claims("claims"),

    IdToken("id_token"),

    // custom parameters
    Offer("vc_offer")
}