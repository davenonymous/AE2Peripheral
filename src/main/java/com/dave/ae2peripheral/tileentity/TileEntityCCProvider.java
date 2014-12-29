package com.dave.ae2peripheral.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import openperipheral.api.Ignore;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.crafting.ICraftingWatcher;
import appeng.api.networking.crafting.ICraftingWatcherHost;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;

import com.dave.ae2peripheral.handler.CraftingPattern;
import com.dave.ae2peripheral.util.LogHelper;
import com.dave.ae2peripheral.util.PeripheralFrameworkHelper;
import com.theoriginalbit.framework.peripheral.LuaType;
import com.theoriginalbit.framework.peripheral.annotation.Computers.Detach;
import com.theoriginalbit.framework.peripheral.annotation.LuaPeripheral;
import com.theoriginalbit.framework.peripheral.annotation.function.LuaFunction;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;

@Ignore
@LuaPeripheral("me_provider")
public class TileEntityCCProvider extends TileEntityAE2 implements ICraftingProvider, ICraftingWatcherHost {
	@com.theoriginalbit.framework.peripheral.annotation.Computers.List
	public ArrayList<IComputerAccess>		computers;

	private boolean							isBusy			= false;
	private List<ICraftingPatternDetails>	craftingList	= null;
	private ICraftingWatcher				craftingWatcher	= null;

	@Detach
	public void detach(IComputerAccess computer) {
		resetPatterns();
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

		isBusy = true;
		PeripheralFrameworkHelper.broadcastEvent(computers, "crafting_request", new Object[] { stacksOut, stacksIn });

		// Return all the ingredients to the ME system
		IMEMonitor<IAEItemStack> monitor = getMEMonitor();
		for (int slot = 0; slot < table.getSizeInventory(); slot++) {
			ItemStack is = table.getStackInSlot(slot);
			if (is == null) {
				continue;
			}

			monitor.injectItems(AEApi.instance().storage().createItemStack(is), Actionable.MODULATE, machineSource);
		}

		table = null;

		return true;
	}

	@Override
	public boolean isBusy() {
		return isBusy;
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

		isBusy = false;

		Object luaObj = null;
		try {
			luaObj = LuaType.toLua(what);
		} catch (LuaException e) {
			LogHelper.debug("Could not convert IAEItemStack to lua.");
		}

		PeripheralFrameworkHelper.broadcastEvent(computers, "crafting_finished", new Object[] { luaObj });
	}

}
