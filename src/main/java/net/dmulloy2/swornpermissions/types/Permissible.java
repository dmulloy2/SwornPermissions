/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.types;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.data.DataSerializable;
import net.dmulloy2.swornpermissions.data.backend.SQLBackend;
import net.dmulloy2.util.NumberUtil;

import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
import org.bukkit.permissions.Permission;

/**
 * @author dmulloy2
 */

public abstract class Permissible implements DataSerializable
{
	protected Map<String, Long> timestamps;
	protected List<String> permissionNodes;
	protected Set<String> sortedPermissions;
	protected Map<String, Boolean> permissions;

	protected Map<String, Object> options;
	protected String prefix;
	protected String suffix;

	protected String worldName;

	protected final String name;
	protected final SwornPermissions plugin;

	// Base Constructor
	protected Permissible(SwornPermissions plugin, String name, String world)
	{
		this.name = name;
		this.plugin = plugin;
		this.worldName = world;
		this.timestamps = new HashMap<>();
		this.permissionNodes = new ArrayList<>();
		this.sortedPermissions = new LinkedHashSet<>();
		this.permissions = new LinkedHashMap<>();
		this.options = new HashMap<>();
		this.prefix = "";
		this.suffix = "";
	}

	protected Permissible(SwornPermissions plugin, String name, String world, MemorySection section)
	{
		this(plugin, name, world);
		this.loadFromDisk(section);
	}

	protected Permissible(SwornPermissions plugin, String name, String world, ResultSet results) throws SQLException
	{
		this(plugin, name, world);
		this.loadFromSQL(results);
	}

	// ---- I/O

	public void loadFromDisk(MemorySection section)
	{
		this.permissionNodes = new ArrayList<>(section.getStringList("permissions"));

		if (section.isSet("options"))
		{
			Map<String, Object> values = section.getConfigurationSection("options").getValues(false);
			for (Entry<String, Object> entry : values.entrySet())
				if (! entry.getValue().toString().isEmpty())
					options.put(entry.getKey().toLowerCase(), entry.getValue());

			this.prefix = options.containsKey("prefix") ? options.get("prefix").toString() : "";
			this.suffix = options.containsKey("suffix") ? options.get("suffix").toString() : "";
		}

		if (section.isSet("timestamps"))
		{
			Map<String, Object> values = section.getConfigurationSection("timestamps").getValues(false);
			for (Entry<String, Object> entry : values.entrySet())
			{
				long expires = NumberUtil.toLong(entry.getValue());
				if (expires < System.currentTimeMillis())
					timestamps.put(entry.getKey(), expires);
			}
		}
	}

	public void loadFromSQL(ResultSet results) throws SQLException
	{
		results.next();

		List<String> permissions = SQLBackend.fromArrayString(results.getString("permissions"));
		this.permissionNodes = new ArrayList<>(permissions);

		Map<String, String> options = SQLBackend.fromArrayStrings(results.getString("option_keys"),
				results.getString("option_values"));
		for (Entry<String, String> entry : options.entrySet())
			if (! entry.getValue().toString().isEmpty())
				this.options.put(entry.getKey().toLowerCase(), entry.getValue());

		this.prefix = options.containsKey("prefix") ? options.get("prefix").toString() : "";
		this.suffix = options.containsKey("suffix") ? options.get("suffix").toString() : "";

		Map<String, String> timestamps = SQLBackend.fromArrayStrings(results.getString("timestamp_keys"),
				results.getString("timestamp_values"));
		for (Entry<String, String> entry : timestamps.entrySet())
		{
			long expires = NumberUtil.toLong(entry.getValue());
			if (expires < System.currentTimeMillis())
				this.timestamps.put(entry.getKey(), expires);
		}
	}

	public abstract boolean shouldBeSaved();

	public String getSaveName()
	{
		return name;
	}

	// ---- Permission Management

	public final void addPermission(String node)
	{
		permissionNodes.add(node);
	}

	public final void addTempPermission(String node, long expires)
	{
		timestamps.put(node, expires);
	}

