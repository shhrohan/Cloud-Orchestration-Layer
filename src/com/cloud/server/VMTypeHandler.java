package com.cloud.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.cloud.virtual.machine.VirtualMachineController;

public class VMTypeHandler extends AbstractHandler{

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request,	HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("application/json;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		
		try {
			String ret = VirtualMachineController.showVMTypes();
			response.getWriter().write(ret);
		} 
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
}
