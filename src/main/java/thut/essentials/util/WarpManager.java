package thut.essentials.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraft.world.World;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import thut.essentials.Config;
import thut.essentials.Essentials;
import thut.essentials.commands.CommandManager;
import thut.essentials.land.LandManager.KGobalPos;

public class WarpManager
{
    public static Map<String, KGobalPos> warpLocs;

    final static Field warpsField;

    static
    {
        Field temp = null;
        try
        {
            temp = Config.class.getDeclaredField("warps");
        }
        catch (SecurityException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        warpsField = temp;
    }

    public static void init()
    {
        WarpManager.warpLocs = Maps.newHashMap();
        for (final String s : Essentials.config.warps)
        {
            if (!s.contains("->"))
            {
                Essentials.LOGGER.error("Error in loading warp for {}", s);
                continue;
            }
            final String[] args = s.split("->");
            if (args.length != 2)
            {
                Essentials.LOGGER.error("Error in loading warp for {}", s);
                continue;
            }
            final KGobalPos warp = CoordinateUtls.fromString(args[1]);
            if (warp != null) WarpManager.warpLocs.put(args[0], warp);
        }

        final IPermissionHandler manager = PermissionAPI.getPermissionHandler();
        for (final String s : WarpManager.warpLocs.keySet())
        {
            final String node = "thutessentials.warp." + s;
            if (!manager.getRegisteredNodes().contains(node)) manager.registerNode(node, DefaultPermissionLevel.ALL,
                    "Warp to " + s);
        }
    }

    static int[] getInt(final String val)
    {
        final String[] args = val.split(" ");
        final int dim = args.length == 4 ? Integer.parseInt(args[3]) : 0;
        return new int[] { Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), dim };
    }

    public static int setWarp(final KGobalPos pos, final String name)
    {
        final List<String> warps = Lists.newArrayList(Essentials.config.warps);
        for (final String s : warps)
        {
            final String[] args = s.split("->");
            if (args.length != 2)
            {
                Essentials.LOGGER.error("Error with warp {}", s);
                continue;
            }
            if (args[0].equals(name)) return 1;
        }
        warps.removeIf(s ->
        {
            if (!s.contains("->")) return true;
            return CoordinateUtls.fromString(s) == null;
        });
        final String warp = name + "->" + CoordinateUtls.toString(pos);
        warps.add(warp);
        WarpManager.warpLocs.put(name, pos);
        final IPermissionHandler manager = PermissionAPI.getPermissionHandler();
        final String node = "thutessentials.warp." + name;
        if (!manager.getRegisteredNodes().contains(node)) manager.registerNode(node, DefaultPermissionLevel.ALL,
                "Warp to " + name);
        Essentials.config.warps = warps;
        Essentials.config.write();
        return 0;
    }

    public static int delWarp(final String name)
    {
        final List<String> warps = Lists.newArrayList(Essentials.config.warps);
        for (final String s : warps)
        {
            final String[] args = s.split("->");
            if (args[0].equals(name))
            {
                warps.remove(s);
                WarpManager.warpLocs.remove(name);
                Essentials.config.warps = warps;
                Essentials.config.write();
                return 0;
            }
        }
        return 1;
    }

    public static KGobalPos getWarp(final String name)
    {
        return WarpManager.warpLocs.get(name);
    }

    public static void sendWarpsList(final ServerPlayerEntity player)
    {
        final IPermissionHandler manager = PermissionAPI.getPermissionHandler();
        final PlayerContext context = new PlayerContext(player);
        player.sendMessage(CommandManager.makeFormattedComponent("thutessentials.warps.header"), Util.DUMMY_UUID);
        for (String s : Essentials.config.warps)
        {
            final String[] args = s.split("->");
            if (args.length != 2)
            {
                Essentials.LOGGER.error("Error with warp {}", s);
                continue;
            }
            s = args[0];
            if (!manager.hasPermission(player.getGameProfile(), "thutessentials.warp." + s, context)) continue;
            final IFormattableTextComponent message = CommandManager.makeFormattedComponent(
                    "thutessentials.warps.entry", null, false, s);
            if (s.contains(" ")) s = "\"" + s + "\"";
            Style style = message.getStyle();
            style = style.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/warp " + s));
            player.sendMessage(message.setStyle(style), Util.DUMMY_UUID);
        }
        player.sendMessage(CommandManager.makeFormattedComponent("thutessentials.warps.footer"), Util.DUMMY_UUID);
    }

    public static int attemptWarp(final ServerPlayerEntity player, final String warpName)
    {
        final KGobalPos warp = WarpManager.getWarp(warpName);
        final CompoundNBT tag = PlayerDataHandler.getCustomDataTag(player);
        final CompoundNBT tptag = tag.getCompound("tp");
        final long last = tptag.getLong("warpDelay");
        final long time = player.getServer().getWorld(World.OVERWORLD).getGameTime();
        // Too Soon
        if (last > time && Essentials.config.warpReUseDelay > 0) return 1;
        if (warp != null)
        {
            final IPermissionHandler manager = PermissionAPI.getPermissionHandler();
            final PlayerContext context = new PlayerContext(player);
            // No allowed
            if (!manager.hasPermission(player.getGameProfile(), "thutessentials.warp." + warpName, context)) return 2;
            final ITextComponent teleMess = CommandManager.makeFormattedComponent("thutessentials.warps.warped", null,
                    false, warpName);
            PlayerMover.setMove(player, Essentials.config.warpActivateDelay, warp, teleMess, PlayerMover.INTERUPTED);
            tptag.putLong("warpDelay", time + Essentials.config.warpReUseDelay);
            tag.put("tp", tptag);
            PlayerDataHandler.saveCustomData(player);
            return 0;
        }
        // No warp
        else return 3;
    }
}