package com.dave.ae2peripheral.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;

import com.dave.ae2peripheral.reference.Reference;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockDefault extends Block {
	public BlockDefault(Material material) {
		super(material);
	}

	public BlockDefault() {
		super(Material.iron);
		setHardness(2.2F);
		setLightOpacity(255);
		setLightLevel(0);
		setHarvestLevel("pickaxe", 0);
	}

	@Override
	public String getUnlocalizedName() {
		return String.format("tile.%s:%s", Reference.MOD_ID.toLowerCase(), getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		blockIcon = iconRegister.registerIcon(String.format("%s:%s", Reference.MOD_ID.toLowerCase(), this.getTextureName()));
	}

	protected String getUnwrappedUnlocalizedName(String unlocalizedName) {
		return unlocalizedName.substring(unlocalizedName.indexOf(".") + 1);
	}
}
