package com.cloud.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.libvirt.LibvirtException;

import com.cloud.virtual.machine.VirtualMachineController;
import com.jcraft.jsch.JSchException;

public class DestroyHandler extends AbstractHandler{

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request,	HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("application/json;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		String vmID=baseRequest.getParameter("vmid");
		try {
			String ret = VirtualMachineController.deleteVirtualMachine(vmID);
			response.getWriter().write("successD("+ret+");");
		} 
		catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (LibvirtException e) {
			e.printStackTrace();
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
