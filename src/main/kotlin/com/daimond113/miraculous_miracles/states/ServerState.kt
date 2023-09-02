package com.daimond113.miraculous_miracles.states

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.core.MiraculousAbility
import com.daimond113.miraculous_miracles.core.MiraculousType
import net.minecraft.entity.LivingEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.world.PersistentState
import net.minecraft.world.World
import java.util.*

class ServerState : PersistentState() {
    var players = HashMap<UUID, PlayerState>()

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        val playersNbtCompound = NbtCompound()

        players.forEach { (uuid: UUID, playerState: PlayerState) ->
            val playerStateNbt = NbtCompound()

            val activeMiraculousNbt = NbtCompound()

            for ((miraculousType, data) in playerState.activeMiraculous) {
                activeMiraculousNbt.put(miraculousType.id.toString(), data)
            }

            playerStateNbt.put("activeMiraculous", activeMiraculousNbt)

            val usedAbilitiesNbt = NbtCompound()

            for ((ability, data) in playerState.usedAbilities) {
                usedAbilitiesNbt.put(ability.id.toString(), data)
            }

            playerStateNbt.put("usedAbilities", usedAbilitiesNbt)

            playersNbtCompound.put(uuid.toString(), playerStateNbt)
        }

        nbt.put("players", playersNbtCompound)

        return nbt
    }

    companion object {
        private fun createFromNbt(tag: NbtCompound): ServerState {
            val serverState = ServerState()

            val playersTag = tag.getCompound("players")

            playersTag.keys.forEach { uuidString: String? ->
                val playerState = PlayerState()

                val playerCompound = playersTag.getCompound(uuidString)

                val activeMiraculousNbt = playerCompound.getCompound("usedAbilities")
                val activeMiraculous: MutableMap<MiraculousType, NbtCompound> = mutableMapOf()

                for (miraculousType in activeMiraculousNbt.keys) {
                    activeMiraculous[PlayerState.getMiraculousTypeById(miraculousType.toInt())] =
                        activeMiraculousNbt.getCompound(miraculousType)
                }

                playerState.activeMiraculous = activeMiraculous

                val usedAbilitiesCompound = playerCompound.getCompound("usedAbilities")
                val usedAbilities: MutableMap<MiraculousAbility, NbtCompound> = mutableMapOf()

                for (abilityKey in usedAbilitiesCompound.keys) {
                    usedAbilities[PlayerState.getAbilityById(abilityKey.toInt())] =
                        usedAbilitiesCompound.getCompound(abilityKey)
                }

                playerState.usedAbilities = usedAbilities

                val uuid = UUID.fromString(uuidString)
                serverState.players[uuid] = playerState
            }

            return serverState
        }

        fun getServerState(server: MinecraftServer): ServerState {
            val persistentStateManager =
                server.getWorld(World.OVERWORLD)!!.persistentStateManager
            return persistentStateManager.getOrCreate(
                ::createFromNbt,
                ::ServerState,
                MiraculousMiracles.MOD_ID
            )
        }

        fun getPlayerState(player: LivingEntity): PlayerState {
            val server = player.world.server ?: throw Error("Called getPlayerState client-side")

            val serverState = getServerState(server)

            return serverState.players.computeIfAbsent(player.uuid) { PlayerState() }
        }
    }
}
