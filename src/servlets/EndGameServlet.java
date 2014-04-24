package servlets;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;


public class EndGameServlet extends HttpServlet {
	private static final long serialVersionUID = 893725263070180021L;
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		System.out.println("joining a game");
		// Parse request 
		Transaction tx2 = datastore.beginTransaction();
		Key idKey = KeyFactory.createKey("isStarted", "gameStartedStatus");
		Entity a = new Entity(idKey);
		a.setProperty("isStarted", false);
		datastore.delete(idKey);
		datastore.put(a);
		tx2.commit();
		resp.getWriter().println("{'return':'ended game'}");
	}
}