	public final void removePermission(String node)
	{
		permissionNodes.remove(node);
	}

	public final void removeTempPermission(String node)
	{
		timestamps.remove(node);
	}

	public boolean hasPermission(String permission)
	{
		Map<String, Boolean> permissions = getPermissions();

		boolean negative = permission.startsWith("-");
		permission = negative ? permission.substring(1) : permission;

		if (permissions.containsKey(permission))
		{
			boolean value = permissions.get(permission);
			return negative ? ! value : value;
		}

		return false;
	}

	/**
	 * Whether or not this permissible has a permission node. Does not take into
	 * account wildcards, children, or group permissions.
	 * 
	 * @param node Permission node to check for
	 * @return True if they have it, false if not
	 */
	public final boolean hasPermissionNode(String node)
	{
		return permissionNodes.contains(node);
	}

	public final boolean hasTempPermission(String node)
	{
		cleanTempPermissions();
		return timestamps.containsKey(node);
	}

	public final String getMatchingPermission(String node)
	{
		List<String> permissions = new ArrayList<>(sortedPermissions);
		if (permissions.contains("*") && ! node.startsWith("-"))
			return "*";

		// Remove *
		permissions.remove("*");

		// Iterate and try to find a match
		for (String permission : permissions)
		{
			if (node.matches(permission))
				return permission;
		}

		// No match :(
		return null;
	}

	// Positive nodes override negative nodes
	protected final void updatePermissionMap()
	{
		// Sort the nodes
		Set<String> permissionNodes = sortPermissions();

		// Update sorted permissions list
		this.sortedPermissions = permissionNodes;
		
		// Translate the set to a hash map
		Map<String, Boolean> permissions = new LinkedHashMap<>();

		if (permissionNodes.contains("*"))
		{
			permissionNodes.remove("*");
			permissions.put("*", true);
		}

		for (String permission : permissionNodes)
		{
			if (permission.startsWith("-"))
			{
				permission = permission.substring(1);
				permissions.put(permission, false);
			}
			else
			{
				permissions.put(permission, true);
			}
		}

		// Update permission map
		this.permissions = permissions;
	}

	public abstract void updatePermissions(boolean force);

	protected abstract Set<String> sortPermissions();

	// Order: *, negative, positive
	protected final Set<String> sort(List<String> permissions)
	{
		Set<String> ret = new LinkedHashSet<>();

		// Add * permission first
		if (permissions.contains("*"))
		{
			permissions.remove("*");
			ret.add("*");
		}

		// Add negative nodes next
		Iterator<String> iter = permissions.iterator();
		while (iter.hasNext())
		{
			String permission = iter.next();
			if (permission.startsWith("-"))
			{
				iter.remove();
				ret.add(permission);
			}
		}

		// Add positive nodes last, overriding negatives
		iter = permissions.iterator();
		while (iter.hasNext())
		{
			String permission = iter.next();
			iter.remove();
			if (ret.contains("-" + permission))
				ret.remove("-" + permission);
			ret.add(permission);
		}

		return ret;
	}

	// Wildcard support
	protected final List<String> getMatchingNodes(List<String> permissionNodes)
	{
		List<String> ret = new ArrayList<>();

		for (String node : permissionNodes)
		{
			// '*' is handled later
			if (node.equals("*"))
			{
				ret.add("*");
				continue;
			}

			ret.add(node);
			boolean negative = node.startsWith("-");
			node = negative ? node.substring(1) : node;

			if (node.contains("*"))
			{
				for (Permission permission : plugin.getServer().getPluginManager().getPermissions())
				{
					String name = permission.getName();
					if (name.matches(node))
					{
						name = negative ? "-" + name : name;
						ret.add(name);
					}
				}
			}
		}

		return ret;
	}

	protected final List<String> getAllChildren(List<String> permissions)
	{
		List<String> ret = new ArrayList<String>();

		for (String permission : permissions)
		{
			boolean negative = permission.startsWith("-");
			ret.add(permission);

			String node = negative ? permission.substring(1) : permission;
			List<String> children = getChildren(node);
			if (children != null)
			{
				for (String child : children)
				{
					child = negative ? "-" + child : child;
					ret.add(child);
				}
			}
		}

		return ret;
	}

