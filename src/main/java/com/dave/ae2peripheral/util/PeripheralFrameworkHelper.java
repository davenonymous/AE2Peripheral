package com.dave.ae2peripheral.util;

import java.util.ArrayList;

import dan200.computercraft.api.peripheral.IComputerAccess;

public class PeripheralFrameworkHelper {

	public static void broadcastEvent(ArrayList<IComputerAccess> computers, String event, Object[] args) {
		for (IComputerAccess c : computers) {
			c.queueEvent(event, args);
		}
	}
}
