package hu.hm.icguide

import android.os.Bundle
import co.zsmb.rainbowcake.navigation.SimpleNavActivity
import com.example.icguide.R
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.ui.list.ListFragment

@AndroidEntryPoint
class MainActivity : SimpleNavActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            navigator.add(ListFragment())
        }
    }

}
