package com.ugleh.trademe.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.ugleh.trademe.TradeMe;

public class MongoDBD {
	public static String username = TradeMe.getPlugin().getConfig().getString("trademe.monodb.username");
	public static String password = TradeMe.getPlugin().getConfig().getString("trademe.monodb.password");
	public static String database = TradeMe.getPlugin().getConfig().getString("trademe.monodb.database");
	public static String host = TradeMe.getPlugin().getConfig().getString("trademe.monodb.host");
	public static int port = TradeMe.getPlugin().getConfig().getInt("trademe.monodb.port");
	
	
	
	public static void addTrade(Player trader1, Player trader2, Inventory t1Inv, Inventory t2Inv)
	{
		
	}
}
