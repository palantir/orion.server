package org.eclipse.orion.server.environment;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {
	ServiceRegistration envServiceRegistration;

	public void start(BundleContext context) throws Exception {
		ShellEnvironmentRegistry envRegistry = new ShellEnvironmentRegistry();
		envServiceRegistration = context.registerService(ShellEnvironmentRegistry.class.getName(),
				envRegistry, null);
	}

	public void stop(BundleContext context) throws Exception {
		envServiceRegistration.unregister();
	}

}
