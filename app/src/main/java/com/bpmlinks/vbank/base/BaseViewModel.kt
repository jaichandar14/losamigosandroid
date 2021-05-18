package com.bpmlinks.vbank.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import retrofit2.HttpException
import java.net.ConnectException

abstract class BaseViewModel(app: Application): AndroidViewModel(app) {

    val sessionExpired = MutableLiveData<Boolean>()
    val serverFailure = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    val commonError = false

    open fun handleError(error: Throwable){
        if (error is HttpException){
//            val response = Gson().fromJson(error.response()?.errorBody().toString(), BaseResponse::class.java)
            when(error.code()){
                401 -> sessionExpired.value = true
                403 -> sessionExpired.value = true
                500 -> serverFailure.value = true
            }
            errorMessage.value = error.message
        }else if (error is ConnectException){
            serverFailure.value = true
        }
    }

}