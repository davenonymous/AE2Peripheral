package com.dave.ae2peripheral.converter;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import org.apache.commons.codec.binary.Hex;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAETagCompound;

import com.dave.ae2peripheral.reference.Reference;
import com.dave.ae2peripheral.util.LogHelper;
import com.google.common.collect.Maps;
import com.theoriginalbit.framework.peripheral.converter.ITypeConverter;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

public class ConverterAEItemStack implements ITypeConverter {

	@Override
	public Object fromLua(Object obj, Class<?> expected) {
		if (!(expected == IAEItemStack.class && obj instanceof Map)) {
			return null;
		}

		Map<Object, Object> map = (Map<Object, Object>) obj;
		Object id = map.get("id");
		if (id == null) {
			return null;
		}

		String[] parts = ((String) id).split(":", 2);
		String modId = parts[0];
		String name = parts[1];
		Item item = GameRegistry.findItem(modId, name);

		int qty = getIntValue(map, "qty", 1);
		int dmg = getIntValue(map, "dmg", 0);

		ItemStack stack = new ItemStack(item, qty, dmg);

		if (map.containsKey("nbt_id")) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString(Reference.MOD_ID + "-ExtraData", (String) map.get("nbt_id"));
			stack.setTagCompound(tag);
		}

		return AEApi.instance().storage().createItemStack(stack);
	}

	@Override
	public Object toLua(Object obj) {
		if (!(obj instanceof IAEItemStack)) {
			return null;
		}

		Map<String, Object> map = Maps.newHashMap();

		IAEItemStack stack = (IAEItemStack) obj;
		UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(stack.getItem());

		if (id != null) {
			map.put("id", id.toString());
			map.put("name", id.name);
			map.put("mod_id", id.modId);
		} else {
			map.put("id", "?");
			map.put("name", "?");
			map.put("mod_id", "?");
		}

		map.put("display_name", getNameForItemStack(stack.getItemStack()));
		map.put("raw_name", getRawNameForStack(stack.getItemStack()));

		map.put("qty", stack.getStackSize());
		map.put("dmg", stack.getItemDamage());
		map.put("max_dmg", stack.getItem().getMaxDamage());
		map.put("max_size", stack.getItemStack().getMaxStackSize());

		map.put("craftable", stack.isCraftable());

		IAETagCompound tag = stack.getTagCompound();
		if (tag != null) {
			// Do not overwrite the nbt_id if it already exists, but copy it instead.
			NBTTagCompound nbt = tag.getNBTTagCompoundCopy();
			if (nbt.hasKey(Reference.MOD_ID + "-ExtraData")) {
				map.put("nbt_id", nbt.getString(Reference.MOD_ID + "-ExtraData"));
			} else {
				map.put("nbt_id", getNBTHash(tag));
			}
		}

		return map;
	}

	// TODO: Implement it correctly: have several files containing mappings for itemstacks and their
	// nbt data to single key+value combinations and provide these.
	// This also includes updating the search functions to care about the given nbt tags and ignore
	// the others. Altogether this might not be worth the hassle and should -if implemented- be configurable
	// in the config.
	public static String getNBTHash(IAETagCompound tag) {
		String result = "00000000000000000000000000000000";
		try {
			byte[] compressed = CompressedStreamTools.compress(tag.getNBTTagCompoundCopy());
			//result = Base64.encode(compressed);
			byte[] digest = MessageDigest.getInstance("MD5").digest(compressed);
			result = new String(Hex.encodeHex(digest));
		} catch (IOException e) {
			LogHelper.fatal("Could not compress NBT Tag using CompressedStreamTools. Stack comparison with NBT data will not work!");
		} catch (NoSuchAlgorithmException e) {
			LogHelper.fatal("MD5 digest algorithm does not exist. Stack comparison with NBT data will not work!");
		}
		return result;
	}

	// Thanks OpenPeripheral!
	private static String getNameForItemStack(ItemStack is) {
		try {
			return is.getDisplayName();
		} catch (Exception e) {}

		try {
			return is.getUnlocalizedName();
		} catch (Exception e2) {}

		return "unknown";
	}

	private static String getRawNameForStack(ItemStack is) {
		try {
			return is.getUnlocalizedName().toLowerCase();
		} catch (Exception e) {}

		return "unknown";
	}

	private static int getIntValue(Map<?, ?> map, String key, int _default) {
		Object value = map.get(key);
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}

		return _default;
	}
}
