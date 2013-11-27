package com.cloud.virtual.machine;

import java.util.HashMap;
import java.util.Map;

public class Capability {
	String wordSize;
	String emulator;
	Map<Domain,Emulator> domain = new HashMap<Domain,Emulator>();

}
