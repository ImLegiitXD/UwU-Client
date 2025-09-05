package wtf.uwu.utils.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class DebugUtils {
    private static final String PREFIX = EnumChatFormatting.LIGHT_PURPLE + "UwU" + EnumChatFormatting.WHITE;
    private static final String SEPARATOR = EnumChatFormatting.GRAY + " » " + EnumChatFormatting.WHITE;

    public static void sendMessage(String message) {
        if (Minecraft.getMinecraft().thePlayer != null) {
            String formattedMessage = PREFIX + SEPARATOR + message;
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(formattedMessage));
        }
    }

    public static void sendInfo(String message) {
        sendColoredMessage(message, EnumChatFormatting.AQUA);
    }

    public static void sendWarning(String message) {
        sendColoredMessage(message, EnumChatFormatting.YELLOW);
    }

    public static void sendError(String message) {
        sendColoredMessage(message, EnumChatFormatting.RED);
    }

    public static void sendSuccess(String message) {
        sendColoredMessage(message, EnumChatFormatting.GREEN);
    }

    private static void sendColoredMessage(String message, EnumChatFormatting color) {
        if (Minecraft.getMinecraft().thePlayer != null) {
            String formattedMessage = PREFIX + SEPARATOR + color + message;
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(formattedMessage));
        }
    }

    public static void sendFormattedMessage(String message, Object... args) {
        sendMessage(String.format(message, args));
    }

    public static void sendDebug(String message) {
        sendColoredMessage("[DEBUG] " + message, EnumChatFormatting.GRAY);
    }

    public static void sendDebugIfEnabled(String message, boolean debugMode) {
        if (debugMode) {
            sendDebug(message);
        }
    }
}