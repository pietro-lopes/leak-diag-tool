package github.uncandango.leakdiagtool.tracker;

import net.minecraft.Util;

public class Generation {

    private final int number;
    private final EvolutionEvent trigger;
    private final long timeMs;

    private Generation(int number, EvolutionEvent trigger, long timeMs) {
        this.number = number;
        this.trigger = trigger;
        this.timeMs = timeMs;
    }

    public Generation(){
        this(0,EvolutionEvent.INIT, Util.getMillis());
    }

    public Generation bump(EvolutionEvent trigger){
        return new Generation(number + 1, trigger, Util.getMillis());
    }

    public int getNumber() {
        return number;
    }

    public EvolutionEvent getEvent() {
        return trigger;
    }

    public enum EvolutionEvent {
        INIT,
        WORLD_LOAD,
        WORLD_UNLOAD,
        LEVEL_LOAD,
        LEVEL_UNLOAD,
        CHUNK_LOAD,
        CHUNK_UNLOAD,
        CUSTOM_TRIGGER,
        RELOAD_RESOURCES,
        PLAYER_LOGIN,
        PLAYER_LOGOUT
    }
}
