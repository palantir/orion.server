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
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;


/**
 * Stores execution preferences.
 * @author mwlodarczyk
 *
 */
public class ExecutionConfiguration {
	
	public static String PYTHON_EXECUTABLE_KEY = "python.executable"; //$NON-NLS-1$
	private static String PYTHON_EXECUTABLE_DEFAULT = "python"; //$NON-NLS-1$
	
	public static String DEBUG_KEY = "debug"; //$NON-NLS-1$
	private static String DEBUG_DEFAULT = "false"; //$NON-NLS-1$
	
	private Map<String, String> config = new HashMap<String, String>();

	/**
	 * Creates a default configuration.
	 */
	public ExecutionConfiguration() {
		config.put(PYTHON_EXECUTABLE_KEY, PYTHON_EXECUTABLE_DEFAULT);
		config.put(DEBUG_KEY, DEBUG_DEFAULT);
	}

	/**
	 * Parses a file looking for pairs "KEY=VALUE". Treats everything in line after '#' as a comment.
	 * @param configFileStore Points to a place where a config file should be.
	 * @throws IOException
	 * @throws CoreException
	 */
	public ExecutionConfiguration(IFileStore configFileStore) throws IOException, CoreException {
		this(); // set default values
		File configFile = configFileStore.toLocalFile(0, null);	
	    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), Charset.forName("UTF-8"))); //$NON-NLS-1$
	    String line;
	    
	    while ((line = reader.readLine()) != null) {
	        String parsed[] = line.split("#", -1)[0].split("=", -1); //$NON-NLS-1$
	        if(parsed.length == 2)
	        	config.put(parsed[0], parsed[1]);
	    }
	    reader.close();
	}
	
	
	/**
	 * 
	 * @param key
	 * @return Value associated with this key.
	 * @throws NoSuchElementException
	 */
	public String get(String key) throws NoSuchElementException {
		String val = config.get(key);
		if (val == null)
			throw new NoSuchElementException("No entry in configuration for: " + key);
		return val;
	}

}
