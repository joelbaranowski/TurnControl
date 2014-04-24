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

import Json.RegisterGameRequest;
import Json.StatusResponse;
import Json.UpdaetGameURLRequest;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.gson.Gson;

public class UpdateGameURLServlet  extends HttpServlet {
	private static final long serialVersionUID = -1292820052767980775L;
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	private Gson gson = new Gson();

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		// Parse request 
		BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));    
		UpdaetGameURLRequest request = gson.fromJson(br, UpdaetGameURLRequest.class);

		Transaction tx = datastore.beginTransaction();

		Key gameKey = KeyFactory.createKey("JoinGameKey", "PlayerList");
		Query query = new Query("JoinGame", gameKey);
		List<Entity> gameList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
		for (Entity e : gameList) {
			if (request.playerName.equals( (String)e.getProperty("playerName"))) {
			    e.setProperty("gameURL", request.gameURL);
				datastore.put(e);
				break;
			}
		}
		tx.commit();
		
		StatusResponse response = new StatusResponse("ok", "successfully updated player's gameURL");
		resp.getWriter().println(response.toJson());
	}
}
