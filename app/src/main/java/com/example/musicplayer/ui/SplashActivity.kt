package com.example.musicplayer.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.lifecycleScope
import com.example.musicplayer.R
import com.example.musicplayer.ui.theme.MusicplayerTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    private var isNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicplayerTheme {
                StartTheme {
                    navigateNext()
                }
            }
        }

        lifecycleScope.launch {
            delay(10000L)
            navigateNext()
        }
    }

    private fun navigateNext() {
        if (!isNavigated) {
            isNavigated = true
            val target = if (FirebaseAuth.getInstance().currentUser != null) {
                MainActivity::class.java
            } else {
                LoginActivity::class.java
            }
            startActivity(Intent(this, target))
            finish()
        }
    }
}

@Composable
fun StartTheme(onClick: () -> Unit) {
    Scaffold { paddingValues ->
        Image(
            painter = painterResource(id = R.drawable.intro_pic),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clickable {
                    onClick()
                }
        )
    }
}