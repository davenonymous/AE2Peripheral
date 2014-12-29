package com.dave.ae2peripheral.client.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.dave.ae2peripheral.tileentity.TileEntityCCBus;

public class ContainerCCBus extends ContainerDefault {
	private TileEntityCCBus	teCCBus;

	public ContainerCCBus(InventoryPlayer inventoryPlayer, TileEntityCCBus teCCBus) {
		this.teCCBus = teCCBus;

		this.addSlotToContainer(new Slot(teCCBus, 0, 62, 19));
		this.addSlotToContainer(new Slot(teCCBus, 1, 80, 19));
		this.addSlotToContainer(new Slot(teCCBus, 2, 98, 19));

		this.addSlotToContainer(new Slot(teCCBus, 3, 62, 37));
		this.addSlotToContainer(new Slot(teCCBus, 4, 80, 37));
		this.addSlotToContainer(new Slot(teCCBus, 5, 98, 37));

		this.addSlotToContainer(new Slot(teCCBus, 6, 62, 55));
		this.addSlotToContainer(new Slot(teCCBus, 7, 80, 55));
		this.addSlotToContainer(new Slot(teCCBus, 8, 98, 55));

		addPlayerSlots(inventoryPlayer, 8, 84);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer entityPlayer, int slotIndex) {
		ItemStack itemStack = null;
		Slot slot = (Slot) inventorySlots.get(slotIndex);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemStack = itemstack1.copy();

			int chestSlots = 9;
			if (slotIndex < chestSlots)
			{
				if (!mergeItemStack(itemstack1, chestSlots, inventorySlots.size(), true))
				{
					return null;
				}
			}
			else if (!mergeItemStack(itemstack1, 0, chestSlots, false))
			{
				return null;
			}
			if (itemstack1.stackSize == 0)
			{
				slot.putStack(null);
			} else
			{
				slot.onSlotChanged();
			}
		}

		return itemStack;
	}
}
