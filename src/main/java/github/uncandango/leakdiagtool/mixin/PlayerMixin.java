package github.uncandango.leakdiagtool.mixin;

import com.mojang.authlib.GameProfile;
import github.uncandango.leakdiagtool.tracker.ClassTracker;
import github.uncandango.leakdiagtool.tracker.ObjectTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    void ldt$grabPlayerInstance(Level arg, BlockPos arg2, float f, GameProfile gameProfile, CallbackInfo ci) {
        ObjectTracker.add(ClassTracker.LOADED, ObjectTracker.ValidClass.PLAYER, this);
    }
}
