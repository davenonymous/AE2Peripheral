package com.dave.ae2peripheral.handler;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;

public class CraftingPattern implements ICraftingPatternDetails {
	private IAEItemStack	inputs[];
	private IAEItemStack	outputs[];
	private IAEItemStack	inputsCondensed[];
	private IAEItemStack	outputsCondensed[];

	int						priority	= 0;

	public CraftingPattern(IAEItemStack inputs[], IAEItemStack outputs[]) {
		this.inputs = inputs;
		this.outputs = outputs;
		this.inputsCondensed = condenseArray(this.inputs);
		this.outputsCondensed = condenseArray(this.outputs);
	}

	private static IAEItemStack[] condenseArray(IAEItemStack input[]) {
		int condensedSize = 0;
		for (IAEItemStack stack : input) {
			if (stack == null) {
				continue;
			}

			condensedSize++;
		}

		IAEItemStack result[] = new IAEItemStack[condensedSize];
		int index = 0;
		for (IAEItemStack stack : input) {
			if (stack == null) {
				continue;
			}

			result[index] = stack;
			index++;
		}

		return result;
	}

	@Override
	public ItemStack getPattern() {
		// TODO: Ohoh, we are not really an item.
		return null;
	}

	@Override
	public boolean isValidItemForSlot(int slotIndex, ItemStack itemStack, World world) {
		return false;
	}

	@Override
	public boolean isCraftable() {
		return false;
	}

	@Override
	public IAEItemStack[] getInputs() {
		return inputs;
	}

	@Override
	public IAEItemStack[] getCondensedInputs() {
		return inputsCondensed;
	}

	@Override
	public IAEItemStack[] getCondensedOutputs() {
		return outputsCondensed;
	}

	@Override
	public IAEItemStack[] getOutputs() {
		return outputs;
	}

	@Override
	public boolean canSubstitute() {
		return false;
	}

	@Override
	public ItemStack getOutput(InventoryCrafting craftingInv, World world) {
		// Only available for crafting recipes
		return null;
	}

	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public int getPriority() {
		return priority;
	}

}
