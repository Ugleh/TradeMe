package com.ugleh.trademe;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.ugleh.trademe.commands.CommandTrade;

public class TradeTimer implements Runnable {
	private BukkitTask toCancel;
	private double cooldown;
	private Player tradeFrom;

	public TradeTimer(Player tradeFrom, double cooldown) {
		this.cooldown = cooldown;
		this.tradeFrom = tradeFrom;
	}

	@Override
	public void run() {
		cooldown -= 0.1;
		if ((int) Math.round(cooldown) >= 1) {
			Player trader1 = tradeFrom;
			Player trader2 = CommandTrade.tradeList.get(trader1);
			if (CommandTrade.finalizedTrades.containsKey(trader1)
					|| CommandTrade.finalizedTrades.containsKey(trader2)) {
				if (CommandTrade.finalizedTrades.containsKey(trader1)) {
					ItemStack newItem = CommandTrade.finalizedTrades.get(trader1).getItem(31);
					newItem.setAmount((int) Math.round(cooldown) / 4);
					CommandTrade.finalizedTrades.get(trader1).setItem(31, newItem);
				}
				if (CommandTrade.finalizedTrades.containsKey(trader2)) {
					ItemStack newItem2 = CommandTrade.finalizedTrades.get(trader2).getItem(31);
					newItem2.setAmount((int) Math.round(cooldown) / 4);
					CommandTrade.finalizedTrades.get(trader2).setItem(31, newItem2);
				}
			} else if (CommandTrade.currentlyTrading.containsKey(trader1) || CommandTrade.currentlyTrading.containsKey(trader2)) {
				ItemStack newItem = CommandTrade.currentlyTrading.get(trader1).getItem(31);
				newItem.setAmount((int) Math.round(cooldown) / 4);
				CommandTrade.currentlyTrading.get(trader1).setItem(31, newItem);

				ItemStack newItem2 = CommandTrade.currentlyTrading.get(trader2).getItem(31);
				newItem2.setAmount((int) Math.round(cooldown) / 4);
				CommandTrade.currentlyTrading.get(trader2).setItem(31, newItem2);
			} else {
				toCancel.cancel();
				CommandTrade.cancelTradeExpired(trader1);
			}
		}
		if (cooldown <= 0) {
			toCancel.cancel();
			CommandTrade.cancelTradeExpired(tradeFrom);
		}
	}

	public void setCancelTask(BukkitTask task) {
		this.toCancel = task;
	}
}
