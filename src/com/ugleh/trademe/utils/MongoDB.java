package com.ugleh.trademe.utils;

import java.util.Arrays;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongoDB {
	private MongoClient mongo = null;
	private DB database = null;
	
	public MongoDB(String username, String password, String database2, String host, int port) {
		MongoCredential credential = MongoCredential.createCredential(username, database2, password.toCharArray());
		mongo = new MongoClient(new ServerAddress(host, port), Arrays.asList(credential));
	}

	public MongoClient getMongo()
	{
		if(mongo == null)
		{
			setupMongo(MongoDBD.username, MongoDBD.password, MongoDBD.database, MongoDBD.host, MongoDBD.port);
		}
		return mongo;
	}
	
	private void setupMongo(String username, String password, String database2, String host, int port) {
		MongoCredential credential = MongoCredential.createCredential(username, database2, password.toCharArray());
		mongo = new MongoClient(new ServerAddress(host, port), Arrays.asList(credential));
		
	}

	@SuppressWarnings("deprecation")
	public DB getDatabase()
	{
		if(database == null)
			database = getMongo().getDB(MongoDBD.database);
		return database;
	}
	
	@SuppressWarnings("deprecation")
	public void setDatabase(String db)
	{
		database = getMongo().getDB(db);
	}
	
	public void closeConnection()
	{
		if(mongo != null)
		{
			mongo.close();
		}
	}
}
