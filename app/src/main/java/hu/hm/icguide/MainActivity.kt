package hu.hm.icguide

import android.os.Bundle
import co.zsmb.rainbowcake.navigation.SimpleNavActivity
import com.example.icguide.R
import hu.hm.icguide.ui.login.LoginFragment
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.ui.maps.MapFragment

@AndroidEntryPoint
class MainActivity : SimpleNavActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            navigator.add(MapFragment())
        }
    }

}
