package com.cloud.storage;
import java.io.BufferedReader; 
import java.io.File;
import java.io.FileReader;
import java.io.IOException; 
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.libvirt.LibvirtException;

import com.cloud.virtual.machine.Node;
import com.cloud.virtual.machine.Pair;
import com.cloud.virtual.machine.Utilities;
import com.cloud.virtual.machine.VirtualMachineController;
import com.jcraft.jsch.*;

public class VolumeManager {

	static public String cephClusterConfFile = "/etc/ceph/ceph.conf";
	static public String pool = "rbd";
	static public Monitor monitor =  new Monitor();
	static public  String username="root";
	static public Map<Integer,RBD> RBDVolumeMap  = new HashMap<Integer,RBD>(); 

	public static Monitor setMonitorFromConf() throws IOException{
		BufferedReader confReader = new BufferedReader(new FileReader(new File(cephClusterConfFile)));
		String line;
		while((line = confReader.readLine()) != null){
			if(line.startsWith("mon_initial_members = ")){
				monitor.hostName = line.split("=")[1].trim();
				line = confReader.readLine();
				monitor.ipAddress = line.split("=")[1].trim();
				confReader.close();
				break;
			}
		}
		if(monitor.hostName.equals("")){
			Utilities.printError("No Monitor found in ceph.conf. Storage commands wont be supported.");
			monitor = null;
		}
		return monitor;
	}
	public static void initializeRBDMap() throws IOException, InterruptedException {

		String listCommand = "sudo rbd  ls"+" -m " + monitor.hostName + " -k /etc/ceph/ceph.client.admin.keyring" ;
		Utilities.printInfo("Command to run : "+listCommand);
		Runtime rt = Runtime.getRuntime();
        java.lang.Process p = rt.exec (listCommand);
        p.waitFor();
        InputStream is = p.getInputStream();
        BufferedReader reader = new BufferedReader (new InputStreamReader (is));
        String s = null;
        StringBuilder output = new StringBuilder();

        while ((s = reader.readLine()) != null) 
            output.append(s+"\n");
        is.close();

        if(p.exitValue() == 0){				
			if(output.toString().equals("") == false){
				Utilities.printInfo("Found pre-existing rbd volumes with names ");
				String [] preExistingVolumes = output.toString().split("\n");
				for(int i = 0 ; i < preExistingVolumes.length ; i++){
					System.out.print(preExistingVolumes[i]+",");
					RBDVolumeMap.put(i, new RBD(i, preExistingVolumes[i],"0"));
				}
				System.out.println();
			}
		}
	}

	public static String destroyVolume(Integer id) throws JSchException, IOException, InterruptedException{

		Utilities.printInfo("Request to destroy volume with id = "+id+" received");

		if(RBDVolumeMap.containsKey(id)){
			Utilities.printInfo("Volume present...");
			RBD RBDVolume = RBDVolumeMap.get(id);
			
			if(RBDVolume.isAttached ==false){
				String removeCommand = "sudo rbd rm "+RBDVolume.name+" -m " + monitor.hostName + " -k /etc/ceph/ceph.client.admin.keyring" ;
				Utilities.printInfo("Command to run : "+removeCommand);
				Runtime rt = Runtime.getRuntime();
		        java.lang.Process p = rt.exec (removeCommand);
		        p.waitFor();
				
				if(p.exitValue() == 0){				
					RBDVolumeMap.remove(id);
					Utilities.printSuccess("Volume destroyed..");
					return "{\"status\":"+1+"}";	
				}
			}
			Utilities.printError("Volume attached to VM id="+RBDVolume.vmid +" .. Can not destroy..");
			return "{\"status\":"+0+"}";	
		}
		Utilities.printError("Volume not present..");
		return "{\"status\":"+0+"}";
	}

	public static String createVolume( String volumeName, String size)	throws JSchException, IOException, InterruptedException {

		Utilities.printInfo("Request to create volume recieved..");
		for(Entry<Integer, RBD> entry : RBDVolumeMap.entrySet()){
			String name =entry.getValue().name; 
			if(name.equals(volumeName)){
				Utilities.printError("Volume named \""+volumeName+"\" already exist");
				return "{\"volumeid\":"+0+"}";
			}
		}
		Utilities.printInfo("creating volume \""+volumeName+"\"");

		
		String command = "sudo rbd create "+volumeName+ " --size "+ Integer.parseInt(size)*1024 +" -m " + monitor.hostName + " -k /etc/ceph/ceph.client.admin.keyring" ;
		Runtime rt = Runtime.getRuntime();
        java.lang.Process p = rt.exec (command);
        p.waitFor();
		
		if(p.exitValue() == 0){
			Integer id = RBDVolumeMap.size();
			RBDVolumeMap.put(id+1, new RBD(id,volumeName, size));
			Utilities.printSuccess("Created.. Volume id : "+""+(id+1));
			return "{\"volumeid\":"+""+(id+1)+"}";
		}
		Utilities.printError("Not able to create volume. Please change configuration");
		return "{\"volumeid\":"+0+"}";
	}

