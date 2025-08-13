// --- File: app/src/main/java/com/webdevry/bloodlineascension/GameplayScreen.kt ---
package com.webdevry.bloodlineascension

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.webdevry.bloodlineascension.core.Enemy
import com.webdevry.bloodlineascension.core.Player
import com.webdevry.bloodlineascension.StatusBar
import com.webdevry.bloodlineascension.viewmodel.GameViewModel
import com.webdevry.bloodlineascension.viewmodel.PlayerAction
import com.webdevry.bloodlineascension.core.Skill
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameplayScreen(
    navController: NavController,
    gameViewModel: GameViewModel = viewModel(),
    onQuit: () -> Unit
) {
    val gameState by gameViewModel.gameState.collectAsState()
    val player = gameState.player
    val enemy = gameState.currentEnemy
    val combatLog = gameState.combatLog
    val isGameOver = gameState.isGameOver
    val isPlayerTurn = gameState.isPlayerTurn
    val currentFloor = gameState.currentDungeonFloor
    val canAdvance = gameState.canAdvanceFloor

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showSkillDialog by remember { mutableStateOf(false) }

    // Auto-scroll combat log
    LaunchedEffect(combatLog.size) {
        if (combatLog.isNotEmpty()) {
            coroutineScope.launch {
                try { listState.animateScrollToItem(combatLog.lastIndex) }
                catch (e: Exception) { Log.w("GameplayScreen", "Log scroll failed: ${e.message}") }
            }
        }
    }

    // Game Over Dialog
    if (isGameOver) {
        AlertDialog(
            onDismissRequest = { /* Non-dismissable */ },
            title = { Text("Game Over!") },
            text = { Text("${player?.name ?: "You"} have been defeated.") },
            confirmButton = {
                Button(onClick = onQuit) { Text("Return to Selection") }
            }
        )
    }

    Scaffold(
        topBar = {
            if (player != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Player bar (name + HP)
                    Box(modifier = Modifier.weight(1f)) { StatusBar(player = player) }

                    // Right: Floor info, right-aligned
                    val floorData = remember(currentFloor) {
                        gameViewModel.gameManager.dungeonLayout.find { it.floorNumber == currentFloor }
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(
                            text = "Floor $currentFloor",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = floorData?.floorName ?: "",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (player == null) {
            Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading Player Data…")
                LaunchedEffect(Unit) {
                    delay(3000)
                    if (gameState.player == null) onQuit()
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Enemy / floor area
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp)
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            enemy != null -> EnemyDisplay(enemy = enemy)
                            !isGameOver && !canAdvance -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Floor $currentFloor", style = MaterialTheme.typography.headlineSmall)
                                    Text("Ready for the next challenge?", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "Enemies Cleared: ${gameState.enemiesClearedOnFloor}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            !isGameOver && canAdvance -> {
                                Text(
                                    "Floor $currentFloor Cleared!",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text("Ready to descend?", style = MaterialTheme.typography.titleMedium)
                            }
                            else -> Text("Battle Ended.", style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }

                // Stamina (only during battle)
                if (enemy != null) {
                    LinearProgressIndicator(
                        progress = if (player.maxStamina > 0)
                            player.currentStamina.toFloat() / player.maxStamina
                        else 0f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Stamina: ${player.currentStamina} / ${player.maxStamina}",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Combat log in a card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        items(combatLog) { message ->
                            Text(message, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }

                // Actions
                Spacer(Modifier.height(14.dp))
                if (!isGameOver) {
                    when {
                        enemy != null && isPlayerTurn -> {
                            // Two evenly-sized pill buttons per row
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val pill = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(28.dp))

                                Button(
                                    onClick = { gameViewModel.processPlayerAction(PlayerAction.ATTACK_LIGHT) },
                                    enabled = player.currentStamina >= Player.LIGHT_ATTACK_STAMINA_COST,
                                    modifier = pill
                                ) { Text("Light") }

                                Button(
                                    onClick = { gameViewModel.processPlayerAction(PlayerAction.ATTACK_MEDIUM) },
                                    enabled = player.currentStamina >= Player.MEDIUM_ATTACK_STAMINA_COST,
                                    modifier = pill
                                ) { Text("Medium") }
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val pill = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(28.dp))

                                Button(
                                    onClick = { gameViewModel.processPlayerAction(PlayerAction.ATTACK_HEAVY) },
                                    enabled = player.currentStamina >= Player.HEAVY_ATTACK_STAMINA_COST,
                                    modifier = pill
                                ) { Text("Heavy") }

                                FilledTonalButton(
                                    onClick = { showSkillDialog = true },
                                    enabled = player.learnedSkills.isNotEmpty(),
                                    modifier = pill
                                ) { Text("Skills") }
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val pill = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(24.dp))

                                OutlinedButton(
                                    onClick = { gameViewModel.processPlayerAction(PlayerAction.FLEE) },
                                    enabled = player.currentStamina >= 5,
                                    modifier = pill
                                ) { Text("Flee") }
                            }
                        }

                        enemy != null && !isPlayerTurn -> {
                            Text(
                                "${enemy.name}'s Turn…",
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(vertical = 20.dp)
                            )
                        }

                        enemy == null && canAdvance -> {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val pill = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(28.dp))

                                Button(
                                    onClick = { gameViewModel.processPlayerAction(PlayerAction.ADVANCE_FLOOR) },
                                    modifier = pill
                                ) { Text("Descend") }

                                FilledTonalButton(
                                    onClick = { navController.navigate("stats") },
                                    modifier = pill
                                ) { Text("Stats") }

                                OutlinedButton(onClick = onQuit, modifier = pill) { Text("Quit") }
                            }
                        }

                        else -> {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val pill = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(28.dp))

                                Button(
                                    onClick = { gameViewModel.processPlayerAction(PlayerAction.NEXT_BATTLE) },
                                    modifier = pill
                                ) { Text("Next Battle") }

                                FilledTonalButton(
                                    onClick = { navController.navigate("stats") },
                                    modifier = pill
                                ) { Text("Stats") }

                                OutlinedButton(onClick = onQuit, modifier = pill) { Text("Quit") }
                            }
                        }
                    }
                } else {
                    Spacer(Modifier.height(50.dp))
                }
            }
        }
    }


    // Skills dialog
    if (showSkillDialog && player != null) {
        SkillSelectionDialog(
            learnedSkills = player.learnedSkills,
            onSkillSelected = { skill ->
                showSkillDialog = false
                Log.d("Gameplay", "Skill selected: ${skill.name}. Calling ViewModel.")
                gameViewModel.processPlayerSkill(skill)
            },
            onDismiss = { showSkillDialog = false }
        )
    }
}

