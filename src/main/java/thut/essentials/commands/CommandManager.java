package thut.essentials.commands;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import thut.essentials.Essentials;
import thut.essentials.land.LandEventsHandler;
import thut.essentials.util.PlayerMover;

public class CommandManager
{
    public static GameProfile getProfile(final MinecraftServer server, final UUID id)
    {
        GameProfile profile = null;
        // First check profile cache.
        if (id != null) profile = server.getPlayerProfileCache().getProfileByUUID(id);
        if (profile == null) profile = new GameProfile(id, null);

        // Try to fill profile via secure method.
        LandEventsHandler.TEAMMANAGER.queueUpdate(profile);
        return profile;
    }

    public static GameProfile getProfile(final MinecraftServer server, final String arg)
    {
        UUID id = null;
        String name = null;

        // First check if arg is a UUID
        try
        {
            id = UUID.fromString(arg);
        }
        catch (final Exception e)
        {
            // If not a UUID, arg is the name.
            name = arg;
        }

        GameProfile profile = null;

        // First check profile cache.
        if (id != null) profile = server.getPlayerProfileCache().getProfileByUUID(id);
        if (profile == null) profile = new GameProfile(id, name);

        // Try to fill profile via secure method.
        LandEventsHandler.TEAMMANAGER.queueUpdate(profile);

        // Temporarily update the UUID from server player list if possible
        if (profile.getId() == null)
        {
            final PlayerEntity player = server.getPlayerList().getPlayerByUsername(profile.getName());
            profile = player.getGameProfile();
        }

        return profile;
    }

    public static boolean hasPerm(final CommandSource source, final String permission)
    {
        try
        {
            final ServerPlayerEntity player = source.asPlayer();
            return CommandManager.hasPerm(player, permission);
        }
        catch (final CommandSyntaxException e)
        {
            // TODO decide what to actually do here?
            return true;
        }
    }

    public static boolean hasPerm(final ServerPlayerEntity player, final String permission)
    { /*
       * Check if the node is registered, if not, register it as OP, and send
       * error message about this.
       */
        if (!PermissionAPI.getPermissionHandler().getRegisteredNodes().contains(permission))
        {
            final String message = "Autogenerated node, this is a bug and should be pre-registered.";
            PermissionAPI.getPermissionHandler().registerNode(permission, DefaultPermissionLevel.OP, message);
            System.err.println(message + ": " + permission);
        }
        return PermissionAPI.hasPermission(player, permission);
    }

    public static void register_commands(final CommandDispatcher<CommandSource> commandDispatcher)
    {
        // We do this first, as commands might need it.
        MinecraftForge.EVENT_BUS.register(new PlayerMover());
        // Register commands.
        thut.essentials.commands.economy.Balance.register(commandDispatcher);
        thut.essentials.commands.economy.Pay.register(commandDispatcher);

        thut.essentials.commands.admin.StaffChat.register(commandDispatcher);

        thut.essentials.commands.homes.Homes.register(commandDispatcher);
        thut.essentials.commands.homes.Create.register(commandDispatcher);
        thut.essentials.commands.homes.Delete.register(commandDispatcher);

        thut.essentials.commands.warps.Warps.register(commandDispatcher);
        thut.essentials.commands.warps.Create.register(commandDispatcher);
        thut.essentials.commands.warps.Delete.register(commandDispatcher);

        thut.essentials.commands.tpa.Tpa.register(commandDispatcher);
        thut.essentials.commands.tpa.TpAccept.register(commandDispatcher);
        thut.essentials.commands.tpa.TpToggle.register(commandDispatcher);

        thut.essentials.commands.misc.Back.register(commandDispatcher);
        thut.essentials.commands.misc.RTP.register(commandDispatcher);
        thut.essentials.commands.misc.Bed.register(commandDispatcher);
        thut.essentials.commands.misc.Config.register(commandDispatcher);
        thut.essentials.commands.misc.Kits.register(commandDispatcher);
        thut.essentials.commands.misc.Spawn.register(commandDispatcher);
        thut.essentials.commands.misc.Nick.register(commandDispatcher);

        thut.essentials.commands.structures.Structuregen.register(commandDispatcher);

        thut.essentials.commands.land.util.Chat.register(commandDispatcher);
        thut.essentials.commands.land.util.Check.register(commandDispatcher);
        thut.essentials.commands.land.util.Home.register(commandDispatcher);
        thut.essentials.commands.land.util.Members.register(commandDispatcher);
        thut.essentials.commands.land.util.Reload.register(commandDispatcher);
        thut.essentials.commands.land.util.Teams.register(commandDispatcher);
        thut.essentials.commands.land.util.Show.register(commandDispatcher);

        thut.essentials.commands.util.Speed.register(commandDispatcher);
        thut.essentials.commands.util.Repair.register(commandDispatcher);
        thut.essentials.commands.util.Heal.register(commandDispatcher);
        thut.essentials.commands.util.RAM.register(commandDispatcher);

        thut.essentials.commands.land.management.Create.register(commandDispatcher);
        thut.essentials.commands.land.management.Rename.register(commandDispatcher);
        thut.essentials.commands.land.management.Invite.register(commandDispatcher);
        thut.essentials.commands.land.management.Join.register(commandDispatcher);
        thut.essentials.commands.land.management.Admins.register(commandDispatcher);
        thut.essentials.commands.land.management.Delete.register(commandDispatcher);
        thut.essentials.commands.land.management.Kick.register(commandDispatcher);
        thut.essentials.commands.land.management.Edit.register(commandDispatcher);
        thut.essentials.commands.land.management.Ranks.register(commandDispatcher);
        thut.essentials.commands.land.management.Relations.register(commandDispatcher);

        thut.essentials.commands.land.claims.Claim.register(commandDispatcher);
        thut.essentials.commands.land.claims.Owner.register(commandDispatcher);
        thut.essentials.commands.land.claims.Unclaim.register(commandDispatcher);
        thut.essentials.commands.land.claims.Deed.register(commandDispatcher);
        thut.essentials.commands.land.claims.Load.register(commandDispatcher);
        thut.essentials.commands.land.claims.Unload.register(commandDispatcher);

        thut.essentials.commands.util.Fly.register(commandDispatcher);
    }

    public static IFormattableTextComponent makeFormattedCommandLink(final String text, final String command,
            final Object... args)
    {
        final IFormattableTextComponent message = Essentials.config.getMessage(text, args);
        return message.setStyle(message.getStyle().setClickEvent(new ClickEvent(Action.RUN_COMMAND, command)));
    }

    public static IFormattableTextComponent makeFormattedComponent(final String text,
            final TextFormatting colour,
            final boolean bold, final Object... args)
    {
        final IFormattableTextComponent message = Essentials.config.getMessage(text, args);
        Style style = message.getStyle();
        if (colour != null) style = style.setColor(Color.fromTextFormatting(colour));
        if (bold) style = style.setBold(bold);
        return message.setStyle(style);
    }

    public static IFormattableTextComponent makeFormattedComponent(final String text,
            final TextFormatting colour,
            final boolean bold)
    {
        return CommandManager.makeFormattedComponent(text, colour, bold, new Object[0]);
    }

    public static IFormattableTextComponent makeFormattedComponent(final String text)
    {
        return CommandManager.makeFormattedComponent(text, null, false, new Object[0]);
    }

    public static IFormattableTextComponent makeFormattedCommandLink(final String text, final String command)
    {
        return CommandManager.makeFormattedCommandLink(text, command, new Object[0]);
    }

}
