package com.dave.ae2peripheral.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemHelper {

	public static ItemStack insertStackIntoSidedInventory(ISidedInventory inv, ItemStack stack, ForgeDirection side) {
		int iTargetSlot = ItemHelper.findBestSlotForSidedInventory(inv, stack, side);
		if (iTargetSlot == -1) {
			return stack;
		}

		ItemStack stackInSlot = inv.getStackInSlot(iTargetSlot);
		if (stackInSlot != null) {
			int iFreeSpace = stackInSlot.getMaxStackSize() - stackInSlot.stackSize;
			if (stack.stackSize > iFreeSpace) {
				// Stack does not fully fit
				stack.stackSize = stack.stackSize - iFreeSpace;
				stackInSlot.stackSize = stackInSlot.getMaxStackSize();

				return insertStackIntoSidedInventory(inv, stack, side);
			} else {
				stackInSlot.stackSize += stack.stackSize;
			}

			inv.markDirty();
		} else {
			inv.setInventorySlotContents(iTargetSlot, stack);
			inv.markDirty();
		}

		return null;
	}

	public static ItemStack insertStackIntoInventory(IInventory inv, ItemStack stack) {
		int iTargetSlot = ItemHelper.findBestSlotForInventory(inv, stack);
		if (iTargetSlot == -1) {
			return stack;
		}

		ItemStack stackInSlot = inv.getStackInSlot(iTargetSlot);
		if (stackInSlot != null) {
			int iFreeSpace = stackInSlot.getMaxStackSize() - stackInSlot.stackSize;
			if (stack.stackSize > iFreeSpace) {
				// Stack does not fully fit
				stack.stackSize = stack.stackSize - iFreeSpace;
				stackInSlot.stackSize = stackInSlot.getMaxStackSize();

				return insertStackIntoInventory(inv, stack);
			} else {
				stackInSlot.stackSize += stack.stackSize;
			}

			inv.markDirty();
		} else {
			inv.setInventorySlotContents(iTargetSlot, stack);
			inv.markDirty();
		}

		return null;
	}

	public static int findBestSlotForSidedInventory(ISidedInventory inv, ItemStack stack, ForgeDirection side) {
		int result = -1;

		// First search for a matching slot
		result = findBestMatchingSlotForSidedInventory(inv, stack, side);
		if (result != -1) {
			return result;
		}

		// Then check for an empty slot
		result = findFirstEmptySlotInSidedInventory(inv, stack, side);
		if (result != -1) {
			return result;
		}

		// No slot found :(
		return result;

	}

	private static int findBestMatchingSlotForSidedInventory(ISidedInventory inv, ItemStack stack, ForgeDirection side) {
		int[] accessibleSlotsFromSide = inv.getAccessibleSlotsFromSide(side.ordinal());

		for (int slot : accessibleSlotsFromSide) {
			ItemStack target = inv.getStackInSlot(slot);
			if (target != null && target.getItem() == stack.getItem() && target.isStackable() && inv.isItemValidForSlot(slot, stack)
					&& target.stackSize < target.getMaxStackSize() && target.stackSize < inv.getInventoryStackLimit()
					&& inv.canInsertItem(slot, stack, side.ordinal()) && (!target.getHasSubtypes() || target.getItemDamage() == stack.getItemDamage())
					&& ItemStack.areItemStackTagsEqual(target, stack)) {
				return slot;
			}
		}

		return -1;
	}

	private static int findFirstEmptySlotInSidedInventory(ISidedInventory inv, ItemStack stack, ForgeDirection side) {
		int[] accessibleSlotsFromSide = inv.getAccessibleSlotsFromSide(side.ordinal());

		for (int slot : accessibleSlotsFromSide) {
			ItemStack target = inv.getStackInSlot(slot);
			if (target == null && inv.isItemValidForSlot(slot, stack) && inv.canInsertItem(slot, stack, side.ordinal())) {
				return slot;
			}
		}

		return -1;
	}

	public static int findBestSlotForInventory(IInventory inv, ItemStack stack) {
		int result = -1;

		// First search for a matching slot
		result = findBestMatchingSlotForInventory(inv, stack);
		if (result != -1) {
			return result;
		}

		// Then check for an empty slot
		result = findFirstEmptySlot(inv, stack);
		if (result != -1) {
			return result;
		}

		// No slot found :(
		return result;
	}

	private static int findBestMatchingSlotForInventory(IInventory inv, ItemStack stack) {
		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack target = inv.getStackInSlot(i);
			if (target != null && target.getItem() == stack.getItem() && target.isStackable() && inv.isItemValidForSlot(i, stack)
					&& target.stackSize < target.getMaxStackSize() && target.stackSize < inv.getInventoryStackLimit()
					&& (!target.getHasSubtypes() || target.getItemDamage() == stack.getItemDamage()) && ItemStack.areItemStackTagsEqual(target, stack)) {
				return i;
			}
		}

		return -1;
	}

	private static int findFirstEmptySlot(IInventory inv, ItemStack stack) {
		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack target = inv.getStackInSlot(i);
			if (target == null && inv.isItemValidForSlot(i, stack)) {
				return i;
			}
		}

		return -1;
	}

	// Taken from CodeChickenLib. All credits to Chicken-Bones
	/**
	 * NBT item loading function with support for stack sizes > 32K
	 */
	public static void readItemStacksFromTag(ItemStack[] items, NBTTagList tagList) {
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = tagList.getCompoundTagAt(i);
			int b = tag.getShort("Slot");
			items[b] = ItemStack.loadItemStackFromNBT(tag);
			if (tag.hasKey("Quantity")) {
				items[b].stackSize = ((NBTBase.NBTPrimitive) tag.getTag("Quantity")).func_150287_d();
			}
		}
	}

	/**
	 * NBT item saving function
	 */
	public static NBTTagList writeItemStacksToTag(ItemStack[] items) {
		return writeItemStacksToTag(items, 64);
	}

	/**
	 * NBT item saving function with support for stack sizes > 32K
	 */
	public static NBTTagList writeItemStacksToTag(ItemStack[] items, int maxQuantity) {
		NBTTagList tagList = new NBTTagList();
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setShort("Slot", (short) i);
				items[i].writeToNBT(tag);

				if (maxQuantity > Short.MAX_VALUE) {
					tag.setInteger("Quantity", items[i].stackSize);
				} else if (maxQuantity > Byte.MAX_VALUE) {
					tag.setShort("Quantity", (short) items[i].stackSize);
				}

				tagList.appendTag(tag);
			}
		}
		return tagList;
	}

	/**
	 * Static default implementation for IInventory method
	 */
	public static ItemStack decrStackSize(IInventory inv, int slot, int size) {
		ItemStack item = inv.getStackInSlot(slot);

		if (item != null) {
			if (item.stackSize <= size) {
				inv.setInventorySlotContents(slot, null);
				inv.markDirty();
				return item;
			}
			ItemStack itemstack1 = item.splitStack(size);
			if (item.stackSize == 0) {
				inv.setInventorySlotContents(slot, null);
			}

			inv.markDirty();
			return itemstack1;
		}
		return null;
	}
}
