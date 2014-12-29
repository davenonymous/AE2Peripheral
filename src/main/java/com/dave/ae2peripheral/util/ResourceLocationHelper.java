package com.dave.ae2peripheral.util;

import net.minecraft.util.ResourceLocation;

import com.dave.ae2peripheral.reference.Reference;

public class ResourceLocationHelper {
	public static ResourceLocation getResourceLocation(String modId, String path)
	{
		return new ResourceLocation(modId, path);
	}

	public static ResourceLocation getResourceLocation(String path)
	{
		return getResourceLocation(Reference.MOD_ID.toLowerCase(), path);
	}
}
