/**
 * (c) 2016 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

// TODO: Add automatic backup?
public class CmdBackup extends SwornPermissionsCommand
{
	public CmdBackup(SwornPermissions plugin)
	{
		super(plugin);
		this.description = "Backs up all SwornPermissions files";
		this.permission = Permission.CMD_BACKUP;
	}

	@Override
	public void perform()
	{
		plugin.getDataHandler().backup(sender);
	}
}