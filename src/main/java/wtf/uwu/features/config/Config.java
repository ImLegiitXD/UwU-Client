package wtf.uwu.features.config;

import com.google.gson.JsonObject;
import lombok.Getter;
import wtf.uwu.UwU;

import java.io.File;

@Getter
public class Config {
    private final File file;
    private final String name;

    public Config(String name) {
        this.name = name;
        this.file = new File(UwU.INSTANCE.getMainDir(), name + ".json");
    }

    public void loadConfig(JsonObject object){

    }

    public JsonObject saveConfig(){
        return null;
    }
}
