// --- File: app/src/main/java/com/webdevry/bloodlineascension/StatsScreen.kt ---
package com.webdevry.bloodlineascension

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.webdevry.bloodlineascension.viewmodel.GameViewModel

@Composable
fun StatsScreen(
    gameViewModel: GameViewModel,
    onBack: () -> Unit
) {
    val player = gameViewModel.gameState.collectAsState().value.player
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Player Stats",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))

        if (player != null) {
            StatRow("Name", player.name)
            StatRow("Health", "${player.currentHealth} / ${player.health}")
            StatRow("Attack Light", "${player.attackLight}")
            StatRow("Attack Medium", "${player.attackMedium}")
            StatRow("Attack Heavy", "${player.attackHeavy}")
            StatRow("Defense", "${player.defense}")
            StatRow("Vitality", "${player.vitality}")
            StatRow("Blood Potency", "${player.bloodPotency}")
        } else {
            Text("No player data found.", color = Color.Gray)
        }

        Spacer(Modifier.height(32.dp))
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
