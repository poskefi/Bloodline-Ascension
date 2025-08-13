// --- File: app/src/main/java/com/webdevry/bloodlineascension/core/GameManager.kt ---
package com.webdevry.bloodlineascension.core

import com.webdevry.bloodlineascension.R // Import base R class
import com.webdevry.bloodlineascension.hunter.HunterPlayer
import com.webdevry.bloodlineascension.vampire.VampirePlayer
import android.util.Log

// Define the structure for a dungeon floor
data class DungeonFloor(
    val floorNumber: Int,
    val floorName: String,
    val enemyPoolIds: List<String>, // IDs of regular enemies found here
    val bossEnemyId: String? = null, // Optional ID of the boss for this floor
    val enemiesToClearForBoss: Int = 5,
    val minPlayerLevel: Int = 1
)

class GameManager {

    // createPlayer uses the specific class constructors now
    fun createPlayer(name: String, choice: Int, imageResId: Int): Player {
        val validImageResId = if (imageResId != -1) imageResId else getDefaultImageForRole(choice)
        return when (choice) {
            1 -> VampirePlayer(name, validImageResId)
            2 -> HunterPlayer(name, validImageResId)
            else -> HunterPlayer(name, validImageResId)
        }
    }

    // getDefaultImageForRole remains the same
    private fun getDefaultImageForRole(choice: Int): Int {
        return when (choice) {
            1 -> R.drawable.vamp_1 // Ensure these exist
            2 -> R.drawable.hunter_1 // Ensure these exist
            else -> R.drawable.ic_launcher_background
        }
    }

    // --- Define Enemy Skills ---
    // Basic Damage / Utility
    private val heavyStrike = Skill("enemy_heavy_strike", "Heavy Strike", "A powerful blow.", 1, false, SkillEffectType.DAMAGE, power = 15)
    private val quickHeal = Skill("enemy_quick_heal", "Quick Heal", "Restores a small amount of health.", 1, false, SkillEffectType.HEAL, power = 20, target = SkillTarget.SELF)
    private val defendStance = Skill("enemy_defend", "Defend", "Increases defense for a turn (NI).", 1, false, SkillEffectType.BUFF_DEFENSE, power = 50, target = SkillTarget.SELF)
    private val boneArmor = Skill("skel_bone_armor", "Bone Armor", "+Def for a few turns.", 3, false, SkillEffectType.BUFF_DEFENSE, power = 10, target=SkillTarget.SELF)
    private val multiAttack = Skill("enemy_multi_attack", "Multi-Attack", "Hits twice for low damage (NI).", 5, false, SkillEffectType.DAMAGE, power = 8) // Need logic for multiple hits
    private val poisonSting = Skill("enemy_poison", "Poison Sting", "Deals damage over time (NI).", 4, false, SkillEffectType.DAMAGE, power = 5) // Need DOT logic
    private val weaken = Skill("enemy_weaken", "Weaken", "Lowers player attack (NI).", 6, false, SkillEffectType.DEBUFF_ATTACK, power = 5)
    private val frostBolt = Skill("enemy_frostbolt", "Frost Bolt", "Deals cold damage (NI).", 7, false, SkillEffectType.DAMAGE, power = 20)

    // Boss Skills
    private val goblinBossRage = Skill("goblin_boss_rage", "Rage", "+Atk briefly", 1, false, SkillEffectType.BUFF_ATTACK, power = 5, target = SkillTarget.SELF)
    private val lichDrainLife = Skill("lich_drain_life", "Drain Life", "Damages player, heals self (NI).", 3, false, SkillEffectType.DAMAGE, power = 25) // Multi-effect
    private val trollRegen = Skill("troll_regen", "Regeneration", "Heals significantly over time (NI).", 8, false, SkillEffectType.HEAL, power = 15, target = SkillTarget.SELF) // HOT logic
    private val golemSlam = Skill("golem_slam", "Golem Slam", "Heavy AOE damage (NI).", 15, false, SkillEffectType.DAMAGE, power = 40, target = SkillTarget.ALL_ALLIES) // Target needs implementation


