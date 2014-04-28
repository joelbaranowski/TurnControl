package servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import request.ExceptionStringify;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;


public class DeleteGameListServlet extends HttpServlet {
	private static final long serialVersionUID = 893725263070180021L;
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
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
		
		tx = datastore.beginTransaction();
		try{
		Key portalKey = KeyFactory.createKey("RegisterPortals", "PortalList");
		Query query = new Query("Portals", portalKey);
		List<Entity> portalList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));
		for (Entity existingEntity : portalList) {
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
		
		resp.getWriter().println("Deleted gameList/portals");
	}
}
