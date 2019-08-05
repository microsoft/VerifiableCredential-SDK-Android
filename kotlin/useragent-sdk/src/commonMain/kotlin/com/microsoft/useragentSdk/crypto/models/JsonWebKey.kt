package com.microsoft.useragentSdk.crypto.models

object RsaOtherPrimesInfo {
    // The following fields are defined in Section 6.3.2.7 of JSON Web Algorithms
    var r: String = ""
    var d: String = ""
    var t: String = ""
};

object JsonWebKey {
    // The following fields are defined in Section 3.1 of JSON Web Key
    var kty: String = ""
    var use: String? = null
    var key_ops: List<String>? = null
    var alg: String? = null

    // The following fields are defined in JSON Web Key Parameters Registration
    var ext: Boolean? = null

    // The following fields are defined in Section 6 of JSON Web Algorithms
    var crv: String? = null
    var x: String? = null
    var y: String? = null
    var d: String? = null
    var n: String? = null
    var e: String? = null
    var p: String? = null
    var q: String? = null
    var dp: String? = null
    var dq: String? = null
    var qi: String? = null
    var oth: List<RsaOtherPrimesInfo>? = null
    var k: String? = null
}