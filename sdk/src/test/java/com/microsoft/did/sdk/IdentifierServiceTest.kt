// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.IdentifierCreator
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants.MAIN_IDENTIFIER_REFERENCE
import com.microsoft.did.sdk.util.controlflow.RepositoryException
import com.microsoft.did.sdk.util.controlflow.Result
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.Test

class IdentifierServiceTest {

    private val mockedEncryptedKeyStore: EncryptedKeyStore = mockk()
    private val mockedIdentifierCreator: IdentifierCreator = mockk()
    private val mockedIdentifierRepository: IdentifierRepository = mockk()
    private val identifierManager = IdentifierService(mockedIdentifierRepository, mockedIdentifierCreator, mockedEncryptedKeyStore)
    private val mockedIdentifier: Identifier = mockk()
    private val mockedCreatedIdentifier: Identifier = mockk()
    private val mockedMasterDid =
        "did:ion:EiB3S_mP1Fu7wn0le0nsNKmkJP1i21bgoslfccKZ_kQNmQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJKUzhfc2lnbl9JT05fMSIsInB1YmxpY0tleUp3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiJkTTBvbVJXMUQ5M2NNOEY5QWw2ODdwa1lUS0tVZ3dEcU9IcEJVNk9aaGxVIiwieSI6ImwyVEJwbUZkQk03QWFucDZuQXZobUt6cGV0T1lRZmtwWjJHOGJiekZfWGsifSwicHVycG9zZXMiOlsiYXV0aGVudGljYXRpb24iXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6IkVpQzVqUTdMbC1EVDZLbDZEVndCcWF4Rk1aOUxRNEwwNFNsNlduWEtZRkhMV0EifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giOiJFaUJzSmhHYVNwV09JZGNqSlhWaGl2RFVSOUljWEYwdEdoSjVTMHFSTE9XaDJnIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlDblJYSEhqMUpzOGhsZGZ0aFhtS01fUnk5dVhFV0ZqM1FZS0Y1aWZ6ZGVIQSJ9fQ"
    private val mockedMasterIdentifier: Identifier = Identifier(mockedMasterDid, "", "", "", "", "")
    private val mockedPeerDid =
        "did:ion:EiAxwToFHI60qTyOv9Q4Ylle1bZcXzfWQyJbWBPtEn6dBQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJmdThfc2lnbl9JT05fMSIsInB1YmxpY0tleUp3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiJHT0VFVlpsYkdsVTdLUFhsQXJiQlA0NURxMUlBclRqVnRsMDBXS1FSNTA4IiwieSI6Im81cTB3Y1gyczZUZ2pLSGt6QjBLd3dOWkdvcDRZTjdWMjVHQkN4V2pOS2MifSwicHVycG9zZXMiOlsiYXV0aGVudGljYXRpb24iXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6IkVpQ1dnOGZ6MXRGU3pvWm4zSm81ZERjbXQ2b2FVX0stWUtNYUxiNUdCNHVrVVEifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giOiJFaUJ4Y2VCWWxxb2VPUnZXRno1akNFNEYxVGxEMlpZeURqbHlDeFRBc25lVHpRIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlETTEzbjY5a01BUFNZYWhJbkphcWdtWWplS2wwVU9waUh6dVV2TTJONFBOUSJ9fQ"
    private val mockedPairwiseName = "PairwiseName"

    @Test
    fun `test get master identifier successfully when it is already created`() {
        coEvery { mockedIdentifierRepository.queryByName(MAIN_IDENTIFIER_REFERENCE) } returns mockedIdentifier
        runBlocking {
            val actualIdentifier = identifierManager.getMasterIdentifier()
            assertThat(actualIdentifier).isInstanceOf(Result.Success::class.java)
            assertThat((actualIdentifier as Result.Success).payload).isEqualTo(mockedIdentifier)
        }
    }

    @Test
    fun `test get master identifier successfully when it doesn't exist`() {
        coEvery { mockedIdentifierRepository.queryByName(MAIN_IDENTIFIER_REFERENCE) } returns null
        coJustRun { mockedEncryptedKeyStore.storeKey(MAIN_IDENTIFIER_REFERENCE, any()) }
        coEvery { mockedIdentifierCreator.create(MAIN_IDENTIFIER_REFERENCE) } returns mockedCreatedIdentifier
        coJustRun { mockedIdentifierRepository.insert(mockedCreatedIdentifier) }
        runBlocking {
            val actualIdentifier = identifierManager.getMasterIdentifier()
            assertThat(actualIdentifier).isInstanceOf(Result.Success::class.java)
            assertThat((actualIdentifier as Result.Success).payload).isEqualTo(mockedCreatedIdentifier)
        }
    }

    @Test
    fun `test get identifier by id successfully`() {
        coEvery { mockedIdentifierRepository.queryByIdentifier(mockedMasterDid) } returns mockedMasterIdentifier
        runBlocking {
            val actualIdentifier = identifierManager.getIdentifierById(mockedMasterDid)
            assertThat(actualIdentifier).isInstanceOf(Result.Success::class.java)
            assertThat((actualIdentifier as Result.Success).payload).isEqualTo(mockedMasterIdentifier)
        }
    }

    @Test
    fun `test get identifier by id with invalid id`() {
        coEvery { mockedIdentifierRepository.queryByIdentifier("") } returns null
        runBlocking {
            val actualIdentifier = identifierManager.getIdentifierById("")
            assertThat(actualIdentifier).isInstanceOf(Result.Failure::class.java)
            assertThat((actualIdentifier as Result.Failure).payload).isInstanceOf(RepositoryException::class.java)
        }
    }
}