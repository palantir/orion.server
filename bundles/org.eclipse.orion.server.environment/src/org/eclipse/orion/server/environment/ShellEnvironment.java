package org.eclipse.orion.server.environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;

public class ShellEnvironment {
	
	private String userID;
	private Process process;
	
	public ShellEnvironment(String userID) {
		this.userID = userID;
	}
	
	private String getFullCommand(String commandType) throws NoSuchElementException {
		commandType = commandType == null ? "" : commandType;
		switch (commandType) {
			case "python-doctest":
				return "python -m doctest -v";
			default:
				throw new NoSuchElementException("Command type unrecognized");
		}
	}
	
	private List<String> getLogs() {
		List<String> out = new ArrayList<String>();
		BufferedReader reader;
		
		synchronized (this) {
			if (process == null) {
				out.add("No process running");
				return out;
			} else {
				reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			}
		}
		try {
			String line;		
			while ((line = reader.readLine()) != null) {
		    	out.add(line);
			}
		} catch (IOException e) {
			out.add("Exception while getting the logs: ");
			out.add(e.getMessage());
		}
		return out;
	}
	
	public synchronized int cancel() throws InterruptedException {
		if (process == null) {
			return 0;
		} else {
			process.destroy();
			return process.waitFor();
		}
	}
	
	public List<String> execute(String commandType, IFileStore fileStore) {
		File file;
		List<String> out = new ArrayList<String>();
		
		try {
			file = fileStore.toLocalFile(0, null);
			
		} catch (CoreException e) {
			out.add("Exception while retrieving the file:");
			out.add(e.getMessage());
			return out;
		}
		
		try {
			List<String> args = new ArrayList<String>(Arrays.asList(getFullCommand(commandType).split(" ")));
			args.add(file.getPath());
			
			synchronized (this) {
				cancel();
				ProcessBuilder processBuilder = new ProcessBuilder(args);
				process = processBuilder.redirectErrorStream(true).start();
			}
			
			out.addAll(getLogs());	
			out.add("Process terminated with the exit status " + process.waitFor());
			
		} catch (Exception e) {
			out.add("Exception while running the command:");
			out.add(e.getMessage());
			e.printStackTrace();
		}		
		return out;
	}
}
