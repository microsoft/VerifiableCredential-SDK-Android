// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.repository

import com.microsoft.did.sdk.datasource.db.SdkDatabase
import com.microsoft.did.sdk.datasource.db.dao.IdentifierDao
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.identifierOperations.ResolveIdentifierNetworkOperation
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.identifier.models.identifierdocument.IdentifierResponse
import com.microsoft.did.sdk.util.controlflow.NotFoundException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.defaultTestSerializer
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IdentifierRepositoryTest {
    private var identifierRepository: IdentifierRepository
    private val identifierDao: IdentifierDao = mockk()
    private val expectedIdentifierDocumentString =
        """{"@context":"https://www.w3.org/ns/did-resolution/v1","didDocument":{"id":"did:ion:EiBFVG2BhADKN48GNNhD-y0EgrcjluWJg7tQbpe1rv6kMQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJSVTRfc2lnbl9JT05fMSIsInB1YmxpY0tleUp3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiIzbE5VXzIwbnREeHBfZnBnbDRMUEdtYUc4c0M4Ty12N1Aydk5waG00R3BVIiwieSI6IkxzSUl4X1lOd0lsZ1BDamNsbklBUnMtUThtSlc3Z0M0OTJwM1B3OWdBWk0ifSwicHVycG9zZXMiOlsiYXV0aGVudGljYXRpb24iXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6IkVpRDRmZDdGUU04WmtTMF9xVDVva0Y5T1J3enFLSDZFSVB6WWRnU2hIYzg4X2cifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giOiJFaURCSFA5RDNLcXBHWE9pTmlhR01rb3pGOEVZNWxRdFVyaW0zcDBVaUEweGxRIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlETldBSXM0cng3eWpRS1FMRlExZ2MxTUlrSDFQRVRqN3lDWlg2NnJtano1QSJ9fQ","@context":["https://www.w3.org/ns/did/v1",{"@base":"did:ion:EiBFVG2BhADKN48GNNhD-y0EgrcjluWJg7tQbpe1rv6kMQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJSVTRfc2lnbl9JT05fMSIsInB1YmxpY0tleUp3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiIzbE5VXzIwbnREeHBfZnBnbDRMUEdtYUc4c0M4Ty12N1Aydk5waG00R3BVIiwieSI6IkxzSUl4X1lOd0lsZ1BDamNsbklBUnMtUThtSlc3Z0M0OTJwM1B3OWdBWk0ifSwicHVycG9zZXMiOlsiYXV0aGVudGljYXRpb24iXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6IkVpRDRmZDdGUU04WmtTMF9xVDVva0Y5T1J3enFLSDZFSVB6WWRnU2hIYzg4X2cifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giOiJFaURCSFA5RDNLcXBHWE9pTmlhR01rb3pGOEVZNWxRdFVyaW0zcDBVaUEweGxRIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlETldBSXM0cng3eWpRS1FMRlExZ2MxTUlrSDFQRVRqN3lDWlg2NnJtano1QSJ9fQ"}],"verificationMethod":[{"id":"#RU4_sign_ION_1","controller":"did:ion:EiBFVG2BhADKN48GNNhD-y0EgrcjluWJg7tQbpe1rv6kMQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJSVTRfc2lnbl9JT05fMSIsInB1YmxpY0tleUp3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiIzbE5VXzIwbnREeHBfZnBnbDRMUEdtYUc4c0M4Ty12N1Aydk5waG00R3BVIiwieSI6IkxzSUl4X1lOd0lsZ1BDamNsbklBUnMtUThtSlc3Z0M0OTJwM1B3OWdBWk0ifSwicHVycG9zZXMiOlsiYXV0aGVudGljYXRpb24iXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6IkVpRDRmZDdGUU04WmtTMF9xVDVva0Y5T1J3enFLSDZFSVB6WWRnU2hIYzg4X2cifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giOiJFaURCSFA5RDNLcXBHWE9pTmlhR01rb3pGOEVZNWxRdFVyaW0zcDBVaUEweGxRIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlETldBSXM0cng3eWpRS1FMRlExZ2MxTUlrSDFQRVRqN3lDWlg2NnJtano1QSJ9fQ","type":"EcdsaSecp256k1VerificationKey2019","publicKeyJwk":{"crv":"secp256k1","kty":"EC","x":"3lNU_20ntDxp_fpgl4LPGmaG8sC8O-v7P2vNphm4GpU","y":"LsIIx_YNwIlgPCjclnIARs-Q8mJW7gC492p3Pw9gAZM"}}],"authentication":["did:ion:EiBFVG2BhADKN48GNNhD-y0EgrcjluWJg7tQbpe1rv6kMQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJSVTRfc2lnbl9JT05fMSIsInB1YmxpY0tleUp3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiIzbE5VXzIwbnREeHBfZnBnbDRMUEdtYUc4c0M4Ty12N1Aydk5waG00R3BVIiwieSI6IkxzSUl4X1lOd0lsZ1BDamNsbklBUnMtUThtSlc3Z0M0OTJwM1B3OWdBWk0ifSwicHVycG9zZXMiOlsiYXV0aGVudGljYXRpb24iXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6IkVpRDRmZDdGUU04WmtTMF9xVDVva0Y5T1J3enFLSDZFSVB6WWRnU2hIYzg4X2cifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giOiJFaURCSFA5RDNLcXBHWE9pTmlhR01rb3pGOEVZNWxRdFVyaW0zcDBVaUEweGxRIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlETldBSXM0cng3eWpRS1FMRlExZ2MxTUlrSDFQRVRqN3lDWlg2NnJtano1QSJ9fQ#RU4_sign_ION_1"]},"methodMetadata":{"published":false,"recoveryCommitment":"EiDNWAIs4rx7yjQKQLFQ1gc1MIkH1PETj7yCZX66rmjz5A","updateCommitment":"EiD4fd7FQM8ZkS0_qT5okF9ORwzqKH6EIPzYdgShHc88_g"},"resolverMetadata":{"duration":403.544624,"identifier":"did:ion:EiBFVG2BhADKN48GNNhD-y0EgrcjluWJg7tQbpe1rv6kMQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJSVTRfc2lnbl9JT05fMSIsInB1YmxpY0tleUp3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiIzbE5VXzIwbnREeHBfZnBnbDRMUEdtYUc4c0M4Ty12N1Aydk5waG00R3BVIiwieSI6IkxzSUl4X1lOd0lsZ1BDamNsbklBUnMtUThtSlc3Z0M0OTJwM1B3OWdBWk0ifSwicHVycG9zZXMiOlsiYXV0aGVudGljYXRpb24iXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6IkVpRDRmZDdGUU04WmtTMF9xVDVva0Y5T1J3enFLSDZFSVB6WWRnU2hIYzg4X2cifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giOiJFaURCSFA5RDNLcXBHWE9pTmlhR01rb3pGOEVZNWxRdFVyaW0zcDBVaUEweGxRIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlETldBSXM0cng3eWpRS1FMRlExZ2MxTUlrSDFQRVRqN3lDWlg2NnJtano1QSJ9fQ","driverId":"discover.did.microsoft.com/did:ion","didUrl":{"didUrlString":"did:ion:EiBFVG2BhADKN48GNNhD-y0EgrcjluWJg7tQbpe1rv6kMQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJSVTRfc2lnbl9JT05fMSIsInB1YmxpY0tleUp3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiIzbE5VXzIwbnREeHBfZnBnbDRMUEdtYUc4c0M4Ty12N1Aydk5waG00R3BVIiwieSI6IkxzSUl4X1lOd0lsZ1BDamNsbklBUnMtUThtSlc3Z0M0OTJwM1B3OWdBWk0ifSwicHVycG9zZXMiOlsiYXV0aGVudGljYXRpb24iXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6IkVpRDRmZDdGUU04WmtTMF9xVDVva0Y5T1J3enFLSDZFSVB6WWRnU2hIYzg4X2cifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giOiJFaURCSFA5RDNLcXBHWE9pTmlhR01rb3pGOEVZNWxRdFVyaW0zcDBVaUEweGxRIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlETldBSXM0cng3eWpRS1FMRlExZ2MxTUlrSDFQRVRqN3lDWlg2NnJtano1QSJ9fQ","did":{"didString":"did:ion:EiBFVG2BhADKN48GNNhD-y0EgrcjluWJg7tQbpe1rv6kMQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJSVTRfc2lnbl9JT05fMSIsInB1YmxpY0tleUp3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiIzbE5VXzIwbnREeHBfZnBnbDRMUEdtYUc4c0M4Ty12N1Aydk5waG00R3BVIiwieSI6IkxzSUl4X1lOd0lsZ1BDamNsbklBUnMtUThtSlc3Z0M0OTJwM1B3OWdBWk0ifSwicHVycG9zZXMiOlsiYXV0aGVudGljYXRpb24iXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6IkVpRDRmZDdGUU04WmtTMF9xVDVva0Y5T1J3enFLSDZFSVB6WWRnU2hIYzg4X2cifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giOiJFaURCSFA5RDNLcXBHWE9pTmlhR01rb3pGOEVZNWxRdFVyaW0zcDBVaUEweGxRIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlETldBSXM0cng3eWpRS1FMRlExZ2MxTUlrSDFQRVRqN3lDWlg2NnJtano1QSJ9fQ","method":"ion","methodSpecificId":"EiBFVG2BhADKN48GNNhD-y0EgrcjluWJg7tQbpe1rv6kMQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJSVTRfc2lnbl9JT05fMSIsInB1YmxpY0tleUp3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiIzbE5VXzIwbnREeHBfZnBnbDRMUEdtYUc4c0M4Ty12N1Aydk5waG00R3BVIiwieSI6IkxzSUl4X1lOd0lsZ1BDamNsbklBUnMtUThtSlc3Z0M0OTJwM1B3OWdBWk0ifSwicHVycG9zZXMiOlsiYXV0aGVudGljYXRpb24iXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6IkVpRDRmZDdGUU04WmtTMF9xVDVva0Y5T1J3enFLSDZFSVB6WWRnU2hIYzg4X2cifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giOiJFaURCSFA5RDNLcXBHWE9pTmlhR01rb3pGOEVZNWxRdFVyaW0zcDBVaUEweGxRIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlETldBSXM0cng3eWpRS1FMRlExZ2MxTUlrSDFQRVRqN3lDWlg2NnJtano1QSJ9fQ","parseTree":null,"parseRuleCount":null},"parameters":null,"parametersMap":{},"path":"","query":null,"fragment":null,"parseTree":null,"parseRuleCount":null}}}"""
    private val expectedIdentifierDocument =
        defaultTestSerializer.decodeFromString(IdentifierResponse.serializer(), expectedIdentifierDocumentString)
    private val expectedIdentifier =
        "did:ion:EiBFVG2BhADKN48GNNhD-y0EgrcjluWJg7tQbpe1rv6kMQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJSVTRfc2lnbl9JT05fMSIsInB1YmxpY0tleUp3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJrdHkiOiJFQyIsIngiOiIzbE5VXzIwbnREeHBfZnBnbDRMUEdtYUc4c0M4Ty12N1Aydk5waG00R3BVIiwieSI6IkxzSUl4X1lOd0lsZ1BDamNsbklBUnMtUThtSlc3Z0M0OTJwM1B3OWdBWk0ifSwicHVycG9zZXMiOlsiYXV0aGVudGljYXRpb24iXSwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSJ9XX19XSwidXBkYXRlQ29tbWl0bWVudCI6IkVpRDRmZDdGUU04WmtTMF9xVDVva0Y5T1J3enFLSDZFSVB6WWRnU2hIYzg4X2cifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giOiJFaURCSFA5RDNLcXBHWE9pTmlhR01rb3pGOEVZNWxRdFVyaW0zcDBVaUEweGxRIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlETldBSXM0cng3eWpRS1FMRlExZ2MxTUlrSDFQRVRqN3lDWlg2NnJtano1QSJ9fQ"

    init {
        val sdkDatabase: SdkDatabase = mockk()
        val apiProvider: ApiProvider = mockk()
        every { sdkDatabase.identifierDao() } returns identifierDao
        identifierRepository = IdentifierRepository(sdkDatabase, apiProvider)
    }

    @Test
    fun insertAndRetrieveIdentifierByIdTest() {
        val expectedIdentifier = Identifier(
            "did:ion:test:testId1",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateKeyReference",
            "testIdentifierName1"
        )
        coJustRun { identifierDao.insert(expectedIdentifier) }
        coEvery { identifierDao.queryByIdentifier(expectedIdentifier.id) } returns expectedIdentifier
        runBlocking {
            identifierRepository.insert(expectedIdentifier)
            val actualIdentifier = identifierRepository.queryByIdentifier(expectedIdentifier.id)
            assertThat(actualIdentifier).isEqualToComparingFieldByFieldRecursively(expectedIdentifier)
        }
    }

    @Test
    fun insertAndRetrieveIdentifierByNameTest() {
        val expectedIdentifier = Identifier(
            "did:ion:test:testId2",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateKeyReference",
            "testIdentifierName2"
        )
        coJustRun { identifierDao.insert(expectedIdentifier) }
        coEvery { identifierDao.queryByName("testIdentifierName2") } returns expectedIdentifier
        runBlocking {
            identifierRepository.insert(expectedIdentifier)
            val actualIdentifier = identifierRepository.queryByName("testIdentifierName2")
            assertThat(actualIdentifier).isEqualToComparingFieldByFieldRecursively(expectedIdentifier)
        }
    }

    @Test
    fun resolveIdentifierTest() {
        val suppliedIdentifier = Identifier(
            "did:ion:test:testId",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateKeyReference",
            "testIdentifierName"
        )
        mockkConstructor(ResolveIdentifierNetworkOperation::class)
        coEvery { anyConstructed<ResolveIdentifierNetworkOperation>().fire() } returns Result.Success(expectedIdentifierDocument)
        runBlocking {
            val actualIdentifierDocument = identifierRepository.resolveIdentifier("testUrl", suppliedIdentifier.id)
            assertThat(actualIdentifierDocument).isInstanceOf(Result.Success::class.java)
            assertThat((actualIdentifierDocument as Result.Success).payload.didDocument.id).isEqualTo(expectedIdentifier)
        }
        coVerify(exactly = 1) {
            anyConstructed<ResolveIdentifierNetworkOperation>().fire()
        }
    }

    @Test
    fun resolveInvalidIdentifierTest() {
        val suppliedIdentifier = Identifier(
            "did:ion:test:testId",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateKeyReference",
            "testIdentifierName"
        )
        mockkConstructor(ResolveIdentifierNetworkOperation::class)
        coEvery { anyConstructed<ResolveIdentifierNetworkOperation>().fire() } returns Result.Failure(
            NotFoundException(
                "Not found",
                true,
            )
        )
        runBlocking {
            val actualIdentifierDocument = identifierRepository.resolveIdentifier("testUrl", suppliedIdentifier.id)
            assertThat(actualIdentifierDocument).isInstanceOf(Result.Failure::class.java)
            assertThat((actualIdentifierDocument as Result.Failure).payload).isInstanceOf(NotFoundException::class.java)
        }
        coVerify(exactly = 1) {
            anyConstructed<ResolveIdentifierNetworkOperation>().fire()
        }
    }
}
