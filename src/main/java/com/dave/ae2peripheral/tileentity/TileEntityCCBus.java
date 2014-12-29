package com.dave.ae2peripheral.tileentity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import openperipheral.api.Ignore;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingWatcher;
import appeng.api.networking.crafting.ICraftingWatcherHost;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

import com.dave.ae2peripheral.handler.CraftingPattern;
import com.dave.ae2peripheral.reference.Names;
import com.dave.ae2peripheral.util.ItemHelper;
import com.dave.ae2peripheral.util.LogHelper;
import com.dave.ae2peripheral.util.PeripheralFrameworkHelper;
import com.google.common.collect.ImmutableSet;
import com.theoriginalbit.framework.peripheral.LuaType;
import com.theoriginalbit.framework.peripheral.annotation.Computers.Detach;
import com.theoriginalbit.framework.peripheral.annotation.LuaPeripheral;
import com.theoriginalbit.framework.peripheral.annotation.function.LuaFunction;
import com.theoriginalbit.framework.peripheral.annotation.function.MultiReturn;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;

@Ignore
@LuaPeripheral("me_bus")
public class TileEntityCCBus extends TileEntityAE2 implements IInventory, ICraftingRequester, ICraftingProvider, ICraftingWatcherHost {
	@com.theoriginalbit.framework.peripheral.annotation.Computers.List
	public ArrayList<IComputerAccess>		computers;
	Set<ICraftingLink>						links				= new HashSet<ICraftingLink>();

	private int								inventory_size		= 9;
	private ItemStack[]						items;

	private List<ICraftingPatternDetails>	craftingList		= null;
	private ICraftingWatcher				craftingWatcher		= null;

	private boolean							notifiedAboutBusy	= false;

	public TileEntityCCBus() {
		super();

		items = new ItemStack[inventory_size];
	}

	@Detach
	public void detach(IComputerAccess computer) {
		cancelAllCraftingJobs();
		resetPatterns();
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);

		NBTTagList tagList = new NBTTagList();
		for (ICraftingLink link : links) {
			if (link != null) {
				NBTTagCompound nbt = new NBTTagCompound();
				link.writeToNBT(nbt);
				tagList.appendTag(nbt);
			}
		}

