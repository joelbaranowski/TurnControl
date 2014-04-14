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
import request.RegisterGame;
import request.TakeTurn;
import request.TurnFinished;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gson.Gson;

@SuppressWarnings("serial")
public class Test2Servlet extends HttpServlet {
	
	private Gson g = new Gson();
	private MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String method = req.getParameter("method");
		String data = req.getParameter("data");
		if(method == null || data == null)
			return;
		this.execute(req.getParameter("method"), req.getParameter("data"), req, resp);
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		MethodWrapper mw = g.fromJson(req.getReader(), MethodWrapper.class);
		this.execute(mw.getMethod(), mw.getData(), req, resp);
		//end of doPost
	}
	
	private void execute(String method, String data, HttpServletRequest req, HttpServletResponse resp) throws IOException{
		switch(method){
			case "joinGame":{
				JoinGame jg = (JoinGame) g.fromJson(data, JoinGame.class);
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
					value = new ArrayList<JoinGame>();
					value.add(jg);
					syncCache.put("playerList", value);
				}
				break;
			}
			case "turnFinished":{
				boolean isStarted = (boolean) syncCache.get("isStarted");
				if(!isStarted)
					break;
				TurnFinished tf = (TurnFinished) g.fromJson(data, TurnFinished.class);
				int currPlayer = tf.getPlayerID();
				int newScore = tf.getNewScore();
				syncCache.put("player" + currPlayer, newScore);
				int newPlayer = currPlayer + 1;
				TurnFinished tuf = (TurnFinished)syncCache.get("player" + newPlayer);
				int newPlayerScore = tuf.getNewScore();
				TakeTurn tt = new TakeTurn(newPlayer, newPlayerScore);
				String gtj = g.toJson(tt);
				MethodWrapper mew = new MethodWrapper("takeTurn", gtj);
				TestPost tp = new TestPost();
				tp.run(mew);
				break;
			}
			case "startGame":{
				syncCache.put("isStarted", true);
				ArrayList<JoinGame> pll = (ArrayList<JoinGame>)syncCache.get("playerList");
				for(JoinGame jog : pll){
					int currentPlayer = jog.getPlayerID();
					TurnFinished turf = new TurnFinished(currentPlayer, 0);
					syncCache.put("player" + currentPlayer, currentPlayer);
				}
				TakeTurn tt = new TakeTurn(0, 0);
				String gtj = g.toJson(tt);
				MethodWrapper mew = new MethodWrapper("takeTurn", gtj);
				TestPost tp = new TestPost();
				tp.run(mew);
				break;
			}
			case "endGame":{
				syncCache.put("isStarted", false);
				break;
			}
			case "registerGame":{
				try{
				RegisterGame rg = (RegisterGame) g.fromJson(data, RegisterGame.class);
				String gameUrl = rg.getUrl();
				ArrayList<String> value = (ArrayList<String>)syncCache.get("gameList");
				if(value != null){
				   	value.add(gameUrl);
				    syncCache.put("gameList", value);
				    String ret = "";
				    for(String gu : value){
				    	ret += gu + "\n";
				    }
				    resp.getWriter().println(ret);
				}
				else{
					value = new ArrayList<String>();
					value.add(gameUrl);
					syncCache.put("gameList", value);
				}
				}
				catch(Exception e){
					ExceptionStringify es = new ExceptionStringify(e);
					resp.getWriter().println(es.run());
					return;
				}
				break;
			}
			case "deletePlayerList":{
				resp.getWriter().println("Deleted playerList");
				syncCache.delete("playerList");
				break;
			}
			case "deleteGameList":{
				resp.getWriter().println("Deleted gameList");
				syncCache.delete("gameList");
				break;
			}
			case "getGameList":{
				ArrayList<String> gl = (ArrayList<String>) syncCache.get("gameList");
				String ret = g.toJson(gl);
				resp.getWriter().println(ret);
				break;
			}
		//end of switch
		}
	//end of method
	}
//end of class
}
