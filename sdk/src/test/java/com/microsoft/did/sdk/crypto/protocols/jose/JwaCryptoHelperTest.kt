// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.protocols.jose

import com.microsoft.did.sdk.util.controlflow.ValidatorException
import org.assertj.core.api.Assertions
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.Test

class JwaCryptoHelperTest {

    @Test
    fun `pass when # is present in jwt header kid`() {
        val kid = """did:ion:EiCZSL2UaBgXfH23f9NTySv0YV0aPlAOdTlqWKPp6y3xLA:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiIzMjc0Y2RkY2IzZTY0MjU4YTlmN2NlMzg1NGE0ZmMwOXZjU2lnbmluZ0tleS1hODI3ZSIsInB1YmxpY0tleUp3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiJPZU81VE9FMlQtaGFoQ3FpUVVOTVdSM0VZaG1ral9LNGxUUUQ1MWR2dkgwIiwieSI6IlVndWVsN29Mc1VjcF9KdWtQbE1DSmppYzBQLUZpd005b3JOMjdVa2ZSSEUifSwicHVycG9zZXMiOlsiYXV0aGVudGljYXRpb24iLCJhc3NlcnRpb25NZXRob2QiXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XSwic2VydmljZXMiOlt7ImlkIjoibGlua2VkZG9tYWlucyIsInNlcnZpY2VFbmRwb2ludCI6eyJvcmlnaW5zIjpbImh0dHBzOi8vc3dlZXBzdGFrZXMuZGlkLm1pY3Jvc29mdC5jb20vIl19LCJ0eXBlIjoiTGlua2VkRG9tYWlucyJ9LHsiaWQiOiJodWIiLCJzZXJ2aWNlRW5kcG9pbnQiOnsiaW5zdGFuY2VzIjpbImh0dHBzOi8vYmV0YS5odWIubXNpZGVudGl0eS5jb20vdjEuMC9lMWY2NmYyZS1jMDUwLTQzMDgtODFiMy0zZDdlYTdlZjNiMWIiXX0sInR5cGUiOiJJZGVudGl0eUh1YiJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6IkVpRHJ6N3ZOY2s4R21mSGoxX3hGZW5aWnJEOTF3QzV3d1I4enZRVVlHdlNWeWcifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giOiJFaUFSMzM0MmpNbzJWeTlQX0w5ak9xdEhkRzZSOUs1ZVZWc2owSTgzbjhKQjhRIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlETTZieldGY0JqZFVXR1FrcGtYQmFyUlBmUU9nUDNNTWxCa18zeVZ1TUxWZyJ9fQ#3274cddcb3e64258a9f7ce3854a4fc09vcSigningKey-a827e"""
        val expectedResult = Pair("""did:ion:EiCZSL2UaBgXfH23f9NTySv0YV0aPlAOdTlqWKPp6y3xLA:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiIzMjc0Y2RkY2IzZTY0MjU4YTlmN2NlMzg1NGE0ZmMwOXZjU2lnbmluZ0tleS1hODI3ZSIsInB1YmxpY0tleUp3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiJPZU81VE9FMlQtaGFoQ3FpUVVOTVdSM0VZaG1ral9LNGxUUUQ1MWR2dkgwIiwieSI6IlVndWVsN29Mc1VjcF9KdWtQbE1DSmppYzBQLUZpd005b3JOMjdVa2ZSSEUifSwicHVycG9zZXMiOlsiYXV0aGVudGljYXRpb24iLCJhc3NlcnRpb25NZXRob2QiXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XSwic2VydmljZXMiOlt7ImlkIjoibGlua2VkZG9tYWlucyIsInNlcnZpY2VFbmRwb2ludCI6eyJvcmlnaW5zIjpbImh0dHBzOi8vc3dlZXBzdGFrZXMuZGlkLm1pY3Jvc29mdC5jb20vIl19LCJ0eXBlIjoiTGlua2VkRG9tYWlucyJ9LHsiaWQiOiJodWIiLCJzZXJ2aWNlRW5kcG9pbnQiOnsiaW5zdGFuY2VzIjpbImh0dHBzOi8vYmV0YS5odWIubXNpZGVudGl0eS5jb20vdjEuMC9lMWY2NmYyZS1jMDUwLTQzMDgtODFiMy0zZDdlYTdlZjNiMWIiXX0sInR5cGUiOiJJZGVudGl0eUh1YiJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6IkVpRHJ6N3ZOY2s4R21mSGoxX3hGZW5aWnJEOTF3QzV3d1I4enZRVVlHdlNWeWcifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giOiJFaUFSMzM0MmpNbzJWeTlQX0w5ak9xdEhkRzZSOUs1ZVZWc2owSTgzbjhKQjhRIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlETTZieldGY0JqZFVXR1FrcGtYQmFyUlBmUU9nUDNNTWxCa18zeVZ1TUxWZyJ9fQ""", """3274cddcb3e64258a9f7ce3854a4fc09vcSigningKey-a827e""")
        val actualResult = JwaCryptoHelper.extractDidAndKeyId(kid)
        assertThat(actualResult).isEqualTo(expectedResult)
    }

    @Test
    fun `fail when # is present in jwt header kid`() {
        val kid = """did:ion:EiCZSL2UaBgXfH23f9NTySv0YV0aPlAOdTlqWKPp6y3xLA:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiIzMjc0Y2RkY2IzZTY0MjU4YTlmN2NlMzg1NGE0ZmMwOXZjU2lnbmluZ0tleS1hODI3ZSIsInB1YmxpY0tleUp3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiJPZU81VE9FMlQtaGFoQ3FpUVVOTVdSM0VZaG1ral9LNGxUUUQ1MWR2dkgwIiwieSI6IlVndWVsN29Mc1VjcF9KdWtQbE1DSmppYzBQLUZpd005b3JOMjdVa2ZSSEUifSwicHVycG9zZXMiOlsiYXV0aGVudGljYXRpb24iLCJhc3NlcnRpb25NZXRob2QiXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XSwic2VydmljZXMiOlt7ImlkIjoibGlua2VkZG9tYWlucyIsInNlcnZpY2VFbmRwb2ludCI6eyJvcmlnaW5zIjpbImh0dHBzOi8vc3dlZXBzdGFrZXMuZGlkLm1pY3Jvc29mdC5jb20vIl19LCJ0eXBlIjoiTGlua2VkRG9tYWlucyJ9LHsiaWQiOiJodWIiLCJzZXJ2aWNlRW5kcG9pbnQiOnsiaW5zdGFuY2VzIjpbImh0dHBzOi8vYmV0YS5odWIubXNpZGVudGl0eS5jb20vdjEuMC9lMWY2NmYyZS1jMDUwLTQzMDgtODFiMy0zZDdlYTdlZjNiMWIiXX0sInR5cGUiOiJJZGVudGl0eUh1YiJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6IkVpRHJ6N3ZOY2s4R21mSGoxX3hGZW5aWnJEOTF3QzV3d1I4enZRVVlHdlNWeWcifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giOiJFaUFSMzM0MmpNbzJWeTlQX0w5ak9xdEhkRzZSOUs1ZVZWc2owSTgzbjhKQjhRIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlETTZieldGY0JqZFVXR1FrcGtYQmFyUlBmUU9nUDNNTWxCa18zeVZ1TUxWZyJ9fQ3274cddcb3e64258a9f7ce3854a4fc09vcSigningKey-a827e"""
        try {
            JwaCryptoHelper.extractDidAndKeyId(kid)
        } catch (exception: Exception) {
            Assertions.assertThat(exception).isInstanceOf(ValidatorException::class.java)
        }
    }
}