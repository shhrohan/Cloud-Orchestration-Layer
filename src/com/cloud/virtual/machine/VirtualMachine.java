package com.cloud.virtual.machine;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.libvirt.Domain;
import org.libvirt.LibvirtException;

public class VirtualMachine {
	protected String name;
	protected int type;
	public  Domain domain;
	protected String image;
	public Integer volumeHandleIndex;
	
	public Map<Integer,Boolean> handle = new HashMap<Integer,Boolean>();
	
	public VirtualMachine(Domain domain,String vmName){
		this.domain = domain;
		this.name = vmName;
		this.volumeHandleIndex = 3;
		
		for(int i = 'a' ; i < 'z'; i++){
			handle.put(i, true);
		}
		
	}
	public VirtualMachine(Node node,String vmName, Requirement userRequirement,String image){
		try{
			this.name = vmName;
			this.type = userRequirement.tid;
			this.image = image;
			this.volumeHandleIndex = 3;
			String domainXmlString = Utilities.getDomainXMLString(UUID.randomUUID().toString(),this.name,node,userRequirement,this.image);
			this.domain = node.connection.domainDefineXML(domainXmlString);
			for(int i = 'a' ; i < 'z'; i++){
				handle.put(i, true);
			}
			
		}
		catch (LibvirtException LE){
			Utilities.printError(LE.getMessage());
		}

		catch (ParserConfigurationException PCE){
			Utilities.printError(PCE.getMessage());
		}
		catch (TransformerException TE){
			Utilities.printError(TE.getMessage());
		}
	}

	public int boot(){
		try{
			this.domain.create();
			return this.domain.getID();
		}
		catch(LibvirtException LE){
			Utilities.printError(LE.getMessage());
			return 0;
		}
	}
	public int shutdown(){
		try {
			this.domain.destroy();
			this.domain.undefine();
			return 1;
		} catch (LibvirtException e) {
			Utilities.printError(e.getMessage());
			return 0;
		}
	}
	
	public String getStatus(){
		return null;
	}

	public Integer nextHandle(){
		int c = 'a';
		while(c!='z'){
			if(handle.get(c) == true){
				return c;
			}
			c++;
		}
		return null;
	}
	
}
