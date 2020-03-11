package com.microsoft.portableIdentity.sdk.crypto.models

enum class AndroidConstants(val value: String) {
    // key factory algorithm names from https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyFactory
    Rsa("RSA"),
    Ec("EC"),
    Aes("AES"),
    AesWrap("AESWRAP"),
    // Signature algorithm names from https://developer.android.com/training/articles/keystore.html#SupportedSignatures
    EcDsaSha1("SHA1withECDSA"),
    EcDsaSha224("SHA224withECDSA"),
    EcDsaSha256("SHA256withECDSA"),
    EcDsaSha384("SHA384withECDSA"),
    EcDsaSha512("SHA512withECDSA"),
    RsSha1("SHA1withRSA"),
    RsSha224("SHA224withRSA"),
    RsSha256("SHA256withRSA"),
    RsSha384("SHA384withRSA"),
    RsSha512("SHA512withRSA"),




    //////////////////////
    // Custom Constants //
    //////////////////////
    KeyReference("KeyReference")
}