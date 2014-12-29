package com.dave.ae2peripheral.block.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import com.dave.ae2peripheral.reference.RenderIds;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class CCBusRenderer implements ISimpleBlockRenderingHandler {

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		Tessellator tessellator = Tessellator.instance;
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 1, metadata));
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 3, metadata));
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 5, metadata));
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 0, metadata));
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 2, metadata));
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 4, metadata));
		tessellator.draw();

		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		int meta = world.getBlockMetadata(x, y, z);

		IIcon down = block.getIcon(0, meta);
		IIcon up = block.getIcon(1, meta);
		IIcon north = block.getIcon(2, meta);
		IIcon south = block.getIcon(3, meta);
		IIcon west = block.getIcon(4, meta);
		IIcon east = block.getIcon(5, meta);

		renderer.renderFaceXNeg(block, x, y, z, west);
		renderer.renderFaceXPos(block, x, y, z, east);
		renderer.renderFaceYNeg(block, x, y, z, down);
		renderer.renderFaceYPos(block, x, y, z, up);
		renderer.renderFaceZNeg(block, x, y, z, north);
		renderer.renderFaceZPos(block, x, y, z, south);

		switch (meta) {
			case 0: // Bottom
				//LogHelper.info(meta + ": Rotating arrows to bottom");
				renderer.uvRotateNorth = 0; //west
				renderer.uvRotateSouth = 0; //east
				renderer.uvRotateWest = 0; //south
				renderer.uvRotateEast = 0; //north
				break;
			case 1: // Top
				//LogHelper.info(meta + ": Rotating arrows to top");
				renderer.uvRotateNorth = 3;
				renderer.uvRotateSouth = 3;
				renderer.uvRotateWest = 3;
				renderer.uvRotateEast = 3;
				break;
			case 2: // North
				//LogHelper.info(meta + ": Rotating arrows to north");
				renderer.uvRotateTop = 3;
				renderer.uvRotateBottom = 3;
				renderer.uvRotateNorth = 1;
				renderer.uvRotateSouth = 2;

				break;
			case 3: // South
				//LogHelper.info(meta + ": Rotating arrows to south");
				renderer.uvRotateTop = 0;
				renderer.uvRotateBottom = 0;
				renderer.uvRotateNorth = 2;
				renderer.uvRotateSouth = 1;
				break;
			case 4: // West
				//LogHelper.info(meta + ": Rotating arrows to west");
				renderer.uvRotateTop = 1;
				renderer.uvRotateBottom = 2;
				renderer.uvRotateWest = 1;
				renderer.uvRotateEast = 2;
				break;
			case 5: // East
				//LogHelper.info(meta + ": Rotating arrows to east");
				renderer.uvRotateTop = 2;
				renderer.uvRotateBottom = 1;
				renderer.uvRotateWest = 2;
				renderer.uvRotateEast = 1;
				break;
		}

		boolean flag = renderer.renderStandardBlock(block, x, y, z);

		// Reset renderer values
		renderer.flipTexture = false;
		renderer.uvRotateNorth = 0;
		renderer.uvRotateSouth = 0;
		renderer.uvRotateWest = 0;
		renderer.uvRotateEast = 0;
		renderer.uvRotateTop = 0;
		renderer.uvRotateBottom = 0;

		return flag;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	@Override
	public int getRenderId() {
		return RenderIds.CCBUS;
	}

}
