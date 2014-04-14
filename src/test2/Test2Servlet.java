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
			case "joinGame":{
				JoinGame jg = (JoinGame) g.fromJson(mw.getData(), JoinGame.class);
				resp.getWriter().println(jg.getPlayerID());
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
				TurnFinished tf = (TurnFinished) g.fromJson(mw.getData(), TurnFinished.class);
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
		}

		resp.getWriter().println(" | end");
	}
}
