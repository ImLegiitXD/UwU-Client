package wtf.uwu.utils;

import net.minecraft.client.Minecraft;
import wtf.uwu.UwU;

public interface InstanceAccess {

    Minecraft mc = Minecraft.getMinecraft();

    UwU INSTANCE = UwU.INSTANCE;
}

