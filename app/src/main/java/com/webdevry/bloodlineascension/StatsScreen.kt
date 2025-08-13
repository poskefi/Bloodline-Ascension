package com.webdevry.bloodlineascension

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.webdevry.bloodlineascension.ui.theme.BloodlineTheme
import com.webdevry.bloodlineascension.ui.theme.components.StatBar
import com.webdevry.bloodlineascension.viewmodel.GameViewModel

@Composable
fun StatsScreen(
    onBack: () -> Unit,
    gameViewModel: GameViewModel = viewModel()
) {
    val state by gameViewModel.uiState.collectAsState()

    BloodlineTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Character Stats",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { inner ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(inner)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Header card with avatar + name + level
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = state.avatarResId),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                state.playerName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Level ${state.level}", style = MaterialTheme.typography.bodyLarge)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Bloodline", style = MaterialTheme.typography.labelLarge)
                            Text(
                                state.className,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Experience
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Experience",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        StatBar(
                            label = "XP",
                            value = state.xp,
                            max = state.xpToNext
                        )
                        Text(
                            "${state.xp} / ${state.xpToNext}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Core stats
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Vitals",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        StatBar(label = "Health", value = state.health, max = state.maxHealth)
                        StatBar(
                            label = "Attack (Light)",
                            value = state.attackLight,
                            max = state.attackLightMax
                        )
                        StatBar(
                            label = "Attack (Medium)",
                            value = state.attackMedium,
                            max = state.attackMediumMax
                        )
                        StatBar(
                            label = "Attack (Heavy)",
                            value = state.attackHeavy,
                            max = state.attackHeavyMax
                        )

                        Spacer(Modifier.height(8.dp))
                        Divider()
                        Spacer(Modifier.height(8.dp))

                        StatBar(label = "Defense", value = state.defense, max = 100)
                        StatBar(label = "Vitality", value = state.vitality, max = 100)
                        StatBar(label = "Blood Potency", value = state.bloodPotency, max = 100)
                    }
                }

                // Allocation controls (wire these to your VM)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ElevatedButton(
                        onClick = { gameViewModel.allocatePoint("defense") },
                        modifier = Modifier.weight(1f)
                    ) { Text("Boost Defense") }

                    ElevatedButton(
                        onClick = { gameViewModel.allocatePoint("vitality") },
                        modifier = Modifier.weight(1f)
                    ) { Text("Boost Vitality") }

                    ElevatedButton(
                        onClick = { gameViewModel.allocatePoint("blood") },
                        modifier = Modifier.weight(1f)
                    ) { Text("Boost Blood") }
                }
