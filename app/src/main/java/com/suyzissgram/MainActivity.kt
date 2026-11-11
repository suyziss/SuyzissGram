package com.suyzissgram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.suyzissgram.di.AppContainer
import com.suyzissgram.ui.navigation.SuyzissNavHost

class MainActivity : ComponentActivity() {
    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppContainer(applicationContext)

        setContent {
            SuyzissNavHost(container = appContainer)
        }
    }
}