@Composable
private fun CombatActionButtons(
    gameViewModel: GameViewModel,
    player: Player,
    onShowSkills: () -> Unit
) {
    val buttonModifier = Modifier.widthIn(min = 90.dp)

    val canLight = player.currentStamina >= Player.LIGHT_ATTACK_STAMINA_COST
    val canMedium = player.currentStamina >= Player.MEDIUM_ATTACK_STAMINA_COST
    val canHeavy = player.currentStamina >= Player.HEAVY_ATTACK_STAMINA_COST
    val canFlee = player.currentStamina >= 5

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Your Turn!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row( modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly ) {
            Button(onClick = { gameViewModel.processPlayerAction(PlayerAction.ATTACK_LIGHT) },  modifier = buttonModifier, enabled = canLight)  { Text("Light") }
            Button(onClick = { gameViewModel.processPlayerAction(PlayerAction.ATTACK_MEDIUM) }, modifier = buttonModifier, enabled = canMedium) { Text("Medium") }
            Button(onClick = { gameViewModel.processPlayerAction(PlayerAction.ATTACK_HEAVY) },  modifier = buttonModifier, enabled = canHeavy)  { Text("Heavy") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row( modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly ) {
            Button(onClick = onShowSkills, enabled = player.learnedSkills.isNotEmpty(), modifier = buttonModifier) { Text("Skills") }
            Button(onClick = { gameViewModel.processPlayerAction(PlayerAction.FLEE) }, modifier = buttonModifier, enabled = canFlee) { Text("Flee") }
            Spacer(modifier = buttonModifier)
        }
    }
}

@Composable
fun EnemyDisplay(enemy: Enemy) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(enemy.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Image(
            painter = painterResource(id = enemy.imageResId),
            contentDescription = enemy.name,
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = if (enemy.maxHealth > 0) enemy.currentHealth.toFloat() / enemy.maxHealth else 0f,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
        )
        Spacer(Modifier.height(4.dp))
        Text("${enemy.currentHealth} / ${enemy.maxHealth}", style = MaterialTheme.typography.labelLarge)
    }
}


@Composable
fun SkillSelectionDialog(
    learnedSkills: List<Skill>,
    onSkillSelected: (Skill) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose a Skill") },
        text = {
            if (learnedSkills.isEmpty()) {
                Text("No skills learned yet.")
            } else {
                LazyColumn {
                    items(learnedSkills) { skill ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSkillSelected(skill) }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(skill.name, fontWeight = FontWeight.Bold)
                            Text(skill.description, style = MaterialTheme.typography.bodySmall)
                            Text("Cost: ${skill.cost} Stamina", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
                        }
                        Divider()
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
