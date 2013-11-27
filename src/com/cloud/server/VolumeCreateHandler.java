package com.cloud.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONException;

import com.cloud.storage.VolumeManager;
import com.jcraft.jsch.JSchException;

public class VolumeCreateHandler extends AbstractHandler {

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request,	HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("application/json;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		String volumeName = baseRequest.getParameter("name");
		String size=baseRequest.getParameter("size");
		String ret;
		try {
			ret = VolumeManager.createVolume(volumeName, size);
			//response.getWriter().write(ret);
			
			if(ret != null){
				response.getWriter().println("successSC("+ret+");");
			}
			else{

				org.json.JSONObject obj = new org.json.JSONObject();
				obj.put("volumeid", 0);
				response.getWriter().println("successSC("+obj+");");
			}
		} 
		catch (NumberFormatException | JSchException | InterruptedException | JSONException e) {
			e.printStackTrace();
		}
	}
}

