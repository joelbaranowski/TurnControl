package servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import request.ExceptionStringify;
import Json.GetGameURLFromPortalResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.gson.Gson;

public class DoPortalAssignmentServlet extends HttpServlet {

	private static final long serialVersionUID = 4564250159863375259L;
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	private Gson gson = new Gson();


	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		doGet(req, resp);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {		
		Transaction tx = datastore.beginTransaction();
		try {
			Key portalKey = KeyFactory.createKey("RegisterPortals", "PortalList");
			Query query = new Query("Portals", portalKey);
			List<Entity> portalList = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));

			for(Entity e : portalList){
				if(!(Boolean)e.getProperty("isOutbound")){
					continue;
				}
				Entity ent = e;
				String fromGameURL = (String)e.getProperty("fromGameURL");
				Random r = new Random();

				List<Entity> subList = new ArrayList<Entity>();
				for(Entity e2 : portalList){
					if(!(e2.getProperty("toGameURL").equals(fromGameURL)) && !((Boolean)e2.getProperty("isOutbound"))){
						subList.add(e2);
					}
				}
				if(subList.size() <= 0)
					continue;
				int choice = r.nextInt(subList.size());
				ent.setProperty("toGameURL", subList.get(choice).getProperty("fromGameURL"));
				ent.setProperty("toGamePortalID", subList.get(choice).getProperty("portalID"));
				datastore.delete(e.getKey());
				datastore.put(ent);
			}

			tx.commit();
		}
		catch(Exception e){
			if(tx.isActive())
				tx.rollback();
			ExceptionStringify es = new ExceptionStringify(e);
			resp.getWriter().print(es.run());
		}
	}

}