    // --- Enemy Templates (Define by ID - Expanded List) ---
    // Use a Map for easy lookup by ID
    private val enemyTemplateMap: Map<String, Enemy> = mapOf(
        // Floor 1-3
        "goblin_scout" to Enemy("goblin_scout", "Goblin Scout", 30, 30, attackLight = 4, attackMedium = 5, attackHeavy = 6, defense = 0, R.drawable.hunter_2, 10, 15, 1),
        "giant_rat" to Enemy("giant_rat", "Giant Rat", 25, 25, attackLight = 3, attackMedium = 4, attackHeavy = 5, defense = 1, R.drawable.hunter_1, 8, 12, 1),
        "goblin_boss" to Enemy("goblin_boss", "Goblorg the Annoying", 80, 80, attackLight = 6, attackMedium = 8, attackHeavy = 10, defense = 2, R.drawable.hunter_3, 50, 75, 1, skills = listOf(goblinBossRage, heavyStrike)),
        "skeleton_warrior" to Enemy("skeleton_warrior", "Skeleton Warrior", 70, 70, attackLight = 9, attackMedium = 10, attackHeavy = 11, defense = 5, R.drawable.vamp_2, 40, 50, 3, skills = listOf(boneArmor)),
        "dire_wolf" to Enemy("dire_wolf", "Dire Wolf", 50, 50, attackLight = 7, attackMedium = 8, attackHeavy = 10, defense = 2, R.drawable.hunter_3, 25, 30, 2, skills = listOf(heavyStrike)),
        "crypt_crawler" to Enemy("crypt_crawler", "Crypt Crawler", 60, 60, attackLight = 8, attackMedium = 9, attackHeavy = 9, defense = 4, R.drawable.vamp_1, 35, 45, 3, skills = listOf(poisonSting)),
        "lich_apprentice" to Enemy("lich_apprentice", "Lich Apprentice", 150, 150, attackLight = 10, attackMedium = 12, attackHeavy = 14, defense = 6, R.drawable.vamp_3, 100, 150, 3, skills = listOf(lichDrainLife, heavyStrike, frostBolt)),

        // Floor 4-7
        "orc_grunt" to Enemy("orc_grunt", "Orc Grunt", 100, 100, attackLight = 12, attackMedium = 15, attackHeavy = 18, defense = 3, R.drawable.vamp_3, 75, 80, 5),
        "goblin_shaman" to Enemy("goblin_shaman", "Goblin Shaman", 80, 80, attackLight = 7, attackMedium = 9, attackHeavy = 10, defense = 2, R.drawable.hunter_2, 60, 70, 4, skills = listOf(quickHeal, weaken)),
        "orc_berserker" to Enemy("orc_berserker", "Orc Berserker", 120, 120, attackLight = 14, attackMedium = 17, attackHeavy = 22, defense = 1, R.drawable.vamp_1, 90, 100, 6, skills=listOf(heavyStrike)),
        "orc_chieftain" to Enemy("orc_chieftain", "Orc Chieftain", 250, 250, attackLight = 15, attackMedium = 18, attackHeavy = 24, defense = 5, R.drawable.vamp_2, 200, 250, 6, skills=listOf(heavyStrike, defendStance)),

        // Floor 8-12
        "troll_scrapper" to Enemy("troll_scrapper", "Troll Scrapper", 200, 200, attackLight = 16, attackMedium = 19, attackHeavy = 23, defense = 6, R.drawable.hunter_1, 150, 180, 8, skills = listOf(trollRegen)),
        "dark_elf_mage" to Enemy("dark_elf_mage", "Dark Elf Mage", 130, 130, attackLight = 11, attackMedium = 14, attackHeavy = 17, defense = 4, R.drawable.vamp_1, 140, 170, 9, skills = listOf(frostBolt, weaken)),
        "imp_trickster" to Enemy("imp_trickster", "Imp Trickster", 90, 90, attackLight = 13, attackMedium = 16, attackHeavy = 16, defense = 3, R.drawable.hunter_2, 120, 150, 7, skills = listOf(multiAttack)),
        "troll_shaman" to Enemy("troll_shaman", "Troll Shaman", 220, 220, attackLight = 14, attackMedium = 17, attackHeavy = 20, defense = 7, R.drawable.hunter_3, 180, 220, 10, skills = listOf(trollRegen, quickHeal)),
        "spider_queen" to Enemy("spider_queen", "Spider Queen", 350, 350, attackLight = 18, attackMedium = 21, attackHeavy = 25, defense = 8, R.drawable.vamp_2, 300, 400, 10, skills = listOf(poisonSting, multiAttack)),

        // Floor 13-17
        "stone_golem" to Enemy("stone_golem", "Stone Golem", 300, 300, attackLight = 15, attackMedium = 18, attackHeavy = 28, defense = 15, R.drawable.hunter_1, 250, 300, 13, skills = listOf(defendStance)),
        "fire_elemental" to Enemy("fire_elemental", "Fire Elemental", 200, 200, attackLight = 20, attackMedium = 24, attackHeavy = 26, defense = 10, R.drawable.vamp_1, 280, 350, 14, skills = listOf(heavyStrike)), // Add fire skill later
        "dark_knight" to Enemy("dark_knight", "Dark Knight", 280, 280, attackLight = 22, attackMedium = 26, attackHeavy = 30, defense = 12, R.drawable.vamp_3, 320, 400, 15, skills = listOf(heavyStrike, defendStance)),
        "elemental_lord" to Enemy("elemental_lord", "Elemental Lord", 500, 500, attackLight = 25, attackMedium = 30, attackHeavy = 35, defense = 14, R.drawable.hunter_3, 500, 600, 15, skills = listOf(frostBolt, heavyStrike)), // Add more elemental skills later

        // Floor 18-20
        "shadow_assassin" to Enemy("shadow_assassin", "Shadow Assassin", 250, 250, attackLight = 30, attackMedium = 35, attackHeavy = 32, defense = 8, R.drawable.vamp_1, 400, 500, 18, skills = listOf(multiAttack, poisonSting)),
        "iron_golem" to Enemy("iron_golem", "Iron Golem", 450, 450, attackLight = 20, attackMedium = 25, attackHeavy = 38, defense = 20, R.drawable.hunter_1, 450, 550, 19, skills = listOf(golemSlam, defendStance)),
        "abyssal_demon" to Enemy("abyssal_demon", "Abyssal Demon", 400, 400, attackLight = 28, attackMedium = 33, attackHeavy = 38, defense = 16, R.drawable.vamp_2, 500, 650, 20),
        "demon_lord" to Enemy("demon_lord", "Azgaroth the Tormentor", 800, 800, attackLight = 35, attackMedium = 40, attackHeavy = 48, defense = 18, R.drawable.vamp_3, 1000, 1500, 20, skills = listOf(heavyStrike, weaken, frostBolt)) // Final Boss Example
    )

