package org.eclipse.orion.internal.server.servlets.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.orion.server.environment.ShellEnvironment;
import org.eclipse.orion.server.environment.ShellEnvironmentRegistry;
import org.eclipse.orion.server.servlets.OrionServlet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class ExeServlet extends OrionServlet {
	
	private ShellEnvironmentRegistry getEnvRegistry() throws IllegalStateException {
		BundleContext ctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
		if (ctx == null) {
			throw new IllegalStateException("Plug-in context non available");
		}

		ServiceReference envServiceReference;
		envServiceReference = ctx.getServiceReference(ShellEnvironmentRegistry.class.getName());
		if (envServiceReference == null) {
			throw new IllegalStateException("Plug-in for execution environment is not running");
		}

		return (ShellEnvironmentRegistry) ctx.getService(envServiceReference);
	}
	
	@Override	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String commandType = req.getParameter("command"), userID = req.getRemoteUser();
		resp.getWriter().println(req.getPathInfo());
		resp.getWriter().println("command = " + (commandType == null ? "null" : commandType));
		resp.getWriter().println("user = " + userID);
		resp.getWriter().println("");
		
		ShellEnvironment shellEnv;
		try {
			shellEnv = getEnvRegistry().getEnvironmentForUser(userID);
		} catch (IllegalStateException e) {
			resp.getWriter().println("Exception while accessing the execution environment:");
			resp.getWriter().println(e.getMessage());
			return;
		}
		
		List<String> out = new ArrayList<String>();
		if("cancel".equals(commandType)) {
			try {
				out.add("Process terminated with the exit status " + shellEnv.cancel());
			} catch (InterruptedException e) {
				out.add("Exception while killing the process:");
				out.add(e.getMessage());
			}
		} else {
			IPath path = req.getPathInfo() == null ? Path.ROOT : new Path(req.getPathInfo());
			IFileStore ifile = NewFileServlet.getFileStore(req, path);
			if (ifile == null) {
				resp.getWriter().println("Cannot access requested file");
				return;
			}
			out.addAll(shellEnv.execute(commandType, ifile));
		}
			
		for(String line: out) {
			resp.getWriter().println(line);
		}
	}
	
	public ExeServlet() {
		super();
	}
}