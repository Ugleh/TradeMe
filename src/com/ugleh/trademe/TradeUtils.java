package com.ugleh.trademe;

import java.util.Date;

import net.md_5.bungee.api.ChatColor;

public class TradeUtils {
	public static String chatPrefix = ChatColor.DARK_PURPLE + "[" + ChatColor.LIGHT_PURPLE + "TRADE" + ChatColor.DARK_PURPLE + "] " + ChatColor.RESET;
	
	//Will be used with the database stats.
	public static String timeAgo(long time)
	{
		String[] periods = {"second", "minute", "hour", "day", "week", "month", "year", "decade"};
		Long[] lengths = {(long) 60,(long) 60,(long) 24,(long) 7,(long) 4.35,(long) 12,(long) 10};
		long now = new Date().getTime();
		long difference = now - time;
		int i = 0;
		for(i=0;difference >= lengths[i] && i < lengths.length-1; i++)
		{
			difference /= lengths[i];
		}
		difference = Math.round(difference);
		if(difference != 1)
		{
			periods[i] += "s";
		}
		return String.valueOf(difference) + periods[i] + " ago";
		
	}
}
