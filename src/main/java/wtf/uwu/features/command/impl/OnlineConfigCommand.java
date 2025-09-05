/*
 * MoonLight Hacked Client
 *
 * A free and open-source hacked client for Minecraft.
 * Developed using Minecraft's resources.
 *
 * Repository: https://github.com/randomguy3725/MoonLight
 *
 * Author(s): [Randumbguy & wxdbie & opZywl & MukjepScarlet & lucas & eonian]
 */
package wtf.uwu.features.command.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import wtf.uwu.UwU;
import wtf.uwu.features.command.Command;
import wtf.uwu.utils.misc.DebugUtils;
import wtf.uwu.utils.misc.HttpUtils;

import java.io.IOException;
import java.util.Locale;

public class OnlineConfigCommand extends Command {
    @Override
    public String getUsage() {
        return "onlineconfig/ocf <load> <config>";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"onlineconfig", "ocf"};
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 1) {
            DebugUtils.sendMessage("Usage: " + getUsage());
            return;
        }

        String url = UwU.INSTANCE.getClientCloud();

        switch (args[1]) {
            case "load":
                JsonObject config;
                try {
                    config = new JsonParser().parse(HttpUtils.get(
                            url + "/configs/" + args[2].toLowerCase(Locale.getDefault())
                    )).getAsJsonObject();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (UwU.INSTANCE.getConfigManager().loadOnlineConfig(UwU.INSTANCE.getConfigManager().getSetting(),config)) {
                    DebugUtils.sendMessage("Loaded config: " + args[2]);
                } else {
                    DebugUtils.sendMessage("Invalid config: " + args[2]);
                }
                break;
        }
    }
}
