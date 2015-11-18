/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package org.eclipse.orion.server.snake.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.orion.internal.server.servlets.ServletResourceHandler;
import org.eclipse.orion.server.servlets.OrionServlet;

/**
 * @author mwong
 */
public final class SnakeServlet extends OrionServlet {

	private static final long serialVersionUID = -9031561672494212828L;

	private final ServletResourceHandler<String> snakeHandler;

	public SnakeServlet() {
		snakeHandler = new SnakeHandler(getStatusHandler());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		snakeHandler.handleRequest(req, resp, req.getPathInfo());
	}
}
