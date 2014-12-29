package com.dave.ae2peripheral.proxy;

import com.dave.ae2peripheral.block.render.CCBusRenderer;
import com.dave.ae2peripheral.reference.RenderIds;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {
	@Override
	public void registerRenderers() {
		RenderIds.CCBUS = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(RenderIds.CCBUS, new CCBusRenderer());
	}
}
