package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Json.JoinGame;
import Json.PlayerJoined;
import Json.StatusResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.gson.Gson;

import request.ExceptionStringify;

public class JoinGameServlet extends HttpServlet {
	private static final long serialVersionUID = 893725263070180021L;
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	private Gson gson = new Gson();

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		System.out.println("joining a game");
		// Parse request 
		BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));    
		JoinGame request = gson.fromJson(br, JoinGame.class);
		doModPost(request, req, resp);
	}
	
	public void doModPost(JoinGame request, HttpServletRequest req, HttpServletResponse resp) throws IOException{

		Transaction tx = datastore.beginTransaction();
		try {

			Long maxID = -1L;
			Long newID = -1L;
			
			Key playerKey = KeyFactory.createKey("JoinGameKey", "PlayerList");
			Query query = new Query("JoinGame", playerKey);
			List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
			for(Entity e : playerList){
				Long currID = (Long)e.getProperty("playerID");
				if(currID > maxID){
					maxID = currID;
				}
			}

			newID = maxID + 1;
			Entity newPlayer = new Entity("JoinGame", playerKey);
			newPlayer.setProperty("playerID", newID);
			newPlayer.setProperty("gameURL", request.gameURL);
			newPlayer.setProperty("playerName", request.playerName);
			newPlayer.setProperty("isAI", request.isAI);
			datastore.put(newPlayer);
			tx.commit();

			StatusResponse reponse = new StatusResponse("ok", newID+"");
			resp.getWriter().println(reponse.toJson());

		}
		catch(Exception e){
			if(tx.isActive())
				tx.rollback();
			ExceptionStringify es = new ExceptionStringify(e);
			resp.getWriter().print(es.run());
		}
	}
}
