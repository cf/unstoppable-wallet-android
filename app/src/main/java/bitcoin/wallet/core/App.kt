package bitcoin.wallet.core

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import io.realm.Realm

class App : Application() {

    companion object {
        lateinit var preferences: SharedPreferences

        val testMode = true
    }

    override fun onCreate() {
        super.onCreate()

        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        Realm.init(this)
//        RealmLog.setLevel(LogLevel.ALL)
    }

}