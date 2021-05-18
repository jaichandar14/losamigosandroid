package com.vbank.vidyovideoview.webservices

import com.vbank.vidyovideoview.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiStories {
    @POST("token/generate-token")
    fun generateToken(@Body tokenParams: TokenParams?): Call<TokenResponse>

    @GET("docusign/getActionItems")
    fun getUserActionItems(@Query("callKeyNb") callKeyNb : Int?) : Call<UserActionItems>

    @GET("call/get-connecting-banker-info")
    fun getBankerName(@Query("callKeyNb") callKeyNb : Int?) : Call<BankerName>

    @POST("call/call-ended")
    fun callEnded(@Body callEndedRequest : CallEndedRequest) : Call<ResponseBody>

    @POST("docusign/submit-pin-change")
    fun submitPinChange(@Body pinChangeRequest: PinChangeRequest) : Call<ResponseBody>

    @POST("docusign/update-docu-sign-status")
    fun updateDocuSignStatus(@Body docuSignStatusRequest: DocuSignStatusRequest) : Call<ResponseBody>

    @POST("customer/consent")
    fun callRecordApi(@Body callRecord :CallRecordAPI) :Call<ResponseBody>

   @POST("call/geo-coordinates")
    fun geoLocation(@Body locationStatus :LocationLatLan) :Call<ResponseBody>

    @GET("docusign/generate-new-docu-sign-url")
    fun getDocuSignUrl(@Query("callKeyNb") callKeyNb : Int?) : Call<Output>

}
