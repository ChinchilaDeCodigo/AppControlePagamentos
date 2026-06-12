package com.leona.controlepagamentos

import android.app.Application
import com.leona.controlepagamentos.di.AppContainer

class PaymentsApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
