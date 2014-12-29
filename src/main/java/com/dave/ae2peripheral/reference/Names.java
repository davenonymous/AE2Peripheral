package com.dave.ae2peripheral.reference;

public class Names {
	public static final class Blocks {
		public static final String	CCBUS		= "ccbus";
		public static final String	CCPROVIDER	= "ccprovider";
	}

	public static final class Containers {
		public static final String	CCBUS	= "container." + Reference.MOD_ID.toLowerCase() + ":" + Blocks.CCBUS;
	}
}
