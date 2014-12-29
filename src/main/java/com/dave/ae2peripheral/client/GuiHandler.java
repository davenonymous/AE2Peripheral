package com.dave.ae2peripheral.client;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.dave.ae2peripheral.client.container.ContainerCCBus;
import com.dave.ae2peripheral.client.gui.GuiCCBus;
import com.dave.ae2peripheral.reference.GuiId;
import com.dave.ae2peripheral.tileentity.TileEntityCCBus;

import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == GuiId.CCBUS.ordinal()) {
			TileEntityCCBus teCCBus = (TileEntityCCBus) world.getTileEntity(x, y, z);
			return new ContainerCCBus(player.inventory, teCCBus);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == GuiId.CCBUS.ordinal()) {
			TileEntityCCBus teCCBus = (TileEntityCCBus) world.getTileEntity(x, y, z);
			return new GuiCCBus(player.inventory, teCCBus);
		}

		return null;
	}

}
