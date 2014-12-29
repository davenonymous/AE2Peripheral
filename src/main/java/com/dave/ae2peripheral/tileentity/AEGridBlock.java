package com.dave.ae2peripheral.tileentity;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;

public class AEGridBlock implements IGridBlock {
	private TileEntityAE2	gridHost;

	public AEGridBlock(TileEntityAE2 gridHost) {
		this.gridHost = gridHost;
	}

	@Override
	public double getIdlePowerUsage() {
		return 0;
	}

	@Override
	public EnumSet<GridFlags> getFlags() {
		return EnumSet.of(GridFlags.REQUIRE_CHANNEL);
	}

	@Override
	public boolean isWorldAccessible() {
		return true;
	}

	@Override
	public DimensionalCoord getLocation() {
		return new DimensionalCoord(gridHost);
	}

	@Override
	public AEColor getGridColor() {
		return AEColor.Transparent;
	}

	@Override
	public void onGridNotification(GridNotification notification) {}

	@Override
	public void setNetworkStatus(IGrid grid, int channelsInUse) {}

	@Override
	public EnumSet<ForgeDirection> getConnectableSides() {
		return EnumSet.of(
				ForgeDirection.DOWN, ForgeDirection.UP,
				ForgeDirection.WEST, ForgeDirection.EAST,
				ForgeDirection.NORTH, ForgeDirection.SOUTH
				);
	}

	@Override
	public IGridHost getMachine() {
		return gridHost;
	}

	@Override
	public void gridChanged() {}

	@Override
	public ItemStack getMachineRepresentation() {
		// TODO: Return item stack here
		return null;
	}

}