	public static String attachVolume(String vmid,String volumeid) throws LibvirtException, JSchException, InterruptedException, IOException{

		Utilities.printInfo("Request to atttach volume (id="+volumeid+") on vm(id="+vmid+") received..");
		// check if vm is up and running.
		Boolean canGoAhead = false;
		int libvirt_vm_id = Integer.parseInt(vmid.split("-")[1]);
		Pair mappingPair = VirtualMachineController.vmMappings.get(vmid);

		if(mappingPair == null){
			Utilities.printInfo("VM (id="+vmid+") does not exesit. Attach operation aborted.");
			return "{\"status\":"+0+"}";	
		}

		Node node = mappingPair.node;

		int[] runningVMs = node.connection.listDomains();
		for(int id : runningVMs){
			if(libvirt_vm_id == id){
				canGoAhead = true;
				Utilities.printInfo("VM with id="+vmid+" up an running. checking for volume presence.. ");
				break;
			}
			else{
				Utilities.printInfo("VM with id="+vmid+" id not running. Attach operation aborted ");
				canGoAhead = false;
			}
		}

		// check if volume exists
		if(canGoAhead == true){
			if(RBDVolumeMap.containsKey(Integer.parseInt(volumeid)) && RBDVolumeMap.get(Integer.parseInt(volumeid)).isAttached == false){
				Utilities.printInfo("Volume id="+volumeid+" available... ");
				canGoAhead = true;
			}
			else{
				canGoAhead = false;
				Utilities.printError("Volume id="+volumeid+" already attached to vm (id="+RBDVolumeMap.get(Integer.parseInt(volumeid)).vmid+"). Can not attach to another VM simultanously.");
			}
		}

		if(canGoAhead == true){
			Utilities.printInfo("Prerequisite met... Trying to attach volume...");
			RBD RBDVolume = RBDVolumeMap.get(Integer.parseInt(volumeid));
			RBDVolume.handle =  node.virtualMachines.get(mappingPair.vmName).nextHandle();

			String secretUUID  = node.connection.listSecrets()[0];

			char handle = (char)RBDVolume.handle;
			
			String device =
					"<disk type='network' device='disk'>"+
							"<source protocol='rbd' name='"+pool+"/"+RBDVolume.name+"'>"+
							"<host name='"+monitor.hostName+"' port='6789'/>"+
							"</source>"+
							"<auth username='admin'>"+
							"<secret type='ceph' uuid='"+secretUUID+"'/>"+
							"</auth>"+
							"<target dev='sd"+handle+"' bus='scsi'/>"+
							"</disk>";

			node.virtualMachines.get(mappingPair.vmName).domain.attachDevice(device);
			node.virtualMachines.get(mappingPair.vmName).handle.put(RBDVolume.handle, false);
			RBDVolume.isAttached = true;
			RBDVolume.vmid = vmid;
			Utilities.printSuccess("Volume with (id="+volumeid+") sucessfully attached on vm(id="+vmid+")..");
			return "{\"status\":"+1+"}";	

			//			String modProbeCommand = "sudo modeprobe rbd";
			//			Utilities.printInfo("Command to run : "+modProbeCommand);
			//			Utilities.executeCommand(username,node.address,modProbeCommand);
			//
			//			//sudo rbd map foo --pool rbd --name client.admin [-m {mon-IP}] [-k /path/to/ceph.client.admin.keyring]
			//			String mapCommand  = "sudo rbd map "+ RBDVolume.name +
			//					" --pool rbd "+
			//					"--name client.admin";
			//			Utilities.printInfo("Command to run : "+mapCommand);
			//			if(Utilities.executeCommand(username,node.address,mapCommand))
			//			{
			//				Integer handleIndex =  node.virtualMachines.get(mappingPair.vmName).volumeHandleIndex;
			//				String attachCommand = "sudo virsh attach-disk " + 
			//						mappingPair.vmName + " --source /dev/rbd/rbd/" +
			//						RBDVolume.name + " --target sd"+ handleIndex.toString() +" --persistent";
			//				Utilities.printInfo("Command to run : "+attachCommand);
			//
			//				if (Utilities.executeCommand(username,node.address,attachCommand) == true){
			//					RBDVolume.isAttached = true;
			//					RBDVolume.vmid = vmid;
			//					RBDVolume.requiredUnmaps++;
			//					Utilities.printSuccess("Volume with (id="+volumeid+" ) sucesfully attached on vm(id="+vmid+")..");
			//					return "{\"status\":"+1+"}";	
			//				}
			//			}
		}
		return "{\"status\":"+0+"}";
	}

