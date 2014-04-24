package servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import request.ExceptionStringify;
import request.MethodWrapper;
import request.TakeTurn;
import test2.UrlPost;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.gson.Gson;

public class StartGameServlet extends HttpServlet {
	private static final long serialVersionUID = -1805220806377281625L;
	private Gson gson = new Gson();

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

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
			System.out.println("startGame: iterate through playerID " +(Long) e.getProperty("playerID"));
			playerToGame.put((Long) e.getProperty("playerID"), (String)e.getProperty("gameURL"));
		}

		String player0GameUrl = playerToGame.get(0L);

		System.out.println("player0GameUrl "+ player0GameUrl);
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
		String gtj = gson.toJson(tt);
		MethodWrapper mew = new MethodWrapper("takeTurn", gtj);
		UrlPost ttp = new UrlPost();
		ttp.run(mew, player0GameUrl+"/takeTurn");
		System.out.println("GameURL: "+ player0GameUrl+"/takeTurn");
		resp.getWriter().println("{'return':'player number: " + playerToGame.size() + ", started game:" + player0GameUrl + "'}");
	}

	public void deletePlayerScores( HttpServletResponse resp) throws IOException{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

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
}

