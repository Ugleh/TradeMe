package com.ugleh.trademe.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import com.ugleh.trademe.TradeMe;
import com.ugleh.trademe.TradeTimer;
import com.ugleh.trademe.TradeUtils;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
public class CommandTrade implements CommandExecutor {
	Plugin plugin;
	//HashMap<TradeFrom, TradeTo>
	public static HashMap<Player, Player> tradeList = new HashMap<Player, Player>();
	public static HashMap<Player, Inventory> currentlyTrading = new HashMap<Player, Inventory>();
	public static HashMap<Player, Inventory> finalizedTrades = new HashMap<Player, Inventory>();
	public static List<Player> approvedTradePlayers = new ArrayList<Player>();
	//TradeTo gets a timer.
	public static HashMap<Player, Integer> tradeRequestTimer = new HashMap<Player, Integer>();
	public HashMap<Player, Integer> tradeTimer = new HashMap<Player, Integer>();
	
	public CommandTrade(TradeMe tradeMe) {
		plugin = tradeMe;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String string, String[] args) {
        if (sender instanceof Player) {
    		Player tradeFrom = (Player) sender;
        	if(args.length == 0)
        	{
        		sender.sendMessage(TradeUtils.chatPrefix + ChatColor.RED + "Not enough Arguments. Usage: /trade <player>");
        		return false;
        	}
        	if(args[0].equalsIgnoreCase("accept"))
        	{
        		tradeAccepted(tradeFrom); //Actually tradeTo
        		return true;
        	}else if(args[0].equalsIgnoreCase("decline"))
        	{
        		tradeDeclined(tradeFrom); //Actually tradeTo
        		return true;
        	}else if(args[0].equalsIgnoreCase("return"))
        	{
        		tradeReturn(tradeFrom);
        		return true;
        	}else if(args[0].equalsIgnoreCase("cancel"))
        	{
        		tradeCancel(tradeFrom);
        		return true;
        	}
        	if(tradeList.containsKey(tradeFrom) || tradeList.containsValue(tradeFrom))
        	{
        		sender.sendMessage(TradeUtils.chatPrefix + ChatColor.RED + "You already have a trade currently pending.");
        		return false;
        	}
        	Player tradeTo = Bukkit.getPlayer(args[0]);
        	if(tradeTo == null)
        	{
        		sender.sendMessage(TradeUtils.chatPrefix + ChatColor.RED + "Sorry, that player is not online.");
        		return false;
        	}
        	sendTradeRequest(tradeFrom, tradeTo);
        	return true;
        }
		return false;
	}

	public static void tradeCancel(Player trader) {
		if(currentlyTrading.containsKey(trader)) //Active Trade
		{
			Player trader2 = null;
			if(tradeList.containsKey(trader))
			{
				trader2 = tradeList.get(trader);
			}else if(tradeList.containsValue(trader))
			{
				trader2 = tradeListKey(trader);
			}
			trader.sendMessage(TradeUtils.chatPrefix + ChatColor.RED + "Trade Canceled.");
			trader2.sendMessage(TradeUtils.chatPrefix + trader.getName() + ChatColor.RED + " canceled the trade.");
			tradeList.remove(trader);
			tradeList.remove(trader2);
			trader.closeInventory();
			trader2.closeInventory();

			giveInventory(trader, currentlyTrading.get(trader));
			giveInventory(trader2, currentlyTrading.get(trader2));

			finalizedTrades.get(trader).clear();
			finalizedTrades.get(trader2).clear();
			finalizedTrades.remove(trader);
			finalizedTrades.remove(trader2);
			approvedTradePlayers.remove(trader);
			approvedTradePlayers.remove(trader2);
			currentlyTrading.remove(trader);
			currentlyTrading.remove(trader2);
			tradeRequestTimer.remove(trader);
			tradeRequestTimer.remove(trader2);
			
		}else
		{
			trader.sendMessage(TradeUtils.chatPrefix + ChatColor.RED + "You do not have an active trade.");
		}
	}

