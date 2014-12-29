package com.dave.ae2peripheral.proxy;

import com.dave.ae2peripheral.reference.Names;
import com.dave.ae2peripheral.tileentity.TileEntityCCBus;

import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy implements IProxy {

	@Override
	public void registerTileEntities() {
		GameRegistry.registerTileEntity(TileEntityCCBus.class, Names.Blocks.CCBUS);
	}

	@Override
	public void registerRenderers() {}

}
