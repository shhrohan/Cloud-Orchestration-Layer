package com.cloud.allocation.algroithm;

import org.libvirt.LibvirtException;

import com.cloud.virtual.machine.Discover;
import com.cloud.virtual.machine.Node;
import com.cloud.virtual.machine.Requirement;

public class RoundRobin extends VMAllocationAlgorithm {
	
	public Integer startIndex;
	
	public RoundRobin(Integer index) {
		this.startIndex = index;
	}
	
	@Override
	public Node getBestNode(String vmName,String imageName,Discover discoveryPhase, Requirement requirement) throws LibvirtException {
		for(int index = startIndex ; index < discoveryPhase.nodes.size() ; index++){
			Node node = discoveryPhase.nodes.get(index);
			if(node.canSatisfyRequirement(vmName,imageName,requirement)){
				this.startIndex++;
				return node;
			}
		}
		this.startIndex = 0;
		for(int index = startIndex ; index < discoveryPhase.nodes.size() ; index++){
			Node node = discoveryPhase.nodes.get(index);
			if(node.canSatisfyRequirement(vmName,imageName,requirement)){
				this.startIndex++;
				return node;
			}
		}
	
		return null;
	}
}
