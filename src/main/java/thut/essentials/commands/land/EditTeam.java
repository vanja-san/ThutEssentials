package thut.essentials.commands.land;

import java.util.UUID;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import thut.essentials.ThutEssentials;
import thut.essentials.land.LandManager;
import thut.essentials.land.LandManager.LandTeam;
import thut.essentials.land.LandSaveHandler;
import thut.essentials.util.BaseCommand;
import thut.essentials.util.ConfigManager;
import thut.essentials.util.Coordinate;
import thut.essentials.util.RuleManager;

public class EditTeam extends BaseCommand
{

    public EditTeam()
    {
        super("editteam", 0, "editTeam");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayer player = getPlayerBySender(sender);
        LandTeam landTeam = LandManager.getTeam(player);
        String arg = args[0];
        String message = "";
        if (args.length > 1) message = args[1];
        for (int i = 2; i < args.length; i++)
        {
            message = message + " " + args[i];
        }
        message = RuleManager.format(message);
        if (arg.equalsIgnoreCase("public"))
        {
            if (!landTeam.isAdmin(player)) throw new CommandException("You are not allowed to do that.");
            landTeam.allPublic = parseBoolean(message);
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Set all public to " + message));
            LandSaveHandler.saveTeam(landTeam.teamName);
            return;
        }
        if (arg.equalsIgnoreCase("exit"))
        {
            if (!landTeam.hasRankPerm(player.getUniqueID(), LandTeam.EDITMESSAGES))
                throw new CommandException("You are not allowed to do that.");
            landTeam.exitMessage = message;
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Set Exit Message to " + message));
            LandSaveHandler.saveTeam(landTeam.teamName);
            return;
        }
        if (arg.equalsIgnoreCase("enter"))
        {
            if (!landTeam.hasRankPerm(player.getUniqueID(), LandTeam.EDITMESSAGES))
                throw new CommandException("You are not allowed to do that.");
            landTeam.enterMessage = message;
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Set Enter Message to " + message));
            LandSaveHandler.saveTeam(landTeam.teamName);
            return;
        }
        if (arg.equalsIgnoreCase("deny"))
        {
            if (!landTeam.hasRankPerm(player.getUniqueID(), LandTeam.EDITMESSAGES))
                throw new CommandException("You are not allowed to do that.");
            landTeam.denyMessage = message;
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Set Deny Message to " + message));
            LandSaveHandler.saveTeam(landTeam.teamName);
            return;
        }
        if (arg.equalsIgnoreCase("prefix"))
        {
            if (!landTeam.hasRankPerm(player.getUniqueID(), LandTeam.SETPREFIX))
                throw new CommandException("You are not allowed to do that.");
            if (message.length() > ConfigManager.INSTANCE.prefixLength)
                message = message.substring(0, ConfigManager.INSTANCE.prefixLength);
            landTeam.prefix = message;
            sender.sendMessage(
                    new TextComponentString(TextFormatting.GREEN + "Set Prefix to " + TextFormatting.RESET + message));
            refreshTeam(landTeam, server);
            LandSaveHandler.saveTeam(landTeam.teamName);
            return;
        }
        if (arg.equalsIgnoreCase("home"))
        {
            if (!landTeam.hasRankPerm(player.getUniqueID(), LandTeam.SETHOME))
                throw new CommandException("You are not allowed to do that.");
            landTeam.home = new Coordinate(player.getPosition(), player.dimension);
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Set Team Home to " + landTeam.home));
            LandSaveHandler.saveTeam(landTeam.teamName);
            return;
        }
        if (arg.equalsIgnoreCase("reserve") && ThutEssentials.perms.hasPermission(player, "land.team.reserve"))
        {
            landTeam.reserved = Boolean.parseBoolean(message);
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "reserved set to " + landTeam.reserved));
            LandSaveHandler.saveTeam(landTeam.teamName);
            return;
        }
        if (arg.equalsIgnoreCase("noPlayerDamage")
                && ThutEssentials.perms.hasPermission(player, "land.team.noplayerdamage"))
        {
            landTeam.noPlayerDamage = Boolean.parseBoolean(message);
            sender.sendMessage(
                    new TextComponentString(TextFormatting.GREEN + "noPlayerDamage set to " + landTeam.noPlayerDamage));
            LandSaveHandler.saveTeam(landTeam.teamName);
            return;
        }
        if (arg.equalsIgnoreCase("friendlyFire")
                && ThutEssentials.perms.hasPermission(player, "land.team.friendlyfire"))
        {
            landTeam.friendlyFire = Boolean.parseBoolean(message);
            sender.sendMessage(
                    new TextComponentString(TextFormatting.GREEN + "friendlyFire set to " + landTeam.friendlyFire));
            LandSaveHandler.saveTeam(landTeam.teamName);
            return;
        }
        if (arg.equalsIgnoreCase("noMobSpawn") && ThutEssentials.perms.hasPermission(player, "land.team.nomobspawn"))
        {
            landTeam.noMobSpawn = Boolean.parseBoolean(message);
            sender.sendMessage(
                    new TextComponentString(TextFormatting.GREEN + "noMobSpawn set to " + landTeam.noMobSpawn));
            LandSaveHandler.saveTeam(landTeam.teamName);
            return;
        }
        if (arg.equalsIgnoreCase("noExplosions")
                && ThutEssentials.perms.hasPermission(player, "land.team.noexplosions"))
        {
            landTeam.noExplosions = Boolean.parseBoolean(message);
            sender.sendMessage(
                    new TextComponentString(TextFormatting.GREEN + "noExplosions set to " + landTeam.noExplosions));
            LandSaveHandler.saveTeam(landTeam.teamName);
            return;
        }
    }

    public static void refreshTeam(LandTeam team, MinecraftServer server)
    {
        for (UUID id : team.member)
        {
            try
            {
                EntityPlayer player = server.getPlayerList().getPlayerByUUID(id);
                if (player != null)
                {
                    player.refreshDisplayName();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
