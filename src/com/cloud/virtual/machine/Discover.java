package com.cloud.virtual.machine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.jdom2.JDOMException;
import org.libvirt.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Discover {
	protected int numberOfNodesConfigured = 0;
	protected int nodeCount = 0;
	public Map<Integer,Node> nodes = new HashMap<Integer, Node>();
	
	public Discover(String[] configFiles){
		instantiateNodes(configFiles[0]);
		instantiateImagesList(configFiles[1]);
		instantiateTypes(configFiles[2]);
		
	}
	public void instantiateImagesList(String imagefile){
		try {
			BufferedReader typeFileReader = new BufferedReader(new FileReader(imagefile));
			String line = "";
			int i=0;
			while((line = typeFileReader.readLine())!= null){
				if(line.startsWith("#") == false)
					VirtualMachineController.imagesNames.put(i, line);
				i++;
			}
			typeFileReader.close();
		}
		catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void instantiateTypes(String typeFile) {
		StringBuilder jsonStr = new StringBuilder();
		try {
			BufferedReader typeFileReader = new BufferedReader(new FileReader(typeFile));
			String line = "";
			while((line = typeFileReader.readLine())!= null){
				jsonStr.append(line);
			}
			typeFileReader.close();
		} 
		catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		try
		{
			JSONObject rootObject = new JSONObject(jsonStr.toString());
			JSONArray vmTypes = rootObject.getJSONArray("types"); 
			for(int i=0; i < vmTypes.length(); i++) { 
				JSONObject type = vmTypes.getJSONObject(i);
				VirtualMachineController.requirementTypes.put(Integer.parseInt(type.get("tid").toString()), 
							new Requirement(Integer.parseInt(type.get("tid").toString())
												,Integer.parseInt(type.get("ram").toString()),
													Integer.parseInt(type.get("disk").toString()),
														Integer.parseInt(type.get("cpu").toString())));
			}
		} 
		catch (JSONException e) {
			e.printStackTrace();
		}
	}
	public void instantiateNodes(String nodesFile) {
		try{
			BufferedReader nodeConfigReader = new BufferedReader(new FileReader(nodesFile));
			String nodeConfig =  new String();
			int i=0;
			while((nodeConfig = nodeConfigReader.readLine())!= null){
				if(nodeConfig.equals("")){
					Utilities.printInfo("Finished reading machine config file");
					break;
				}
				if(!nodeConfig.startsWith("#")){
					Node newNode = new Node(i,nodeConfig.split("[@]"));
					this.nodes.put(i++,newNode);
					this.nodeCount++;
				}
			}
			nodeConfigReader.close();
		}
		catch (IOException IOE){
			Utilities.printError(IOE.getMessage());
			Utilities.exit();
		}
	}
	public boolean instantiateConnections(){
		for(int i = 0 ; i < this.nodes.size() ; i++){  		
			try{
				Utilities.printInfo("Logging in "+ this.nodes.get(i).address + " with "+ this.nodes.get(i).username +" user...");

				ConnectAuth ca = new ConnectAuthDefault ();
				Connect connection =new Connect("qemu+tcp://"+this.nodes.get(i).username+"@"+this.nodes.get(i).address+"/system", ca, 0);  //			this.connections.add(new Connect("qemu:///system",false));
				Utilities.printInfo("Node configured...");

				this.numberOfNodesConfigured++;
				this.nodes.get(i).connection =  connection;
				this.nodes.get(i).machineName = connection.getHostName();
				this.nodes.get(i).availableCPUs = connection.nodeInfo().cpus;
				this.nodes.get(i).availableRAM = connection.nodeInfo().memory;
				this.nodes.get(i).model = connection.nodeInfo().model;
				this.nodes.get(i).setCapability(connection.getCapabilities());
				
				
				int [] preExistingDomains = connection.listDomains();	
				
				for(int domainID : preExistingDomains){
					String vmName = connection.domainLookupByID(domainID).getName();
					VirtualMachineController.vmMappings.put(i+"-"+domainID,
								new Pair(vmName, this.nodes.get(i)));
					this.nodes.get(i).virtualMachines.put(vmName,new VirtualMachine(connection.domainLookupByID(domainID), vmName));
				}
			}
			catch (LibvirtException e ){
				Utilities.printError(e.getError().getStr2());
				Utilities.printInfo("skipping "+this.nodes.get(i).address +" from setup");
				this.nodes.remove(i);
				i--;
			}
			catch (IOException IOE){

			}
			catch (JDOMException JE) {

			}
		}
		if(this.numberOfNodesConfigured == 0){
			Utilities.printError("0 nodes configured. Can not proceed");
			return false;
		}
		else{
			Utilities.printSuccess(this.numberOfNodesConfigured+"/"+this.nodeCount+" nodes configured successfully");
			Utilities.printInfo("Finished setup...");

			Scanner in = new Scanner(System.in);
			System.out.print("\n\nContinue ? (y/n) : ");
			String s = in.nextLine();
			if(!s.toLowerCase().equals("y")){
				in.close();
				Utilities.printInfo("User choose to abort. Exiting...");
				Utilities.exit();
			}
			in.close();
		}
		return true;

	}
	public void printNodes(){
		for(int i = 0 ; i < this.nodes.size() ; i++)  			
			this.nodes.get(i).printConfiguration(i);
	}
}