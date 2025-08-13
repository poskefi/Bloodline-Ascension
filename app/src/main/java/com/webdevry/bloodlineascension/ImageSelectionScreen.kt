package com.webdevry.bloodlineascension

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.webdevry.bloodlineascension.R.*
import com.webdevry.bloodlineascension.viewmodel.GameViewModel
import kotlin.collections.emptyList

@Composable
fun ImageSelectionScreen(
    onSelectionDone: () -> Unit,
    gameViewModel: GameViewModel = viewModel()
) {
    val role = gameViewModel.playerSelection.value.role

    val imageList = when (role) {
        1 -> listOf(drawable.vamp_1, drawable.vamp_2, drawable.vamp_3, drawable.hunter_1)
        2 -> listOf(drawable.hunter_1, drawable.hunter_2, drawable.hunter_3)
        else -> emptyList()
    }

    if (imageList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No images available for the selected role. Check resource names and role selection.")
        }
        return
    }

    var selectedImage by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Select Your Character Image")
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            imageList.forEach { imageRes ->
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Character Image",
                    modifier = Modifier
                        .size(100.dp)
                        .clickable {
                            selectedImage = imageRes
                            gameViewModel.updateImage(imageRes)
                        }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val sel = gameViewModel.playerSelection.value
                if (selectedImage != -1 && sel.name.isNotBlank() && sel.role != 0) {
                    gameViewModel.updateImage(selectedImage) // in case they didn't tap again
                    gameViewModel.initializePlayer(sel.name, sel.role, selectedImage)
                    onSelectionDone() // now go to gameplay
                }
            }
        ) { Text("Confirm") }
    }
}
