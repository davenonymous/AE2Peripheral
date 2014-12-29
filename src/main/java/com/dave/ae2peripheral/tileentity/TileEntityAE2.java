package com.dave.ae2peripheral.tileentity;

import java.util.Iterator;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;

import com.dave.ae2peripheral.converter.ConverterAEItemStack;
import com.dave.ae2peripheral.reference.Reference;
import com.dave.ae2peripheral.util.LogHelper;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

public class TileEntityAE2 extends TileEntityDefault implements IActionHost {
	AEGridBlock		gridBlock		= null;
	IGridNode		gridNode		= null;
	MachineSource	machineSource	= null;

	public TileEntityAE2() {
		super();
	}

	@Override
	public void initialize() {
		super.initialize();

		getGridNode(ForgeDirection.UNKNOWN);
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if (gridNode != null) {
			gridNode.destroy();
		}
	}

	protected IMEMonitor<IAEItemStack> getMEMonitor() {
		IStorageGrid storageGrid = gridNode.getGrid().getCache(IStorageGrid.class);
		return storageGrid.getItemInventory();
	}

	protected IAEItemStack findItemStack(IMEMonitor<IAEItemStack> monitor, IAEItemStack needle) {
		NBTTagCompound tag = null;
		String extra = null;
		if (needle.hasTagCompound()) {
			tag = needle.getTagCompound().getNBTTagCompoundCopy();
			extra = tag.getString(Reference.MOD_ID + "-ExtraData");
		}

		UniqueIdentifier needleId = GameRegistry.findUniqueIdentifierFor(needle.getItem());
		if (needleId == null) {
			return null;
		}

		IItemList<IAEItemStack> list = monitor.getStorageList();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			IAEItemStack stack = (IAEItemStack) iterator.next();

			if (!compareStacks(needle, needleId, extra, stack)) {
				continue;
			}

			return stack.copy();
		}

		return null;
	}

	private static boolean compareStacks(IAEItemStack needle, UniqueIdentifier needleId, String extra, IAEItemStack stack) {
		UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(stack.getItem());

		if (id == null) {
			LogHelper.trace("Skipping Item: UniqueID not found");
			return false;
		}

		if (!(id.toString().equals(needleId.toString()))) {
			LogHelper.trace("Skipping Item: ID does not match");
			return false;
		}

		if (stack.getItemDamage() != needle.getItemDamage()) {
			LogHelper.trace("Skipping Item: Metadata does not match");
			return false;
		}

		if (extra != null && stack.hasTagCompound()) {
			if (!extra.equals(ConverterAEItemStack.getNBTHash(stack.getTagCompound()))) {
				LogHelper.trace("Skipping Item: NBT does not match");
				return false;
			}
		}

		return true;
	}

	@Override
	public IGridNode getGridNode(ForgeDirection dir) {
		if (worldObj.isRemote) {
			return null;
		}

		if (gridBlock == null) {
			gridBlock = new AEGridBlock(this);
		}

		if (gridNode == null) {
			gridNode = AEApi.instance().createGridNode(gridBlock);
			gridNode.updateState();
		}

		machineSource = new MachineSource(this);

		return gridNode;
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir) {
		return AECableType.SMART;
	}

	@Override
	public void securityBreak() {
		this.worldObj.func_147480_a(xCoord, yCoord, zCoord, true);
	}

	@Override
	public IGridNode getActionableNode() {
		return gridNode;
	}

}
