package com.bpmlinks.vbank.ui.purpose

import com.bpmlinks.vbank.model.ApisResponse
import com.bpmlinks.vbank.model.ServiceType
import com.bpmlinks.vbank.model.SubServiceType
import com.bpmlinks.vbank.model.UserInput
import com.bpmlinks.vbank.webservices.ApiStories
import com.google.gson.Gson
import com.google.gson.JsonIOException
import kotlinx.android.synthetic.main.login_bottom.*
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.lang.StringBuilder
import javax.inject.Inject

class PurposeRepository @Inject constructor(private val apiStories: ApiStories) {

    suspend fun getSubServiceType(masterServiceKeyNb: Int): ApisResponse<SubServiceType> {
        return try {
            val callApi = apiStories.getSubServiceType(masterServiceKeyNb)
            ApisResponse.Success(callApi)
        } catch (e: Exception) {
            ApisResponse.Error(e)
        }
    }

    suspend fun newCustomer(userInput: UserInput): ApisResponse<ServiceType> {

        return try {
            val callApi = apiStories.newCustomer(userInput)
            ApisResponse.Success(callApi)
        } catch (e: HttpException) {
            ApisResponse.Error(e)
            val errorMessage = errorMessagefromapi(e)
            ApisResponse.CustomError(errorMessage!!)

        }
    }

    private fun errorMessagefromapi(httpException: HttpException): String? {
        var errorMessage: String? = null
        val error = httpException.response()?.errorBody()
       
        try {

            val adapter = Gson().getAdapter(ServiceType::class.java)
            val errorParser = adapter.fromJson(error?.string())
            errorMessage = errorParser.message
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            return errorMessage
        }
    }


}








