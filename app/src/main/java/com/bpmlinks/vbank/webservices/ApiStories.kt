package com.bpmlinks.vbank.webservices

import com.bpmlinks.vbank.model.*
import okhttp3.ResponseBody
import retrofit2.http.*

interface ApiStories {

    @GET("banker/master-service-type")
    suspend fun getServiceType() : ServiceType

    @GET("banker/sub-service-type/{key}")
    suspend fun getSubServiceType(@Path("key") key: Int?) : SubServiceType

    @GET("banker/branches")
    suspend fun getBranchesByZipCode(@Query("zipcode") key: String?) : Branches

    @POST("customer/mobile-login")
    suspend fun newCustomer(@Body userInput: UserInput) : ServiceType

    @GET("banker/banker-timeslot")
    suspend fun getTimeSlot(@Query("bankerKeyNb") bankerKeyNb: Int?,
                            @Query("branchKeyNb") branchKeyNb: Int?) : TimeSlots

    @POST("banker/callback")
    suspend fun scheduleCall(@Body callbackRequest: CallbackRequest?) : ResponseBody

    @GET("banker/get-faq")
    suspend fun getFAQList() : FAQListResponse

}