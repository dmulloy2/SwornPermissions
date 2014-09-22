/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.listeners;

import lombok.AllArgsConstructor;
import net.dmulloy2.swornpermissions.SwornPermissions;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

/**
 * @author dmulloy2
 */

@AllArgsConstructor
public class ServerListener implements Listener
{
	private final SwornPermissions plugin;

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPluginEnable(PluginEnableEvent event)
	{
		if (plugin.isUpdated())
		{
			long start = System.currentTimeMillis();
			plugin.getLogHandler().log("Plugin {0} enabled. Performing permission update.", event.getPlugin());

			plugin.getPermissionHandler().updateGroups();
			plugin.getPermissionHandler().updateUsers();

			plugin.getLogHandler().log("Permissions updated. Took {0} ms.", System.currentTimeMillis() - start);
		}
	}
}