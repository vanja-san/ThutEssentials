package thut.essentials.commands.land.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import thut.essentials.Essentials;
import thut.essentials.commands.CommandManager;
import thut.essentials.land.LandManager;
import thut.essentials.land.LandManager.LandTeam;
import thut.essentials.util.PlayerDataHandler;
import thut.essentials.util.PlayerMover;

public class Home
{

    public static void register(final CommandDispatcher<CommandSource> commandDispatcher)
    {
        final String name = "team_home";
        if (Essentials.config.commandBlacklist.contains(name)) return;
        String perm;
        PermissionAPI.registerNode(perm = "command." + name, DefaultPermissionLevel.ALL,
                "Can the player use the team_home commant.");

        // Setup with name and permission
        LiteralArgumentBuilder<CommandSource> command = Commands.literal(name).requires(cs -> CommandManager.hasPerm(cs,
                perm));
        // No target argument version
        command = command.executes(ctx -> Home.execute(ctx.getSource()));

        // Actually register the command.
        commandDispatcher.register(command);
    }

    private static int execute(final CommandSource source) throws CommandSyntaxException
    {
        final PlayerEntity player = source.asPlayer();
        final LandTeam team = LandManager.getTeam(player);

        if (team.home == null)
        {
            source.sendErrorMessage(CommandManager.makeFormattedComponent("thutessentials.team.nohomeset"));
            return 1;
        }

        final int[] home = { team.home.x, team.home.y, team.home.z, team.home.dim };

        final CompoundNBT tag = PlayerDataHandler.getCustomDataTag(player);
        final CompoundNBT tptag = tag.getCompound("tp");
        final long last = tptag.getLong("homeDelay");
        final long time = player.getServer().getWorld(DimensionType.OVERWORLD).getGameTime();
        if (last > time)
        {
            player.sendMessage(CommandManager.makeFormattedComponent("thutessentials.tp.tosoon", TextFormatting.RED,
                    false));
            return 1;
        }

        final ITextComponent teleMess = CommandManager.makeFormattedComponent("Warping to your Team's Home",
                TextFormatting.GREEN);
        tptag.putLong("homeDelay", time + Essentials.config.homeReUseDelay);
        tag.put("tp", tptag);
        PlayerDataHandler.saveCustomData(player);
        PlayerMover.setMove(player, Essentials.config.homeActivateDelay, home[3], new BlockPos(home[0], home[1],
                home[2]), teleMess, PlayerMover.INTERUPTED);
        return 0;
    }
}
