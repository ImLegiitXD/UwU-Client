package wtf.uwu.features.command.impl;

import wtf.uwu.UwU;
import wtf.uwu.features.command.Command;
import wtf.uwu.utils.misc.DebugUtils;

import java.util.Arrays;

public final class HelpCommand extends Command {
    @Override
    public String[] getAliases() {
        return new String[]{"help", "h"};
    }

    @Override
    public void execute(final String[] arguments) {
        for (final Command command : UwU.INSTANCE.getCommandManager().cmd) {
            if(!(command instanceof ModuleCommand))
                DebugUtils.sendMessage(Arrays.toString(command.getAliases()) + ": " + command.getUsage());
        }
    }

    @Override
    public String getUsage() {
        return "help/h";
    }
}
