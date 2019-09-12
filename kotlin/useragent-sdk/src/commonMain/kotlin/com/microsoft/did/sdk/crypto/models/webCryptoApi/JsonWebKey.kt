package com.microsoft.did.sdk.crypto.models.webCryptoApi

/** The following fields are defined in Section 6.3.2.7 of JSON Web Algorithms */
//@Serializable
data class RsaOtherPrimesInfo (val r: String, val d: String, val t: String)

//@Serializable
data class JsonWebKey(
    // The following fields are defined in Section 3.1 of JSON Web Key
    var kty: String = "",
    var kid: String? = null,
    var use: String? = null,
    var key_ops: List<String>? = null,
    var alg: String? = null,

    // The following fields are defined in JSON Web Key Parameters Registration
    var ext: Boolean? = null,

    // The following fields are defined in Section 6 of JSON Web Algorithms
    var crv: String? = null,
    var x: String? = null,
    var y: String? = null,
    var d: String? = null,
    var n: String? = null,
    var e: String? = null,
    var p: String? = null,
    var q: String? = null,
    var dp: String? = null,
    var dq: String? = null,
    var qi: String? = null,
    var oth: List<RsaOtherPrimesInfo>? = null,
    var k: String? = null
) {
    fun toJson(): String {
        var json = StringBuilder()
        json.append("{\"kty\": \"$kty\"")
        appendIfNonNull("kid", kid, json)
        appendIfNonNull("use", use, json)
        appendIfNonNull("key_ops", key_ops, json) { it }
        appendIfNonNull("alg", alg, json)
        appendIfNonNull("ext", ext?.toString(), json)
        appendIfNonNull("crv", crv, json)
        appendIfNonNull("x", x, json)
        appendIfNonNull("y", y, json)
        appendIfNonNull("d", d, json)
        appendIfNonNull("n", n, json)
        appendIfNonNull("e", e, json)
        appendIfNonNull("p", p, json)
        appendIfNonNull("q", q, json)
        appendIfNonNull("dp", dp, json)
        appendIfNonNull("dq", dq, json)
        appendIfNonNull("qi", qi, json)
        appendIfNonNull("oth", oth, json) {
            "{ \"r\": \"${it.r}\", \"d\": \"${it.d}\", \"t\": \"${it.t}\" }"
        }
        appendIfNonNull("k", k, json)
        json.append("}")
        return json.toString()
    }

    private fun appendIfNonNull(name: String, value: String?, builder: StringBuilder) {
        if (value != null) {
            builder.append(", \"$name\": \"$value\"")
        }
    }

    private fun <E> appendIfNonNull(name: String, value: List<E>?, builder: StringBuilder, transform: (E) -> String) {
        if (value != null) {
            builder.append(", \"$name\": [")
            val ops = value.iterator()
            while(ops.hasNext()) {
                val it = ops.next()
                builder.append("\"${transform(it)}\"")
                if (ops.hasNext()) {
                    builder.append(", ")
                }
            }
            builder.append("]")
        }
    }
}
