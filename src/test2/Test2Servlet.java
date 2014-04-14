package test2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.servlet.http.*;

import request.ExceptionStringify;
import request.JoinGame;
import request.MethodWrapper;

import com.google.gson.Gson;

@SuppressWarnings("serial")
public class Test2Servlet extends HttpServlet {
	
	private Gson g = new Gson();
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("Hello, world2");
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("from server");
		MethodWrapper mw = g.fromJson(req.getReader(), MethodWrapper.class);
		switch(mw.getMethod()){
			case "joinGame":
				JoinGame jg = (JoinGame) g.fromJson(mw.getData(), JoinGame.class);
				resp.getWriter().println(jg.getPlayerID());
				break;
		}

		resp.getWriter().println(" | end");
	}
}
