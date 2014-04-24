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
			boolean foundMatch = false;

			List<Entity> playerList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
			for(Entity e : playerList)
				if(((String)e.getProperty("playerName")).equals(request.playerName)){
					foundMatch = true;
					StatusResponse reponse = new StatusResponse("fail", "already found a match");
					resp.getWriter().println(reponse.toJson());
					datastore.delete(e.getKey());
					break;
				}


			Entity newPlayer = new Entity("JoinGame", playerKey);
			newPlayer.setProperty("playerID", newId);
			newPlayer.setProperty("gameURL", request.gameURL);
			newPlayer.setProperty("playerName", request.playerName);
			newPlayer.setProperty("isAI", request.isAI);
			datastore.put(newPlayer);
			tx.commit();

			if (!foundMatch) {
				StatusResponse reponse = new StatusResponse("ok", newId+"");
				resp.getWriter().println(reponse.toJson());
			}

		}
		catch(Exception e){
			if(tx.isActive())
				tx.rollback();
			ExceptionStringify es = new ExceptionStringify(e);
			resp.getWriter().print(es.run());
		}

	}
}
