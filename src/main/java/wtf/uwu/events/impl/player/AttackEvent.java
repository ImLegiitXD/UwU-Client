package wtf.uwu.events.impl.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;
import wtf.uwu.events.impl.CancellableEvent;

@Getter
@AllArgsConstructor
public final class AttackEvent extends CancellableEvent {
    private final Entity targetEntity;

    public Entity getTargetEntity() {
        return this.targetEntity;
    }
}