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
				Transaction tx2 = datastore.beginTransaction();
				try {
				
				Key idKey = KeyFactory.createKey("idMakerKey", "PlayerIdGenerator");
				System.out.println("Starting join game");
				long newId;
				
				try {
					System.out.println("Start a new try for getting next id");
					Entity a = datastore.get(idKey);
					System.out.println("Got entity from datastore");
					newId = (long) a.getProperty("nextId");
					System.out.println("Got id from entity");
					a.setProperty("nextId",newId+1);
					System.out.println("Set id in entity");
					datastore.delete(idKey);
					System.out.println("Delete entity by key");
					datastore.put(a);
					System.out.println("Add changed entity back");
					tx2.commit();
					System.out.println("tx commited");
				}
				catch (EntityNotFoundException e) {
					System.out.println("Entity not found, handle exception");
					//this is the first player to register, so no key in datastore
					Entity a = new Entity(idKey);
					System.out.println("created new entity");
					a.setProperty("nextId", 1);
					System.out.println("Set entity property");
					newId = 0;
					datastore.put(a);
					System.out.println("Add entity to datastore");
					tx2.commit();
					System.out.println("tx commited");
				}
				
				
				
				System.out.println("newId to use is: " + newId);
				Key playerKey = KeyFactory.createKey("JoinGameKey", "PlayerList");
				Query query = new Query("JoinGame", playerKey);
				List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
				for(Entity e : playerList)
					if(((Long)e.getProperty("playerID")) == jg.getPlayerID()){
						resp.getWriter().println("found match");
						datastore.delete(e.getKey());
						break;
					}
				
				System.out.println("Creating newPlayer entity");
				
				Entity newPlayer = new Entity("JoinGame", playerKey);
				newPlayer.setProperty("playerID", newId);
				newPlayer.setProperty("gameURL", jg.getGameURL());
				System.out.println("Put new player");
				datastore.put(newPlayer);
				tx.commit();
				System.out.println("committed");
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
				boolean isStarted = (boolean) syncCache.get("isStarted");
				if(!isStarted)
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
				syncCache.put("isStarted", true);
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
				syncCache.put("isStarted", false);
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
					playerToGame.put((Long) e.getProperty("playerID"), (String)e.getProperty("gameURL"));
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
				deleteGames(resp);
				deletePlayers(resp);
				deletePlayerScores(resp);
				deleteIdGen();
				resp.getWriter().println("{'result':'init'}");
				break;
			}
		//end of switch
		}
	//end of method
	}
	
	public void deleteIdGen() {
		Key idKey = KeyFactory.createKey("idMakerKey", "PlayerIdGenerator");
		datastore.delete(idKey);
		
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
