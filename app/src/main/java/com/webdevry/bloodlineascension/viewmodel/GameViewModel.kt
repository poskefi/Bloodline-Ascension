package com.webdevry.bloodlineascension.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.webdevry.bloodlineascension.core.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log
import com.webdevry.bloodlineascension.hunter.HunterPlayer
import com.webdevry.bloodlineascension.vampire.VampirePlayer
import kotlin.random.Random

// --- Data classes and Enums ---
data class PlayerSelection(
    val name: String = "",
    val role: Int = 0,
    val imageRes: Int = -1
)

enum class StatType {
    VITALITY,         // For Hunters (affects HP)
    BLOOD_POTENCY,    // For Vampires (affects HP)
    ATTACK_POWER,     // Affects all attack damages
    DEFENSE_RATING    // Affects defense value
}

enum class PlayerAction {
    ATTACK_LIGHT, ATTACK_MEDIUM, ATTACK_HEAVY,
    SKILL_LIST, FLEE,
    NEXT_BATTLE,
    ADVANCE_FLOOR
}

data class GameState(
    val player: Player? = null,
    val currentEnemy: Enemy? = null,
    val combatLog: List<String> = emptyList(),
    val isGameOver: Boolean = false,
    val isPlayerTurn: Boolean = false,

    // Dungeon State
    val currentDungeonFloor: Int = 1,
    val enemiesClearedOnFloor: Int = 0,
    val isBossEncounter: Boolean = false,
    val canAdvanceFloor: Boolean = false
)

// Enemy Action Definitions
enum class EnemyAttackType { LIGHT, MEDIUM, HEAVY }
enum class EnemyActionType { ATTACK, SKILL, DEFEND }
data class ChosenEnemyAction(
    val type: EnemyActionType,
    val skill: Skill? = null,
    val attackType: EnemyAttackType? = null
)

class GameViewModel : ViewModel() {
    // --- Player Selection State ---
    val playerSelection = MutableStateFlow(PlayerSelection())
    fun updateName(name: String) { playerSelection.value = playerSelection.value.copy(name = name); Log.d("GameViewModel", "Name updated: ${playerSelection.value}") }
    fun updateRole(role: Int) { playerSelection.value = playerSelection.value.copy(role = role); Log.d("GameViewModel", "Role updated: ${playerSelection.value}") }
    fun updateImage(imageRes: Int) { playerSelection.value = playerSelection.value.copy(imageRes = imageRes); Log.d("GameViewModel", "Image updated: ${playerSelection.value}") }
    fun reset() { playerSelection.value = PlayerSelection(); _gameState.value = GameState(); Log.d("GameViewModel", "ViewModel reset.") }

    // --- Game State Management ---
    val gameManager = GameManager()
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // --- Game Initialization ---
    fun initializePlayer(name: String, role: Int, imageRes: Int) {
        Log.d("GameViewModelInit", "Initializing player: Name=$name, Role=$role, Image=$imageRes")
        val newPlayer = gameManager.createPlayer(name, role, imageRes)
        Log.d("GameViewModelInit", "Player created: ${newPlayer.name}")
        _gameState.update {
            it.copy(
                player = newPlayer,
                isGameOver = false,
                combatLog = listOf("Game Started! You are on Floor 1."),
                isPlayerTurn = false,
                currentDungeonFloor = 1,
                enemiesClearedOnFloor = 0,
                isBossEncounter = false,
                canAdvanceFloor = false
            )
        }
        refreshUiState()
        Log.d("GameViewModelInit", "GameState player after update: ${_gameState.value.player?.name}")
    }

