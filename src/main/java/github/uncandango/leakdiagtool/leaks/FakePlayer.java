package github.uncandango.leakdiagtool.leaks;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class FakePlayer {
    // leak test of FakePlayer
    private static boolean fakePlayerCreated = false;

    public static void levelTick(LevelTickEvent.Post event) {
        if (!fakePlayerCreated && event.getLevel() instanceof ServerLevel && !event.getLevel().players().isEmpty()) {
            fakePlayerCreated = true;
            var randomUUID = event.getLevel().players().getFirst().getUUID();
            var profile = new GameProfile(randomUUID, "FakePlayerLeaker");
            FakePlayerFactory.get((ServerLevel) event.getLevel(), profile);
        }
    }
}
