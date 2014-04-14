/*
 * Copyright (c) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package request;

import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.urlfetch.*;
import com.google.gson.Gson;
import com.google.*;

import java.net.*;
import java.util.concurrent.Future;

import javax.servlet.http.*;

/**
 * 
 * 
 * @author Joel Baranowski
 */
public class MakePost {

	private URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();

	private Gson g = new Gson();
	private URL url;
  
  
	public MakePost(String  url) {
		try {
			this.url = new URL(url);
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public String execute(Object obj) throws Exception {
		HTTPRequest request = new HTTPRequest(url, HTTPMethod.POST);
		request.setPayload(g.toJson(obj).toString().getBytes());
		return new String(fetcher.fetch(request).getContent());
	}
}
