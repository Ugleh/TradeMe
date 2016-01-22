package com.ugleh.trademe;

import org.bukkit.plugin.java.JavaPlugin;

import com.ugleh.trademe.commands.CommandTrade;
import com.ugleh.trademe.utils.MongoDB;
import com.ugleh.trademe.utils.MongoDBD;

public class TradeMe extends JavaPlugin{
	
	private static TradeMe plugin;
	private static MongoDB mongoDB;
	
	public static TradeMe getPlugin()
	{
		return plugin;
	}
	
	@Override
	public void onEnable(){
		plugin = this;
		setupConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		MongoDB mdb = new MongoDB(MongoDBD.username, MongoDBD.password, MongoDBD.database, MongoDBD.host, MongoDBD.port);
		mdb.setDatabase(MongoDBD.database);
		mongoDB = mdb;
		getServer().getPluginManager().registerEvents(new TradeMeListener(), this);
		getCommand("trade").setExecutor(new CommandTrade(this));
		}
	
	@Override
	public void onDisable(){
		getMongo().closeConnection();
	}
	
	
	private void setupConfig() {
		//Trade Settings
		getConfig().addDefault("trademe.settings.range", -1);
		getConfig().addDefault("trademe.settings.sameworld", false);
		getConfig().addDefault("trademe.settings.traderequesttimer", 20);
		getConfig().addDefault("trademe.settings.tradetimer", 240);
		
		//Add trademe text settings.
		getConfig().addDefault("trademe.text.prefix", "TRADE");

		//MonoDB Config
		getConfig().addDefault("trademe.monodb.username", "root");
		getConfig().addDefault("trademe.monodb.password", "");
		getConfig().addDefault("trademe.monodb.database", "TradeMe");
		getConfig().addDefault("trademe.monodb.host", "127.0.0.1");
		getConfig().addDefault("trademe.monodb.port", 27017);
	}
	
	public static MongoDB getMongo()
	{
		return mongoDB;
	}
}
