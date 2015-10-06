/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package org.eclipse.orion.server.environment;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.orion.server.core.OrionConfiguration;
import org.eclipse.orion.server.core.PreferenceHelper;
import org.eclipse.orion.server.core.metastore.ProjectInfo;
import org.eclipse.orion.server.servlets.OrionServlet;

/**
 * Servlet to handle all execution requests.
 * @author mwlodarczyk
 *
 */

public class ExecutionServlet extends OrionServlet {

	private static final long serialVersionUID = 5345085797302519095L;
	
	static final String CONFIG_FILE_NAME = "orion.conf"; //$NON-NLS-1$
	static final String COMMAND = "command"; //$NON-NLS-1$
	static final String CONFIG_EXECUTION_ENABLED = "plugin.execution.enabled"; //$NON-NLS-1$

	
	@Override	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		traceRequest(req);
		if(!"true".equals(PreferenceHelper.getString(CONFIG_EXECUTION_ENABLED, ""))) { //$NON-NLS-1$
			displayError(req, resp, "Execution environment is disabled.");
			return;
		}
		
		String commandType = req.getParameter(COMMAND);
		String userID = req.getRemoteUser();
		String pathInfo = req.getPathInfo() == null ? "" : req.getPathInfo(); //$NON-NLS-1$
		IPath path = new Path(pathInfo);
		IFileStore configFile = null, myFile = null;
		
		// the path's form is /workspaceName/projectName[/filePath]&command=(...)
		try {
			IFileStore project = getProjectStore(path);
			configFile = project.getChild(CONFIG_FILE_NAME);
			myFile = project.getFileStore(path.removeFirstSegments(2));
		}
		catch (Exception e) {
			e.printStackTrace();
			displayError(req, resp, e.getMessage());
			return;
		}
		
		// get the project configuration
		ExecutionConfiguration config = new ExecutionConfiguration();
		try {
			config = new ExecutionConfiguration(configFile);
			resp.getWriter().println("Configuration file loaded.\n");
		} catch (Exception e) {
			resp.getWriter().println(e.getMessage() + "\nUsing default configuration.\n");
		}
		
		// execute and print
		List<String> out = ShellEnvironmentRegistry.getEnvironmentForUser(userID).execute(commandType, myFile, config);		
		for(String line: out) {
			resp.getWriter().println(line);
		}
	}
	
	/**
	 * Retrieves file store from the path.
	 * @param path Taken from the GET request.
	 * @return File store object pointing to a file in an existing project. 
	 * @throws CoreException
	 * @throws InvalidPathException
	 */
	private IFileStore getProjectStore(IPath path) throws CoreException, InvalidPathException {
		String wrongProjectMsg = "Path should point inside a project"; //$NON-NLS-1$
		if (path.segmentCount() <= 1) {
			throw new InvalidPathException(path.toString(), wrongProjectMsg);
		} else {
			ProjectInfo projectInfo = OrionConfiguration.getMetaStore().readProject(path.segment(0), path.segment(1));			
			if(projectInfo == null) {
				throw new InvalidPathException(path.toString(), wrongProjectMsg);
			} else {	
				return projectInfo.getProjectStore();
			}
		} 
	}
	
	private void displayError(HttpServletRequest req, HttpServletResponse resp, String msg) throws IOException {
		resp.getWriter().println(msg);
		resp.flushBuffer();
	}
	
	public ExecutionServlet() {
		super();
	}
}