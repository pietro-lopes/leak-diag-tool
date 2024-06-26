package github.uncandango.leakdiagtool.mixin;

import com.mojang.datafixers.DataFixer;
import github.uncandango.leakdiagtool.tracker.ClassTracker;
import github.uncandango.leakdiagtool.tracker.ObjectTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    void ldt$grabServerInstance(Thread thread, LevelStorageSource.LevelStorageAccess arg, PackRepository arg2, WorldStem arg3, Proxy proxy, DataFixer dataFixer, Services arg4, ChunkProgressListenerFactory arg5, CallbackInfo ci) {
        ObjectTracker.add(ClassTracker.LOADED, ObjectTracker.ValidClass.SERVER, this);
    }
}
