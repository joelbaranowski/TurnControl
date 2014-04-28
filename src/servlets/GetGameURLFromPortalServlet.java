package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Json.GetGameURLFromPortal;
import Json.GetGameURLFromPortalResponse;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import request.ExceptionStringify;

public class GetGameURLFromPortalServlet extends HttpServlet {
	private static final long serialVersionUID = 893725263070180021L;
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	private Gson gson = new Gson();

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		System.out.println("joining a game");
		// Parse request 
		BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));    
		GetGameURLFromPortal ggufp = gson.fromJson(br, GetGameURLFromPortal.class);
		doModPost(ggufp, req, resp);
	}
	
	public void doModPost(GetGameURLFromPortal ggufp, HttpServletRequest req, HttpServletResponse resp) throws IOException{
		try{
		Key portalKey = KeyFactory.createKey("RegisterPortals", "PortalList");
		Query query = new Query("Portals", portalKey);
		List<Entity> portalList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
		GetGameURLFromPortalResponse ggufpr = new GetGameURLFromPortalResponse();
		ggufpr.setStatus("fail");
		for(Entity e : portalList){
			if(((Long)e.getProperty("portalID")).equals(ggufp.getPortalID())){
				ggufpr.setGameURL((String) e.getProperty("toGameURL"));
				ggufpr.setInboundPortalID((Long)e.getProperty("toGamePortalID"));
				ggufpr.setPlayerID(ggufp.getPlayerID());
			}
		}
		ggufpr.setStatus("ok");
		resp.getWriter().println(gson.toJson(ggufpr));
		}
		catch(Exception e){
			ExceptionStringify es = new ExceptionStringify(e);
			resp.getWriter().print(es.run());
		}
	}
}
