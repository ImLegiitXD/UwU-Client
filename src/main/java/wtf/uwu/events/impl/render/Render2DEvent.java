
package wtf.uwu.events.impl.render;

import net.minecraft.client.gui.ScaledResolution;
import wtf.uwu.events.impl.Event;

public record Render2DEvent(float partialTicks, ScaledResolution scaledResolution) implements Event {
}

