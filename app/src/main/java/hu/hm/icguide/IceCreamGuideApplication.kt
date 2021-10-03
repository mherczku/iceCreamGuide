package hu.hm.icguide

import android.app.Application
import co.zsmb.rainbowcake.config.Loggers
import co.zsmb.rainbowcake.config.rainbowCake
import co.zsmb.rainbowcake.timber.TIMBER
import com.example.icguide.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class IceCreamGuideApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        rainbowCake {
            logger = Loggers.TIMBER
            isDebug = BuildConfig.DEBUG
        }
        Timber.plant(Timber.DebugTree())
    }

}
