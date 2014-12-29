package com.dave.ae2peripheral;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.storage.data.IAEItemStack;

import com.dave.ae2peripheral.client.GuiHandler;
import com.dave.ae2peripheral.converter.ConverterAEItemStack;
import com.dave.ae2peripheral.converter.ConverterForgeDirection;
import com.dave.ae2peripheral.init.ModBlocks;
import com.dave.ae2peripheral.proxy.IProxy;
import com.dave.ae2peripheral.reference.Reference;
import com.theoriginalbit.framework.peripheral.LuaType;
import com.theoriginalbit.framework.peripheral.PeripheralProvider;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import dan200.computercraft.api.ComputerCraftAPI;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, dependencies = "required-after:appliedenergistics2;required-after:ComputerCraft;after:OpenPeripheralIntegration")
public class AE2Peripheral {

	@Mod.Instance(Reference.MOD_ID)
	public static AE2Peripheral	instance;

	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static IProxy		proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ModBlocks.init();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.registerTileEntities();
		proxy.registerRenderers();

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

		LuaType.registerTypeConverter(new ConverterAEItemStack());
		LuaType.registerTypeConverter(new ConverterForgeDirection());

		LuaType.registerClassToNameMapping(IAEItemStack.class, "aeitem");
		LuaType.registerClassToNameMapping(ForgeDirection.class, "direction");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		ComputerCraftAPI.registerPeripheralProvider(new PeripheralProvider());
	}
}
