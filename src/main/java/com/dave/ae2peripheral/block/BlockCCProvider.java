package com.dave.ae2peripheral.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.dave.ae2peripheral.reference.Names;
import com.dave.ae2peripheral.tileentity.TileEntityCCProvider;

public class BlockCCProvider extends BlockDefault implements ITileEntityProvider {
	public BlockCCProvider() {
		super();
		this.setBlockName(Names.Blocks.CCPROVIDER);
		this.setBlockTextureName(Names.Blocks.CCPROVIDER);
	}

	@Override
	public boolean hasTileEntity(int metadata) {
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityCCProvider();
	}

}
