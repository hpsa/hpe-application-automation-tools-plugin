package com.hp.devops.demoapp;

import org.json.JSONArray;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 23/11/14
 * Time: 17:40
 * To change this template use File | Settings | File Templates.
 */
public class RestServlet extends HttpServlet {

	@Override
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		DataManager.init(servletContext);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] pathNodes = Utils.nodify(request.getRequestURI());
		if (pathNodes.length == 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "path too short");
		} else if (pathNodes[0].compareTo("bands") == 0) {
			serveBands(pathNodes, response);
		} else if (pathNodes[0].compareTo("reloadDB") == 0) {
			DataManager.loadData();
			response.setContentType("text/plain");
			response.getOutputStream().print("done");
			response.flushBuffer();
		}
	}

	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] pathNodes = Utils.nodify(request.getRequestURI());
		int bandId;
		if (pathNodes.length == 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "path too short");
		} else if (pathNodes[0].compareTo("band") == 0) {
			bandId = Integer.parseInt(pathNodes[1]);
			if (pathNodes[2].compareTo("vote") == 0) {
				upvoteBand(bandId, response);
			}
		}
	}

	private void serveBands(String[] pathNodes, HttpServletResponse response) throws IOException {
		JSONArray resBody = new JSONArray();
		response.setContentType("application/json");
		try {
			if (pathNodes.length == 1) {
				for (Band band : DataManager.getAll()) {
					resBody.put(band.toJSON());
				}
			}
			response.getOutputStream().print(resBody.toString());
			response.flushBuffer();
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void upvoteBand(int bandId, HttpServletResponse response) throws IOException {
		Cookie voted;
		JSONArray resBody = new JSONArray();
		response.setContentType("application/json");
		try {
			DataManager.upVoteBand(bandId);
			for (Band band : DataManager.getAll()) {
				resBody.put(band.toJSONVotes());
			}
			voted = new Cookie("hpDevopsDemoApp", "true");
			voted.setPath("/");
			voted.setMaxAge(60);
			response.addCookie(voted);
			response.getOutputStream().print(resBody.toString());
			response.flushBuffer();
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public void destroy() {
	}
}
