package com.bpmlinks.vbank.ui.searchbranches

import com.bpmlinks.vbank.model.ApisResponse
import com.bpmlinks.vbank.model.Branches
import com.bpmlinks.vbank.webservices.ApiStories
import javax.inject.Inject

class SearchBranchesRepository @Inject constructor(private val apiStories: ApiStories) {

    suspend fun getBranchesByZipCode(zipCode: String?): ApisResponse<Branches> {
        return try {
            val callApi = apiStories.getBranchesByZipCode(zipCode)
            ApisResponse.Success(callApi)
        } catch (e: Exception) {
            ApisResponse.Error(e)
        }
    }
}