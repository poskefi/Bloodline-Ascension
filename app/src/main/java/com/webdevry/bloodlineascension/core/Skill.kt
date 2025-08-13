// app/src/main/java/com/webdevry/bloodlineascension/core/Skill.kt
package com.webdevry.bloodlineascension.core

// Enum for different ways skills can affect targets
enum class SkillEffectType {
    DAMAGE, HEAL, BUFF_ATTACK, BUFF_DEFENSE, DEBUFF_ATTACK, DEBUFF_DEFENSE
}

// Enum for targeting
enum class SkillTarget {
    ENEMY, SELF, ALL_ENEMIES, ALL_ALLIES
}

// Added 'cost' (stamina) and optional 'cooldownTurns'
data class Skill(
    val id: String,
    val name: String,
    val description: String,
    val levelRequirement: Int,
    val isUnique: Boolean = false,
    val effectType: SkillEffectType,
    val power: Int,
    val target: SkillTarget = SkillTarget.ENEMY,
    val cost: Int = 0,
    val cooldownTurns: Int = 0
)
