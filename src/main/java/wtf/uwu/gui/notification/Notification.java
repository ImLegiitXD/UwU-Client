
package wtf.uwu.gui.notification;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import wtf.uwu.UwU;
import wtf.uwu.features.modules.impl.visual.Interface;
import wtf.uwu.gui.font.Fonts;
import wtf.uwu.utils.InstanceAccess;
import wtf.uwu.utils.animations.Animation;
import wtf.uwu.utils.animations.Translate;
import wtf.uwu.utils.animations.impl.EaseOutSine;
import wtf.uwu.utils.math.TimerUtils;


@Getter
public class Notification implements InstanceAccess {

    private final NotificationType notificationType;
    private final String title, description;
    private final float time;
    private final TimerUtils timerUtils;
    private final Animation animation;
    private final Translate translate;

    public Notification(NotificationType type, String title, String description) {
        this(type, title, description, UwU.INSTANCE.getNotificationManager().getToggleTime());
    }

    public Notification(NotificationType type, String title, String description, float time) {
        this.title = title;
        this.description = description;
        this.time = (long) (time * 1000);
        timerUtils = new TimerUtils();
        this.notificationType = type;
        this.animation = new EaseOutSine(250, 1);
        final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        this.translate = new Translate(sr.getScaledWidth() - this.getWidth(), sr.getScaledHeight() - getHeight());
    }
    
    public double getWidth(){
        return switch (INSTANCE.getModuleManager().getModule(Interface.class).notificationMode.get()) {
            case "Default" -> Fonts.interMedium.get(15).getStringWidth(getDescription()) + 5;
            case "Test" ->
                    Math.max(Fonts.interSemiBold.get(15).getStringWidth(getTitle()), Fonts.interSemiBold.get(15).getStringWidth(getDescription())) + 5;
            case "Test2" ->
                    Math.max(Fonts.interSemiBold.get(17).getStringWidth(getTitle()), Fonts.interRegular.get(17).getStringWidth(getDescription())) + 10;
            case "Exhi" ->
                    Math.max(100.0f, Math.max(Fonts.interRegular.get(18).getStringWidth(getTitle()) + 2, Fonts.interRegular.get(14).getStringWidth(getDescription())) + 24.0f);
            case "Type 2" ->
                    Math.max(100.0f, Math.max(Fonts.interRegular.get(18).getStringWidth(getTitle()), Fonts.interRegular.get(14).getStringWidth(getDescription())) + 70);
            case "Type 3" ->
                    Math.max(Fonts.interRegular.get(22).getStringWidth(getTitle()), Fonts.interRegular.get(20).getStringWidth(getDescription())) + 50;
            case "Type 4" ->
                    Math.max(140, Math.max(Fonts.interRegular.get(10).getStringWidth(getTitle()), Fonts.interRegular.get(6).getStringWidth(getDescription())) + 40);
            case "Type 5" ->
                    Math.max(mc.fontRendererObj.getStringWidth(getTitle()), mc.fontRendererObj.getStringWidth(getDescription())) + 16 + 25;
            default -> 0;
        };
    }

    public double getHeight(){
        return switch (INSTANCE.getModuleManager().getModule(Interface.class).notificationMode.get()) {
            case "Default" -> 16;
            case "Test" -> Fonts.interRegular.get(15).getHeight() * 2 + 2;
            case "Test2" -> 33;
            case "Exhi" -> 26;
            case "Type 2" -> 30;
            case "Type 3" -> 35;
            case "Type 4" -> 27f;
            case "Type 5" -> 24;
            default -> 0;
        };
    }
}