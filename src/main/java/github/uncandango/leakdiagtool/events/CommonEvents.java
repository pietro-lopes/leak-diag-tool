package github.uncandango.leakdiagtool.events;

import com.mojang.authlib.GameProfile;
import github.uncandango.leakdiagtool.LeakDiagTool;
import github.uncandango.leakdiagtool.commands.LDTCommands;
import github.uncandango.leakdiagtool.tracker.ClassTracker;
import github.uncandango.leakdiagtool.tracker.ObjectTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

import static github.uncandango.leakdiagtool.commands.LDTCommands.heapDumpScheduledOnServerShutdown;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = LeakDiagTool.MOD_ID)
public class CommonEvents {

    @SubscribeEvent
    public static void playerClone(PlayerEvent.Clone event){
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

    // Testing Forge bug with FakePlayers
    public static void levelTick(TickEvent.LevelTickEvent event){
        if (event.level.isClientSide()) return;
        if (event.level.getServer().getTickCount() % 100 == 0) { // runs every 5s
            var randomUUID = UUID.randomUUID();
            var profile = new GameProfile(randomUUID, "Player" + event.level.getServer().getTickCount());
            FakePlayerFactory.get((ServerLevel) event.level, profile);
        }
    }
}
