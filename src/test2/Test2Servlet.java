package test2;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.*;

import request.ExceptionStringify;
import request.JoinGame;
import request.MethodWrapper;
import request.RegisterGame;
import request.TakeTurn;
import request.TurnFinished;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
		this.execute(req.getParameter("method"), URLDecoder.decode(req.getParameter("data"), "UTF-8"), req, resp);
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		MethodWrapper mw = g.fromJson(req.getReader(), MethodWrapper.class);
		try{
			this.execute(mw.getMethod(), mw.getData(), req, resp);
		}
		catch(Exception e){
			ExceptionStringify es = new ExceptionStringify(e);
			resp.getWriter().println(es.run());
			return;
		}
		//end of doPost
	}
	
	private void execute(String method, String data, HttpServletRequest req, HttpServletResponse resp) throws IOException{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
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
				    resp.getWriter().println("{'return':'player added'}");
				}
				else{
					value = new ArrayList<JoinGame>();
					value.add(jg);
					syncCache.put("playerList", value);
					resp.getWriter().println("{'return':'player added'}");
				}
				break;
			}
			case "turnFinished":{
				try{
				boolean isStarted = (boolean) syncCache.get("isStarted");
				if(!isStarted)
					break;
				TurnFinished tf = (TurnFinished) g.fromJson(data, TurnFinished.class);
				int currPlayer = tf.getPlayerID();
				int newScore = tf.getNewScore();
				syncCache.put("player" + currPlayer, newScore);
				int newPlayer = currPlayer + 1;
				
				
				String gameURL = "";
				ArrayList<JoinGame> pll = (ArrayList<JoinGame>)syncCache.get("playerList");
				int numberOfPlayers = 0;
				
				for(JoinGame jog : pll){
					numberOfPlayers+=1;
				}
				
				if(newPlayer >= numberOfPlayers)
					newPlayer = 0;
				for(JoinGame jog : pll){
					if(jog.getPlayerID() == newPlayer)
						gameURL = jog.getGameURL();
				}
				
				int newPlayerScore = (int)syncCache.get("player" + newPlayer);
				TakeTurn tt = new TakeTurn(newPlayer, newPlayerScore);
				String gtj = g.toJson(tt);
				
				MethodWrapper mew = new MethodWrapper("takeTurn", gtj);
				TakeTurnPost ttp = new TakeTurnPost();
				ttp.run(mew, gameURL);
				break;
				}
				catch(Exception e){
					ExceptionStringify es = new ExceptionStringify(e);
					resp.getWriter().println(es.run());
					return;
				}
			}
			case "startGame":{
				syncCache.put("isStarted", true);
				ArrayList<JoinGame> pll = (ArrayList<JoinGame>)syncCache.get("playerList");
				String player0GameUrl = "";
				for(JoinGame jog : pll){
					int currentPlayer = jog.getPlayerID();
					String gameURL = jog.getGameURL();
					TurnFinished turf = new TurnFinished(currentPlayer, 0);
					syncCache.put("player" + currentPlayer, 0);
					if(currentPlayer == 0)
						player0GameUrl = gameURL;
				}
				TakeTurn tt = new TakeTurn(0, 0);
				String gtj = g.toJson(tt);
				MethodWrapper mew = new MethodWrapper("takeTurn", gtj);
				TakeTurnPost ttp = new TakeTurnPost();
				ttp.run(mew, player0GameUrl);
				resp.getWriter().println("{'return':'started game and sent take turn to player 0'}");
				break;
			}
			case "endGame":{
				syncCache.put("isStarted", false);
				resp.getWriter().println("{'return':'ended game'}");
				break;
			}
			case "registerGame":{
				RegisterGame rg = (RegisterGame) g.fromJson(data, RegisterGame.class);
				Transaction tx = datastore.beginTransaction();
				Key gameKey = KeyFactory.createKey("GameListKey", "GameList");
				//Query query = new Query("GameList", gameKey).addSort("name", Query.SortDirection.DESCENDING);
				//List<Entity> gameList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));
				try {
					Entity newGame = new Entity("RegisterGame", gameKey);
					newGame.setProperty("url", rg.getUrl());
					datastore.put(newGame);
				} catch (Exception e) {
					e.printStackTrace();
				}
				tx.commit();
				
				resp.getWriter().println("added game");
				break;
			}
			case "deletePlayerList":{
				resp.getWriter().println("Deleted playerList");
				syncCache.delete("playerList");
				break;
			}
			case "getGameList":{
				ArrayList<String> gl = (ArrayList<String>) syncCache.get("gameList");
				String ret = g.toJson(gl);
				resp.getWriter().println(ret);
				break;
			}
			case "getPlayerList":{
				ArrayList<JoinGame> pll = (ArrayList<JoinGame>)syncCache.get("playerList");
				ArrayList<String> playerResults = new ArrayList<String>();
				for(JoinGame jog : pll){
					int playerID = jog.getPlayerID();
					playerResults.add(playerID + ": " + (int)syncCache.get("player" + playerID));
				}
				resp.getWriter().println(g.toJson(playerResults));
				break;
			}
			case "deleteGameList":{
				deleteGames(resp);
				resp.getWriter().println("Deleted gameList");
				break;
			}
			case "deletePlayers":{
				deletePlayers();
				resp.getWriter().println("{'result':'Deleted players'}");
				break;
			}
			case "init":{
				deletePlayers();
				deleteGames(resp);
				resp.getWriter().println("{'result':'init'}");
				break;
			}
		//end of switch
		}
	//end of method
	}
	
	public void deletePlayers(){
		ArrayList<JoinGame> pll = (ArrayList<JoinGame>)syncCache.get("playerList");
		if(pll == null){
			return;
		}
		ArrayList<String> playerResults = new ArrayList<String>();
		for(JoinGame jog : pll){
			int playerID = jog.getPlayerID();
			syncCache.delete("player" + playerID);
		}
		syncCache.delete("playerList");
	}
	
	public void deleteGames( HttpServletResponse resp) throws IOException{
		try{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Transaction tx = datastore.beginTransaction();
		Key gameKey = KeyFactory.createKey("GameListKey", "GameList");
		Query query = new Query("RegisterGame", gameKey).addSort("url", Query.SortDirection.DESCENDING);
		List<Entity> gameList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));
		for (Entity existingEntity : gameList) {
			datastore.delete(existingEntity.getKey());
		}
		}
		catch(Exception e){
			ExceptionStringify es = new ExceptionStringify(e);
			resp.getWriter().println(es.run());
			return;
		}

	}
//end of class
}
