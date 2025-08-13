// --- File: app/src/main/java/com/webdevry/bloodlineascension/core/Player.kt ---
package com.webdevry.bloodlineascension.core

import android.util.Log
import kotlin.math.roundToInt

// Base Player class
abstract class Player(
    val name: String,
    initialHealth: Int, // Starting base health
    initialAttackLight: Int,
    initialAttackMedium: Int,
    initialAttackHeavy: Int,
    val imageResId: Int,
    initialDefense: Int = 5,
    initialVitality: Int = 5, // Base vitality
    initialBloodPotency: Int = 5 // Base potency (only one will be used per subclass)
) {
    companion object {
        // Stat Point Allocation Constants
        const val VITALITY_HP_BONUS = 8 // HP gained per Vitality point
        const val BLOOD_POTENCY_HP_BONUS = 10 // HP gained per Blood Potency point
        const val ATTACK_POINT_BOOST = 1 // Boosts all attack types per point
        const val DEFENSE_PER_POINT = 2
        const val POINTS_PER_LEVEL = 3

        // Stamina Constants
        const val BASE_MAX_STAMINA = 100
        const val LIGHT_ATTACK_STAMINA_COST = 10
        const val MEDIUM_ATTACK_STAMINA_COST = 15
        const val HEAVY_ATTACK_STAMINA_COST = 25
        const val STAMINA_REGEN_PER_TURN = 12 // How much stamina recovers
    }

    abstract val role: String

    // Base Stats (affected by points)
    var vitality: Int = initialVitality
        protected set
    var bloodPotency: Int = initialBloodPotency // Only relevant for Vampires usually
        protected set
    var attackPower: Int = 0 // Represents points put into "Attack"
        protected set
    var defenseRating: Int = 0 // Represents points put into "Defense"
        protected set

    // Calculated Stats
    var health: Int = calculateMaxHealth(initialHealth, 1, vitality, bloodPotency) // Initial calculation
        protected set
    var currentHealth: Int = health // Start at full health

    var attackLight: Int = calculateAttackValue(initialAttackLight, 0)
        protected set
    var attackMedium: Int = calculateAttackValue(initialAttackMedium, 0)
        protected set
    var attackHeavy: Int = calculateAttackValue(initialAttackHeavy, 0)
        protected set

    var defense: Int = calculateDefenseValue(initialDefense, 0)
        protected set

    // Stamina
    var maxStamina: Int = BASE_MAX_STAMINA
        protected set
    var currentStamina: Int = maxStamina
        protected set

    // Progression Stats
    var level: Int = 1
        protected set
    var experience: Int = 0
        protected set
    var experienceToNextLevel: Int = 10
        protected set
    var wealth: Long = 100L
        protected set
    var statPoints: Int = 0
        protected set

    // Skill Properties
    abstract val availableSkills: List<Skill>
    val learnedSkills: MutableList<Skill> = mutableListOf()

    // --- Calculation Helpers ---
    protected abstract fun calculateMaxHealth(base: Int, level: Int, vit: Int, potency: Int): Int
    private fun calculateAttackValue(base: Int, points: Int): Int = base + (points * ATTACK_POINT_BOOST)
    private fun calculateDefenseValue(base: Int, points: Int): Int = base + (points * DEFENSE_PER_POINT)

    // --- Combat Methods ---
    open fun takeDamage(amount: Int) {
        val damageTaken = (amount - defense).coerceAtLeast(1)
        currentHealth = (currentHealth - damageTaken).coerceAtLeast(0)
        Log.d("PlayerDamage", "$name took $damageTaken damage (Reduced by $defense defense). HP: $currentHealth/$health")
    }

    open fun restoreFullHealth() {
        Log.d("PlayerHealth", "$name restoring health. Before: $currentHealth/$health")
        currentHealth = health
        Log.d("PlayerHealth", "$name health restored. After: $currentHealth/$health")
    }

    open fun consumeStamina(amount: Int): Boolean {
        if (currentStamina >= amount) {
            currentStamina -= amount
            Log.d("PlayerStamina", "$name consumed $amount stamina. Remaining: $currentStamina/$maxStamina")
            return true
        }
        Log.w("PlayerStamina", "$name tried to use $amount stamina, but only has $currentStamina.")
        return false
    }

    open fun regenerateStamina() {
        val recovered = STAMINA_REGEN_PER_TURN
        currentStamina = (currentStamina + recovered).coerceAtMost(maxStamina)
        Log.d("PlayerStamina", "$name recovered $recovered stamina. Current: $currentStamina/$maxStamina")
    }

    // --- Progression Methods ---
    open fun gainWealth(amount: Long) { if (amount > 0) wealth += amount }

    open fun gainXP(amount: Int) {
        if (amount > 0) {
            experience += amount
            Log.d("PlayerXP", "$name gained $amount XP. Total: $experience / $experienceToNextLevel")
            checkLevelUp()
        }
    }

    private fun checkLevelUp() {
        var leveledUpThisCheck = false
        while (experience >= experienceToNextLevel) {
            val oldLevel = level
            level += 1
            experience -= experienceToNextLevel
            experienceToNextLevel = (experienceToNextLevel * 1.5).toInt().coerceAtLeast(10)
            this.statPoints += POINTS_PER_LEVEL
            leveledUpThisCheck = true

            health = calculateMaxHealth(health - getHealthBonusFromStats(oldLevel), level, vitality, bloodPotency)

            Log.d("PlayerLevelUp", "$name Leveled Up to Level $level! Gained $POINTS_PER_LEVEL SP. HP: $health. Next: $experience / $experienceToNextLevel")
            checkForSkillUnlocks(level)
        }
        if (leveledUpThisCheck) {
            restoreFullHealth()
            currentStamina = maxStamina
            Log.d("PlayerLevelUp", "$name fully restored on level up.")
        }
    }

    protected abstract fun getHealthBonusFromStats(forLevel: Int): Int

    private fun checkForSkillUnlocks(currentLevel: Int) {
        Log.d("SkillCheck", "Checking skills for level $currentLevel")
        if (currentLevel % 50 == 0) {
            val potentialUniques = availableSkills.filter { it.isUnique && it.levelRequirement == currentLevel }
            if (potentialUniques.isNotEmpty()) {
                val skillToLearn = potentialUniques.first()
                if (learnSkill(skillToLearn)) {
                    Log.i("SkillUnlock", "$name unlocked UNIQUE skill: ${skillToLearn.name}")
                    return
                }
            }
        }
        if (currentLevel % 5 == 0) {
            val potentialNormals = availableSkills.filter { !it.isUnique && it.levelRequirement == currentLevel }
            if (potentialNormals.isNotEmpty()) {
                val skillToLearn = potentialNormals.first()
                if (learnSkill(skillToLearn)) {
                    Log.i("SkillUnlock", "$name unlocked NORMAL skill: ${skillToLearn.name}")
                }
            }
        }
    }

    private fun learnSkill(skill: Skill): Boolean {
        if (!learnedSkills.any { it.id == skill.id }) {
            learnedSkills.add(skill)
            Log.d("SkillLearn", "Skill ${skill.name} added to learned list.")
            return true
        }
        Log.d("SkillLearn", "Skill ${skill.name} already known.")
        return false
    }

    // --- Stat Allocation Methods ---
    open fun allocatePointToVitality(): Boolean {
        if (statPoints > 0) {
            statPoints--
            vitality++
            val oldHealth = health
            health = calculateMaxHealth(health - getHealthBonusFromStats(level), level, vitality, bloodPotency)
            currentHealth += (health - oldHealth).coerceAtLeast(0)
            Log.d("StatAllocate", "$name allocated point to Vitality. New VIT: $vitality, Max HP: $health, Points Left: $statPoints")
            return true
        }
        return false
    }
    open fun allocatePointToBloodPotency(): Boolean {
        if (statPoints > 0) {
            statPoints--
            bloodPotency++
            val oldHealth = health
            health = calculateMaxHealth(health - getHealthBonusFromStats(level), level, vitality, bloodPotency)
            currentHealth += (health - oldHealth).coerceAtLeast(0)
            Log.d("StatAllocate", "$name allocated point to Blood Potency. New BP: $bloodPotency, Max HP: $health, Points Left: $statPoints")
            return true
        }
        return false
    }

    open fun allocatePointToAttackPower(): Boolean {
        if (statPoints > 0) {
            statPoints--
            val oldAttackPower = attackPower
            attackPower++
            attackLight = calculateAttackValue(attackLight - oldAttackPower * ATTACK_POINT_BOOST, attackPower)
            attackMedium = calculateAttackValue(attackMedium - oldAttackPower * ATTACK_POINT_BOOST, attackPower)
            attackHeavy = calculateAttackValue(attackHeavy - oldAttackPower * ATTACK_POINT_BOOST, attackPower)
            Log.d("StatAllocate", "$name allocated point to Attack Power. New ATK Power: $attackPower (L/M/H: $attackLight/$attackMedium/$attackHeavy), Points Left: $statPoints")
            return true
        }
        return false
    }

    open fun allocatePointToDefenseRating(): Boolean {
        if (statPoints > 0) {
            statPoints--
            val oldDefenseRating = defenseRating
            defenseRating++
            defense = calculateDefenseValue(defense - oldDefenseRating * DEFENSE_PER_POINT, defenseRating)
            Log.d("StatAllocate", "$name allocated point to Defense Rating. New DEF Rating: $defenseRating (DEF: $defense), Points Left: $statPoints")
            return true
        }
        return false
    }

    // --- Display ---
    open fun displayStats(): String {
        val pointsStr = if (statPoints > 0) " ($statPoints Avail)" else ""
        return "Lvl:$level$pointsStr HP:$currentHealth/$health ATK:$attackMedium DEF:$defense W:$wealth XP:$experience/$experienceToNextLevel"
    }

    abstract fun displayRole(): String
}
