package com.example.logisticsmanagement

import android.app.Application
import com.google.firebase.FirebaseApp

class LogisticsApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Firebase 초기화
        FirebaseApp.initializeApp(this)
    }
}