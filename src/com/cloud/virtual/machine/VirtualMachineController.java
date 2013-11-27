package com.cloud.virtual.machine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.libvirt.LibvirtException;

import com.cloud.allocation.algroithm.FirstFit;
import com.cloud.allocation.algroithm.RoundRobin;
import com.cloud.allocation.algroithm.VMAllocationAlgorithm;
import com.cloud.storage.RBD;
import com.cloud.storage.VolumeManager;
import com.jcraft.jsch.JSchException;

public class VirtualMachineController {
	public static Discover discoveryPhase;
	public static VMAllocationAlgorithm algorithm;
	public static Map<Integer,Requirement> requirementTypes = new HashMap<Integer,Requirement>();
	protected static Map<Integer,String> imagesNames = new HashMap<Integer,String>();
	public static Map<String,Pair> vmMappings = new HashMap<String,Pair>();
	public static int currentNodexIndex = 0; 

	public static void setAllocationAlgorithm(int algo){
		switch (algo) {
		case 1:
			algorithm = new FirstFit();
			currentNodexIndex = 0;
			break;
		case 2:
			algorithm = new RoundRobin(currentNodexIndex);
			break;
		default:
			algorithm = new FirstFit();
		}
	}

	public static String spawnNewVirtualMachine(String vmName,int tid,int imageIndex) throws LibvirtException  {
		Utilities.printInfo("Request received to create VM.. Name = "+vmName);
		String image = imagesNames.get(imageIndex);
		Requirement requirement = requirementTypes.get(tid);
		Node node = algorithm.getBestNode(vmName,imagesNames.get(imageIndex),discoveryPhase,requirement);

		if (node == null){
			Utilities.printInfo("Not able to accomodate this type of virtual machine on any Node. Please change configuration.");
			return null;
		}

		Utilities.printInfo("VM to be created on " + node.machineName +" (" + node.address+")");
		node.createVirtualMachine(vmName,requirement,image);

		try {
			String vmID = node.bootVirtualMachine(vmName);
			if(vmID != null){
				Utilities.printSuccess("VM (id:" + vmID +") created successfully !!!");
				vmMappings.put(vmID, new Pair(vmName,node));
				vmID = "{\"vmid\":\""+vmID+"\"}";
				return vmID;
			}
			else{
				Utilities.printError("Unable to create VM");
				node.virtualMachines.remove(vmName);
				return null;
			}
		} 
		catch (LibvirtException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String deleteVirtualMachine(String vmID) throws LibvirtException, JSchException, InterruptedException, IOException{
		Utilities.printInfo("Request to destory VM (id= "+vmID+") received...");
		for(String id : vmMappings.keySet()){
			if(id.equals(vmID)){
					for(RBD rbd: VolumeManager.RBDVolumeMap.values()){
						if(rbd.vmid.equals(id) && rbd.isAttached==true){
							VolumeManager.detachVolume(rbd.id);
						}
					}
				Integer status = vmMappings.get(vmID).node.shutdownVirtualMachine(vmMappings.get(vmID).vmName);
				vmMappings.remove(vmID);
				Utilities.printSuccess("Vm id="+vmID+" destroyed...");
				return "{\"status\":"+status.toString()+"}";	
			}
		}
		Utilities.printInfo("Either VM with id="+vmID+" is not up and running or does not exist");
		return "{\"status\":\"0\"}";
	}

	public static String queryVirtualMachine(String vmID){
		Utilities.printInfo("Request to query VM (id= "+vmID+") received...");

		if(vmMappings.containsKey(vmID)){
			Integer pmid = vmMappings.get(vmID).node.id;
			String name = vmMappings.get(vmID).vmName;
			Integer type = vmMappings.get(vmID).node.virtualMachines.get(name).type;
			Utilities.printSuccess("VM with id("+vmID+") found...");
			return "{\"vmid\":\""+vmID +"\" ,\"name\":\""+name + "\" ,\"instance_type\":" + type +" ,\"pmid\":"+pmid+"}";
		}
		Utilities.printInfo("VM with id="+vmID+" does not exist");
		return "{\"status\":\"0\"}";
	}

	public static String showVMTypes(){
		Utilities.printInfo("Request to show VM types received...");
		StringBuilder vmTypes =  new StringBuilder();
		String sep=",";
		vmTypes.append("{\"types\": [");
		for(Entry<Integer,Requirement> type : requirementTypes.entrySet()){
			int tid = type.getValue().tid;
			int ram = (int)(type.getValue().RAM/1024);
			int cpu = type.getValue().CPUs;
			int disk = type.getValue().disk;
			vmTypes.append("{\"tid\":"+tid +" ,\"ram\":"+ram + " ,\"cpu\":" + cpu +" ,\"disk\":"+disk+"}"+sep);
		}
		vmTypes.deleteCharAt(vmTypes.length()-1);
		vmTypes.append("]}");
		return vmTypes.toString();
	}

	public static String showImageTypes(){
		Utilities.printInfo("Request to show Image types received...");
		StringBuilder imageTypes =  new StringBuilder();
		String sep=",";
		imageTypes.append("{\"images\": [");
		for(Entry<Integer,String> imageName : imagesNames.entrySet()){
			int id = imageName.getKey();
			String image = imageName.getValue();
			imageTypes.append("{\"id\":"+id +" ,\"name\":\""+image+ "\"}"+sep);
		}
		imageTypes.deleteCharAt(imageTypes.length()-1);
		imageTypes.append("]}");
		return imageTypes.toString();
	}
}
