package com.vbank.vidyovideoview.fullscreenintent.workmanager


import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.vbank.vidyovideoview.helper.BundleKeys
import com.vbank.vidyovideoview.model.CallEndedRequest
import com.vbank.vidyovideoview.webservices.ApiCall
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response


class DeclineCallWorker(private val appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val  customerKeyNb=  inputData.getInt(BundleKeys.CustomerKeyNb,0)
        val  callKeyNb=  inputData.getInt(BundleKeys.CallKeyNb,0)
        val  callEndReason=  inputData.getString(BundleKeys.CallEndReason)
        declineOrDisconnectCall(customerKeyNb,callKeyNb,callEndReason)
        return Result.success()
    }

    private fun declineOrDisconnectCall(
        customerKeyNb: Int?,
        callKeyNb: Int?,
        callEndReason: String?
    ) {
        val callEndRequest = CallEndedRequest(callKeyNb = callKeyNb,customerKeyNb = customerKeyNb,reason = callEndReason)
        ApiCall.retrofitClient.callEnded(callEndRequest).enqueue(object :
            retrofit2.Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    if (response.body() != null) {

                    }
                }
            }
        })
    }
}