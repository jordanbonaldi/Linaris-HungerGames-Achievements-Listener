package net.theuniverscraft.HgAchievementsListener;

import java.util.HashMap;
import java.util.List;

import net.theuniverscraft.HgAchievements.AchievementType;
import net.theuniverscraft.HgAchievements.Managers.DbManager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class AchievementsListener implements Listener {
	private HashMap<String, Location> m_lastPos = new HashMap<String, Location>();
	private HashMap<String, Long> m_lastKill = new HashMap<String, Long>();	
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
				
		if(!m_lastPos.containsKey(player.getName())) {
			m_lastPos.put(player.getName(), player.getLocation());
		}
		else {
			Location last = m_lastPos.get(player.getName());
			
			if(!last.getWorld().getName().equals(player.getWorld().getName())) {
				m_lastPos.put(player.getName(), player.getLocation());
			}
			else {
				if(last.distance(player.getLocation()) >= 1) {
					DbManager.getInstance().addAction(player, AchievementType.SPEEDER);
					m_lastPos.put(player.getName(), player.getLocation());
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player dead = event.getEntity();
		Player killer = dead.getKiller();
		
		if(killer == null) {
			if(dead.getLastDamageCause().getCause() == DamageCause.FIRE) {
				Player nearestPlayer = null;
				List<Entity> entities = dead.getNearbyEntities(10D, 10D, 10D);
				for(Entity entity : entities) {
					if(entity instanceof Player) {
						if(nearestPlayer == null || dead.getLocation().distanceSquared(entity.getLocation()) < 
								dead.getLocation().distanceSquared(nearestPlayer.getLocation())) {
							nearestPlayer = (Player) entity;
						}
					}
				}
				
				if(nearestPlayer != null) {
					DbManager.getInstance().addAction(nearestPlayer, AchievementType.BURN_ENNEMIE);
				}
			}
		}
		else {
			if(killer.getItemInHand() != null) {
				if(killer.getItemInHand().getType() == Material.IRON_AXE) {
					DbManager.getInstance().addAction(killer, AchievementType.IRON_AXE_KILL);
				}
				else if(killer.getItemInHand().getType() == Material.IRON_SWORD) {
					DbManager.getInstance().addAction(killer, AchievementType.IRON_SWORD_KILL);
				}
				else if(killer.getItemInHand().getType() == Material.WOOD_SWORD) {
					DbManager.getInstance().addAction(killer, AchievementType.WOOD_SWORD_KILL);
				}
			}
		}
		
		if(killer != null) {
			DbManager.getInstance().addAction(killer, AchievementType.KILLER);
			DbManager.getInstance().addAction(killer, AchievementType.KILL_FIRST_ENNEMIE);
			
			if(m_lastKill.containsKey(killer.getName())) {
				if(m_lastKill.get(killer.getName()) + 10000 >= System.currentTimeMillis()) {
					DbManager.getInstance().addAction(killer, AchievementType.DOUBLE_KILL);
				}
			}
			m_lastKill.put(killer.getName(), System.currentTimeMillis());
		}
		
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		Entity dead = event.getEntity();
		if(dead.getLastDamageCause() instanceof EntityDamageByEntityEvent)
		{
			EntityDamageByEntityEvent nEvent = (EntityDamageByEntityEvent) dead.getLastDamageCause();
			if(nEvent.getDamager() instanceof Player || nEvent.getDamager() instanceof Arrow ||
					(nEvent.getDamager() instanceof Wolf && ((Wolf) nEvent.getDamager()).isTamed()))
			{
				Player killer = null;
				if(nEvent.getDamager() instanceof Wolf) {
					Wolf wolf = (Wolf) nEvent.getDamager();
					killer = (Player) wolf.getOwner();
				}
				else if(nEvent.getDamager() instanceof Arrow) {
					Arrow arrow = (Arrow) nEvent.getDamager();
					if(arrow.getShooter() instanceof Player) killer = (Player) arrow.getShooter();
					else return;
				}
				else {
					killer = (Player) nEvent.getDamager();
				}
				if(dead.getType() == EntityType.COW) {
					DbManager.getInstance().addAction(killer, AchievementType.KILL_COW);
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.getBlock().getType() == Material.IRON_ORE) {
			DbManager.getInstance().addAction(event.getPlayer(), AchievementType.BREAK_IRON);
		}
		else if(event.getBlock().getType() == Material.LOG) {
			DbManager.getInstance().addAction(event.getPlayer(), AchievementType.BREAK_LOG);
		}
		else if(event.getBlock().getType() == Material.COAL_ORE) {
			DbManager.getInstance().addAction(event.getPlayer(), AchievementType.BREAK_COAL);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(event.getItem() == null) return;
			if(event.getItem().getType() == Material.FLINT_AND_STEEL) {
				DbManager.getInstance().addAction(event.getPlayer(), AchievementType.USE_FLINT_AND_STEEL);
			}
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(event.getCause() == TeleportCause.ENDER_PEARL) {
			DbManager.getInstance().addAction(event.getPlayer(), AchievementType.USE_ENDERPEARLS);
		}
	}
	
	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		if(event.getItem().getType() == Material.GOLDEN_APPLE) {
			DbManager.getInstance().addAction(event.getPlayer(), AchievementType.EAT_GOLDEN_APPLE);
		}
	}
	
	@EventHandler
	public void onCraftItem(CraftItemEvent event) {
		if(event.getCurrentItem().getType() == Material.IRON_CHESTPLATE) {
			Player player = (Player) event.getWhoClicked();
			DbManager.getInstance().addAction(player, AchievementType.MAKE_IRON_ARMOR);
		}
	}
}
