package com.cloud.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.libvirt.LibvirtException;

import com.cloud.storage.VolumeManager;
import com.jcraft.jsch.JSchException;

public class VolumeAttachHandler extends AbstractHandler {

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request,	HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("application/json;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		String vmid = baseRequest.getParameter("vmid");
		String volumeid=baseRequest.getParameter("volumeid");
		String ret;
		try {
			ret = VolumeManager.attachVolume(vmid, volumeid);
			response.getWriter().write(ret);
		} 
		catch (NumberFormatException | JSchException | InterruptedException | LibvirtException e) {
			e.printStackTrace();
		}
	}
}