    // --- Combat / Dungeon Flow ---
    fun findNextBattleOnFloor() {
        val currentState = _gameState.value
        val player = currentState.player ?: return
        if (currentState.currentEnemy != null) {
            Log.w("ViewModelCombat", "Already in battle!")
            _gameState.update { it.copy(combatLog = it.combatLog + "Already in combat!") }
            return
        }
        if (currentState.canAdvanceFloor) {
            _gameState.update { it.copy(combatLog = it.combatLog + "Floor cleared! Advance to the next floor.") }
            return
        }

        val currentFloorData = gameManager.dungeonLayout.find { it.floorNumber == currentState.currentDungeonFloor }
        if (currentFloorData == null) {
            Log.e("ViewModelCombat", "Could not find data for floor ${currentState.currentDungeonFloor}")
            _gameState.update { it.copy(combatLog = it.combatLog + "Error: Dungeon floor data missing.") }
            return
        }

        val shouldFightBoss = currentFloorData.bossEnemyId != null &&
                currentState.enemiesClearedOnFloor >= currentFloorData.enemiesToClearForBoss

        val enemyToSpawn = gameManager.getEnemyForDungeonFloor(currentState.currentDungeonFloor, shouldFightBoss)

        if (enemyToSpawn == null) {
            val message = if (shouldFightBoss) "Error finding boss for floor ${currentState.currentDungeonFloor}." else "No enemies found on floor ${currentState.currentDungeonFloor}."
            _gameState.update { it.copy(combatLog = it.combatLog + message) }
            if (shouldFightBoss) {
                Log.e("ViewModelCombat", "Boss should spawn but wasn't found! Marking floor clear as fallback.")
                _gameState.update { it.copy(canAdvanceFloor = true, combatLog = it.combatLog + " Error finding boss, floor marked clear.")}
            }
            return
        }

        val encounterMessage = if(shouldFightBoss) "The Floor Boss appears: ${enemyToSpawn.name}!" else "You encounter a ${enemyToSpawn.name}!"
        Log.d("ViewModelCombat", "Starting battle with ${enemyToSpawn.name}. Boss: $shouldFightBoss")

        _gameState.update {
            it.copy(
                currentEnemy = enemyToSpawn,
                combatLog = it.combatLog + encounterMessage,
                isPlayerTurn = true,
                isBossEncounter = shouldFightBoss
            )
        }
    }
    fun advanceDungeonFloor() {
        val currentState = _gameState.value
        if (!currentState.canAdvanceFloor) {
            Log.w("ViewModelDungeon", "Attempted to advance floor when not allowed.")
            _gameState.update { it.copy(combatLog = it.combatLog + "You must clear the current floor first!")}
            return
        }
        if (currentState.currentEnemy != null) {
            Log.w("ViewModelDungeon", "Attempted to advance floor during combat.")
            _gameState.update { it.copy(combatLog = it.combatLog + "Cannot advance during combat!")}
            return
        }

        val nextFloorNumber = currentState.currentDungeonFloor + 1
        val nextFloorData = gameManager.dungeonLayout.find { it.floorNumber == nextFloorNumber }

        if (nextFloorData == null) {
            Log.i("ViewModelDungeon", "Player cleared the last available floor.")
            _gameState.update { it.copy(combatLog = it.combatLog + "You have cleared the deepest depths... for now!")}
            return
        }

        Log.i("ViewModelDungeon", "Advancing player to floor $nextFloorNumber.")
        _gameState.update {
            it.copy(
                currentDungeonFloor = nextFloorNumber,
                enemiesClearedOnFloor = 0,
                isBossEncounter = false,
                canAdvanceFloor = false,
                isPlayerTurn = false,
                combatLog = it.combatLog + "You descend deeper... Now on Floor $nextFloorNumber: ${nextFloorData.floorName}."
            )
        }
    }
    fun processPlayerAction(action: PlayerAction) {
        val currentState = _gameState.value
        val player = currentState.player ?: return

        // Handle actions outside active combat
        when (action) {
            PlayerAction.NEXT_BATTLE -> { findNextBattleOnFloor(); return }
            PlayerAction.ADVANCE_FLOOR -> { advanceDungeonFloor(); return }
            else -> { /* Continue */ }
        }

        val enemy = currentState.currentEnemy ?: return
        if (!currentState.isPlayerTurn || currentState.isGameOver) {
            Log.w("ViewModelAction", "Invalid state for action: $action (Turn: ${currentState.isPlayerTurn}, Over: ${currentState.isGameOver})")
            return
        }

        Log.d("ViewModelAction", "Processing Player Combat Action: $action")
        val newLog = mutableListOf<String>()
        var enemyDefeated = false
        var turnEnds = true
        val staminaCost = when (action) {
            PlayerAction.ATTACK_LIGHT -> Player.LIGHT_ATTACK_STAMINA_COST
            PlayerAction.ATTACK_MEDIUM -> Player.MEDIUM_ATTACK_STAMINA_COST
            PlayerAction.ATTACK_HEAVY -> Player.HEAVY_ATTACK_STAMINA_COST
            PlayerAction.SKILL_LIST -> { turnEnds = false; 0 }
            PlayerAction.FLEE -> 5
            else -> 0
        }

        if (!player.consumeStamina(staminaCost)) {
            newLog.add("Not enough stamina for ${action.name}!")
            _gameState.update { it.copy(combatLog = it.combatLog + newLog, player = player) }
            return
        }

        when (action) {
            PlayerAction.ATTACK_LIGHT -> {
                val playerDamage = player.attackLight
                enemy.takeDamage(playerDamage)
                newLog.add("${player.name} uses Light Attack (-${staminaCost} Sta) for $playerDamage damage. (${enemy.currentHealth}/${enemy.maxHealth})")
                if (enemy.isDefeated()) enemyDefeated = true
            }
            PlayerAction.ATTACK_MEDIUM -> {
                val playerDamage = player.attackMedium
                enemy.takeDamage(playerDamage)
                newLog.add("${player.name} uses Medium Attack (-${staminaCost} Sta) for $playerDamage damage. (${enemy.currentHealth}/${enemy.maxHealth})")
                if (enemy.isDefeated()) enemyDefeated = true
            }
            PlayerAction.ATTACK_HEAVY -> {
                val playerDamage = player.attackHeavy
                enemy.takeDamage(playerDamage)
                newLog.add("${player.name} uses Heavy Attack (-${staminaCost} Sta) for $playerDamage damage. (${enemy.currentHealth}/${enemy.maxHealth})")
                if (enemy.isDefeated()) enemyDefeated = true
            }
            PlayerAction.SKILL_LIST -> newLog.add("Choose a skill...")
            PlayerAction.FLEE -> {
                if (currentState.isBossEncounter) {
                    newLog.add("You cannot flee from the Floor Boss!")
                    turnEnds = true
                } else if (Random.nextInt(10) >= 7) {
                    newLog.add("You successfully fled!")
                    _gameState.update { it.copy(currentEnemy = null, combatLog = it.combatLog + newLog, isPlayerTurn = false, player = player) }
                    return
                } else {
                    newLog.add("You failed to flee!")
                }
            }
            else -> {}
        }

        _gameState.update { it.copy(combatLog = it.combatLog + newLog, player = player) }

        if (enemyDefeated) {
            handleEnemyDefeat(player, enemy, mutableListOf())
            return
        }

        if (turnEnds && !currentState.isGameOver) {
            Log.d("ViewModelAction", "Action finished, ending player turn.")
            player.regenerateStamina()
            _gameState.update { it.copy(isPlayerTurn = false, player = player) }
            viewModelScope.launch {
                delay(1000L)
                processEnemyTurn()
            }
        } else {
            _gameState.update { it.copy(isPlayerTurn = true, player = player) }
        }
    }
    fun processPlayerSkill(skill: Skill) {
        val currentState = _gameState.value
        val player = currentState.player ?: return
        val enemy = currentState.currentEnemy ?: return
        if (!currentState.isPlayerTurn || currentState.isGameOver) return

        val staminaCost = skill.cost
        if (!player.consumeStamina(staminaCost)) {
            _gameState.update { it.copy(combatLog = it.combatLog + "Not enough stamina for ${skill.name}!", player = player) }
            return
        }

        Log.d("ViewModelAction", "Processing skill: ${skill.name} (-${staminaCost} Sta)")
        val newLog = mutableListOf<String>()
        var enemyDefeated = false

        when (skill.effectType) {
            SkillEffectType.DAMAGE -> {
                val damage = skill.power
                enemy.takeDamage(damage)
                newLog.add("${player.name} uses ${skill.name} on ${enemy.name} for $damage damage! (${enemy.currentHealth}/${enemy.maxHealth})")
                if (enemy.isDefeated()) enemyDefeated = true
            }
            SkillEffectType.HEAL -> {
                if (skill.target == SkillTarget.SELF) {
                    val healAmount = skill.power
                    player.currentHealth = (player.currentHealth + healAmount).coerceAtMost(player.health)
                    newLog.add("${player.name} uses ${skill.name}, healing for $healAmount. (${player.currentHealth}/${player.health})")
                } else newLog.add("${player.name} failed ${skill.name} targeting (NI).")
            }
            SkillEffectType.BUFF_ATTACK, SkillEffectType.BUFF_DEFENSE,
            SkillEffectType.DEBUFF_ATTACK, SkillEffectType.DEBUFF_DEFENSE -> {
                newLog.add("${skill.name} effect not fully implemented yet.")
            }
        }

        if (skill.id == "vamp_bite" && !enemyDefeated) {
            val healAmount = skill.power / 3
            player.currentHealth = (player.currentHealth + healAmount).coerceAtMost(player.health)
            newLog.add("${player.name} recovers $healAmount health from the bite.")
        }

        _gameState.update { it.copy(combatLog = it.combatLog + newLog, player = player) }

        if (enemyDefeated) {
            handleEnemyDefeat(player, enemy, mutableListOf())
        } else {
            Log.d("ViewModelAction", "Skill used, ending player turn.")
            player.regenerateStamina()
            _gameState.update { it.copy(isPlayerTurn = false, player = player) }
            viewModelScope.launch {
                delay(1000L)
                processEnemyTurn()
            }
        }
    }
    private fun processEnemyTurn() {
        val currentState = _gameState.value
        val player = currentState.player ?: return
        val enemy = currentState.currentEnemy ?: return
        if (currentState.isPlayerTurn || currentState.isGameOver) return

        Log.d("ViewModelCombat", "Enemy ${enemy.name}'s turn. Deciding action...")
        val chosenAction = decideEnemyAction(enemy, player)
        Log.d("ViewModelCombat", "Enemy decided action: ${chosenAction.type}" + if (chosenAction.skill != null) " (${chosenAction.skill.name})" else if (chosenAction.attackType != null) " (${chosenAction.attackType})" else "")

        val newLog = mutableListOf<String>()
        var playerDefeated = false

        when (chosenAction.type) {
            EnemyActionType.ATTACK -> {
                val enemyDamage = when (chosenAction.attackType) {
                    EnemyAttackType.LIGHT -> enemy.attackLight
                    EnemyAttackType.MEDIUM -> enemy.attackMedium
                    EnemyAttackType.HEAVY -> enemy.attackHeavy
                    null -> enemy.attackMedium
                }
                player.takeDamage(enemyDamage)
                newLog.add("${enemy.name} uses ${chosenAction.attackType ?: "Medium"} Attack on ${player.name} for $enemyDamage damage. (${player.currentHealth}/${player.health})")
            }
            EnemyActionType.SKILL -> {
                val skill = chosenAction.skill
                if (skill != null) {
                    when (skill.effectType) {
                        SkillEffectType.DAMAGE -> {
                            val damage = skill.power
                            player.takeDamage(damage)
                            newLog.add("${enemy.name} uses ${skill.name} on ${player.name} for $damage damage! (${player.currentHealth}/${player.health})")
                        }
                        SkillEffectType.HEAL -> {
                            if (skill.target == SkillTarget.SELF) {
                                val healAmount = skill.power
                                enemy.currentHealth = (enemy.currentHealth + healAmount).coerceAtMost(enemy.maxHealth)
                                newLog.add("${enemy.name} uses ${skill.name}, healing for $healAmount. (${enemy.currentHealth}/${enemy.maxHealth})")
                            } else newLog.add("${enemy.name} failed ${skill.name} targeting (NI).")
                        }
                        SkillEffectType.BUFF_DEFENSE, SkillEffectType.BUFF_ATTACK,
                        SkillEffectType.DEBUFF_DEFENSE, SkillEffectType.DEBUFF_ATTACK -> {
                            newLog.add("${enemy.name} uses ${skill.name} (Effect NI).")
                        }
                    }
                } else {
                    newLog.add("${enemy.name} tried skill but failed. Attacks instead.")
                    val enemyDamage = enemy.attackMedium
                    player.takeDamage(enemyDamage)
                    newLog.add("${enemy.name} attacks ${player.name} for $enemyDamage damage. (${player.currentHealth}/${player.health})")
                }
            }
            EnemyActionType.DEFEND -> newLog.add("${enemy.name} takes a defensive stance (Effect NI).")
        }

        if (player.currentHealth <= 0) playerDefeated = true

        _gameState.update {
            it.copy(
                combatLog = it.combatLog + newLog,
                currentEnemy = enemy
            )
        }

        if (playerDefeated) {
            handlePlayerDefeat(mutableListOf())
        } else {
            _gameState.update { it.copy(player = player, isPlayerTurn = true) }
            Log.d("ViewModelCombat", "Enemy turn ended. Player turn now.")
        }
    }
    private fun decideEnemyAction(enemy: Enemy, player: Player): ChosenEnemyAction {
        val possible = mutableListOf<Pair<ChosenEnemyAction, Int>>()
        val hpPct = enemy.currentHealth.toFloat() / enemy.maxHealth.toFloat()

        val heals = enemy.skills.filter { it.effectType == SkillEffectType.HEAL && it.target == SkillTarget.SELF }
        if (heals.isNotEmpty() && hpPct < 0.4f) possible.add(ChosenEnemyAction(EnemyActionType.SKILL, heals.random()) to 50)

        val damageSkills = enemy.skills.filter { it.effectType == SkillEffectType.DAMAGE && it.target == SkillTarget.ENEMY }
        if (damageSkills.isNotEmpty()) possible.add(ChosenEnemyAction(EnemyActionType.SKILL, damageSkills.random()) to 40)

        possible.add(ChosenEnemyAction(EnemyActionType.ATTACK, attackType = EnemyAttackType.LIGHT) to 20)
        possible.add(ChosenEnemyAction(EnemyActionType.ATTACK, attackType = EnemyAttackType.MEDIUM) to 30)
        possible.add(ChosenEnemyAction(EnemyActionType.ATTACK, attackType = EnemyAttackType.HEAVY) to 10)

        val totalWeight = possible.sumOf { it.second }
        val roll = Random.nextInt(totalWeight)
        var cumulative = 0
        for ((action, weight) in possible) {
            cumulative += weight
            if (roll < cumulative) return action
        }

        return ChosenEnemyAction(EnemyActionType.ATTACK, attackType = EnemyAttackType.MEDIUM)
    }
    private fun handleEnemyDefeat(player: Player, enemy: Enemy, newLog: MutableList<String>) {
        val xpGained = Random.nextInt(20, 40)
        player.gainXP(xpGained)
        newLog.add("${enemy.name} is defeated! You gain $xpGained XP.")
        _gameState.update {
            it.copy(
                currentEnemy = null,
                enemiesClearedOnFloor = it.enemiesClearedOnFloor + 1,
                canAdvanceFloor = (it.enemiesClearedOnFloor + 1) >= 3 || it.isBossEncounter,
                combatLog = it.combatLog + newLog,
                player = player
            )
        }
    }
    private fun handlePlayerDefeat(newLog: MutableList<String>) {
        newLog.add("You have been defeated! Game Over.")
        _gameState.update { it.copy(isGameOver = true, combatLog = it.combatLog + newLog) }
    }

