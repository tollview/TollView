package com.shinetech.tollview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shinetech.tollview.models.Toll
import com.shinetech.tollview.util.Utility

class ViewActivity : AppCompatActivity() {

    private lateinit var utility: Utility

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        utility = Utility(applicationContext)

        utility.getTollsForUser { tolls ->
            var emptyTollsList: ArrayList<Toll> = ArrayList()

        }
    }
}
