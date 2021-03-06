package thut.essentials;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import thut.essentials.commands.CommandManager;
import thut.essentials.defuzz.SpawnDefuzzer;
import thut.essentials.economy.EconomyManager;
import thut.essentials.land.LandEventsHandler;
import thut.essentials.land.LandEventsHandler.ChunkLoadHandler;
import thut.essentials.land.LandManager;
import thut.essentials.util.MobManager;
import thut.essentials.util.PlayerDataHandler;
import thut.essentials.util.world.WorldStructures;

@Mod(Essentials.MODID)
public class Essentials
{
    public static final String MODID  = "thutessentials";
    public static final Config config = new Config();
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger(Essentials.MODID);

    public Essentials()
    {
        MinecraftForge.EVENT_BUS.register(this);
        thut.essentials.config.Config.setupConfigs(Essentials.config, Essentials.MODID, Essentials.MODID);
        final File logfile = FMLPaths.GAMEDIR.get().resolve("logs").resolve(Essentials.MODID + ".log").toFile();
        if (logfile.exists())
        {
            FMLPaths.GAMEDIR.get().resolve("logs").resolve(Essentials.MODID).toFile().mkdirs();
            try
            {
                Files.move(FMLPaths.GAMEDIR.get().resolve("logs").resolve(Essentials.MODID + ".log"), FMLPaths.GAMEDIR
                        .get().resolve("logs").resolve(Essentials.MODID).resolve(logfile.lastModified() + ".log"));
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
        }
        final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) Essentials.LOGGER;
        final FileAppender appender = FileAppender.newBuilder().withFileName(logfile.getAbsolutePath()).setName(
                Essentials.MODID).build();
        logger.addAppender(appender);
        appender.start();

        // This won't actually do anything unless config is enabled.
        MinecraftForge.EVENT_BUS.register(ChunkLoadHandler.class);

        // Register the mob grief preventer
        MinecraftForge.EVENT_BUS.register(MobManager.class);

        if (Essentials.config.defuzz)
        {
            Essentials.LOGGER.info("Registering Defuzzer!");
            MinecraftForge.EVENT_BUS.register(SpawnDefuzzer.class);
        }

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(
                () -> FMLNetworkConstants.IGNORESERVERONLY, (in, net) -> true));
    }

    public void setup(final FMLCommonSetupEvent event)
    {
        // Initialize the world structure tracker
        WorldStructures.setup();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void serverStarting(final FMLServerStartingEvent event)
    {
        if (Essentials.config.landEnabled) MinecraftForge.EVENT_BUS.register(LandEventsHandler.TEAMMANAGER);
        if (Essentials.config.shopsEnabled) EconomyManager.getInstance();
        LandEventsHandler.TEAMMANAGER.registerPerms();
        LandEventsHandler.ChunkLoadHandler.server = event.getServer();
        Essentials.LOGGER.info("Server Started");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void registerServerCommands(final RegisterCommandsEvent event)
    {
        CommandManager.register_commands(event.getDispatcher());
    }

    @SubscribeEvent
    public void serverUnload(final FMLServerStoppingEvent evt)
    {
        if (Essentials.config.landEnabled) MinecraftForge.EVENT_BUS.unregister(LandEventsHandler.TEAMMANAGER);
        if (Essentials.config.shopsEnabled) EconomyManager.clearInstance();
        LandManager.clearInstance();
        PlayerDataHandler.saveAll();
        PlayerDataHandler.clear();
        Essentials.LOGGER.info("Server Stopped");
    }
}
