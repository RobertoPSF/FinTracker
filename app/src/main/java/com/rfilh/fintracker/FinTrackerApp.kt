package com.rfilh.fintracker

import android.app.Application
import com.rfilh.fintracker.notification.BudgetAlertNotifier
import com.rfilh.fintracker.notification.DebugNotifier

class FinTrackerApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        BudgetAlertNotifier.ensureChannel(this)
        DebugNotifier.ensureChannel(this)
    }
}
