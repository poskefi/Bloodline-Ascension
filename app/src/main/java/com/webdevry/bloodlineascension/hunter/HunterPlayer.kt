package com.webdevry.bloodlineascension.hunter

import com.webdevry.bloodlineascension.R
import com.webdevry.bloodlineascension.core.Player
import com.webdevry.bloodlineascension.core.Skill
import com.webdevry.bloodlineascension.core.SkillEffectType

class HunterPlayer(
    name: String,
    imageResId: Int
) : Player(
    name = name,
    initialHealth = 120,
    initialAttackLight = 10,
    initialAttackMedium = 14,
    initialAttackHeavy = 20,
    imageResId = imageResId,
    initialDefense = 3,
    initialVitality = 10,
    initialBloodPotency = 0
) {
    override val role: String = "Hunter"

    override val availableSkills: List<Skill> = listOf(
        Skill(
            id = "aimed_shot",
            name = "Aimed Shot",
            description = "Deals a powerful ranged attack.",
            levelRequirement = 1,
            isUnique = false,
            effectType = SkillEffectType.DAMAGE,
            power = 18
        )
    )

    override fun calculateMaxHealth(base: Int, level: Int, vit: Int, potency: Int): Int {
        return base + (vit * VITALITY_HP_BONUS) + (level * 8)
    }

    override fun getHealthBonusFromStats(forLevel: Int): Int {
        return vitality * VITALITY_HP_BONUS
    }

    override fun displayRole(): String = role
}
