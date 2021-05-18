package com.bpmlinks.vbank.ui.weview

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.ui.thankyou.ThankYouActivity
import com.vbank.vidyovideoview.connector.MeetingParams
import com.vbank.vidyovideoview.connector.UserDataParams
import com.vbank.vidyovideoview.helper.AppConstant
import com.vbank.vidyovideoview.helper.BundleKeys
import com.vbank.vidyovideoview.model.DocuSignStatusRequest
import com.vbank.vidyovideoview.webservices.ApiCall
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response


class WebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var userDataParams: UserDataParams? = null
    private var meetingParams: MeetingParams? = null
    private lateinit var progressBar:ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_web_view)
        webView = findViewById(R.id.webView)
        progressBar =findViewById(R.id.web_progress_bar)
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.domStorageEnabled = true
        removeNotification()

        try {
            userDataParams = if (intent.hasExtra(BundleKeys.UserDataParams)) {
                intent.getParcelableExtra(BundleKeys.UserDataParams) as UserDataParams
            } else {
                UserDataParams()
            }

            meetingParams = if (intent.hasExtra(BundleKeys.MeetingParams)) {
                intent.getParcelableExtra(BundleKeys.MeetingParams) as MeetingParams
            } else {
                MeetingParams()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {

                Log.d("URL",url)

                if(url.toString().contains( AppConstant.DOCUSIGN_BASE_URL))
                {
                    updateDocumentSignStatus()
                    var thanksActivty =Intent(applicationContext,ThankYouActivity::class.java)
                    startActivity(thanksActivty)
                    finish()
                }
                else
                {
                    view?.loadUrl(url)
                }

                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.GONE
            }
        }
        webView.loadUrl(meetingParams?.docusignurl)

        Log.d("URL",meetingParams?.docusignurl)
    }

    override fun onBackPressed() {

        if(webView.canGoBack())
        {
            webView.goBack()
        }
        else
        {
            super.onBackPressed()
        }
    }


    private fun removeNotification() {
        if (intent.hasExtra(AppConstant.NOTIFICATION_ID)) {
            val notificationId = intent.getIntExtra(AppConstant.NOTIFICATION_ID, 0)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notificationId)
        }
    }




    private fun updateDocumentSignStatus() {
        val docuSignStatusRequest = DocuSignStatusRequest()
        docuSignStatusRequest.callKeyNb = meetingParams?.callKeyNb
        docuSignStatusRequest.action="COMPLETED"
        ApiCall.retrofitClient.updateDocuSignStatus(docuSignStatusRequest).enqueue(object :
            retrofit2.Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@WebViewActivity, t.localizedMessage, Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
            }
        })
    }
}
