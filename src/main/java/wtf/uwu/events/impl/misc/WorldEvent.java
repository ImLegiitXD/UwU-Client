package wtf.uwu.events.impl.misc;

import net.minecraft.world.World;
import wtf.uwu.events.impl.Event;

public record WorldEvent(World oldWorld, World newWorld) implements Event {
}