    // --- Define Dungeon Layout (Expanded to 20 Floors) ---
    val dungeonLayout: List<DungeonFloor> = listOf(
        // 1-5
        DungeonFloor(1, "Forgotten Cellar", listOf("goblin_scout", "giant_rat"), "goblin_boss", 3, 1),
        DungeonFloor(2, "Decrepit Crypt", listOf("skeleton_warrior", "giant_rat", "crypt_crawler"), null, 4, 2), // No boss floor example
        DungeonFloor(3, "Moldy Catacombs", listOf("skeleton_warrior", "crypt_crawler"), "lich_apprentice", 4, 3),
        DungeonFloor(4, "Goblin Warrens", listOf("goblin_scout", "goblin_shaman"), null, 5, 4),
        DungeonFloor(5, "Orc Outpost", listOf("orc_grunt", "goblin_shaman"), "orc_berserker", 5, 5), // Use Berserker as mini-boss
        // 6-10
        DungeonFloor(6, "Flooded Passage", listOf("crypt_crawler", "orc_grunt"), "orc_chieftain", 4, 6),
        DungeonFloor(7, "Spider Nest", listOf("crypt_crawler", "imp_trickster"), null, 6, 7),
        DungeonFloor(8, "Troll Cave", listOf("troll_scrapper", "orc_grunt"), null, 5, 8),
        DungeonFloor(9, "Dark Elf Enclave", listOf("dark_elf_mage", "imp_trickster"), null, 6, 9),
        DungeonFloor(10, "Arachnid Lair", listOf("crypt_crawler", "dark_elf_mage"), "spider_queen", 5, 10),
        // 11-15
        DungeonFloor(11, "Ancient Ruins", listOf("skeleton_warrior", "troll_scrapper"), null, 6, 11),
        DungeonFloor(12, "Troll Stronghold", listOf("troll_scrapper", "orc_berserker"), "troll_shaman", 5, 12), // Troll Shaman as boss
        DungeonFloor(13, "Golem Workshop", listOf("stone_golem", "imp_trickster"), null, 5, 13),
        DungeonFloor(14, "Elemental Plane (Fire)", listOf("fire_elemental", "imp_trickster"), null, 6, 14),
        DungeonFloor(15, "Haunted Armoury", listOf("dark_knight", "skeleton_warrior"), "elemental_lord", 5, 15),
        // 16-20
        DungeonFloor(16, "Shadowy Cloister", listOf("dark_elf_mage", "shadow_assassin"), null, 6, 16),
        DungeonFloor(17, "Forgotten Forge", listOf("stone_golem", "fire_elemental"), "iron_golem", 5, 17), // Iron Golem boss
        DungeonFloor(18, "Knight's Burial Ground", listOf("dark_knight", "skeleton_warrior", "lich_apprentice"), null, 7, 18), // Mix enemies
        DungeonFloor(19, "Abyssal Breach", listOf("abyssal_demon", "shadow_assassin"), null, 6, 19),
        DungeonFloor(20, "Throne of Torment", listOf("abyssal_demon", "dark_knight"), "demon_lord", 5, 20) // Final Boss
    )

    // --- REVISED Enemy Fetching Logic (Uses Dungeon Floor) ---
    fun getEnemyForDungeonFloor(floorNumber: Int, isBossFight: Boolean): Enemy? {
        val floor = dungeonLayout.find { it.floorNumber == floorNumber }
        if (floor == null) {
            Log.e("GameManager", "Cannot find dungeon floor data: $floorNumber")
            return null
        }

        val enemyIdToFetch: String? = if (isBossFight) {
            floor.bossEnemyId
        } else {
            floor.enemyPoolIds.randomOrNull() // Get random normal enemy ID
        }

        if (enemyIdToFetch == null) {
            Log.e("GameManager", "No ${if(isBossFight) "boss" else "regular"} enemy ID found for floor $floorNumber")
            return null
        }

        val template = enemyTemplateMap[enemyIdToFetch]
        if (template == null) {
            Log.e("GameManager", "Enemy template not found for ID: $enemyIdToFetch")
            return null
        }

        Log.d("GameManager", "Spawning enemy '${template.name}' (ID: $enemyIdToFetch) for floor $floorNumber. Boss: $isBossFight")
        // Return a fresh copy with full health
        return template.copy(currentHealth = template.maxHealth)
    }
}
