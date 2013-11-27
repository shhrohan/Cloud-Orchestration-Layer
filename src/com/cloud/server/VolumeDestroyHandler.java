package com.cloud.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.cloud.storage.VolumeManager;
import com.jcraft.jsch.JSchException;

public class VolumeDestroyHandler  extends AbstractHandler {

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request,	HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("application/json;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		String volumeid=baseRequest.getParameter("volumeid");
		String ret;
		System.out.println();
		try {
			ret = VolumeManager.destroyVolume(Integer.parseInt(volumeid));
			response.getWriter().write(ret);
		} 
		catch (NumberFormatException | JSchException | InterruptedException e) {
			System.out.println(e.getMessage());
			System.out.println(e.getLocalizedMessage());
			System.out.println(e.getStackTrace().toString());
		}
	}
}


