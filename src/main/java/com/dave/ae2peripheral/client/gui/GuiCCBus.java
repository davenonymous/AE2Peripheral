package com.dave.ae2peripheral.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.dave.ae2peripheral.client.container.ContainerCCBus;
import com.dave.ae2peripheral.reference.Textures;
import com.dave.ae2peripheral.tileentity.TileEntityCCBus;

public class GuiCCBus extends GuiContainer {
	private TileEntityCCBus	teCCBus;

	public GuiCCBus(InventoryPlayer inventoryPlayer, TileEntityCCBus teCCBus) {
		super(new ContainerCCBus(inventoryPlayer, teCCBus));
		this.teCCBus = teCCBus;
		xSize = 176;
		ySize = 187;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) {
		String containerName = StatCollector.translateToLocal(teCCBus.getInventoryName());
		fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2, 6, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		this.mc.getTextureManager().bindTexture(Textures.Gui.CCBUS);

		int xStart = (width - xSize) / 2;
		int yStart = (height - ySize) / 2;
		this.drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);
	}

}
