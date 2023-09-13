package com.daimond113.miraculous_miracles.content

import net.minecraft.block.Block
import net.minecraft.block.Material
import org.quiltmc.qkl.library.blocks.blockSettingsOf

class ShellterBlock :
    Block(blockSettingsOf(material = Material.GLASS, hardness = -1.0f, resistance = 3600000.0f, isOpaque = false))
