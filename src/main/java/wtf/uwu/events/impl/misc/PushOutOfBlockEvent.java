package wtf.uwu.events.impl.misc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import wtf.uwu.events.impl.CancellableEvent;

@Getter
@Setter
@AllArgsConstructor
public class PushOutOfBlockEvent extends CancellableEvent {
    private float pushMotion;
}