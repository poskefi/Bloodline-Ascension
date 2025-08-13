// --- File: app/src/main/java/com/webdevry/bloodlineascension/core/Enemy.kt ---
package com.webdevry.bloodlineascension.core

import androidx.annotation.DrawableRes

data class Enemy(
    val id: String,
    val name: String,
    var maxHealth: Int,
    var currentHealth: Int,
    // Add specific attack types for enemies
    val attackLight: Int,
    val attackMedium: Int,
    val attackHeavy: Int,
    val defense: Int = 0,
    @DrawableRes val imageResId: Int, // Ensure this drawable exists!
    val rewardWealth: Long = 0,
    val rewardXp: Int = 0,
    val levelRequirement: Int = 1,
    val skills: List<Skill> = emptyList() // Enemy skills
) {
    fun takeDamage(amount: Int) {
        val damageTaken = (amount - defense).coerceAtLeast(1)
        currentHealth = (currentHealth - damageTaken).coerceAtLeast(0)
    }

    fun isDefeated(): Boolean = currentHealth <= 0
}
