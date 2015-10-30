package org.eclipse.orion.server.environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

/**
 * Singleton class to check if a file contains a word from the black list.
 *
 * @author mwlodarczyk
 */
public final class PythonSecurityFilter {

	private enum ForbiddenWord {
		compile, exec, eval, globals, os, subprocess, runpy, sys
	}

	private static final PythonSecurityFilter SINGLETON = new PythonSecurityFilter();

	/**
	 *
	 * @return The singleton instance of the class.
	 */
	public static PythonSecurityFilter getInstance() {
		return SINGLETON;
	}

	private final Set<String> ForbiddenWordsSet = new HashSet<String>();

	private PythonSecurityFilter() {
		for (ForbiddenWord word : ForbiddenWord.values()) {
			ForbiddenWordsSet.add(word.toString());
		}
	}

	/**
	 * Checks if a line contains any forbidden word.
	 *
	 * @param line
	 *            A line from the checked file.
	 * @return An error message or an empty string if the line is fine.
	 */
	private String checkLine(String line) {
		for (String word : line.split("\\W+", -1)) { //$NON-NLS-1$
			if (ForbiddenWordsSet.contains(word))
				return "File contains a forbidden word: " + word;
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Checks if a file contains any forbidden word.
	 *
	 * @param file
	 *            A file to be filtered.
	 * @return An error message or an empty string if the file is fine.
	 * @throws IOException
	 */
	public String doFilter(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), Charset.forName("UTF-8"))); //$NON-NLS-1$
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				String result = checkLine(line);
				if (!result.isEmpty())
					return result;
			}
		} finally {
			reader.close();
		}
		return ""; //$NON-NLS-1$
	}
}
