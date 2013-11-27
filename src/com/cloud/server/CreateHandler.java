package com.cloud.server;
import com.cloud.virtual.machine.VirtualMachineController;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONException;
import org.libvirt.LibvirtException;

public class CreateHandler extends AbstractHandler {

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request,	HttpServletResponse response) throws IOException, ServletException {

		response.setContentType("application/json;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		String name=baseRequest.getParameter("name");
		String type=baseRequest.getParameter("instance_type");
		String imageID=baseRequest.getParameter("image_id");
		String ret;
		try {
			ret = VirtualMachineController.spawnNewVirtualMachine(name, Integer.valueOf(type), Integer.valueOf(imageID));
			if(ret != null){
				response.getWriter().println("successF("+ret+");");
			}
			else{

				org.json.JSONObject obj = new org.json.JSONObject();
				obj.put("vmid", 0);
				response.getWriter().println("successF("+obj+");");
			}
		} 
		catch (NumberFormatException | LibvirtException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
