
package wtf.uwu.events.impl.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import wtf.uwu.events.impl.Event;

@Getter
@Setter
@AllArgsConstructor
public class PreStepEvent implements Event {
    @Setter
    public double height;
}
