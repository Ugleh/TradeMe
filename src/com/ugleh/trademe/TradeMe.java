package com.ugleh.trademe;

import org.bukkit.plugin.java.JavaPlugin;

import com.ugleh.trademe.commands.CommandTrade;

public class TradeMe extends JavaPlugin{
	
	@Override
	public void onEnable(){
		getServer().getPluginManager().registerEvents(new TradeMeListener(), this);
		getCommand("trade").setExecutor(new CommandTrade(this));
		}
	
}
