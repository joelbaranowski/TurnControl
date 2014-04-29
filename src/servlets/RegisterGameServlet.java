package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import request.ExceptionStringify;
import Json.Portal;
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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RegisterGameServlet extends HttpServlet {
	private static final long serialVersionUID = -1292820052767980775L;
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	private Gson gson = new Gson();

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		// Parse request 
		BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));    
		JsonParser jp = new JsonParser();
		JsonObject jo = jp.parse(br).getAsJsonObject();
		String url = jo.get("url").getAsString();
		Portal[] portals = gson.fromJson(jo.get("portals"), Portal[].class);
		RegisterGame rg = new RegisterGame(url, new ArrayList<Portal>(Arrays.asList(portals)));
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

		tx = datastore.beginTransaction();
		ArrayList<Long> portalIDs = new ArrayList<Long>();
		try{
			Long newID = -1L;
			Key portalKey = KeyFactory.createKey("RegisterPortals", "PortalList");
			Query query = new Query("Portals", portalKey);
			List<Entity> portalList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
			for(Entity e : portalList){
				if((Long)e.getProperty("portalID") > newID)
					newID = (Long)e.getProperty("portalID");
			}

			for(Portal p : rg.getPortals()){
				Entity newPortal = new Entity("Portals", portalKey);
				newPortal.setProperty("fromGameURL", rg.getUrl());
				newPortal.setProperty("toGameURL", "null");
				newPortal.setProperty("toGamePortalID", -1L);
				newPortal.setProperty("portalID", ++newID);
				portalIDs.add(newID);
				newPortal.setProperty("isOutbound", p.getIsOutbound());
				datastore.put(newPortal);
			}
			tx.commit();
		}
		catch(Exception e){
			if(tx.isActive())
				tx.rollback();
			ExceptionStringify es = new ExceptionStringify(e);
			resp.getWriter().print(es.run());
		}
		resp.getWriter().println(gson.toJson(portalIDs));
	}
}
