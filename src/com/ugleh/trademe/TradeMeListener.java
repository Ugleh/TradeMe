package com.ugleh.trademe;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
//import org.bukkit.event.

import com.ugleh.trademe.commands.CommandTrade;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;

public class TradeMeListener implements Listener {
	String chatPrefix = ChatColor.DARK_PURPLE + "[" + ChatColor.LIGHT_PURPLE + "TRADE" + ChatColor.DARK_PURPLE + "] "
			+ ChatColor.RESET;

	@EventHandler
	public void PlayerQuitEvent(PlayerQuitEvent e) {
		if (CommandTrade.tradeList.containsKey(e.getPlayer()) || CommandTrade.tradeList.containsValue(e.getPlayer())) {
			CommandTrade.tradeListKey(e.getPlayer())
					.sendMessage(chatPrefix + ChatColor.RED + "The person you attempted to trade with has left.");
			CommandTrade.tradeCancel(e.getPlayer());
		}
	}

	@EventHandler
	public void InventoryClickEvent(InventoryClickEvent e) {
		if (e.getInventory().getName().toLowerCase().contains("your offer")) {
			if (e.getRawSlot() == 30) {
				e.setCancelled(true);
				CommandTrade.tradeDone((Player) e.getWhoClicked());
				// Accept Trade
			} else if (e.getRawSlot() == 32) {
				e.setCancelled(true);
				CommandTrade.tradeCancel((Player) e.getWhoClicked());
			} else if (e.getRawSlot() >= 36) {
				// Nothing
			} else if (e.getRawSlot() >= 27) {
				e.setCancelled(true);
			}
		}
		if (e.getInventory().getName().toLowerCase().contains("their offer")) {
			e.setCancelled(true);
			if (e.getRawSlot() == 30) {
				e.setCancelled(true);
				CommandTrade.approvedTrade((Player) e.getWhoClicked());
				// Accept Trade
			} else if (e.getRawSlot() == 32) {
				e.setCancelled(true);
				CommandTrade.tradeCancel((Player) e.getWhoClicked());
				// Decline Trade
			}
		}

	}

	@EventHandler
	public void InventoryCloseEvent(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		if(CommandTrade.finalizedTrades.containsKey(p)) return;
		if (CommandTrade.tradeList.containsKey(p) || CommandTrade.tradeList.containsValue(p)) {
			if (e.getInventory().equals(CommandTrade.currentlyTrading.get(p))) // Left
																				// the
																				// active
																				// trade
																				// inventory.
			{
				p.sendMessage(chatPrefix + ChatColor.GOLD + " You left an active trade. What will you do?");
				PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a(
						"[\"\",{\"text\":\">>>>\",\"color\":\"dark_blue\",\"bold\":true},{\"text\":\"[ \",\"color\":\"gold\",\"bold\":true},{\"text\":\"RETURN\",\"color\":\"green\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/trade return\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click to return to trade.\",\"color\":\"green\"}]}}},{\"text\":\" | \",\"color\":\"black\",\"bold\":true},{\"text\":\"CANCEL\",\"color\":\"red\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/trade cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click to cancel trade.\",\"color\":\"red\"}]}}},{\"text\":\" ]\",\"color\":\"gold\",\"bold\":true},{\"text\":\"<<<<\",\"color\":\"dark_blue\",\"bold\":true}]"));
				((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
			}

		}
	}
}
