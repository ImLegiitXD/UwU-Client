package wtf.uwu.features.config.impl;

import com.google.gson.JsonObject;
import wtf.uwu.UwU;
import wtf.uwu.features.config.Config;
import wtf.uwu.gui.widget.Widget;

public class WidgetConfig extends Config {
    public WidgetConfig(String name) {
        super(name);
    }
    @Override
    public void loadConfig(JsonObject object) {
        for (Widget widget : UwU.INSTANCE.getWidgetManager().widgetList) {
            if (object.has(widget.name)) {
                JsonObject obj = object.get(widget.name).getAsJsonObject();
                widget.x = obj.get("x").getAsFloat();
                widget.y = obj.get("y").getAsFloat();
            }
        }
    }
    @Override
    public JsonObject saveConfig() {
        JsonObject object = new JsonObject();
        for (Widget widget : UwU.INSTANCE.getWidgetManager().widgetList) {
            JsonObject widgetObj = new JsonObject();
            widgetObj.addProperty("x", widget.x);
            widgetObj.addProperty("y", widget.y);
            object.add(widget.name, widgetObj);
        }
        return object;
    }
}