	private static void giveInventory(Player trader, Inventory inventory) {
		int index = 0;
		for(ItemStack items : inventory)
		{
			if(index >= 27) break;
			if(items != null)
			{
				if(trader.getInventory().firstEmpty() == -1)
				{
					trader.getWorld().dropItem(trader.getLocation(), items);
				}else
				{
					trader.getInventory().addItem(items);
				}
			}
			++index;
		}
	}

	private void tradeReturn(Player trader) {
		if(finalizedTrades.containsKey(trader))
		{
			trader.sendMessage(TradeUtils.chatPrefix + ChatColor.RED + "Your trade has been finalized and cannot be accessed. You can still cancel it with /trade cancel.");
			return;
		}
		if(currentlyTrading.containsKey(trader)) //Active Trade
		{
			trader.openInventory(currentlyTrading.get(trader)); //Returns them back to their trade.
		}else
		{
			trader.sendMessage(TradeUtils.chatPrefix + ChatColor.RED + "You do not have an active trade.");
		}
		
	}

	private void tradeDeclined(Player tradeTo) {
		if(tradeList.containsValue(tradeTo)) //Active Trade Request
		{
			tradeListRemove(tradeTo);
			tradeRequestTimer.remove(tradeTo);
			tradeRequestTimer.remove(tradeListKey(tradeTo));

			tradeTo.sendMessage(TradeUtils.chatPrefix + ChatColor.RED + "Trade Declined.");
			tradeListKey(tradeTo).sendMessage(TradeUtils.chatPrefix + tradeTo.getName() + ChatColor.RED + " has declined trade.");
		}else{
			tradeTo.sendMessage(TradeUtils.chatPrefix + ChatColor.RED + "You do not have a trade pending.");
		}			
	}

	private void tradeAccepted(Player tradeTo) {
		if(!currentlyTrading.containsKey(tradeTo))
		{
			if(tradeList.containsValue(tradeTo))//Active Trade Request
			{
				tradeRequestTimer.remove(tradeTo);
				tradeRequestTimer.remove(tradeListKey(tradeTo));
				tradeTo.sendMessage(TradeUtils.chatPrefix + ChatColor.GREEN + "Trade Accepted.");
				tradeListKey(tradeTo).sendMessage(TradeUtils.chatPrefix + tradeTo.getName() + ChatColor.GREEN + " has accepted trade.");
				initTrade(tradeListKey(tradeTo), tradeTo);
			}else{
				tradeTo.sendMessage(TradeUtils.chatPrefix + ChatColor.RED + "You do not have a trade pending.");
			}
		}

	}

	private void initTrade(Player tradeFrom, Player tradeTo) {
		tradeTimer.put(tradeFrom, (int) (System.currentTimeMillis()/1000L));
		tradeTimer.put(tradeTo, (int) (System.currentTimeMillis()/1000L));
		Inventory tradeFromInv = Bukkit.createInventory(null, 36, ChatColor.DARK_GREEN + "Trade Window - " + ChatColor.BLUE + "Your offer.");
		Inventory tradeToInv = Bukkit.createInventory(null, 36, ChatColor.DARK_GREEN + "Trade Window - " + ChatColor.BLUE + "Your offer.");
		TradeTimer cooldown = new  TradeTimer(tradeFrom, 240);
		cooldown.setCancelTask(Bukkit.getScheduler().runTaskTimer(plugin, cooldown, 0, 2));
		ItemStack stainedGlass = getStainedGlass();
		for(int i = 27; i<36;i++)
		{
			tradeFromInv.setItem(i, stainedGlass);
		}
		ItemStack accept = new ItemStack(Material.INK_SACK, 1, (short)10);
		ItemMeta acceptMeta = accept.getItemMeta();
		acceptMeta.setDisplayName(ChatColor.GREEN + "DONE WITH YOUR OFFER");
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.RED + "Make sure you have space in your inventory!!!");
		acceptMeta.setLore(lore);
		acceptMeta.setDisplayName(ChatColor.GREEN + "DONE WITH YOUR OFFER");
		accept.setItemMeta(acceptMeta);
		tradeFromInv.setItem(30, accept);

