package com.cloud.virtual.machine;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.libvirt.LibvirtException;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class Utilities {
	public static void printInfo(String message){
		System.out.println("[INFO]    : "+ message);
	}
	public static void printError(String error){
		System.out.println("[Error]   : "+ error);
	}
	public static void printSuccess(String successMessage){
		System.out.println("[Success] : "+ successMessage);
	}
	public static void exit(){
		System.exit(0);
	}
	public static String getDomainXMLString(String uuid,
			String vmName,
			Node node,
			Requirement userRequirement,
			String image) throws ParserConfigurationException, DOMException, LibvirtException, TransformerException{

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
		Element domainElement = doc.createElement("domain");
		doc.appendChild(domainElement);

		domainElement.setAttribute("type",node.connection.getType().toLowerCase());

		Element nameElement = doc.createElement("name");
		nameElement.appendChild(doc.createTextNode(vmName));
		domainElement.appendChild(nameElement);

		Element uuidElement = doc.createElement("uuid");
		uuidElement.appendChild(doc.createTextNode(uuid));
		domainElement.appendChild(uuidElement);

		Element memoryElement = doc.createElement("memory");
		memoryElement.appendChild(doc.createTextNode(userRequirement.RAM.toString()));
		domainElement.appendChild(memoryElement);
        
		Element currentMemoryElement = doc.createElement("currentMemory");
		currentMemoryElement.appendChild(doc.createTextNode(userRequirement.RAM.toString()));
		domainElement.appendChild(currentMemoryElement);

		Element vCPUElement = doc.createElement("vcpu");
		vCPUElement.appendChild(doc.createTextNode(userRequirement.CPUs.toString()));
		domainElement.appendChild(vCPUElement);

		Element osElement = doc.createElement("os");
		domainElement.appendChild(osElement);

		Element feature=doc.createElement("features");
		
		Element acpi =doc.createElement("acpi");
		Element apic =doc.createElement("apic");
		Element pae = doc.createElement("pae");
		 feature.appendChild(acpi);
		 feature.appendChild(apic);
		 feature.appendChild(pae);
		 domainElement.appendChild(feature);
		
		Element devicesElement = doc.createElement("devices");
		domainElement.appendChild(devicesElement);


		Element typeElement = doc.createElement("type");
		typeElement.setAttribute("arch","x86_64");
		typeElement.setAttribute("machine","pc");
		typeElement.appendChild(doc.createTextNode("hvm"));
		osElement.appendChild(typeElement);

		Element emulatorElement = doc.createElement("emulator");
		emulatorElement.appendChild(doc.createTextNode(node.capabilityMap.get(new Guest(new Architecture("x86_64"),"hvm")).emulator));
		devicesElement.appendChild(emulatorElement);
		
		/*if(image.endsWith("*.iso")){
			Element bootElement2 = doc.createElement("boot");
			bootElement2.setAttribute("dev","cdrom");
			osElement.appendChild(bootElement2);
			
			Element diskElement1 = doc.createElement("disk");
			diskElement1.setAttribute("type", "file");
			diskElement1.setAttribute("device", "cdrom");

			Element sourceElement1 = doc.createElement("source");
			sourceElement1.setAttribute("file", "/home/"+image);
			diskElement1.appendChild(sourceElement1);

			Element targetElement1 = doc.createElement("target");
			targetElement1.setAttribute("dev", "hdc");
			diskElement1.appendChild(targetElement1);

			Element readonlyElement1 = doc.createElement("readonly");
			diskElement1.appendChild(readonlyElement1);

			devicesElement.appendChild(diskElement1);
		}
		else{*/
		Element bootElement1 = doc.createElement("boot");
			bootElement1.setAttribute("dev","hd");
			osElement.appendChild(bootElement1);
			

			Element diskElement2 = doc.createElement("disk");
			diskElement2.setAttribute("type", "file");
			diskElement2.setAttribute("device", "disk");

			Element driver = doc.createElement("driver");
			driver.setAttribute("name", "qemu");
			driver.setAttribute("type", "qcow2");
			diskElement2.appendChild(driver);
			
			Element sourceElement2 = doc.createElement("source");
			sourceElement2.setAttribute("file", "/home/"+image);
			diskElement2.appendChild(sourceElement2);

			Element targetElement2 = doc.createElement("target");
			targetElement2.setAttribute("dev", "hda");
			diskElement2.appendChild(targetElement2);

			devicesElement.appendChild(diskElement2);
//		}


		Element interfaceElement = doc.createElement("interface");
		interfaceElement.setAttribute("type", "network");

		Element sourceElement3 =  doc.createElement("source");
		sourceElement3.setAttribute("network", "default");

		interfaceElement.appendChild(sourceElement3);
		devicesElement.appendChild(interfaceElement);

		Element graphicsElement = doc.createElement("graphics");
		graphicsElement.setAttribute("port", "-1");
		graphicsElement.setAttribute("type", "vnc");


		devicesElement.appendChild(graphicsElement);
		StringWriter domainXMLString = new StringWriter();

		DOMImplementation domImpl = doc.getImplementation();
		DOMImplementationLS domImplLS = (DOMImplementationLS)domImpl.getFeature("LS", "3.0");
		LSSerializer serializer = domImplLS.createLSSerializer();
		serializer.getDomConfig().setParameter("xml-declaration", Boolean.valueOf(false));
		LSOutput lsOutput = domImplLS.createLSOutput();
		lsOutput.setCharacterStream(domainXMLString);
		serializer.write(doc, lsOutput);

		return domainXMLString.toString();
	}
	public static boolean executeCommand(String username,String hostname, String command) throws JSchException, InterruptedException, IOException{
		String userHome = System.getProperty ("user.home");
		JSch jsch=new JSch();  
		Session session=jsch.getSession(username, hostname, 22);
		jsch.setKnownHosts (userHome + "/.ssh/known_hosts");
		jsch.addIdentity (userHome + "/.ssh/id_rsa");
		java.util.Properties config = new java.util.Properties(); 
		config.put ("StrictHostKeyChecking", "no");
		session.setConfig(config);
		session.connect();

		ChannelExec channelCreate = (ChannelExec) session.openChannel("exec");
		channelCreate.setCommand (command);
		channelCreate.setInputStream (null);
		((ChannelExec)channelCreate).setErrStream (System.err);

		InputStream in = channelCreate.getInputStream();
		channelCreate.connect();
		Thread.sleep(1000);

		byte [] tmp = new byte [1024];
		while (true)
		{
			while (in.available() > 0)
			{
				int i = in.read (tmp, 0, 1024);
				if (i < 0)
					break;
				Utilities.printInfo(new String (tmp, 0, i));
			}
			if (channelCreate.isClosed())
			{
				if(channelCreate.getExitStatus()==0){
					channelCreate.disconnect();
					session.disconnect();
					return true;
				}
				else{
					channelCreate.disconnect();
					session.disconnect();
					return false;
				}
			}
		}
	}
}
