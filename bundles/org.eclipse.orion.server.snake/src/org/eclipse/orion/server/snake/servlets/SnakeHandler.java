package org.eclipse.orion.server.snake.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.orion.internal.server.servlets.ServletResourceHandler;
import org.eclipse.orion.server.core.LogHelper;
import org.eclipse.orion.server.core.OrionConfiguration;
import org.eclipse.orion.server.core.ServerStatus;
import org.eclipse.orion.server.core.metastore.IMetaStore;
import org.eclipse.orion.server.core.metastore.UserInfo;
import org.eclipse.orion.server.servlets.JsonURIUnqualificationStrategy;
import org.eclipse.orion.server.servlets.OrionServlet;
import org.json.JSONException;
import org.json.JSONObject;

public final class SnakeHandler extends ServletResourceHandler<String> {

	private final ServletResourceHandler<IStatus> statusHandler;

	SnakeHandler(ServletResourceHandler<IStatus> statusHandler) {
		this.statusHandler = statusHandler;
	}

	@Override
	public boolean handleRequest(HttpServletRequest request, HttpServletResponse response, String pathInfo)
			throws ServletException {
		try {
			switch (getMethod(request)) {
				case GET:
					return handleGet(request, response, pathInfo);
				default:
					// we don't know how to handle this request
					return false;
			}
		} catch (Exception e) {
			ServerStatus status = new ServerStatus(IStatus.ERROR, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to handle request!", e);
			LogHelper.log(status);
			return statusHandler.handleRequest(request, response, status);
		}
	}

	private boolean handleGet(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException, JSONException, CoreException {
		String[] infoParts = pathInfo.split("\\/", 2);
		if (infoParts.length < 2) {
			return false;
		}

		String pathString = infoParts[1];
		if (request.getContextPath().length() != 0) {
			IPath path = pathString == null ? Path.EMPTY : new Path(pathString);
			IPath contextPath = new Path(request.getContextPath());
			if (contextPath.isPrefixOf(path)) {
				pathString = path.removeFirstSegments(contextPath.segmentCount()).toString();
			}
		}

		if (infoParts[1].equals("projects")) {
			Map<String, String> projects = new HashMap<String, String>();
			String userId = request.getRemoteUser();
			IMetaStore metaStore = OrionConfiguration.getMetaStore();
			UserInfo userInfo = metaStore.readUser(userId);
			// we're assuming there's only one workspace for now
			for (String workspaceId : userInfo.getWorkspaceIds()) {
				for (String projectName : metaStore.readWorkspace(workspaceId).getProjectNames()) {
					projects.put(projectName, metaStore.readProject(workspaceId, projectName).getContentLocation().toURL().getPath());
				}
			}
			OrionServlet.writeJSONResponse(request, response, new JSONObject(projects), JsonURIUnqualificationStrategy.ALL);
			response.addHeader("Access-Control-Allow-Origin", "*");
			response.addHeader("Access-Control-Allow-Methods", "POST");
			return true;
		}

		return false;
	}
}
