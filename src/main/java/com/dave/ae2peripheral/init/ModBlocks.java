package com.dave.ae2peripheral.init;

import com.dave.ae2peripheral.block.BlockCCBus;
import com.dave.ae2peripheral.block.BlockDefault;
import com.dave.ae2peripheral.reference.Names;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModBlocks {
	public static final BlockDefault	ccbus	= new BlockCCBus();

	public static void init() {
		GameRegistry.registerBlock(ccbus, Names.Blocks.CCBUS);
	}
}
