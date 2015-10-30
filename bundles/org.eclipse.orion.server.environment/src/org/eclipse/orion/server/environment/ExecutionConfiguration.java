/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package org.eclipse.orion.server.environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.Properties;
import org.eclipse.core.runtime.CoreException;

/**
 * Stores execution preferences.
 *
 * @author mwlodarczyk
 */
public final class ExecutionConfiguration {

	private enum ConfigurationField {
		PYTHON_EXECUTABLE("python.executable", "python"), //$NON-NLS-1$
		DEBUG("debug", "false"); //$NON-NLS-1$

		private final String key, defaultValue;

		ConfigurationField(String key, String defaultValue) {
			this.key = key;
			this.defaultValue = defaultValue;
		}
	}

	private final Properties config = new Properties();

	/**
	 * Creates a default configuration.
	 */
	public ExecutionConfiguration() {
		for (ConfigurationField field : ConfigurationField.values()) {
			config.setProperty(field.key, field.defaultValue);
		}
	}

	/**
	 * Parses a file looking for pairs "KEY=VALUE". Treats everything in line
	 * after '#' as a comment.
	 *
	 * @param configFile
	 *            A file containing the project-specific configuration.
	 * @throws IOException
	 * @throws CoreException
	 */
	public ExecutionConfiguration(File configFile) throws IOException,
			CoreException {
		this(); // set default values
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(configFile), Charset.forName("UTF-8"))); //$NON-NLS-1$
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				String parsed[] = line.split("#", -1)[0].split("=", -1); //$NON-NLS-1$ //$NON-NLS-2$
				if (parsed.length == 2)
					config.setProperty(parsed[0], parsed[1]);
			}
		} finally {
			reader.close();
		}
	}

	/**
	 *
	 * @param key
	 * @return Value associated with this key.
	 * @throws NoSuchElementException
	 */
	public String get(String key) throws NoSuchElementException {
		String val = config.getProperty(key);
		if (val == null)
			throw new NoSuchElementException("No entry in configuration for: "
					+ key);
		return val;
	}

	/**
	 *
	 * @return Configured command to run python executable.
	 */
	public String getPythonExecutable() {
		return config.getProperty(ConfigurationField.PYTHON_EXECUTABLE.key, ConfigurationField.PYTHON_EXECUTABLE.defaultValue);
	}

}
