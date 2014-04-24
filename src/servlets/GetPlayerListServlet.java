package servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;

public class GetPlayerListServlet extends HttpServlet {

	private static final long serialVersionUID = 4564250159863375259L;
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	private Gson gson = new Gson();

	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		doGet(req, resp);
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
	
		Key gameKey = KeyFactory.createKey("JoinGameKey", "PlayerList");
		Query query = new Query("JoinGame", gameKey);
		List<Entity> gameList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
		Map<Long, String> playerToGame = new HashMap<Long, String>();
		for (Entity e : gameList) {
			playerToGame.put((Long) e.getProperty("playerID"), (String)e.getProperty("playerName") + "  at   " + (String)e.getProperty("gameURL"));
		}
		resp.getWriter().println(gson.toJson(playerToGame));
	}

}
