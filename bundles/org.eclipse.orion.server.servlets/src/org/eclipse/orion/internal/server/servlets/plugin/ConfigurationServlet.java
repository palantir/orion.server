package org.eclipse.orion.internal.server.servlets.plugin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.orion.internal.server.servlets.Activator;
import org.eclipse.orion.server.core.LogHelper;
import org.eclipse.orion.server.core.PreferenceHelper;
import org.eclipse.orion.server.servlets.OrionServlet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet that returns server configuration.
 *
 * For security reasons, only configuration keys starting with "plugin." will be exposed by
 * this servlet.
 * TODO: Add a whitelist of non-plugin-prefixed configuration keys which may be accessed.
 */
public class ConfigurationServlet extends OrionServlet {

	private static final long serialVersionUID = -3989265174924783964L;

	private static final String PLUGIN_PREFIX = "plugin.";

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		traceRequest(req);

		JSONObject requestObject;
		try {
			requestObject = OrionServlet.readJSONRequest(req);
		} catch (JSONException e) {
			handleException(e, resp);
			return;
		}
		JSONObject result = new JSONObject();
		JSONArray keys;
		try {
			keys = requestObject.getJSONArray("configKeys");
		} catch (JSONException e) {
			handleException(e, resp);
			return;
		}
		for (int i = 0; i < keys.length(); i++) {
			String key;
			try {
				key = keys.getString(i);
			} catch (JSONException e) {
				handleException(e, resp);
				return;
			}
			if (key.indexOf(PLUGIN_PREFIX) == 0) {
				try {
					result.put(key, PreferenceHelper.getString(key, ""));
				} catch (JSONException e) {
					handleException(e, resp);
					return;
				}
			} else {
				handleException(new RuntimeException("Invalid key: " + key), resp);
				return;
			}
		}
		writeJSONResponse(req, resp, result, null);
	}

	private void handleException(Exception e, HttpServletResponse resp) throws ServletException {
		LogHelper.log(e);
		handleException(resp, new Status(IStatus.ERROR, Activator.PI_SERVER_SERVLETS,
				e.getMessage()), HttpServletResponse.SC_BAD_REQUEST);
	}
}
