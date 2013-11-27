package com.cloud.allocation.algroithm;

import org.libvirt.LibvirtException;

import com.cloud.virtual.machine.Discover;
import com.cloud.virtual.machine.Node;
import com.cloud.virtual.machine.Requirement;

public class FirstFit extends VMAllocationAlgorithm {
	
	public FirstFit() {
	}
	@Override
	public Node getBestNode(String vmName,String imageName,Discover discoveryPhase, Requirement requirement) throws LibvirtException {
		for(Node node : discoveryPhase.nodes.values()){
			if(node.canSatisfyRequirement(vmName,imageName,requirement))
				return node;
		}
		return null;
	}
}
