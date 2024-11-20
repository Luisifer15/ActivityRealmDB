package ph.edu.auf.activityrealmdb

import android.app.Application
import ph.edu.auf.activityrealmdb.database.RealmHelper

class ActivityRealmDBApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RealmHelper.initializeRealm()
    }

    override fun onTerminate() {
        super.onTerminate()
        RealmHelper.closeRealm()
    }
}