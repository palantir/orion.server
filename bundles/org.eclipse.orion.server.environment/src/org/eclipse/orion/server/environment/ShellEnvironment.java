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
import org.eclipse.orion.server.core.LogHelper;

/**
 * Object specific for each user, forwarding the execution requests to the
 * server system. Only single task could run at one time. Thread-safe.
 *
 * @author mwlodarczyk
 */
public final class ShellEnvironment {

	private enum Command {
		doctest;
	}

	public static final String CANCEL = "cancel"; //$NON-NLS-1$

	@SuppressWarnings("unused")
	private String userID;
	private Process process;

	public ShellEnvironment(String userID) {
		this.userID = userID;
	}

	/**
	 *
	 * @param commandType
	 *            Parameter from GET request.
	 * @param fileStore
	 *            Points to a file to be run.
	 * @param config
	 *            Configuration object specific for this project.
	 * @return String with a command ready for execution.
	 * @throws NoSuchElementException
	 */
	private String getFullCommand(String commandType, IFileStore fileStore,
			ExecutionConfiguration config) throws IllegalArgumentException {
		commandType = commandType == null ? "" : commandType; //$NON-NLS-1$
		switch (Command.valueOf(commandType)) {
		case doctest:
			return config.getPythonExecutable()
					+ " -m doctest -v " + fileStore.toString(); //$NON-NLS-1$
		default:
			throw new IllegalArgumentException("Cannot run command " + commandType);
		}
	}

	/**
	 * Reads from the active process until it terminates.
	 *
	 * @param activeProcess
	 *            Process that we want to listen to.
	 * @return A list of read lines.
	 */
	private List<String> getLogs(Process activeProcess) {
		List<String> out = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(activeProcess.getInputStream()));

		try {
			try {
				String line;
				while ((line = reader.readLine()) != null) {
					out.add(line);
				}
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			out.add("Exception while getting the logs: ");
			out.add(e.getMessage());
		}
		return out;
	}

	/**
	 * Tries to cancel the active process. Does not check if the operation
	 * succeeded.
	 */
	private synchronized void cancel() {
		if (process != null) {
			process.destroy();
		}
	}

	/**
	 * Performs the execution. If the commandType equals 'cancel' the the
	 * fileStore parameter is irrelevant.
	 *
	 * @param commandType
	 *            Parameter from GET request.
	 * @param fileStore
	 *            Points to a file to be run.
	 * @param config
	 *            Configuration object specific for this project.
	 * @return A list of lines produced by the task.
	 */
	public List<String> execute(String commandType, IFileStore fileStore,
			ExecutionConfiguration config) {
		List<String> out = new ArrayList<String>();
		
		if (CANCEL.equals(commandType)) {
			cancel();
		} else try {
				List<String> args = new ArrayList<String>(
						Arrays.asList(getFullCommand(commandType, fileStore,
								config).split(" "))); //$NON-NLS-1$
			Process privateProcess;

			synchronized (this) {
				cancel();
				ProcessBuilder processBuilder = new ProcessBuilder(args);
				process = privateProcess = processBuilder.redirectErrorStream(true).start();
			}

			out.addAll(getLogs(privateProcess));
			int exitStatus = privateProcess.waitFor();
			if (exitStatus != 0) {
				out.add("Process terminated with the exit status " + exitStatus);
			}
		} catch (Exception e) {
			LogHelper.log(e);
			out.add("Exception while running the command:");
			out.add(e.getMessage());
		}		
		return out;
	}
}
