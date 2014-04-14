package test2;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.servlet.http.*;

import request.ExceptionStringify;
import request.JoinGame;
import request.MethodWrapper;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gson.Gson;

@SuppressWarnings("serial")
public class Test2Servlet extends HttpServlet {
	
	private Gson g = new Gson();
	 MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("Deleted playerList");
		syncCache.delete("playerList");
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
				try{
				ArrayList<JoinGame> value = (ArrayList<JoinGame>)syncCache.get("playerList");
				if(value != null){
					
				   	value.add(jg);
				    syncCache.put("playerList", value);
				    String ret = "";
				    for(JoinGame gj : value){
				    	ret += gj.getPlayerID() + ", " + gj.getGameURL() + "\n";
				    }
				    resp.getWriter().println(ret);
				}
				else{
					ArrayList<JoinGame> list = new ArrayList<JoinGame>();
					list.add(jg);
					syncCache.put("playerList", list);
				}
				break;
				}
				catch(Exception e){
					ExceptionStringify es = new ExceptionStringify(e);
					resp.getWriter().println(es.run());
					return;
				}
		}

		resp.getWriter().println(" | end");
	}
}
