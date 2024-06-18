package github.uncandango.leakdiagtool;

import com.mojang.logging.LogUtils;
import github.uncandango.leakdiagtool.commands.LDTCommands;
import github.uncandango.leakdiagtool.leaks.FakePlayer;
import github.uncandango.leakdiagtool.tracker.ClassTracker;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static github.uncandango.leakdiagtool.commands.LDTCommands.heapDumpScheduledOnServerShutdown;

@Mod(LeakDiagTool.MOD_ID)
public class LeakDiagTool {
    public static final String MOD_ID = "leakdiagtool";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static boolean leakTest = true;

    public LeakDiagTool(IEventBus eventModBus, ModContainer modContainer) {
        var eventBus = NeoForge.EVENT_BUS;
        eventBus.addListener(LeakDiagTool::registerCommands);
        eventBus.addListener(LeakDiagTool::registerClientCommands);
        eventBus.addListener(LeakDiagTool::unloadLevel);
        eventBus.addListener(LeakDiagTool::unloadChunk);
        eventBus.addListener(LeakDiagTool::shutdownServer);
        eventBus.addListener(LeakDiagTool::playerLogout);
        eventBus.addListener(Scheduler.INSTANCE::onShutdown);
        eventBus.addListener(LeakDiagTool::closedServer);

        if (leakTest) {
            // Fake Player does not handle disconnection properly and leaks
            eventBus.addListener(FakePlayer::levelTick);
        }
    }



    private static void playerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ClassTracker.LEAKING.add(ValidClass.PLAYER, event.getEntity());
    }

    private static void shutdownServer(ServerStoppingEvent event) {
        ClassTracker.LEAKING.add(ValidClass.SERVER, event.getServer());
    }

    private static void closedServer(ServerStoppedEvent event) {
        if (heapDumpScheduledOnServerShutdown) {
            heapDumpScheduledOnServerShutdown = false;
            LDTCommands.dumpHeap();
        }
    }

    private static void unloadChunk(ChunkEvent.Unload event) {
        ClassTracker.LEAKING.add(ValidClass.CHUNK, event.getChunk());
    }

    public static void unloadLevel(LevelEvent.Unload event) {
        ClassTracker.LEAKING.add(ValidClass.LEVEL, event.getLevel());
    }

    public static void registerCommands(RegisterCommandsEvent event) {
        LDTCommands.registerCommands(event.getDispatcher(), event.getBuildContext());
    }

    private static void registerClientCommands(RegisterClientCommandsEvent event) {
        LDTCommands.registerClientCommands(event.getDispatcher(), event.getBuildContext());
    }

    public enum ValidClass {
        CHUNK("net.minecraft.world.level.chunk.ChunkAccess"),
        LEVEL("net.minecraft.client.multiplayer.ClientLevel", "net.minecraft.server.level.ServerLevel"),
        SERVER("net.minecraft.server.dedicated.DedicatedServer", "net.minecraft.client.server.IntegratedServer", "net.minecraft.gametest.framework.GameTestServer"),
        PLAYER("net.minecraft.client.player.AbstractClientPlayer", "net.minecraft.server.level.ServerPlayer");

        private final Lazy<Set<Class<?>>> classes;

        ValidClass(String... classNames) {
            this.classes = Lazy.of(() -> Stream.of(classNames).map(Utils::safeGetClass).filter(Objects::nonNull).collect(Collectors.toSet()));
        }

        public Set<Class<?>> getClasses() {
            return classes.get();
        }
    }
}
