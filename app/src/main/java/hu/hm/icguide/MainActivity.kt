package hu.hm.icguide

import android.os.Bundle
import co.zsmb.rainbowcake.navigation.SimpleNavActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.ui.list.ListFragment
import hu.hm.icguide.ui.login.LoginFragment

@AndroidEntryPoint
class MainActivity : SimpleNavActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {

            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) navigator.add(ListFragment())
            else navigator.add(LoginFragment())
        }
    }

}
