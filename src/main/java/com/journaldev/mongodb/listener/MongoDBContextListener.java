package com.journaldev.mongodb.listener;

import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

@WebListener
public class MongoDBContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		MongoClient mongo = (MongoClient) sce.getServletContext()
							.getAttribute("MONGO_CLIENT");
		mongo.close();
		System.out.println("MongoClient closed successfully");
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		
		MongoCredential journaldevAuth = MongoCredential.createScramSha1Credential(
				ctx.getInitParameter("MONGODB_USER"), 
				ctx.getInitParameter("MONGODB_AUTHDB"), 
				ctx.getInitParameter("MONGODB_PASSWORD").toCharArray());

		ServerAddress serverAddress = new ServerAddress(ctx.getInitParameter("MONGODB_HOST"), 
				Integer.parseInt(ctx.getInitParameter("MONGODB_PORT")));
		
		MongoClientOptions.Builder options = MongoClientOptions.builder();
		options.socketKeepAlive(true);
		
		MongoClient mongo = new MongoClient(serverAddress, 
				Arrays.asList(journaldevAuth), options.build());
		
		System.out.println("MongoClient initialized successfully");
		sce.getServletContext().setAttribute("MONGO_CLIENT", mongo);
		initProperties(ctx);
	}

	private void initProperties(ServletContext ctx) {
		if(System.getProperty("MONGODB_USER") == null) {
			System.setProperty("MONGODB_HOST", ctx.getInitParameter("MONGODB_HOST"));
		}
		if(System.getProperty("MONGODB_PORT") == null) {
			System.setProperty("MONGODB_PORT", ctx.getInitParameter("MONGODB_PORT"));
		}
		if(System.getProperty("MONGODB_AUTHDB") == null) {
			System.setProperty("MONGODB_AUTHDB", ctx.getInitParameter("MONGODB_AUTHDB"));
		}
		if(System.getProperty("MONGODB_USER") == null) {
			System.setProperty("MONGODB_USER", ctx.getInitParameter("MONGODB_USER"));
		}
		if(System.getProperty("MONGODB_PASSWORD") == null) {
			System.setProperty("MONGODB_PASSWORD", ctx.getInitParameter("MONGODB_PASSWORD"));
		}
	}

}
