package com.daimond113.miraculous_miracles.states

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.daimond113.miraculous_miracles.core.MiraculousAbility
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

            playerStateNbt.putIntArray(
                "activeMiraculous",
                playerState.activeMiraculous.map { miraculousType -> miraculousType.id })

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

                playerState.activeMiraculous =
                    playerCompound.getIntArray("activeMiraculous")
                        .map { miraculousId -> PlayerState.getMiraculousTypeById(miraculousId) }
                        .toMutableSet()

                val usedAbilitiesCompound = playerCompound.getCompound("usedAbilities")
                val usedAbilities: MutableSet<Pair<MiraculousAbility, NbtCompound>> = mutableSetOf()

                for (abilityKey in usedAbilitiesCompound.keys) {
                    usedAbilities.add(
                        Pair(
                            PlayerState.getAbilityById(abilityKey.toInt()),
                            usedAbilitiesCompound.getCompound(abilityKey)
                        )
                    )
                }

                playerState.usedAbilities = usedAbilities

                val uuid = UUID.fromString(uuidString)
                serverState.players[uuid] = playerState
            }

            serverState.markDirty()

            return serverState
        }

        private fun getServerState(server: MinecraftServer): ServerState {
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
