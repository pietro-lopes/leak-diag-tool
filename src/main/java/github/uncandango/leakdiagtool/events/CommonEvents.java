package github.uncandango.leakdiagtool.events;

import github.uncandango.leakdiagtool.LeakDiagTool;
import github.uncandango.leakdiagtool.commands.LDTCommands;
import github.uncandango.leakdiagtool.tracker.ClassTracker;
import github.uncandango.leakdiagtool.tracker.ObjectTracker;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import static github.uncandango.leakdiagtool.commands.LDTCommands.heapDumpScheduledOnServerShutdown;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = LeakDiagTool.MOD_ID)
public class CommonEvents {

    @SubscribeEvent
    public static void playerClone(PlayerEvent.Clone event) {
        ObjectTracker.add(ClassTracker.LEAKING, ObjectTracker.ValidClass.PLAYER, event.getOriginal());
    }

    @SubscribeEvent
    public static void playerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ObjectTracker.add(ClassTracker.LEAKING, ObjectTracker.ValidClass.PLAYER, event.getEntity());
    }

    @SubscribeEvent
    public static void shutdownServer(ServerStoppingEvent event) {
        ObjectTracker.add(ClassTracker.LEAKING, ObjectTracker.ValidClass.SERVER, event.getServer());
    }

    @SubscribeEvent
    public static void closedServer(ServerStoppedEvent event) {
        if (heapDumpScheduledOnServerShutdown) {
            heapDumpScheduledOnServerShutdown = false;
            LDTCommands.dumpHeap();
        }
    }

    @SubscribeEvent
    public static void unloadChunk(ChunkEvent.Unload event) {
        ObjectTracker.add(ClassTracker.LEAKING, ObjectTracker.ValidClass.CHUNK, event.getChunk());
    }

    @SubscribeEvent
    public static void unloadLevel(LevelEvent.Unload event) {
        ObjectTracker.add(ClassTracker.LEAKING, ObjectTracker.ValidClass.LEVEL, event.getLevel());
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        LDTCommands.registerCommands(event.getDispatcher(), event.getBuildContext());
    }
}
