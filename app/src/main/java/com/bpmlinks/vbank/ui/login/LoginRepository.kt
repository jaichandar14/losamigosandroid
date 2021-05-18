package com.bpmlinks.vbank.ui.login

import com.bpmlinks.vbank.model.ApisResponse
import com.bpmlinks.vbank.model.ServiceType
import com.bpmlinks.vbank.webservices.ApiStories
import javax.inject.Inject

class LoginRepository @Inject constructor(private val apiStories: ApiStories)  {

    suspend fun getServiceType(): ApisResponse<ServiceType> {
        return try {
            val callApi = apiStories.getServiceType()
            ApisResponse.Success(callApi)
        }catch (e : Exception){
          ApisResponse.Error(e)

        }
    }
}