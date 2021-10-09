package hu.hm.icguide

import android.os.Bundle
import android.widget.Toast
import co.zsmb.rainbowcake.navigation.SimpleNavActivity
import com.example.icguide.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import hu.hm.icguide.ui.list.ListFragment
import hu.hm.icguide.ui.login.LoginFragment

@AndroidEntryPoint
class MainActivity : SimpleNavActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {

            if(FirebaseAuth.getInstance().currentUser != null)
                navigator.add(ListFragment())
            else navigator.add(LoginFragment())
        }
    }

}
