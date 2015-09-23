/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.types.User;

import org.bukkit.entity.Player;

/**
 * @author dmulloy2
 */

public class CmdNick extends SwornPermissionsCommand
{
	public CmdNick(SwornPermissions plugin)
	{
		super(plugin);
		this.name = "nick";
		this.optionalArgs.add("user");
		this.requiredArgs.add("nick");
		this.description = "Set a player''s nickname";
		this.permission = Permission.CMD_NICK;
	}

	@Override
	public void perform()
	{
		if (args.length == 1)
		{
			User user = getUser();
			if (user == null)
				return;

			String nick = args[0];
			if (nick.equalsIgnoreCase("off") || nick.equalsIgnoreCase("null"))
			{
				user.setOption("name", null);
				sendpMessage("You have removed your nickname.");
				return;
			}

			user.setOption("name", args[0]);
			sendpMessage("You have set your nickname to \"&r{0}&e\"", args[0]);
		}
		else
		{
			User user = getUser(0);
			if (user == null)
				return;

			String nick = args[1];
			if (nick.equalsIgnoreCase("off") || nick.equalsIgnoreCase("null"))
			{
				user.setOption("name", null);

				Player target = user.getPlayer();
				if (target != null && ! hasArgument("--silent"))
					sendpMessage(target, "Your nickname has been removed.");

				sendpMessage("You have removed &b{0}&e''s nickname.", user.getName());
				return;
			}

			user.setOption("name", args[1]);

			Player target = user.getPlayer();
			if (target != null && ! hasArgument("--silent"))
				sendpMessage(target, "Your nickname is now \"&r{0}&e\"", args[1]);

			sendpMessage("You have set &b{0}&e''s nickname to \"&r{1}&e\"", user.getName(), user.getDisplayName());
		}
	}
}