		tradeFromInv.setItem(31, getSkull(tradeTo, tradeFrom));
		ItemStack decline = new ItemStack(Material.INK_SACK, 1, (short)8);
		ItemMeta declineMeta = decline.getItemMeta();
		declineMeta.setLore(lore);
		declineMeta.setDisplayName(ChatColor.GRAY + "CANCEL");
		decline.setItemMeta(declineMeta);
		tradeFromInv.setItem(32, decline);

		tradeFromInv.setMaxStackSize(240);
		tradeToInv.setContents(tradeFromInv.getContents()); //Just copy the inventory.
		tradeToInv.setItem(31, getSkull(tradeFrom, tradeTo));

		currentlyTrading.put(tradeFrom, tradeFromInv);
		currentlyTrading.put(tradeTo, tradeToInv);
		tradeFrom.openInventory(tradeFromInv);
		tradeTo.openInventory(tradeToInv);
	}

	private ItemStack getSkull(Player tradeTo, Player tradeFrom) {
		SkullMeta  meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		meta.setOwner(tradeTo.getName());
		meta.setDisplayName(ChatColor.GREEN + "Trading with " + ChatColor.RESET + tradeTo.getName());
		ItemStack skullStack = new ItemStack(Material.SKULL_ITEM,1 , (byte)3);
		skullStack.setItemMeta(meta);
		return skullStack;
	}

	private ItemStack getStainedGlass() {
		ItemStack ret = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)14);
		ItemMeta im = ret.getItemMeta();
		im.setDisplayName(ChatColor.RED  + "---");
		ret.setItemMeta(im);
		return ret;
	}

	public static void tradeListRemove(Player tradeTo) {
		Player tradeFrom = null;
		for(Entry<Player, Player> entry : tradeList.entrySet())
		{
			if(entry.getValue().equals(tradeTo))
			{
				tradeFrom = entry.getKey();
				break;
			}
		}
		tradeList.remove(tradeFrom);
		
	}
	
	public static Player tradeListKey(Player value) {
		for(Entry<Player, Player> entry : tradeList.entrySet())
		{
			if(entry.getValue().equals(value))
			{
				return entry.getKey();
			}
		}
		return null;		
	}
	private void sendTradeRequest(Player tradeFrom, Player tradeTo) {
		tradeList.put(tradeFrom, tradeTo);
		tradeRequestTimer.put(tradeFrom, (int)(System.currentTimeMillis()/1000));
		tradeFrom.sendMessage(TradeUtils.chatPrefix + ChatColor.GREEN + "Trade request sent to " + ChatColor.RESET + tradeTo.getName());
		tradeTo.sendMessage(TradeUtils.chatPrefix + ChatColor.GREEN + "Trade request from " + ChatColor.RESET + tradeFrom.getName());
		//Bukkit.dispatchCommand(sender, commandLine)
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("[\"\",{\"text\":\">>>>\",\"color\":\"dark_blue\",\"bold\":true},{\"text\":\"[ \",\"color\":\"gold\",\"bold\":true},{\"text\":\"ACCEPT\",\"color\":\"green\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/trade accept\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click to accept trade.\",\"color\":\"green\"}]}}},{\"text\":\" | \",\"color\":\"black\",\"bold\":true},{\"text\":\"DECLINE\",\"color\":\"red\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/trade decline\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click to decline trade.\",\"color\":\"red\"}]}}},{\"text\":\" ]\",\"color\":\"gold\",\"bold\":true},{\"text\":\"<<<<\",\"color\":\"dark_blue\",\"bold\":true}]"));
		((CraftPlayer) tradeTo).getHandle().playerConnection.sendPacket(packet);
		
		 Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				  @Override
				  public void run() {
						 tradeExpiredCheck();
				  }
				}, 400);
	}

	protected void tradeExpiredCheck() {
		for(Player s : tradeList.keySet())
		{
			if(tradeRequestTimer.containsKey(s)) //There is an active trade going on
			{
				int time = (int)(System.currentTimeMillis()/1000) - tradeRequestTimer.get(s);
				if(time >= 19)
				{
					tradeExpired(s, tradeList.get(s));
				}
			}
		}
	}

	private void tradeExpired(Player tradeFrom, Player tradeTo) {
		tradeRequestTimer.remove(tradeFrom);
		tradeList.remove(tradeFrom);
		tradeFrom.sendMessage(TradeUtils.chatPrefix + ChatColor.RED + "Trade attempt expired.");
		tradeTo.sendMessage(TradeUtils.chatPrefix + ChatColor.RED + "Trade attempt expired.");
	}

	public static void cancelTradeExpired(Player trader) {
		if(currentlyTrading.containsKey(trader)) //Active Trade
		{
			Player trader2 = null;
			if(tradeList.containsKey(trader))
			{
				trader2 = tradeList.get(trader);
			}else if(tradeList.containsValue(trader))
			{
				trader2 = tradeListKey(trader);
			}
			trader.sendMessage(TradeUtils.chatPrefix + ChatColor.RED + "Trade Expired.");
			trader2.sendMessage(TradeUtils.chatPrefix + ChatColor.RED + "Trade Expired.");
			trader.closeInventory();
			trader2.closeInventory();
			giveInventory(trader, currentlyTrading.get(trader));
			giveInventory(trader2, currentlyTrading.get(trader2));
			currentlyTrading.remove(trader);
			currentlyTrading.remove(trader2);
			tradeList.remove(trader);
			tradeList.remove(trader2);

		}		
	}

	public static void tradeDone(Player trader) {
		if(finalizedTrades.containsKey(trader))
			return;
		if(currentlyTrading.containsKey(trader)) //Active Trade
		{
			Player trader2 = null;
			if(tradeList.containsKey(trader))
			{
				trader2 = tradeList.get(trader);
			}else if(tradeList.containsValue(trader))
			{
				trader2 = tradeListKey(trader);
			}
			finalizedTrades.put(trader, currentlyTrading.get(trader));
			//currentlyTrading.remove(trader);
			trader.closeInventory();
			if(finalizedTrades.containsKey(trader2) && finalizedTrades.containsKey(trader))
			{
				trader.sendMessage(TradeUtils.chatPrefix + ChatColor.GREEN + "Your trade offer has been finalized");
				trader2.sendMessage(TradeUtils.chatPrefix + trader.getName() + ChatColor.GREEN + " has finished their trade offer.");	
				modifyInventoryForFinalTradeApproval(trader, trader2);
			}else{
				trader.sendMessage(TradeUtils.chatPrefix + ChatColor.GREEN + "Your trade offer has been finalized, waiting on " + ChatColor.RESET + trader2.getName());
				trader2.sendMessage(TradeUtils.chatPrefix + trader.getName() + ChatColor.GREEN + " has finished their trade offer, just waiting on you.");

			}
		}		
	}

	private static void modifyInventoryForFinalTradeApproval(Player trader1, Player trader2) {
		Inventory trader1Inventory = finalizedTrades.get(trader1);
		Inventory trader2Inventory = finalizedTrades.get(trader2);
		ItemStack acceptItem = trader1Inventory.getItem(30);
		ItemMeta acceptItemMeta = acceptItem.getItemMeta();
		acceptItemMeta.setDisplayName(ChatColor.GREEN + "ACCEPT TRADE");
		acceptItem.setItemMeta(acceptItemMeta);

		ItemStack declineItem = trader1Inventory.getItem(32);
		ItemMeta declineItemMeta = declineItem.getItemMeta();
		declineItemMeta.setDisplayName(ChatColor.RED + "DECLINE TRADE");
		declineItem.setItemMeta(declineItemMeta);

		ItemStack trader1Skull = trader1Inventory.getItem(31);
		ItemMeta trader1SkullMeta = trader1Skull.getItemMeta();
		trader1SkullMeta.setDisplayName(ChatColor.GOLD + "Trade Offer from " + ChatColor.RESET + trader1.getName());
		trader1Skull.setItemMeta(trader1SkullMeta); 

		ItemStack trader2Skull = trader2Inventory.getItem(31);
		ItemMeta trader2SkullMeta = trader2Skull.getItemMeta();
		trader2SkullMeta.setDisplayName(ChatColor.GOLD + "Trade Offer from " + ChatColor.RESET + trader2.getName());
		trader2Skull.setItemMeta(trader2SkullMeta); 

		trader1Inventory.setItem(30, acceptItem);
		trader1Inventory.setItem(31, trader1Skull);
		trader1Inventory.setItem(32, declineItem);
		trader2Inventory.setItem(30, acceptItem);
		trader2Inventory.setItem(31, trader2Skull);
		trader2Inventory.setItem(32, declineItem);
		
		Inventory t1Inv = Bukkit.createInventory(null, 36, ChatColor.DARK_GREEN + "Trade Window - " + ChatColor.BLUE + "Their offer.");
		Inventory t2Inv = Bukkit.createInventory(null, 36, ChatColor.DARK_GREEN + "Trade Window - " + ChatColor.BLUE + "Their offer.");
		t2Inv.setContents(trader2Inventory.getContents());
		t1Inv.setContents(trader1Inventory.getContents());
		finalizedTrades.put(trader1, t1Inv);
		finalizedTrades.put(trader2, t2Inv);

		showTradeOfferFinalized(trader1, trader2, t1Inv, t2Inv);
		//exchangeItemsFinalized(trader1, trader2, trader1Inventory, trader2Inventory);
	}

	private static void showTradeOfferFinalized(Player trader1, Player trader2, Inventory t1Inv,
			Inventory t2Inv) {
		trader1.openInventory(t2Inv);
		trader2.openInventory(t1Inv);
	}

	private static void exchangeItemsFinalized(Player trader, Player trader2, Inventory trader1Inventory, Inventory trader2Inventory) {
		giveInventory(trader, trader2Inventory);
		giveInventory(trader2, trader1Inventory);
		trader.sendMessage(TradeUtils.chatPrefix + ChatColor.GREEN + "Your trade with " + ChatColor.RESET + trader2.getName()+ ChatColor.GREEN + " has been fulfilled.");
		trader2.sendMessage(TradeUtils.chatPrefix + ChatColor.GREEN + "Your trade with " + ChatColor.RESET + trader.getName()+ ChatColor.GREEN + " has been fulfilled.");	
		finalizedTrades.get(trader).clear();
		finalizedTrades.get(trader2).clear();
		finalizedTrades.remove(trader);
		finalizedTrades.remove(trader2);
		approvedTradePlayers.remove(trader);
		approvedTradePlayers.remove(trader2);
		tradeList.remove(trader);
		tradeList.remove(trader2);
		currentlyTrading.remove(trader);
		currentlyTrading.remove(trader2);
		tradeRequestTimer.remove(trader);
		tradeRequestTimer.remove(trader2);
		trader.closeInventory();
		trader2.closeInventory();


		
	}

	public static void approvedTrade(Player trader1) {
		if(!approvedTradePlayers.contains(trader1))
		{
			approvedTradePlayers.add(trader1);
			Player trader2 = null;
			if(tradeList.containsKey(trader1))
			{
				trader2 = tradeList.get(trader1);
			}else if(tradeList.containsValue(trader1))
			{
				trader2 = tradeListKey(trader1);
			}
			trader1.closeInventory();
			if(approvedTradePlayers.contains(trader2))
			{
				trader1.sendMessage(TradeUtils.chatPrefix + ChatColor.GREEN + "Trade offer accepted.");
				trader2.sendMessage(TradeUtils.chatPrefix + trader1.getName() + ChatColor.GREEN + " accepted your trade offer.");
				exchangeItemsFinalized(trader1, trader2, finalizedTrades.get(trader1), finalizedTrades.get(trader2));
				
			}else{
				trader1.sendMessage(TradeUtils.chatPrefix + ChatColor.GREEN + "Trade offer accepted, waiting for " + ChatColor.RESET + trader2.getName());
				trader2.sendMessage(TradeUtils.chatPrefix + trader1.getName() + ChatColor.GREEN + " accepted your trade offer, just waiting on you.");
			}

		}
		
	}

}