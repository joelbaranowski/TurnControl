package test2;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.*;

import request.ExceptionStringify;
import request.JoinGame;
import request.MethodWrapper;
import request.RegisterGame;
import request.TakeTurn;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.*;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@SuppressWarnings("serial")
public class Test2Servlet extends HttpServlet {
	
	private Gson g = new Gson();
	private MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
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
		switch(method){
			case "joinGame":{
				JoinGame jg = (JoinGame) g.fromJson(data, JoinGame.class);

				Transaction tx = datastore.beginTransaction();
				try {
			

				Transaction tx2 = datastore.beginTransaction();
				//Get the playerId to use for this new player from the datastore
				//And update the nextId field for future access
				Key idKey = KeyFactory.createKey("idMakerKey", "PlayerIdGenerator");
				long newId;
				Entity a = datastore.get(idKey);
				newId = (long) a.getProperty("nextId");
				a.setProperty("nextId",newId+1);
				datastore.delete(idKey);
				datastore.put(a);
				tx2.commit();
			
				
				//This checks if an existing player has the same name as the
				//new player from the request, and if so deletes the existing entry
				//Remove if you want multiple players with the same name
				Key playerKey = KeyFactory.createKey("JoinGameKey", "PlayerList");
				Query query = new Query("JoinGame", playerKey);
				List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
				for(Entity e : playerList)
					if(((String)e.getProperty("playerName")).equals(jg.getPlayerName())){
						resp.getWriter().println("found match");
						datastore.delete(e.getKey());
						break;
					}
				
				
				Entity newPlayer = new Entity("JoinGame", playerKey);
				newPlayer.setProperty("playerID", newId);
				newPlayer.setProperty("gameURL", jg.getGameURL());
				newPlayer.setProperty("playerName", jg.getPlayerName());
				datastore.put(newPlayer);
				tx.commit();
				}
				catch(Exception e){
					if(tx.isActive())
						tx.rollback();
					ExceptionStringify es = new ExceptionStringify(e);
					resp.getWriter().print(es.run());
				}
				resp.getWriter().println("player joined");
				break;
			}
			case "turnFinished":{
				try{
					
					Key idKey = KeyFactory.createKey("isStarted", "gameStartedStatus");
					Entity a = datastore.get(idKey);
					boolean isStarted = (boolean) a.getProperty("isStarted");
					
					if (!isStarted)
						break;
					
				
				TakeTurn tf = (TakeTurn) g.fromJson(data, TakeTurn.class);
				Long oldPlayerScore = tf.getCurrentScore();
				Long oldPlayerID = tf.getPlayerID();
				
				//update the taketurn value
				Transaction tx = datastore.beginTransaction();
				try {
				Key playerScoreKey = KeyFactory.createKey("TakeTurnKey", "PlayerScoreList");
				Query query = new Query("TakeTurn", playerScoreKey);
				Filter f = new FilterPredicate("playerID", Query.FilterOperator.EQUAL, oldPlayerID);
				query.setFilter(f);
				List<Entity> playerScoreList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
				for(Entity e : playerScoreList)
					datastore.delete(e.getKey());
				Entity newPlayerScore = new Entity("TakeTurn", playerScoreKey);
				newPlayerScore.setProperty("playerID", oldPlayerID);
				newPlayerScore.setProperty("currentScore", oldPlayerScore);
				datastore.put(newPlayerScore);
				tx.commit();
				}
				catch(Exception e){
					if(tx.isActive())
						tx.rollback();
					ExceptionStringify es = new ExceptionStringify(e);
					resp.getWriter().println(es.run());
				}
				
				Long newPlayerID = oldPlayerID + 1;
				Key gameKey = KeyFactory.createKey("JoinGameKey", "PlayerList");
				Query query = new Query("JoinGame", gameKey);
				List<Entity> gameList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
				Map<Long, String> playerToGame = new HashMap<Long, String>();
				for (Entity e : gameList) {
					playerToGame.put((Long) e.getProperty("playerID"), (String)e.getProperty("gameURL"));
				}
				int numberOfPlayers = playerToGame.size();
				resp.getWriter().println("#players: " + numberOfPlayers + " | newplayer: " + newPlayerID);
				if(newPlayerID >= numberOfPlayers)
					newPlayerID = 0L;
				String newPlayerGameUrl = playerToGame.get(newPlayerID);
				for(Long key : playerToGame.keySet())
					resp.getWriter().println("key: " + key + " | value: " + playerToGame.get(key));
				resp.getWriter().println("new player: " + newPlayerID + " | value: " + playerToGame.get(newPlayerID));
				Key playerScoreKey = KeyFactory.createKey("TakeTurnKey", "PlayerScoreList");
				query = new Query("TakeTurn", playerScoreKey);
				Filter f = new FilterPredicate("playerID", Query.FilterOperator.EQUAL, newPlayerID);
				query.setFilter(f);
				List<Entity> playerScoreList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
				Long newPlayerScore = -1L;
				for(Entity e : playerScoreList)
					newPlayerScore = (Long)e.getProperty("currentScore");
				
				TakeTurn tt = new TakeTurn(newPlayerID, newPlayerScore);
				String gtj = g.toJson(tt);
				
				MethodWrapper mew = new MethodWrapper("takeTurn", gtj);
				TakeTurnPost ttp = new TakeTurnPost();
				ttp.run(mew, newPlayerGameUrl);
				break;
				}
				catch(Exception e){
					ExceptionStringify es = new ExceptionStringify(e);
					resp.getWriter().println(es.run());
					return;
				}
			}
			case "startGame":{
				
				Transaction tx2 = datastore.beginTransaction();
				Key idKey = KeyFactory.createKey("isStarted", "gameStartedStatus");
				Entity a = new Entity(idKey);
				a.setProperty("isStarted", true);
				datastore.put(a);
				tx2.commit();
				
				
				deletePlayerScores(resp);
				Key gameKey = KeyFactory.createKey("JoinGameKey", "PlayerList");
				Query query = new Query("JoinGame", gameKey);
				List<Entity> gameList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
				Map<Long, String> playerToGame = new HashMap<Long, String>();
				for (Entity e : gameList) {
					playerToGame.put((Long) e.getProperty("playerID"), (String)e.getProperty("gameURL"));
				}
				
				String player0GameUrl = playerToGame.get(0L);
				
				for(Long currentPlayer : playerToGame.keySet()){
					Transaction tx = datastore.beginTransaction();
					try {
					
					Key playerKey = KeyFactory.createKey("TakeTurnKey", "PlayerScoreList");
					
					Entity newPlayer = new Entity("TakeTurn", playerKey);
					newPlayer.setProperty("playerID", currentPlayer);
					newPlayer.setProperty("currentScore", 0);
					datastore.put(newPlayer);
					tx.commit();
					}
					catch(Exception e){
						if(tx.isActive())
							tx.rollback();
						ExceptionStringify es = new ExceptionStringify(e);
						resp.getWriter().print(es.run());
					}
				}
				
				TakeTurn tt = new TakeTurn(0L, 0L);
				String gtj = g.toJson(tt);
				MethodWrapper mew = new MethodWrapper("takeTurn", gtj);
				TakeTurnPost ttp = new TakeTurnPost();
				ttp.run(mew, player0GameUrl);
				resp.getWriter().println("{'return':'player number: " + playerToGame.size() + ", started game:" + player0GameUrl + "'}");
				break;
			}
			case "endGame":{
				Transaction tx2 = datastore.beginTransaction();
				Key idKey = KeyFactory.createKey("isStarted", "gameStartedStatus");
				Entity a = new Entity(idKey);
				a.setProperty("isStarted", false);
				datastore.delete(idKey);
				datastore.put(a);
				tx2.commit();
				resp.getWriter().println("{'return':'ended game'}");
				break;
			}
			case "registerGame":{
				RegisterGame rg = (RegisterGame) g.fromJson(data, RegisterGame.class);
				Transaction tx = datastore.beginTransaction();
				try {
				
				Key gameKey = KeyFactory.createKey("RegisterGameKey", "GameList");
				Query query = new Query("RegisterGame", gameKey);
				List<Entity> gameList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
				for(Entity e : gameList)
					if(e.getProperty("url").equals(rg.getUrl())){
						resp.getWriter().println("Game already registered");
						return;
					}
				
				Entity newGame = new Entity("RegisterGame", gameKey);
				newGame.setProperty("url", rg.getUrl());
				datastore.put(newGame);
				tx.commit();
				}
				catch(Exception e){
					if(tx.isActive())
						tx.rollback();
					ExceptionStringify es = new ExceptionStringify(e);
					resp.getWriter().print(es.run());
				}
				resp.getWriter().println("added game");
				break;
			}
			case "getGameList":{
				Key gameKey = KeyFactory.createKey("RegisterGameKey", "GameList");
				Query query = new Query("RegisterGame", gameKey);
				List<Entity> gameList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
				List<String> ret = new ArrayList<String>();
				for (Entity e : gameList) {
					ret.add((String) e.getProperty("url"));
				}
				resp.getWriter().println(g.toJson(ret));
				break;
			}
			case "getPlayerList":{
				Key gameKey = KeyFactory.createKey("JoinGameKey", "PlayerList");
				Query query = new Query("JoinGame", gameKey);
				List<Entity> gameList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
				Map<Long, String> playerToGame = new HashMap<Long, String>();
				for (Entity e : gameList) {
					playerToGame.put((Long) e.getProperty("playerID"), (String)e.getProperty("playerName") + "  at   " + (String)e.getProperty("gameURL"));
				}
				resp.getWriter().println(g.toJson(playerToGame));
				break;
			}
			case "deleteGameList":{
				deleteGames(resp);
				resp.getWriter().println("Deleted gameList");
				break;
			}
			case "deletePlayers":{
				deletePlayers(resp);
				resp.getWriter().println("{'result':'Deleted players'}");
				break;
			}
			case "deletePlayerScores":{
				deletePlayerScores(resp);
				resp.getWriter().println("{'result':'Deleted player scores'}");
				break;
			}
			case "init":{
				deleteStatus(resp);
				deleteGames(resp);
				deletePlayers(resp);
				deletePlayerScores(resp);
				deleteIdGen(resp);
				resp.getWriter().println("{'result':'init'}");
				break;
			}
		//end of switch
		}
	//end of method
	}
	
	public void deleteStatus(HttpServletResponse resp) throws IOException {
		Transaction tx = datastore.beginTransaction();
		Key idKey = KeyFactory.createKey("isStarted", "gameStartedStatus");
		datastore.delete(idKey);
		Entity newE = new Entity(idKey);
		newE.setProperty("isStarted", false);
		datastore.put(newE);
		tx.commit();
		resp.getWriter().println("Reset isStarted to false");
	}
	
	public void deleteIdGen(HttpServletResponse resp) throws IOException {
		Transaction tx = datastore.beginTransaction();
		Key idKey = KeyFactory.createKey("idMakerKey", "PlayerIdGenerator");
		datastore.delete(idKey);
		Entity newE = new Entity(idKey);
		newE.setProperty("nextId", 0);
		datastore.put(newE);
		tx.commit();
		resp.getWriter().println("Restarted id counter");
		
	}
	
	public void deletePlayers( HttpServletResponse resp) throws IOException{
		Transaction tx = datastore.beginTransaction();
		try {
		Key playerKey = KeyFactory.createKey("JoinGameKey", "PlayerList");
		Query query = new Query("JoinGame", playerKey);
		List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
		for(Entity e : playerList)
			datastore.delete(e.getKey());
		tx.commit();
		}
		catch(Exception e){
			if(tx.isActive())
				tx.rollback();
			ExceptionStringify es = new ExceptionStringify(e);
			resp.getWriter().print(es.run());
		}
		resp.getWriter().println("player deleted");
	}
	
	public void deletePlayerScores( HttpServletResponse resp) throws IOException{
		Transaction tx = datastore.beginTransaction();
		try {
		Key playerScoreKey = KeyFactory.createKey("TakeTurnKey", "PlayerScoreList");
		Query query = new Query("TakeTurn", playerScoreKey);
		List<Entity> playerScoreList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
		for(Entity e : playerScoreList)
			datastore.delete(e.getKey());
		tx.commit();
		}
		catch(Exception e){
			if(tx.isActive())
				tx.rollback();
			ExceptionStringify es = new ExceptionStringify(e);
			resp.getWriter().print(es.run());
		}
		resp.getWriter().println("player score deleted");
	}
	
	public void deleteGames( HttpServletResponse resp) throws IOException{
		Transaction tx = datastore.beginTransaction();
		try{
		Key gameKey = KeyFactory.createKey("RegisterGameKey", "GameList");
		Query query = new Query("RegisterGame", gameKey);
		List<Entity> gameList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
		for (Entity existingEntity : gameList) {
			datastore.delete(existingEntity.getKey());
		}
		tx.commit();
		}
		catch(Exception e){
			if(tx.isActive()){
				tx.rollback();
			}
			ExceptionStringify es = new ExceptionStringify(e);
			resp.getWriter().println(es.run());
		}
	}
//end of class
}
