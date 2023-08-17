package net.minecraft.command.server;

import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public class CommandPardonIp extends CommandBase
{
    /**
     * Gets the name of the command
     */
    public String getName()
    {
        return "pardon-ip";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel()
    {
        return 3;
    }

    /**
     * Check if the given ICommandSender has permission to execute this command
     */
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return server.getPlayerList().getBannedIPs().isLanServer() && super.checkPermission(server, sender);
    }

    /**
     * Gets the usage string for the command.
     */
    public String getUsage(ICommandSender sender)
    {
        return "commands.unbanip.usage";
    }

    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 1 && args[0].length() > 1)
        {
            Matcher matcher = CommandBanIp.IP_PATTERN.matcher(args[0]);

            if (matcher.matches())
            {
                server.getPlayerList().getBannedIPs().removeEntry(args[0]);
                notifyCommandListener(sender, this, "commands.unbanip.success", args[0]);
            }
            else
            {
                throw new SyntaxErrorException("commands.unbanip.invalid");
            }
        }
        else
        {
            throw new WrongUsageException("commands.unbanip.usage");
        }
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, server.getPlayerList().getBannedIPs().getKeys()) : Collections.emptyList();
    }
}
