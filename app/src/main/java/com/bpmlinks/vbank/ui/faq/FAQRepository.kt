package com.bpmlinks.vbank.ui.faq

import com.bpmlinks.vbank.model.*
import com.bpmlinks.vbank.webservices.ApiStories
import javax.inject.Inject

class FAQRepository  @Inject constructor(private val apiStories: ApiStories)  {

    suspend fun getFAQList(): ApisResponse<FAQListResponse> {
        return try {
            val callApi = apiStories.getFAQList()
            ApisResponse.Success(callApi)
        }catch (e : Exception){
            ApisResponse.Error(e)
        }
    }
}