    // --- UI State for StatsScreen ---
    data class UiState(
        val avatarResId: Int = -1,
        val playerName: String = "",
        val level: Int = 1,
        val className: String = "",
        val xp: Int = 0,
        val xpToNext: Int = 100,
        val health: Int = 0,
        val maxHealth: Int = 0,
        val attackLight: Int = 0,
        val attackLightMax: Int = 0,
        val attackMedium: Int = 0,
        val attackMediumMax: Int = 0,
        val attackHeavy: Int = 0,
        val attackHeavyMax: Int = 0,
        val defense: Int = 0,
        val vitality: Int = 0,
        val bloodPotency: Int = 0
    )
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun refreshUiState() {
        val player = _gameState.value.player
        if (player != null) {
            _uiState.update {
                it.copy(
                    avatarResId = player.imageResId,
                    playerName = player.name,
                    level = player.level,
                    className = player::class.simpleName ?: "",
                    xp = player.xp,
                    xpToNext = player.xpToNextLevel,
                    health = player.currentHealth,
                    maxHealth = player.health,
                    attackLight = player.attackLight,
                    attackLightMax = player.attackLight,
                    attackMedium = player.attackMedium,
                    attackMediumMax = player.attackMedium,
                    attackHeavy = player.attackHeavy,
                    attackHeavyMax = player.attackHeavy,
                    defense = player.defense,
                    vitality = player.vitality,
                    bloodPotency = player.bloodPotency
                )
            }
        }
    }

    fun allocatePoint(stat: String) {
        val player = _gameState.value.player ?: return
        when (stat.lowercase()) {
            "defense" -> player.defense += 1
            "vitality" -> player.vitality += 1
            "blood" -> player.bloodPotency += 1
            "attacklight" -> player.attackLight += 1
            "attackmedium" -> player.attackMedium += 1
            "attackheavy" -> player.attackHeavy += 1
        }
        refreshUiState()
    }
   //private val _uiState = MutableStateFlow(UiState())
    //val uiState: StateFlow<UiState> = _uiState.asStateFlow()
}
