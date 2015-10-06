/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package org.eclipse.orion.server.environment;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages environment objects for users. Thread-safe.
 * @author mwlodarczyk
 *
 */
public class ShellEnvironmentRegistry {

	private static Map<String, ShellEnvironment> registry = new HashMap<String, ShellEnvironment>();

	/**
	 * Returns an environment specific for the user. If there is no such object, it gets created.
	 * @param userID An ID of the requesting user.
	 * @return
	 */
	public static synchronized ShellEnvironment getEnvironmentForUser(String userID) {
		ShellEnvironment env = registry.get(userID);
		if (env == null) {
			env = new ShellEnvironment(userID);
			registry.put(userID, env);
		}
		return env;
	}
}
