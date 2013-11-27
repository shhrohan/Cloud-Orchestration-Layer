package com.cloud.virtual.machine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.libvirt.Connect;
import org.libvirt.DomainInfo;
import org.libvirt.LibvirtException;

public class Node {
	public int id;
	public String address;
	public String username ;
	public Integer availableCPUs;
	public Long availableRAM;
	public String machineName;
	public boolean supports64BitArch = false;
	public String model;
	public Connect connection;
	public Map<String,VirtualMachine> virtualMachines =  new HashMap<String,VirtualMachine>();
	public Map<Guest, Capability>  capabilityMap = new HashMap<Guest,Capability>();

	public Node(int id,String[] machineConfig) {
		this.id = id;
		this.username = machineConfig[0];
		this.address  = machineConfig[1];
	}

	public void setCapability(String capabilityXMLString) throws JDOMException, IOException{

		SAXBuilder builder = new SAXBuilder();
		InputStream stream = new ByteArrayInputStream(capabilityXMLString.getBytes("UTF-8"));
		Document document = builder.build(stream);
		
		String hostArch = document.getRootElement()
									.getChild("host")
									 .getChild("cpu")
									  .getChild("arch")
									   .getText();
		if(hostArch.equals("x_86_64")){
			this.supports64BitArch = true;
		}
		
		for(Element guest :  document.getRootElement().getChildren("guest")){
			
			String OSType = guest.getChildren("os_type").get(0).getText();
			Element architecture  = guest.getChildren("arch").get(0);
			Capability capability = new Capability();
			capability.wordSize = architecture.getChildren("wordsize").get(0).getText();
			capability.emulator = architecture.getChildren("emulator").get(0).getText();

			for(Element domain : architecture.getChildren("domain")){
				if(domain.getChildren("emulator").size() >  0)
					capability.domain.put(new Domain(domain.getAttributeValue("type")), new Emulator(domain.getChildren("emulator").get(0).getText()));
				else
					capability.domain.put(new Domain(domain.getAttributeValue("type")), null);
			}
			capabilityMap.put(new Guest(new Architecture(architecture.getAttributeValue("name")),OSType), capability);
		}
	}

	public void printConfiguration(int nodeIndex){
		System.out.println("Node "+nodeIndex+" Info : ");
		System.out.println("\tMachine Name   : "+ this.machineName);
		System.out.println("\tLogged in as   : "+ this.username);
		System.out.println("\tModel          : "+ this.model);
		System.out.println("\tvCPUs          : "+ this.availableCPUs);
		System.out.println("\tRAM            : "+ this.availableRAM+" kb");
		System.out.println("\tAddress        : "+ this.address);
	}

	public String bootVirtualMachine(String vmName) throws LibvirtException{
		this.availableCPUs -= this.virtualMachines.get(vmName).domain.getInfo().nrVirtCpu;
		this.availableRAM  -= this.virtualMachines.get(vmName).domain.getInfo().memory;
		
		Integer vmID = this.virtualMachines.get(vmName).boot();
		
		if(vmID == 0)
			return null;
		
		String id = this.id +"-"+vmID.toString(); 
		return   id;
	}

	public boolean createVirtualMachine(String vmName,Requirement requirement,String image) {
		this.virtualMachines.put(vmName,new VirtualMachine(this, vmName, requirement,image));
		return true;
	}

	public Integer shutdownVirtualMachine(String vmName) throws LibvirtException{
		DomainInfo domainInfo = this.virtualMachines.get(vmName).domain.getInfo();
		this.availableRAM += domainInfo.memory;
		this.availableCPUs += domainInfo.nrVirtCpu;
		Integer status = this.virtualMachines.get(vmName).shutdown();
		this.virtualMachines.remove(vmName);
		
		return status;
	}

	public boolean canSatisfyRequirement(String vmName,String imageName,Requirement requirement) throws LibvirtException{
		if(this.availableCPUs >= requirement.CPUs && this.availableRAM >= requirement.RAM){
			int[] runningVMs = this.connection.listDomains();
			for(int id : runningVMs){
				if(vmName.equals(this.connection.domainLookupByID(id).getName()))
					return false;
			}
			String[] definedVMs = this.connection.listDefinedDomains();
			for(String name :definedVMs){
				if(vmName.equals(name))
					return false;
			}
			
			if(imageName.contains("64") && this.supports64BitArch == false){
				return false;
			}
			
			return true;	
		}
		return false;
	}
}	
