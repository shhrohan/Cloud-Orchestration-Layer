package com.cloud.virtual.machine;

import java.io.IOException;

import org.libvirt.LibvirtException;

import com.cloud.server.RestServer;
import com.cloud.storage.VolumeManager;
import com.jcraft.jsch.JSchException;

public class Main {
	public static void main(String[] configFiles) throws LibvirtException, IOException, JSchException, InterruptedException{
		/*int type = 2;
		String vmName = "vm";
		int imagerIndex = 0;*/

		VirtualMachineController.discoveryPhase = new Discover(configFiles);
		if(VirtualMachineController.discoveryPhase.instantiateConnections() == true){
			VirtualMachineController.setAllocationAlgorithm(2);	
			VolumeManager.setMonitorFromConf();
			if(VolumeManager.monitor != null)
				VolumeManager.initializeRBDMap();
			RestServer.start(null);
		}
		
/*		if(VirtualMachineController.discoveryPhase.instantiateConnections()){
			String vmID = VirtualMachineController.spawnNewVirtualMachine(vmName,type,imagerIndex);
			if(vmID != null){
				String queryOutput = VirtualMachineController.queryVirtualMachine(vmID);
				System.out.println(queryOutput);
				int status = VirtualMachineController.deleteVirtualMachine(vmID);
					if(status == 1){
						VirtualMachineController.vmMappings.remove(vmID);
						Utilities.printSuccess("VM (id="+vmID+") deleted succesfully !!!");
					}
			}
		}
		
		System.out.println(VirtualMachineController.showImageTypes());
*/		
	}
}
