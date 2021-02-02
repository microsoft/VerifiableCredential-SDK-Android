// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

object JavaObjectSerializer {
    fun fromString(serializedObject: String): Any {
        val data = Base64.decode(serializedObject, Base64.DEFAULT)
        val ois = ObjectInputStream(ByteArrayInputStream(data))
        val obj = ois.readObject()
        ois.close()
        return obj
    }

    fun toString(obj: Serializable): String {
        val baos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(baos)
        oos.writeObject(obj)
        oos.close()
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
    }
}