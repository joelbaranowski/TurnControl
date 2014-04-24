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


public class InitServlet extends HttpServlet {
	private static final long serialVersionUID = 893725263070180021L;
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		DeleteStatusServlet dss = new DeleteStatusServlet();
		dss.doPost(req, resp);
		
		DeleteGameListServlet dgl = new DeleteGameListServlet();
		dgl.doPost(req, resp);
		
		DeletePlayersServlet dps = new DeletePlayersServlet();
		dps.doPost(req, resp);
		
		DeletePlayerScoresServlet dpss = new DeletePlayerScoresServlet();
		dpss.doPost(req, resp);
		resp.getWriter().println("{'result':'init'}");
	}
}
