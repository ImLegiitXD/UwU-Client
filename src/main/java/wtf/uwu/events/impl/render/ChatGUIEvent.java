
package wtf.uwu.events.impl.render;

import lombok.AllArgsConstructor;
import wtf.uwu.events.impl.Event;

@AllArgsConstructor
public class ChatGUIEvent implements Event {
    public int mouseX, mouseY;
}
