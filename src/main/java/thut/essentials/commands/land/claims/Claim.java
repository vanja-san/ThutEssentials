package thut.essentials.commands.land.claims;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import thut.essentials.Essentials;
import thut.essentials.commands.CommandManager;
import thut.essentials.events.ClaimLandEvent;
import thut.essentials.land.LandManager;
import thut.essentials.land.LandManager.LandTeam;
import thut.essentials.util.Coordinate;

public class Claim
{
    private static final String BYPASSLIMIT = "thutessentials.land.claim.nolimit";

    public static void register(final CommandDispatcher<CommandSource> commandDispatcher)
    {
        // TODO configurable this.
        final String name = "claim";
        if (Essentials.config.commandBlacklist.contains(name)) return;
        String perm;
        PermissionAPI.registerNode(perm = "command." + name, DefaultPermissionLevel.ALL, "Can the player use /" + name);
        PermissionAPI.registerNode(Claim.BYPASSLIMIT, DefaultPermissionLevel.OP,
                "Permission to bypass the land per player limit for a team.");

        // Setup with name and permission
        LiteralArgumentBuilder<CommandSource> command = Commands.literal(name).requires(cs -> CommandManager.hasPerm(cs,
                perm));
        // Register the execution.
        command = command.executes(ctx -> Claim.execute(ctx.getSource()));

        // Actually register the command.
        commandDispatcher.register(command);
    }

    // Single claim version
    private static int execute(final CommandSource source) throws CommandSyntaxException
    {
        final PlayerEntity player = source.asPlayer();
        final LandTeam team = LandManager.getTeam(player);
        if (!team.hasRankPerm(player.getUniqueID(), LandTeam.CLAIMPERM))
        {
            player.sendMessage(CommandManager.makeFormattedComponent("thutessentials.claim.notallowed.teamperms",
                    TextFormatting.RED));
            return 1;
        }
        final int teamCount = team.member.size();
        final int maxLand = team.maxLand < 0 ? teamCount * Essentials.config.teamLandPerPlayer : team.maxLand;
        final int count = LandManager.getInstance().countLand(team.teamName);
        if (count >= maxLand && !PermissionAPI.hasPermission(player, Claim.BYPASSLIMIT))
        {
            player.sendMessage(CommandManager.makeFormattedComponent("thutessentials.claim.notallowed.needmoreland",
                    TextFormatting.RED));
            return 1;
        }

        final int x = MathHelper.floor(player.getPosition().getX() >> 4);
        final int y = MathHelper.floor(player.getPosition().getY() >> 4);
        final int z = MathHelper.floor(player.getPosition().getZ() >> 4);
        if (y < 0 || y > 15) return 1;
        final int dim = player.dimension.getId();
        final Coordinate chunk = new Coordinate(x, y, z, dim);
        final LandTeam owner = LandManager.getInstance().getLandOwner(chunk);
        if (owner != null)
        {
            player.sendMessage(new TranslationTextComponent("thutessentials.claim.notallowed.alreadyclaimedby", owner));
            return 1;
        }
        final ClaimLandEvent event = new ClaimLandEvent(new BlockPos(x, y, z), dim, player, team.teamName);
        MinecraftForge.EVENT_BUS.post(event);
        LandManager.getInstance().addTeamLand(team.teamName, chunk, true);
        player.sendMessage(new TranslationTextComponent("thutessentials.claim.claimed", team.teamName));
        return 0;
    }

}
