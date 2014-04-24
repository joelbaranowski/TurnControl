package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Json.JoinGame;
import Json.PlayerJoined;
import Json.TakeTurn;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.gson.Gson;

import request.ExceptionStringify;
import request.MethodWrapper;
import test2.UrlPost;

public class TurnFinishedServlet extends HttpServlet {
	private static final long serialVersionUID = 893725263070180021L;
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	private Gson gson = new Gson();

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		System.out.println("joining a game");
		// Parse request 
		BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));    
		TakeTurn tf = gson.fromJson(br, TakeTurn.class);
		doModPost(tf, req, resp);
	}
	
	public void doModPost(TakeTurn tf, HttpServletRequest req, HttpServletResponse resp) throws IOException{
		try{
			
			Key idKey = KeyFactory.createKey("isStarted", "gameStartedStatus");
			Entity a = datastore.get(idKey);
			Boolean isStarted = (Boolean) a.getProperty("isStarted");
			
			if (!isStarted)
				return;
			
		
		Long oldPlayerScore = tf.getCurrentScore();
		Long oldPlayerID = tf.getPlayerID();
		
		resp.getWriter().println("id: " + oldPlayerID + " | score: " + oldPlayerScore);
		
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
		if(newPlayerID >= numberOfPlayers)
			newPlayerID = 0L;
		
		String newPlayerGameUrl = playerToGame.get(newPlayerID);
		
		Key playerScoreKey = KeyFactory.createKey("TakeTurnKey", "PlayerScoreList");
		query = new Query("TakeTurn", playerScoreKey);
		Filter f = new FilterPredicate("playerID", Query.FilterOperator.EQUAL, newPlayerID);
		query.setFilter(f);
		List<Entity> playerScoreList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
		Long newPlayerScore = -1L;
		for(Entity e : playerScoreList)
			newPlayerScore = (Long)e.getProperty("currentScore");
		
		TakeTurn tt = new TakeTurn(newPlayerID, newPlayerScore);
		String gtj = gson.toJson(tt);
		resp.getWriter().println("gtj: " + gtj);
		MethodWrapper mew = new MethodWrapper("takeTurn", gtj);
		UrlPost ttp = new UrlPost();
		ttp.run(mew, newPlayerGameUrl);
		}
		catch(Exception e){
			ExceptionStringify es = new ExceptionStringify(e);
			resp.getWriter().println(es.run());
			return;
		}
	}
}
