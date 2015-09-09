package org.eclipse.orion.server.environment;

import java.util.Dictionary;
import java.util.Hashtable;

public class ShellEnvironmentRegistry {
	
	private Dictionary<String, ShellEnvironment> registry = new Hashtable<String, ShellEnvironment>();
	
	public synchronized ShellEnvironment getEnvironmentForUser(String userID) {
		ShellEnvironment env = registry.get(userID);
		if (env == null) {
			env = new ShellEnvironment(userID);
			registry.put(userID, env);
		}
		return env;
	}
}
