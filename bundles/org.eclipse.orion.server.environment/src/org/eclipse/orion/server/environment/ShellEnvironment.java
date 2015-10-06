/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package org.eclipse.orion.server.environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import org.eclipse.core.filesystem.IFileStore;

/**
 * Object specific for each user, forwarding the execution requests to the server system.
 * Only single task could run at one time. Thread-safe.
 * @author mwlodarczyk
 *
 */
public class ShellEnvironment {
	
	static final String CANCEL = "cancel";  //$NON-NLS-1$
	
	private String userID;
	private Process process;
	
	public ShellEnvironment(String userID) {
		this.userID = userID;
	}

	/**
	 * 
	 * @param commandType Parameter from GET request.
	 * @param fileStore Points to a file to be run.
	 * @param config Configuration object specific for this project.
	 * @return String with a command ready for execution.
	 * @throws NoSuchElementException
	 */
	private String getFullCommand(String commandType, IFileStore fileStore, ExecutionConfiguration config) throws NoSuchElementException {
		commandType = commandType == null ? "" : commandType; //$NON-NLS-1$
		if(commandType.equals("doctest"))
			return config.get(ExecutionConfiguration.PYTHON_EXECUTABLE_KEY) + " -m doctest -v " + fileStore.toString(); //$NON-NLS-1$
		else
			throw new NoSuchElementException("Command type unrecognized");
	}

	/**
	 * Reads from the active process until it terminates.
	 * @param activeProcess Process that we want to listen to.
	 * @return A list of read lines.
	 */
	private List<String> getLogs(Process activeProcess) {
		List<String> out = new ArrayList<String>();
		BufferedReader reader;
		
		synchronized (this) {
			if (activeProcess == null) {
				out.add("No process running");
				return out;
			} else {
				reader = new BufferedReader(new InputStreamReader(activeProcess.getInputStream()));
			}
		}
		try {
			String line;		
			while ((line = reader.readLine()) != null) {
		    	out.add(line);
			}
			reader.close();
		} catch (IOException e) {
			out.add("Exception while getting the logs: ");
			out.add(e.getMessage());
		}
		return out;
	}

	/**
	 * Tries to cancel the active process. Does not check if the operation succeeded.
	 */
	private synchronized void cancel() {
		if (process != null) {
				process.destroy();
		}
	}

	/**
	 * Performs the execution. If the commandType equals 'cancel' the the fileStore parameter is irrelevant.
	 * @param commandType Parameter from GET request.
	 * @param fileStore Points to a file to be run.
	 * @param config Configuration object specific for this project.
	 * @return A list of lines produced by the task.
	 */
	public List<String> execute(String commandType, IFileStore fileStore, ExecutionConfiguration config) {
		List<String> out = new ArrayList<String>();
		
		if(CANCEL.equals(commandType)) {
			cancel();
		} else try {
			List<String> args = new ArrayList<String>(Arrays.asList(getFullCommand(commandType, fileStore, config).split(" ")));
			Process privateProcess;

			synchronized (this) {
				cancel();
				ProcessBuilder processBuilder = new ProcessBuilder(args);
				process = privateProcess = processBuilder.redirectErrorStream(true).start();
			}
			
			out.addAll(getLogs(privateProcess));	
			out.add("Process terminated with the exit status " + privateProcess.waitFor());
			
		} catch (Exception e) {
			out.add("Exception while running the command:");
			out.add(e.getMessage());
		}		
		return out;
	}
}
