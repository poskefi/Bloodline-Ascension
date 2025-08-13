package com.webdevry.bloodlineascension

import android.graphics.Color
import com.webdevry.bloodlineascension.MyNavHost
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.view.WindowCompat
import com.webdevry.bloodlineascension.core.GameManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tell Android we want to draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Optional: if using Material 3's edge-to-edge helper
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        val gameManager = GameManager()

        setContent {
            MaterialTheme {
                Surface {
                    MyNavHost()
                }
            }
        }
    }
}