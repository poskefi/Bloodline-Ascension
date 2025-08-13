package com.webdevry.bloodlineascension.vampire

import com.webdevry.bloodlineascension.R
import com.webdevry.bloodlineascension.core.Player
import com.webdevry.bloodlineascension.core.Skill
import com.webdevry.bloodlineascension.core.SkillEffectType

class VampirePlayer(
    name: String,
    imageResId: Int
) : Player(
    name = name,
    initialHealth = 100,
    initialAttackLight = 8,
    initialAttackMedium = 12,
    initialAttackHeavy = 16,
    imageResId = imageResId,
    initialDefense = 5,
    initialVitality = 7,
    initialBloodPotency = 10
) {
    override val role: String = "Vampire"

    override val availableSkills: List<Skill> = listOf(
        Skill(
            id = "vampire_bite",
            name = "Vampire Bite",
            description = "Drains health from the enemy.",
            levelRequirement = 1,
            isUnique = false,
            effectType = SkillEffectType.DAMAGE,
            power = 15
        )
    )

    override fun calculateMaxHealth(base: Int, level: Int, vit: Int, potency: Int): Int {
        return base + (vit * VITALITY_HP_BONUS) + (potency * BLOOD_POTENCY_HP_BONUS) + (level * 5)
    }

    override fun getHealthBonusFromStats(forLevel: Int): Int {
        return (vitality * VITALITY_HP_BONUS) + (bloodPotency * BLOOD_POTENCY_HP_BONUS)
    }

    override fun displayRole(): String = role
}
