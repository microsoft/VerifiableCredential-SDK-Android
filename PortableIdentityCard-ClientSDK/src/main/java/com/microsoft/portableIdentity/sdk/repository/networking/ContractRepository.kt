package com.microsoft.portableIdentity.sdk.repository.networking

import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract

class ContractRepository(private val api: ContractApi): HttpBaseRepository() {

    suspend fun getContract(url: String): PicContract? {
        return safeApiCall(
            call = {api.getContract(url).await()},
            errorMessage = "Error Fetching Contract."
        )
    }
}