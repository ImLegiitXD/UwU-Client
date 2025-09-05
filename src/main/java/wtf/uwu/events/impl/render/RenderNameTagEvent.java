
package wtf.uwu.events.impl.render;

import lombok.Getter;
import net.minecraft.entity.Entity;
import wtf.uwu.events.impl.CancellableEvent;

@Getter
public class RenderNameTagEvent extends CancellableEvent {

    final Entity entity;

    public RenderNameTagEvent(Entity entity) {
        this.entity = entity;
    }

}