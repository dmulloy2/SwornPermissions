/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.user;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdHasPermission extends UserCommand
{
	public CmdHasPermission(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "has";
		this.name = "permission";
		this.requiredArgs.add("permission");
		this.description = "Checks if a user has a permission";
		this.permission = Permission.USER_HAS_PERMISSION;
	}

	@Override
	public void perform()
	{
		String permission = args[0];
		if (user.hasPermission(permission))
		{
			sendpMessage("User &b{0} &ehas access to permission &b{1}&e.", user.getName(), permission);

			String node = user.getMatchingPermission(permission);
			if (node != null && ! node.equalsIgnoreCase(permission))
			{
				sendpMessage("Matching node: &b{0}", node);
			}
		}
		else
		{
			sendpMessage("User &b{0} &edoes not have access to permission &b{1}&e.", user.getName(), permission);

			String node = user.getMatchingPermission(permission);
			if (node != null)
				sendpMessage("Negated, node: &b{0}", node);
		}
	}
}