		compound.setTag("Links", tagList);
		compound.setTag("Items", ItemHelper.writeItemStacksToTag(items));
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTagCompound) {
		super.readFromNBT(nbtTagCompound);

		links.clear();
		NBTTagList tagList = nbtTagCompound.getTagList("Links", 10);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = tagList.getCompoundTagAt(i);
			links.add(AEApi.instance().storage().loadCraftingLink(tag, this));
		}

		ItemHelper.readItemStacksFromTag(items, nbtTagCompound.getTagList("Items", 10));
	}

	@LuaFunction
	public boolean isNetworkActive() {
		if (gridNode == null) {
			return false;
		}

		return gridNode.isActive();
	}

	@LuaFunction
	public boolean cancelCraftingJob(String craftingId) {
		Set<ICraftingLink> copy = new HashSet<ICraftingLink>(links);
		for (ICraftingLink link : copy) {
			if (link.getCraftingID().equals(craftingId)) {
				link.cancel();
				links.remove(link);
				return true;
			}
		}

		return false;
	}

	@LuaFunction
	public int cancelAllCraftingJobs() {
		int count = 0;
		if (links == null) {
			return 0;
		}

		Set<ICraftingLink> copy = new HashSet<ICraftingLink>(links);

		for (ICraftingLink link : copy) {
			link.cancel();
			count++;
		}

		links.clear();
		return count;
	}

	/*
	@LuaFunction
	public int getCraftingCPUs() {
		ICraftingGrid craftingGrid = gridNode.getGrid().getCache(ICraftingGrid.class);
		return craftingGrid.getCpus().size();
	}
	*/

	@LuaFunction
	public ArrayList<String> getJobs() {
		ArrayList<String> result = new ArrayList<String>();
		Set<ICraftingLink> copy = new HashSet<ICraftingLink>(links);
		for (ICraftingLink link : copy) {
			result.add(link.getCraftingID());
		}

		return result;
	}

	@Override
	public ImmutableSet<ICraftingLink> getRequestedJobs() {
		return ImmutableSet.copyOf(links);
	}

	@Override
	public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
		if (mode == Actionable.MODULATE) {
			// Put it in the inventory and return the remaining stuff to the ME system
			ItemStack remaining = ItemHelper.insertStackIntoInventory(this, items.getItemStack());
			return AEApi.instance().storage().createItemStack(remaining);
		}

		return items.copy();
	}

	private ICraftingLink getLinkById(String craftingId) {
		for (ICraftingLink link : links) {
			if (link.getCraftingID().equals(craftingId)) {
				return link;
			}
		}

		return null;
	}

	@LuaFunction
	public boolean isJobRunning(String craftingId) {
		ICraftingLink link = getLinkById(craftingId);
		return link != null;
	}

	@Override
	public void jobStateChange(ICraftingLink link) {
		if (link.isCanceled()) {
			PeripheralFrameworkHelper.broadcastEvent(computers, "job_canceled", new Object[] { link.getCraftingID() });

			if (links.contains(link)) {
				links.remove(link);
			}
			return;
		}

		if (link.isDone()) {
			PeripheralFrameworkHelper.broadcastEvent(computers, "job_done", new Object[] { link.getCraftingID() });

			if (links.contains(link)) {
				links.remove(link);
			}
			return;
		}
	}

	@LuaFunction
	@MultiReturn
	public Object[] requestCrafting(IAEItemStack stack) {
		ICraftingGrid craftingGrid = gridNode.getGrid().getCache(ICraftingGrid.class);
		Future<ICraftingJob> future = craftingGrid.beginCraftingJob(getWorldObj(), gridNode.getGrid(), machineSource, stack, null);

		try {
			ICraftingJob job = future.get();
			ICraftingLink link = craftingGrid.submitJob(job, this, null, false, machineSource);
			if (link == null) {
				return new Object[] { false, "Not craftable" };
			}
			links.add(link);

			return new Object[] { true, link.getCraftingID() };
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return new Object[] { false, "Not craftable" };
	}

	@LuaFunction
	public void setRotation(ForgeDirection dir) {
		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, dir.ordinal(), 3);
	}

	@LuaFunction
	public ForgeDirection getRotation() {
		return ForgeDirection.getOrientation(getBlockMetadata());
	}

	@LuaFunction
	public ArrayList<IAEItemStack> listItems() {
		ArrayList<IAEItemStack> list = new ArrayList<IAEItemStack>();

		IItemList<IAEItemStack> items = getMEMonitor().getStorageList();
		for (IAEItemStack stack : items) {
			list.add(stack);
		}

		return list;
	}

	@LuaFunction
	public boolean dumpAllItems() throws Exception {
		boolean success = true;
		for (int slotIndex = 1; slotIndex <= this.inventory_size; slotIndex++) {
			Object[] result = dumpItem(slotIndex);
			if (((Boolean) result[0]).booleanValue() == false) {
				success = false;
			}
		}

		return success;
	}

	@LuaFunction
	@MultiReturn
	public Object[] dumpItem(int slot) throws Exception {
		if (slot < 1 || slot > 9) {
			throw new Exception("Slot must be in range 1-9");
		}

		slot -= 1;
		if (items[slot] == null) {
			return new Object[] { false, "Slot is empty", null };
		}

		int metaData = this.getBlockMetadata();
		ForgeDirection side = ForgeDirection.getOrientation(metaData);

		boolean success = false;
		if (worldObj.isAirBlock(xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ)) {
			Float offsetF = 0.5F;
			EntityItem entityitem = new EntityItem(this.getWorldObj(), this.xCoord + 0.5F + side.offsetX * offsetF, this.yCoord + 0.5F + side.offsetY * offsetF, this.zCoord + 0.5F + side.offsetZ * offsetF, items[slot]);

			entityitem.lifespan = 1200;
			entityitem.delayBeforeCanPickup = 10;

			float f3 = 0.2F;

			entityitem.motionX = side.offsetX * f3;
			entityitem.motionY = side.offsetY * f3;
			entityitem.motionZ = side.offsetZ * f3;
			this.getWorldObj().spawnEntityInWorld(entityitem);

			items[slot] = null;

			success = true;
		} else if (worldObj.getBlock(xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ).hasTileEntity()) {
			int previousStackSize = items[slot].stackSize;

			TileEntity ent = worldObj.getTileEntity(xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ);
			if (ent instanceof ISidedInventory) {
				items[slot] = ItemHelper.insertStackIntoSidedInventory((ISidedInventory) ent, items[slot], side.getOpposite());
				if (items[slot] != null && items[slot].stackSize == previousStackSize) {
					return new Object[] { false, "Could not insert items into inventory.", AEApi.instance().storage().createItemStack(items[slot]) };
				}

				success = true;
			} else if (ent instanceof IInventory) {
				items[slot] = ItemHelper.insertStackIntoInventory((IInventory) ent, items[slot]);
				if (items[slot] != null && items[slot].stackSize == previousStackSize) {
					return new Object[] { false, "Could not insert items into inventory.", AEApi.instance().storage().createItemStack(items[slot]) };
				}

				success = true;
			}
		}

		if (success) {
			markDirty();
		}

		return new Object[] { success, "OK", AEApi.instance().storage().createItemStack(items[slot]) };
	}

	@LuaFunction
	public boolean importAllItems() throws Exception {
		boolean success = true;
		for (int slotIndex = 1; slotIndex <= this.inventory_size; slotIndex++) {
			Object[] result = importItem(slotIndex);
			if (((Boolean) result[0]).booleanValue() == false) {
				success = false;
			}
		}

		return success;
	}

	@LuaFunction
	@MultiReturn
	public Object[] importItem(int slot) throws Exception {
		if (slot < 1 || slot > 9) {
			throw new Exception("Slot must be in range 1-9");
		}

		slot -= 1;
		if (items[slot] == null) {
			return new Object[] { false, "Slot is empty", null };
		}

		if (!isNetworkActive()) {
			return new Object[] { false, "ME Network not active", null };
		}

		IAEItemStack stack = AEApi.instance().storage().createItemStack(items[slot]);

		IMEMonitor<IAEItemStack> monitor = getMEMonitor();

		if (!monitor.canAccept(stack)) {
			return new Object[] { false, "ME system cannot accept item", null };
		}

		stack = monitor.injectItems(stack, Actionable.MODULATE, machineSource);
		if (stack != null) {
			if (stack.getStackSize() == items[slot].stackSize) {
				return new Object[] { false, "ME system has no more storage space or is unable to store this kind of item.", null };
			}

			items[slot] = stack.getItemStack();
		} else {
			items[slot] = null;
		}

		this.markDirty();
		return new Object[] { true, "OK", stack };
	}

	@LuaFunction
	@MultiReturn
	public Object[] isItemAvailable(IAEItemStack needle) {
		if (!isNetworkActive()) {
			return new Object[] { false, "ME Network not active", null };
		}

		IMEMonitor<IAEItemStack> monitor = getMEMonitor();
		IAEItemStack copy = findItemStack(monitor, needle);
		if (copy != null) {
			return new Object[] { false, "Item not found", null };
		}

		return new Object[] { true, "OK", copy };
	}

	@LuaFunction
	@MultiReturn
	public Object[] exportItem(IAEItemStack needle) {
		IMEMonitor<IAEItemStack> monitor = getMEMonitor();

		IAEItemStack stack = findItemStack(monitor, needle);
		if (stack == null) {
			return new Object[] { false, "Item not found in ME System", null };
		}

		IAEItemStack copy = stack.copy();

		// Only allow a maximum of one stack to be extracted
		if (needle.getStackSize() > stack.getItemStack().getMaxStackSize()) {
			stack.setStackSize(stack.getItemStack().getMaxStackSize());
		} else {
			stack.setStackSize(needle.getStackSize());
		}

		IAEItemStack out = monitor.extractItems(stack, Actionable.MODULATE, machineSource);
		if (out == null) {
			return new Object[] { false, "Item found, but cannot be extracted", copy };
		}

		ItemStack remaining = ItemHelper.insertStackIntoInventory(this, out.getItemStack());

		// Put stuff that does not fit back into the ME system
		if (remaining != null && remaining.stackSize > 0) {
			monitor.injectItems(AEApi.instance().storage().createItemStack(remaining), Actionable.MODULATE, machineSource);
			out.setStackSize(stack.getStackSize() - remaining.stackSize);
		}

		// Report what was actually put into the CCBus
		return new Object[] { true, "OK", out };
	}

	@Override
	public int getSizeInventory() {
		return inventory_size;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return items[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int decreaseAmount) {
		return ItemHelper.decrStackSize(this, slot, decreaseAmount);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		items[slot] = stack;
		markDirty();
	}

	@Override
	public String getInventoryName() {
		return Names.Containers.CCBUS;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return false;
	}

	@Override
	public void openInventory() {
		return;
	}

	@Override
	public void closeInventory() {
		return;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return true;
	}

	@LuaFunction
	public void resetPatterns() {
		craftingList = new ArrayList<ICraftingPatternDetails>();
		if (craftingWatcher != null) {
			craftingWatcher.clear();
		}

		gridNode.getGrid().postEvent(new MENetworkCraftingPatternChange(this, gridNode));
	}

	@LuaFunction
	public void addPattern(IAEItemStack inputs[], IAEItemStack outputs[]) {

		if (craftingList == null) {
			craftingList = new ArrayList<ICraftingPatternDetails>();
		}

		CraftingPattern cp = new CraftingPattern(inputs, outputs);

		craftingList.add(cp);
		gridNode.getGrid().postEvent(new MENetworkCraftingPatternChange(this, gridNode));

		craftingWatcher.add(cp.getCondensedOutputs()[0]);
	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
		// Notify all attached computers about the crafting request		
		Object stacksIn = null;
		try {
			stacksIn = LuaType.toLua(patternDetails.getInputs());
		} catch (LuaException e) {
			LogHelper.debug("Could not convert input stacks to lua.");
		}

		Object stacksOut = null;
		try {
			stacksOut = LuaType.toLua(patternDetails.getCondensedOutputs());
		} catch (LuaException e) {
			LogHelper.debug("Could not convert input stacks to lua.");
		}

		if (isBusy()) {
			if (!notifiedAboutBusy) {
				PeripheralFrameworkHelper.broadcastEvent(computers, "crafting_request_waiting", new Object[] { stacksOut, stacksIn });
				notifiedAboutBusy = true;
			}
			return false;
		}

		// Put the ingredients in the item slots
		for (int slot = 0; slot < table.getSizeInventory(); slot++) {
			ItemStack is = table.getStackInSlot(slot);
			if (is == null) {
				continue;
			}

			items[slot] = is;
		}

		notifiedAboutBusy = false;
		PeripheralFrameworkHelper.broadcastEvent(computers, "crafting_request", new Object[] { stacksOut, stacksIn });

		table = null;

		return true;
	}

	@Override
	public boolean isBusy() {
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		if (craftingList != null) {
			for (ICraftingPatternDetails details : craftingList) {
				craftingTracker.addCraftingOption(this, details);
			}
		}
	}

	@Override
	public void updateWatcher(ICraftingWatcher newWatcher) {
		craftingWatcher = newWatcher;
	}

	@Override
	public void onRequestChange(ICraftingGrid craftingGrid, IAEItemStack what) {
		if (craftingGrid.isRequesting(what)) {
			return;
		}

		Object luaObj = null;
		try {
			luaObj = LuaType.toLua(what);
		} catch (LuaException e) {
			LogHelper.debug("Could not convert IAEItemStack to lua.");
		}

		PeripheralFrameworkHelper.broadcastEvent(computers, "crafting_finished", new Object[] { luaObj });
	}

}
