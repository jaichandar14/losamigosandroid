package com.bpmlinks.vbank.ui.thankyou

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.helper.BundleKeys
import com.bpmlinks.vbank.ui.HomeActivity
import kotlinx.android.synthetic.main.activity_thank_you.*

class ThankYouActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thank_you)

        btn_exit.setOnClickListener {
            val intent = Intent(this,HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(BundleKeys.MOVE_TO_USER_INPUT_SCREEN,true)
            startActivity(intent)
            finish()
        }
    }
}