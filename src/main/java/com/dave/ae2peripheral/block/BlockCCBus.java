package com.dave.ae2peripheral.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.implementations.items.IAEWrench;
import buildcraft.api.tools.IToolWrench;

import com.dave.ae2peripheral.AE2Peripheral;
import com.dave.ae2peripheral.reference.GuiId;
import com.dave.ae2peripheral.reference.Names;
import com.dave.ae2peripheral.reference.Reference;
import com.dave.ae2peripheral.reference.RenderIds;
import com.dave.ae2peripheral.tileentity.TileEntityCCBus;

import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Interface(iface = "buildcraft.api.tools.IToolWrench", modid = "BuildCraft|Core")
public class BlockCCBus extends BlockDefault implements ITileEntityProvider {
	@SideOnly(Side.CLIENT)
	private IIcon[]	icons;

	public BlockCCBus() {
		super();
		this.setBlockName(Names.Blocks.CCBUS);
		this.setBlockTextureName(Names.Blocks.CCBUS);

		// TODO: Create a creative tab
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		icons = new IIcon[2];

		icons[0] = iconRegister.registerIcon(Reference.MOD_ID.toLowerCase() + ":ccbus");
		icons[1] = iconRegister.registerIcon(Reference.MOD_ID.toLowerCase() + ":ccbus_arrow");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int metadata) {
		ForgeDirection dir = ForgeDirection.getOrientation(metadata);

		// The side we're pointing to and the side opposite of that use the normal icon
		if (metadata == side || dir.getOpposite().ordinal() == side) {
			return icons[0];
		}

		// The other sides use the arrow
		return icons[1];
	}

	@Override
	public int getRenderType() {
		return RenderIds.CCBUS;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int faceHit, float faceX, float faceY, float faceZ) {
		if (player.isSneaking()) {
			// TODO: Allow breaking it with a Tool
			// TODO: Drop inventory contents accordingly
			return false;
		}

		if (!world.isRemote && player instanceof EntityPlayerMP) {
			ItemStack playerStack = player.getCurrentEquippedItem();

			if (playerStack != null && (playerStack.getItem() instanceof IToolWrench || playerStack.getItem() instanceof IAEWrench)) {
				int currentMeta = world.getBlockMetadata(x, y, z);
				if (currentMeta == faceHit) {
					world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.getOrientation(currentMeta).getOpposite().ordinal(), 3);
				} else {
					world.setBlockMetadataWithNotify(x, y, z, faceHit, 3);
				}
			} else {
				player.openGui(AE2Peripheral.instance, GuiId.CCBUS.ordinal(), world, x, y, z);
			}
		}

		return true;
	}

	@Override
	public boolean hasTileEntity(int metadata) {
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityCCBus();
	}
}