	public static String detachVolume(Integer volumeid) throws JSchException, InterruptedException, IOException, LibvirtException{
		Boolean canGoAhead;
		Utilities.printInfo("Request to detach volume with id="+volumeid+" recieved...");
		if(RBDVolumeMap.containsKey(volumeid) && RBDVolumeMap.get(volumeid).isAttached == true){
			Utilities.printSuccess("Volume id="+volumeid+" found attached on vmid="+RBDVolumeMap.get(volumeid).vmid+".. Trying to detach now..");
			canGoAhead = true;
		}
		else{
			Utilities.printInfo("Volume id="+volumeid+" not found attached to any vm. Detach operation aborted..");
			canGoAhead = false;
		}

		if(canGoAhead == true){
			RBD RBDVolume = RBDVolumeMap.get(volumeid);

			String vmName = VirtualMachineController.vmMappings.get(RBDVolume.vmid).vmName;
			Node node  = VirtualMachineController.vmMappings.get(RBDVolume.vmid).node;
			String secretUUID  = node.connection.listSecrets()[0];
			char handle = (char)RBDVolume.handle;
			String device =
					"<disk type='network' device='disk'>"+
							"<source protocol='rbd' name='"+pool+"/"+RBDVolume.name+"'>"+
							"<host name='"+monitor.hostName+"' port='6789'/>"+
							"</source>"+
							"<auth username='admin'>"+
							"<secret type='ceph' uuid='"+secretUUID+"'/>"+
							"</auth>"+
							"<target dev='sd"+handle+"' bus='scsi'/>"+
							"</disk>";

			node.virtualMachines.get(vmName).domain.detachDevice(device);
			Utilities.printSuccess("Volume with (id="+volumeid+" ) sucessfully detached from vm(id="+RBDVolume.vmid+")..");
			node.virtualMachines.get(vmName).handle.put(RBDVolume.handle, true);
			RBDVolume.isAttached = false;
			return "{\"status\":"+1+"}";	

			/*	String detachCommand = "sudo virsh detach-disk "+vmName+" sd"+node.virtualMachines.get(vmName).volumeHandleIndex.toString();
			Utilities.printInfo("Command to run : " + detachCommand);
			if (Utilities.executeCommand(username,node.address,detachCommand) == true){
				node.virtualMachines.get(vmName).volumeHandleIndex++;
				return "{\"status\":"+1+"}";	
			}		*/	
		}
		return "{\"status\":"+0+"}";	
	}

	public static String queryVolume(String volumeid){
		if(!RBDVolumeMap.containsKey(Integer.parseInt(volumeid))){
			//“error “: “volumeid : xxxx does not exist”
			return "{\"error\":\"volumeid : "+volumeid+" does not exist"+"\"}";	
		}
		RBD RBDVolume = RBDVolumeMap.get(Integer.parseInt(volumeid));
		if(RBDVolume.isAttached == true) {
			String volumeIdPart  = "\"volumeid\" : "+volumeid;
			String namePart  = "\"name\" : \""+RBDVolume.name+"\"";
			String sizePart  = "\"size\" : "+RBDVolume.size;
			String statusPart  = "\"status\":\"attached\"";
			String vmid = "\"vmid\":"+"\""+ RBDVolume.vmid+"\"" ;
			return "{"+volumeIdPart+","+namePart+","+sizePart+","+statusPart+","+vmid+"}";
		}
		else{
			String volumeIdPart  = "\"volumeid\":"+ volumeid;
			String namePart  = "\"name\" : \""+RBDVolume.name+"\"";
			String sizePart  = "\"size\" : "+RBDVolume.size;
			String statusPart  = "\"status\":\"available\"";
			String vmid = "\"vmid\":\"-1\"";
			return "{"+volumeIdPart+","+namePart+","+sizePart+","+statusPart+","+vmid+"}";
		}
	}
}