	protected final List<String> getChildren(String node)
	{
		if (node.equals("*"))
		{
			List<String> ret = new ArrayList<String>();
			for (Permission permission : plugin.getPermissionHandler().getPermissions())
			{
				ret.add(permission.getName());
				List<String> children = getChildren(permission);
				if (children != null)
					ret.addAll(children);
			}

			return ! ret.isEmpty() ? ret : null;
		}

		return getChildren(plugin.getPermissionHandler().getPermission(node));
	}

	protected final List<String> getChildren(Permission permission)
	{
		if (permission == null)
			return null;

		Map<String, Boolean> children = permission.getChildren();
		if (children == null || children.isEmpty())
			return null;

		List<String> ret = new ArrayList<String>();
		for (Entry<String, Boolean> child : children.entrySet())
		{
			if (child.getValue())
			{
				ret.add(child.getKey());
				List<String> childNodes = getChildren(child.getKey());
				if (childNodes != null)
					ret.addAll(childNodes);
			}
		}

		return ! ret.isEmpty() ? ret : null;
	}

	// ---- Getters and Setters

	public final String getName()
	{
		return name;
	}

	public final Map<String, Boolean> getPermissions()
	{
		if (permissions.isEmpty())
			updatePermissionMap();

		return permissions;
	}

	public List<String> getPermissionNodes()
	{
		List<String> ret = new ArrayList<String>();

		// Permission nodes
		ret.addAll(permissionNodes);

		// Temp Permissions
		if (! timestamps.isEmpty())
		{
			cleanTempPermissions();
			ret.addAll(timestamps.keySet());
		}

		return ret;
	}

	protected final void cleanTempPermissions()
	{
		Iterator<Entry<String, Long>> iter = timestamps.entrySet().iterator();
		while (iter.hasNext())
		{
			Entry<String, Long> entry = iter.next();
			if (entry.getKey().isEmpty() || entry.getValue() > System.currentTimeMillis())
				iter.remove();
		}
	}

	public List<String> getAllPermissionNodes()
	{
		return getPermissionNodes();
	}

	public Map<String, Object> getOptions()
	{
		return options;
	}

	public Map<String, Long> getTimestamps()
	{
		return timestamps;
	}

	public Object getOption(String key)
	{
		return options.get(key.toLowerCase());
	}

	public final void setOption(String key, Object value)
	{
		key = key.toLowerCase();

		if (value == null)
			options.remove(key);
		else
			options.put(key, value);
	}

	public boolean hasOption(String key)
	{
		return options.containsKey(key.toLowerCase());
	}

	public String getPrefix()
	{
		return prefix;
	}

	public final void setPrefix(String prefix)
	{
		options.put("prefix", prefix);
		this.prefix = prefix;
	}

	public final void resetPrefix()
	{
		options.remove("prefix");
		this.prefix = findPrefix();
	}

	public abstract String findPrefix();

	public String getSuffix()
	{
		return suffix;
	}

	public final void setSuffix(String suffix)
	{
		options.put("suffix", suffix);
		this.suffix = suffix;
	}

	public final void resetSuffix()
	{
		options.remove("suffix");
		this.suffix = "";
	}

	public final World getWorld()
	{
		return plugin.getServer().getWorld(worldName);
	}

	// ---- Conversion Methods

	/**
	 * @deprecated For conversion use ONLY
	 */
	@Deprecated
	public final void setPermissionNodes(List<String> permissionNodes)
	{
		this.permissionNodes = permissionNodes;
	}

	/**
	 * @deprecated For conversion use ONLY
	 */
	@Deprecated
	public final void setOptions(Map<String, Object> options)
	{
		for (Entry<String, Object> entry : options.entrySet())
			this.options.put(entry.getKey().toLowerCase(), entry.getValue());
	}

	// ---- Required Abstract Methods

	@Override
	public abstract String toString();

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();
}