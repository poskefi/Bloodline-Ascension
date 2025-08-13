package com.webdevry.bloodlineascension

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.webdevry.bloodlineascension.viewmodel.GameViewModel

@Composable
fun SelectionScreen(
    onSelectionDone: () -> Unit,
    gameViewModel: GameViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableIntStateOf(0) }

    // Background Image
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.dark_gothic_bg), // your gothic background drawable
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark overlay for better text contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Enter Your Name:",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    gameViewModel.updateName(it)
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedIndicatorColor = Color(0xFFD4AF37), // outline when focused
                    unfocusedIndicatorColor = Color.Gray       // outline when not focused
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Select Role:",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GothicButton("Vampire") {
                    role = 1
                    gameViewModel.updateRole(1)
                }
                GothicButton("Hunter") {
                    role = 2
                    gameViewModel.updateRole(2)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            GothicButton("Next") {
                if (name.isNotBlank() && role != 0) {
                    onSelectionDone()
                }
            }
        }
    }
}

@Composable
fun GothicButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black.copy(alpha = 0.6f),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .height(50.dp)
            .width(120.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp))
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}
