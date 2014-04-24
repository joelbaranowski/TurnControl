package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import request.ExceptionStringify;
import Json.RegisterGame;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.gson.Gson;

public class RegisterGameServlet extends HttpServlet {
	private static final long serialVersionUID = -1292820052767980775L;
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	private Gson gson = new Gson();

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		// Parse request 
		BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));    
		RegisterGame rg = gson.fromJson(br, RegisterGame.class);

		doModPost(rg, req, resp);
	}
	
	public void doModPost(RegisterGame rg, HttpServletRequest req, HttpServletResponse resp) throws IOException{
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
	}
}
