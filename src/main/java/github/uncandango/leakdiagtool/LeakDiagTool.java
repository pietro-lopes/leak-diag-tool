package github.uncandango.leakdiagtool;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import github.uncandango.leakdiagtool.commands.LDTCommands;
import github.uncandango.leakdiagtool.tracker.ClassTracker;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static github.uncandango.leakdiagtool.commands.LDTCommands.heapDumpScheduledOnServerShutdown;

@Mod(LeakDiagTool.MOD_ID)
public final class LeakDiagTool {
    public static final String MOD_ID = "leakdiagtool";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LeakDiagTool() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        final IEventBus eventBus = MinecraftForge.EVENT_BUS;
        eventBus.addListener(LeakDiagTool::registerCommands);
        eventBus.addListener(LeakDiagTool::registerClientCommands);
        eventBus.addListener(LeakDiagTool::unloadLevel);
        eventBus.addListener(LeakDiagTool::unloadChunk);
        eventBus.addListener(LeakDiagTool::shutdownServer);
        eventBus.addListener(LeakDiagTool::playerLogout);
        eventBus.addListener(Scheduler.INSTANCE::onShutdown);
        eventBus.addListener(LeakDiagTool::closedServer);
//        eventBus.addListener(LeakDiagTool::levelTick); // leak test of FakePlayer
    }

    // leak test of FakePlayer
//    private static void levelTick(TickEvent.LevelTickEvent event){
//        if (event.level.isClientSide()) return;
//        if (event.level.getServer().getTickCount() % 100 == 0) { // runs every 5s
//            var randomUUID = UUID.randomUUID();
//            var profile = new GameProfile(randomUUID, "Player" + event.level.getServer().getTickCount());
//            FakePlayerFactory.get((ServerLevel) event.level, profile);
//        }
//    }

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

        private final LazyOptional<Set<Class<?>>> classes;

        ValidClass(String... classNames) {
            this.classes = LazyOptional.of(() -> Stream.of(classNames).map(Utils::safeGetClass).filter(Objects::nonNull).collect(Collectors.toSet()));
        }

        public Set<Class<?>> getClasses() {
            return classes.orElse(Set.of());
        }
    }
}
