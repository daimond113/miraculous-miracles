package com.daimond113.miraculous_miracles.content

import com.daimond113.miraculous_miracles.MiraculousMiracles
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.block.BlockState
import net.minecraft.structure.StructureManager
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryOps
import net.minecraft.world.ChunkRegion
import net.minecraft.world.HeightLimitView
import net.minecraft.world.Heightmap
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.BiomeKeys
import net.minecraft.world.biome.source.BiomeAccess
import net.minecraft.world.biome.source.FixedBiomeSource
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.RandomState
import net.minecraft.world.gen.chunk.*
import net.minecraft.world.gen.structure.StructureSet
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.BiFunction


class BurrowChunkGenerator(registry: Registry<StructureSet>, val biomeRegistry: Registry<Biome>) :
    ChunkGenerator(
        registry,
        Optional.empty(),
        FixedBiomeSource(biomeRegistry.getOrCreateHolderOrThrow(BiomeKeys.PLAINS))
    ) {
    companion object {
        val CODEC: Codec<BurrowChunkGenerator> =
            RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<BurrowChunkGenerator> ->
                method_41042(instance).and(
                    RegistryOps.getRegistry(Registry.BIOME_KEY)
                        .forGetter { generator -> generator.biomeRegistry }).apply(
                    instance,
                    instance.stable(BiFunction { registry, biomeRegistry ->
                        BurrowChunkGenerator(
                            registry,
                            biomeRegistry
                        )
                    })
                )
            }

        val DIMENSION_WIDTH = 15
        val DIMENSION_HEIGHT = 15
    }

    override fun getCodec(): Codec<out ChunkGenerator?> {
        return CODEC
    }

    override fun buildSurface(
        region: ChunkRegion,
        structureManager: StructureManager,
        randomState: RandomState,
        chunk: Chunk
    ) {
        if (chunk.pos.x != 0 || chunk.pos.z != 0) return

        for (x in 0 until DIMENSION_WIDTH) {
            for (y in 0 until DIMENSION_HEIGHT) {
                for (z in 0 until DIMENSION_WIDTH) {
                    if (x == 0 || x == DIMENSION_WIDTH - 1 || y == 0 || y == DIMENSION_HEIGHT - 1 || z == 0 || z == DIMENSION_WIDTH - 1) {
                        val blockPos = BlockPos.ORIGIN.add(x, y, z)
                        chunk.setBlockState(blockPos, MiraculousMiracles.BURROW_DIMENSION_BLOCK.defaultState, false)
                    }
                }
            }
        }
    }

    override fun getSpawnHeight(world: HeightLimitView): Int {
        return 1
    }

    override fun populateNoise(
        executor: Executor,
        blender: Blender,
        randomState: RandomState,
        structureManager: StructureManager,
        chunk: Chunk
    ): CompletableFuture<Chunk> {
        return CompletableFuture.completedFuture(chunk)
    }

    override fun getHeight(
        x: Int,
        z: Int,
        heightmap: Heightmap.Type,
        world: HeightLimitView,
        randomState: RandomState
    ): Int {
        return 0
    }

    override fun getColumnSample(
        x: Int,
        z: Int,
        world: HeightLimitView,
        randomState: RandomState
    ): VerticalBlockSample {
        return VerticalBlockSample(
            0,
            arrayOf<BlockState>()
        )
    }

    // add debug screen info
    override fun method_40450(list: List<String>, randomState: RandomState, pos: BlockPos) {}

    override fun carve(
        chunkRegion: ChunkRegion,
        seed: Long,
        randomState: RandomState,
        biomeAccess: BiomeAccess,
        structureManager: StructureManager,
        chunk: Chunk,
        generationStep: GenerationStep.Carver
    ) {
    }

    override fun populateEntities(region: ChunkRegion) {}
    override fun getMinimumY(): Int {
        return 0
    }

    override fun getWorldHeight(): Int {
        return 16
    }

    override fun getSeaLevel(): Int {
        return 0
    }
}

