package com.cloud.allocation.algroithm;

import org.libvirt.LibvirtException;

import com.cloud.virtual.machine.Discover;
import com.cloud.virtual.machine.Node;
import com.cloud.virtual.machine.Requirement;

public abstract class VMAllocationAlgorithm {
	public Node getBestNode (String vmName,String imageName,Discover discoveryPhase, Requirement requirement) throws LibvirtException{
		return null;
	}
}
