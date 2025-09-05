
package wtf.uwu.gui.widget.impl;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import wtf.uwu.events.impl.render.Shader2DEvent;
import wtf.uwu.features.modules.impl.visual.Interface;
import wtf.uwu.features.values.impl.ModeValue;
import wtf.uwu.gui.font.Fonts;
import wtf.uwu.gui.widget.Widget;
import wtf.uwu.utils.InstanceAccess;
import wtf.uwu.utils.animations.Animation;
import wtf.uwu.utils.math.MathUtils;
import wtf.uwu.utils.render.ColorUtils;
import wtf.uwu.utils.render.ParticleRenderer;
import wtf.uwu.utils.render.RenderUtils;
import wtf.uwu.utils.render.RoundedUtils;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.gui.inventory.GuiInventory.drawEntityOnScreen;

public class TargetHUDWidget extends Widget {
    public TargetHUDWidget() {
        super("Target HUD");
        this.x = 0.5f;
        this.y = 0.8f;
    }

    @Override
    public void render() {
        int count = 0;
        float lastTargetWidth = 0;
        for (EntityPlayer target : setting.animationEntityPlayerMap.keySet()) {
            this.height = getTHUDHeight();
            float currentTargetWidth = getTHUDWidth(target);
            this.width = currentTargetWidth;
            if (count > 9) continue;
            TargetHUD targetHUD = new TargetHUD((float) (renderX + ((count % 3) * (lastTargetWidth + 4)) * setting.animationEntityPlayerMap.get(target).getOutput()), (float) (this.renderY + ((count / 3) * (this.height + 4)) * setting.animationEntityPlayerMap.get(target).getOutput()), target, setting.animationEntityPlayerMap.get(target), false, setting.targetHudMode);
            targetHUD.render();
            lastTargetWidth = currentTargetWidth;
            count++;
        }
    }

    @Override
    public void onShader(Shader2DEvent event) {
        int count = 0;
        float lastTargetWidth = 0;
        for (EntityPlayer target : setting.animationEntityPlayerMap.keySet()) {
            this.height = getTHUDHeight();
            float currentTargetWidth = getTHUDWidth(target);
            this.width = currentTargetWidth;
            if (count > 9) continue;
            TargetHUD targetHUD = new TargetHUD((float) (renderX + ((count % 3) * (lastTargetWidth + 4)) * setting.animationEntityPlayerMap.get(target).getOutput()), (float) (this.renderY + ((count / 3) * (this.height + 4)) * setting.animationEntityPlayerMap.get(target).getOutput()), target, setting.animationEntityPlayerMap.get(target), true, setting.targetHudMode);
            targetHUD.render();
            lastTargetWidth = currentTargetWidth;
            count++;
        }
    }

    public float getTHUDWidth(Entity entity) {
        return switch (setting.targetHudMode.get()) {
            case "Type 1" -> Math.max(120, Fonts.interBold.get(18).getStringWidth(entity.getName()) + 50);
            case "Astolfo" -> Math.max(130, mc.fontRendererObj.getStringWidth(entity.getName()) + 60);
            case "Type 2" -> Math.max(100, mc.fontRendererObj.getStringWidth(entity.getDisplayName().getFormattedText())) + 11;
            case "Exhi" -> Math.max(124.0f, Fonts.interBold.get(17).getStringWidth(entity.getName()) + 54.0f);
            case "Adjust" -> 130;
            case "Moon" -> 35 + Fonts.interSemiBold.get(18).getStringWidth(entity.getName()) + 33;
            case "Augustus" -> 35 + Fonts.interSemiBold.get(18).getStringWidth(entity.getName()) + 33;
            case "New" -> Math.max(160, Fonts.interSemiBold.get(17).getStringWidth(entity.getName()) + 30);
            case "Novo 1", "Novo 2" -> 35 + mc.fontRendererObj.getStringWidth(entity.getName()) + 33;
            case "Novo 3" -> 35 + mc.fontRendererObj.getStringWidth(entity.getName()) + 34;
            case "Novo 4" -> 135.0f;
            case "Novo 5" -> Math.max(118, Fonts.interSemiBold.get(17).getStringWidth(entity.getName()) + 38) + 27F;
            case "Akrien" -> 114 + ((35 + Fonts.interSemiBold.get(21).getStringWidth(entity.getName())) / 25f);
            case "Felix" -> 140.0f;
            case "Innominate" -> Math.max(78, Fonts.interSemiBold.get(17).getStringWidth(entity.getName()) + 39) + 22;
            default -> 0;
        };
    }

    public float getTHUDHeight() {
        return switch (setting.targetHudMode.get()) {
            case "Type 1" -> 44;
            case "Astolfo" -> 56;
            case "Type 2" -> 38.0F;
            case "Exhi" -> 38;
            case "Adjust" -> 35;
            case "Moon" -> 40.5f;
            case "Augustus" -> 40.5f;
            case "New" -> 40.5f;
            case "Novo 1" -> 37.5f;
            case "Novo 2", "Novo 3" -> 36f;
            case "Novo 4" -> 45.0f;
            case "Novo 5" -> 47;
            case "Akrien" -> 39.5f;
            case "Felix" -> 37.6f;
            case "Innominate" -> 35;
            default -> 0;
        };
    }

    @Override
    public boolean shouldRender() {
        return setting.isEnabled() && setting.elements.isEnabled("Target HUD");
    }
}

@Getter
@Setter
class TargetHUD implements InstanceAccess {
    private float x, y, width, height;
    private EntityPlayer target;
    private Animation animation;
    private boolean shader;
    private ModeValue style;
    private Interface setting = INSTANCE.getModuleManager().getModule(Interface.class);
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0");

    public TargetHUD(float x, float y, EntityPlayer target, Animation animation, boolean shader, ModeValue style) {
        this.x = x;
        this.y = y;
        this.target = target;
        this.animation = animation;
        this.shader = shader;
        this.style = style;
    }

    public void render() {
        setWidth(INSTANCE.getWidgetManager().get(TargetHUDWidget.class).getTHUDWidth(target));
        setHeight(INSTANCE.getWidgetManager().get(TargetHUDWidget.class).getTHUDHeight());
        GlStateManager.pushMatrix();
        if (!style.is("Exhi")) {
            GlStateManager.translate(x + width / 2F, y + height / 2F, 0);
            GlStateManager.scale(animation.getOutput(), animation.getOutput(), animation.getOutput());
            GlStateManager.translate(-(x + width / 2F), -(y + height / 2F), 0);
        }
        switch (style.get()) {
            case "Astolfo": {
                if (!shader) {
                    RoundedUtils.drawRound(x, y, width, height, 0, ColorUtils.applyOpacity(new Color(0, 0, 0), (float) (.4 * animation.getOutput())));
                    GlStateManager.pushMatrix();
                    drawEntityOnScreen((int) (x + 22), (int) (y + 51), 24, mc.thePlayer.rotationYaw, -mc.thePlayer.rotationPitch, target);
                    mc.fontRendererObj.drawStringWithShadow(target.getName(), x + 50, y + 6, -1);
                    GlStateManager.scale(1.5, 1.5, 1.5);
                    mc.fontRendererObj.drawStringWithShadow(String.format("%.1f", target.getHealth()) + " ❤", (x + 50) / 1.5f, (y + 22) / 1.5f, setting.color(1));
                    GlStateManager.popMatrix();
                    float healthWidth = (width - 54);
                    target.healthAnimation.animate(healthWidth * MathHelper.clamp_float(target.getHealth() / target.getMaxHealth(), 0, 1), 30);
                    RoundedUtils.drawRound(x + 48, y + 42, width - 54, 7, 0, ColorUtils.applyOpacity(new Color(setting.color(1)).darker().darker().darker(), (float) (1 * animation.getOutput())));
                    RoundedUtils.drawRound(x + 48, y + 42, target.healthAnimation.getOutput(), 7, 0, ColorUtils.applyOpacity(new Color(setting.color(1)), (float) (1 * animation.getOutput())));
                } else {
                    RoundedUtils.drawRound(x, y, width, height, 0, ColorUtils.applyOpacity(new Color(0, 0, 0), (float) (1f * animation.getOutput())));
                }
            }

            break;
            case "Type 1": {
                target.healthAnimation.animate((width - 52) * MathHelper.clamp_float(target.getHealth() / target.getMaxHealth(), 0, 1), 30);
                float hurtTime = (target.hurtTime == 0 ? 0 :
                        target.hurtTime - mc.timer.renderPartialTicks) * 0.5f;
                if (!shader) {
                    //RoundedUtils.drawRoundOutline(x,y,width,height,6,.1f,ColorUtils.applyOpacity(Color.BLACK, (float) (.3f * animation.getOutput())),ColorUtils.applyOpacity(new Color(INSTANCE.getModuleManager().getModule(Interface.class).color(1)), (float) (1f * animation.getOutput())));
                    RoundedUtils.drawRound(x, y, width, height, 6, ColorUtils.applyOpacity(Color.BLACK, (float) (.4f * animation.getOutput())));
                    RenderUtils.renderPlayer2D(target, x + 4 + (hurtTime) / 2, y + 4 + (hurtTime) / 2, 34 - hurtTime, 8f, ColorUtils.interpolateColor2(Color.WHITE, Color.RED, hurtTime / 7));
                    Fonts.interBold.get(18).drawString(target.getName(), x + 43, y + 10, ColorUtils.applyOpacity(Color.WHITE, (float) animation.getOutput()).getRGB());
                    Fonts.interBold.get(14).drawString("HP: " + String.format("%.1f", target.healthAnimation.getOutput() / (width - 52) * target.getMaxHealth()), x + 43, y + 20, ColorUtils.applyOpacity(Color.WHITE, (float) animation.getOutput()).getRGB());
                    RoundedUtils.drawRound(x + 44, y + 30, width - 52, 6, 3, ColorUtils.applyOpacity(Color.BLACK, (float) (.47f * animation.getOutput())));
                    RoundedUtils.drawGradientHorizontal(x + 44, y + 30, target.healthAnimation.getOutput(), 6, 3, ColorUtils.applyOpacity(new Color(INSTANCE.getModuleManager().getModule(Interface.class).color(0)), (float) animation.getOutput()), ColorUtils.applyOpacity(new Color(INSTANCE.getModuleManager().getModule(Interface.class).color(10)), (float) animation.getOutput()));
                } else {
                    RoundedUtils.drawRound(x, y, width, height, 6, ColorUtils.applyOpacity(Color.BLACK, (float) (1f * animation.getOutput())));
                    //RoundedUtils.drawRound(x,y,width,height,6,new Color(INSTANCE.getModuleManager().getModule(Interface.class).color(1)));
                }
            }

            if (setting.targetHudParticle.get()) {
                ParticleRenderer.renderParticle(target, x + 4, y + 4);
            }
            break;

            case "Type 2": {
                if (!shader) {
                    target.healthAnimation.animate((width - (5 * 4 + 26.5f)) * MathHelper.clamp_float(target.getHealth() / target.getMaxHealth(), 0, 1), 30);
                    RoundedUtils.drawRound(x, y, width, height, 4, new Color(setting.bgColor(), true));
                    RenderUtils.renderPlayer2D(target, x + 5, y + 6.8f, 26.5f, 2, -1);

                    RoundedUtils.drawRound(x + 5 * 2 + 26.5f, y + 6.8f, 0.5f, 26.5f, 2, new Color(30, 30, 30));

                    Fonts.interSemiBold.get(14).drawString(target.getDisplayName().getFormattedText(), x + 5 * 3 + 26.5f, y + 6.8f + (float) Fonts.interSemiBold.get(14).getHeight() / 2, -1);
                    Fonts.interRegular.get(12).drawString((int) (MathUtils.roundToHalf(target.getHealth())) + "HP", x + 5 * 3 + 26.5f, y + 6.8f * 2.25 + (float) Fonts.interSemiBold.get(14).getHeight() / 2, -1);

                    RoundedUtils.drawGradientHorizontal(x + 5 * 3 + 26.5f, y + 26.5f, target.healthAnimation.getOutput(), 3.8f, 2, new Color(setting.color(0)), new Color(setting.color(90)));

                    RenderUtils.drawRect(x + 5 * 3 + 26.5f + Fonts.interRegular.get(12).getStringWidth((int) (MathUtils.roundToHalf(target.getHealth())) + "HP") + 2, y + 6.8f * 2.25f + 1.5f, 0.5f, Fonts.psRegular.get(12).getHeight(), new Color(128, 128, 128).getRGB());

                    List<ItemStack> items = new ArrayList<>();
                    if (target.getHeldItem() != null) {
                        items.add(target.getHeldItem());
                    }
                    for (int index = 3; index >= 0; index--) {
                        ItemStack stack = target.inventory.armorInventory[index];
                        if (stack != null) {
                            items.add(stack);
                        }
                    }
                    float i = 0;

                    for (ItemStack stack : items) {
                        RenderUtils.renderItemStack(stack, i + x + 5 * 3 + 26.5f + Fonts.interRegular.get(12).getStringWidth((int) (MathUtils.roundToHalf(target.getHealth())) + "HP") + 2 + 1, y + 6.8f * 2.25f + 1.5f, 0.5f);
                        i += 7.5f;
                    }

                } else {
                    RoundedUtils.drawGradientHorizontal(x, y, width, height, 4, new Color(setting.color(0)), new Color(setting.color(90)));
                }

                if (setting.targetHudParticle.get()) {
                    ParticleRenderer.renderParticle(target, x + 5, y + 6.8f);
                }
            }
            break;

            case "Exhi": {
                float health = target.getHealth();
                float totalHealth = health + target.getAbsorptionAmount();
                float progress = health / target.getMaxHealth();
                if ((double) health < 19.5 || health < 10.0f || health < 15.0f || health < 5.0f || health < 2.0f) {
                    progress = (float) ((double) progress - 0.175);
                }
                float healthLocation = 50.0f * progress;
                Color color = new Color(ColorUtils.getHealthColor(target));
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, 0.0);

                RenderUtils.drawExhiRect(0, -2, width, height, 1);

                RenderUtils.drawRect(42.5f, 9.5f, 61.5f, 3.5f, ColorUtils.darker(color.getRGB(), 0.2f));
                RenderUtils.drawRect(42.5f, 9.5f, 11 + healthLocation, 3.5f, color.getRGB());

                RenderUtils.drawBorderedRect(42.0f, 9.0f, 61.5f, 4.5f, 0.5f, new Color(0, 0, 0, 0).getRGB(), new Color(0, 0, 0).getRGB());
                for (int i = 1; i < 10; ++i) {
                    float separator = 5.882353f * i;
                    RenderUtils.drawRect(43.5f + separator, 9.0f, 0.5f, 4.5f, new Color(0, 0, 0).getRGB());
                }
                Fonts.interBold.get(17).drawString(target.getName(), 42.0f, 1f, -1);
                Fonts.interRegular.get(10, false).drawString("HP: " + (int) totalHealth + " | Dist: " + (int) mc.thePlayer.getDistanceToEntity(target), 42.5f, 15.5f, -1);
                List<ItemStack> items = new ArrayList<>();
                if (target.getHeldItem() != null) {
                    items.add(target.getHeldItem());
                }
                for (int index = 3; index >= 0; index--) {
                    ItemStack stack = target.inventory.armorInventory[index];
                    if (stack != null) {
                        items.add(stack);
                    }
                }
                float i = 0;

                for (ItemStack stack : items) {
                    RenderUtils.renderItemStack(stack, i + 28 + 16, 19, 1, true, 0.5f);
                    i += 16;
                }

                GlStateManager.scale(0.31, 0.31, 0.31);
                GlStateManager.translate(73.0f, 102.0f, 40.0f);
                RenderUtils.drawEntityOnScreen(target.rotationYaw, target.rotationPitch, target);
                GlStateManager.popMatrix();
            }
            break;

            case "Felix": {
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, 0);
                final float scale = 2;
                RenderUtils.drawRect(1f, 1f, 140.0f, 37.6f, new Color(25, 25, 25, 210));
                String string = String.format("%.1f", target.getHealth() / 2.0f);

                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, scale);
                mc.fontRendererObj.drawStringWithShadow(string.replace(".0", ""), (29), 7, ColorUtils.getHealthColor(target));
                mc.fontRendererObj.drawStringWithShadow("❤", 20, 6, ColorUtils.getHealthColor(target));
                GlStateManager.popMatrix();

                float healthPercentage = target.getHealth() / target.getMaxHealth();
                float healthWidth = (width - 2) * healthPercentage;
                target.healthAnimation.animate(healthWidth, 50);

                RenderUtils.drawRect(2f, 34, 138, 3.5f, (ColorUtils.darker(ColorUtils.getHealthColor(target), 0.35f)));
                RenderUtils.drawRect(2f, 34, target.healthAnimation.getOutput(), 3.5f, (ColorUtils.getHealthColor(target)));

                final String name = target.getName();

                mc.fontRendererObj.drawStringWithShadow(name, (35), (3), -855638017);
                RenderUtils.renderPlayer2D(target, 2, 2, 31, 0, -1);

                GlStateManager.popMatrix();
            }
            break;

            case "Adjust": {

                float padding = 2;
                float healthX = x + padding;
                float healthPercentage = target.getHealth() / target.getMaxHealth();
                float healthWidth = (width - padding * 2) * healthPercentage;
                target.healthAnimation.animate(healthWidth, 25);

                String sheesh = decimalFormat.format(Math.abs(mc.thePlayer.getHealth() - target.getHealth()));
                String healthDiff = mc.thePlayer.getHealth() < target.getHealth() ? "-" + sheesh : "+" + sheesh;

                if (!shader) {
                    RenderUtils.drawRect(x, y, width, height, new Color(0, 0, 0, 150).getRGB());
                    RenderUtils.drawRect(healthX, y + height - 5, width - padding * 2, 4, ColorUtils.darker(setting.color(0), 0.3f));
                    RenderUtils.drawRect(healthX, y + height - 5, target.healthAnimation.getOutput(), 4, setting.color(0));
                    RenderUtils.renderPlayer2D(target, x + padding, y + padding, 28 - padding, 0, -1);

                    Fonts.interRegular.get(15).drawStringWithShadow(target.getName(), x + padding + 30, y + 3 + padding, -1);
                    Fonts.interRegular.get(15).drawStringWithShadow(healthDiff, x + width - padding - Fonts.interRegular.get(15).getStringWidth(healthDiff), y + height - 5 * 2 - padding, -1);

                    List<ItemStack> items = new ArrayList<>();
                    if (target.getHeldItem() != null) {
                        items.add(target.getHeldItem());
                    }
                    for (int index = 3; index >= 0; index--) {
                        ItemStack stack = target.inventory.armorInventory[index];
                        if (stack != null) {
                            items.add(stack);
                        }
                    }
                    float i = x + 30 + padding;

                    for (ItemStack stack : items) {
                        RenderUtils.renderItemStack(stack, i, y + 10 + padding, 1, true, 0.5f);
                        i += 16;
                    }
                }

                if (setting.targetHudParticle.get()) {
                    ParticleRenderer.renderParticle(target, x + padding, y + padding);
                }
            }
            break;

            case "Moon": {

                float healthPercentage = target.getHealth() / target.getMaxHealth();
                float space = (width - 48) / 100;

                target.healthAnimation.animate((100 * space) * MathHelper.clamp_float(healthPercentage, 0, 1), 30);

                if (!shader) {
                    RoundedUtils.drawRound(x, y, width, height, 8, new Color(setting.bgColor(), true));

                    RoundedUtils.drawRound(x + 42, y + 26.5f, (100 * space), 8, 4, new Color(0, 0, 0, 150));
                    String text = String.format("%.1f", target.getHealth());

                    RoundedUtils.drawRound(x + 42, y + 26.5f, target.healthAnimation.getOutput(), 8.5f, 4, new Color(setting.color(0)));
                    RenderUtils.renderPlayer2D(target, x + 2.5f, y + 2.5f, 35, 10, -1);
                    Fonts.interSemiBold.get(13).drawStringWithShadow(text + "HP", x + 40, y + 17, -1);
                    Fonts.interSemiBold.get(18).drawStringWithShadow(target.getName(), x + 40, y + 6, -1);
                } else {
                    RoundedUtils.drawRound(x, y, width, height, 8, new Color(setting.color()));
                }

                if (setting.targetHudParticle.get()) {
                    ParticleRenderer.renderParticle(target, x + 2.5f, y + 2.5f);
                }
            }
            break;
            case "Augustus": {
                target.healthAnimation.animate((width - 52) * MathHelper.clamp_float(target.getHealth() / target.getMaxHealth(), 0, 1), 30);
                float hurtTime = (target.hurtTime == 0 ? 0 :
                        target.hurtTime - mc.timer.renderPartialTicks) * 0.5f;
                float healthPercentage = target.getHealth() / target.getMaxHealth();
                float space = (width - 51) / 100;

                target.healthAnimation.animate((100 * space) * MathHelper.clamp_float(healthPercentage, 0, 1), 30);

                if (!shader) {
                    RoundedUtils.drawRound(x, y, width, height, 8, new Color(0, 0, 0, 100));

                    RoundedUtils.drawRound(x + 45, y + 23f, (100 * space), 10, 5, new Color(0, 0, 0, 255));

                    RoundedUtils.drawRound(x + 45, y + 23f, target.healthAnimation.getOutput(), 10f, 4, new Color(255, 0, 0));
                    RenderUtils.renderPlayer2D(target, x + 2.5f + (hurtTime) / 2, y + 2.5f + (hurtTime) / 2, 35 - hurtTime, 15, ColorUtils.interpolateColor2(Color.WHITE, Color.RED, hurtTime / 7));
                    Fonts.interSemiBold.get(18).drawString(target.getName(), x + 52.5F, y + 10.5f, -1);
                } else {
                    RoundedUtils.drawRound(x, y, width, height, 8, new Color(setting.color()));
                }

                if (setting.targetHudParticle.get()) {
                    ParticleRenderer.renderParticle(target, x + 2.5f, y + 2.5f);
                }
            }
            break;
            case "New": {

                float healthPercentage = target.getHealth() / target.getMaxHealth();
                float space = (width - 48) / 100;

                target.healthAnimation.animate((100 * space) * MathHelper.clamp_float(healthPercentage, 0, 1), 30);

                if (!shader) {
                    RoundedUtils.drawRound(x, y, width, height, 8, new Color(setting.bgColor(), true));

                    RoundedUtils.drawRound(x + 42, y + 22f, (100 * space), 6, 3, new Color(0, 0, 0, 120));
                    String text = String.format("%.1f", target.getHealth());

                    RoundedUtils.drawRound(x + 42, y + 22f, target.healthAnimation.getOutput(), 6f, 3, new Color(setting.color(0)));
                    RoundedUtils.drawRoundOutline(x, y, this.width, this.height, 5, 0.1f, new Color(0, 0, 0, 0), new Color(setting.color(0)));
                    RenderUtils.renderPlayer2D(target, x + 4.0f, y + 3.3f, 33, 12, -1);
                    Fonts.interSemiBold.get(19).drawString(text + "  ", x + 134, y + 9, setting.color());
                    Fonts.interSemiBold.get(17).drawString(target.getName(), x + 42, y + 9, setting.color());
                } else {
                    RoundedUtils.drawRound(x, y, width, height, 3, new Color(setting.color()));
                    RoundedUtils.drawRoundOutline(x, y, this.width, this.height, 5, 0.1f, new Color(0, 0, 0, 0), new Color(setting.color(0)));
                }

                if (setting.targetHudParticle.get()) {
                    ParticleRenderer.renderParticle(target, x + 2.5f, y + 2.5f);
                }
            }
            break;

            case "Novo 1": {

                float healthPercentage = target.getHealth() / target.getMaxHealth();
                float space = (width - 50) / 100;

                target.healthAnimation.animate((100 * space) * MathHelper.clamp_float(healthPercentage, 0, 1), 30);

                if (!shader) {
                    RenderUtils.drawEntityOnScreen(x + 18, y + 32, 15, target);
                    RenderUtils.drawBorderedRect(x, y, width, height, (float) 1, new Color(0, 0, 0, 50).getRGB(), new Color(40, 40, 40, 178).getRGB());
                    RenderUtils.drawRect(x, y, width, height, new Color(40, 40, 40, 178).getRGB());
                    RenderUtils.drawRect(x + 40, y + 14, 100 * space, 10, new Color(30, 30, 30, 203).getRGB());
                    RenderUtils.drawRect(x + 40, y + 15.5f, target.healthAnimation.getOutput(), 8.5f, new Color(0, 255, 0, 255).getRGB());
                    String text = String.format("%.1f", target.getHealth() / 2);
                    mc.fontRendererObj.drawStringWithShadow(text, x + 40, y + 27, -1);
                    mc.fontRendererObj.drawStringWithShadow(target.getName(), x + 40, y + 4, -1);
                    mc.fontRendererObj.drawStringWithShadow("❤", x + 40 + 23, y + 27, new Color(255, 100, 100).getRGB());
                }
            }
            break;

            case "Novo 2": {

                float healthPercentage = target.getHealth() / target.getMaxHealth();
                float space = (width - 50) / 100;

                target.healthAnimation.animate((100 * space) * MathHelper.clamp_float(healthPercentage, 0, 1), 30);

                if (!shader) {
                    RenderUtils.drawBorderedRect(x, y, width, height, 1f, new Color(0, 0, 0, 50).getRGB(), new Color(29, 29, 29, 180).getRGB());
                    RenderUtils.drawRect(x, y, width, height, new Color(40, 40, 40, 180).getRGB());
                    RenderUtils.renderPlayer2D(target, (x + 1.5f + 1), (float) (y + 0.4), 35, 0, -1);
                    String text = String.format("%.1f", target.getHealth());
                    mc.fontRendererObj.drawStringWithShadow("❤", x + 62 + 1, y + 26.6f, setting.color(0));
                    RenderUtils.drawRect(x + 40 + 1, y + 16.5f, target.healthAnimation.getOutput(), 8.8f, setting.color(0));
                    mc.fontRendererObj.drawStringWithShadow(text, x + 40 + 1, y + 28f, -1);
                    mc.fontRendererObj.drawStringWithShadow(target.getName(), x + 40f, y + 4, -1);
                }

                if (setting.targetHudParticle.get()) {
                    ParticleRenderer.renderParticle(target, (x + 1.5f + 1), (float) (y + 0.4));
                }
            }
            break;

            case "Novo 3": {

                float healthPercentage = target.getHealth() / target.getMaxHealth();
                float space = (width - 50) / 100;

                target.healthAnimation.animate((100 * space) * MathHelper.clamp_float(healthPercentage, 0, 1), 30);

                if (!shader) {

                    RenderUtils.drawBorderedRect(x, y, width, height, 1f, new Color(0, 0, 0, 50).getRGB(), new Color(29, 29, 29, 180).getRGB());
                    RenderUtils.drawRect(x, y, width, height, new Color(40, 40, 40, 130).getRGB());
                    RenderUtils.drawRect(x + 40 + 1, y + 16.5f, 100 * space + 1, 10.8f, new Color(0, 0, 0, 50).getRGB());
                    RenderUtils.drawRect(x + 40 + 1, y + 16.5f, target.healthAnimation.getOutput(), 10.8f, setting.color());
                    RenderUtils.renderPlayer2D(target, (x + 1.5f + 1f), (float) (y + 0.4), 35, 0, -1);
                    String text = String.format("%.1f", healthPercentage * 100) + "%";
                    mc.fontRendererObj.drawStringWithShadow(text, x + 1 + 40 + 50 * space - mc.fontRendererObj.getStringWidth(text) / 2f, y + 18f, -1);
                    mc.fontRendererObj.drawStringWithShadow(target.getName(), x + 1 + 40, y + 4, -1);
                }
            }

            if (setting.targetHudParticle.get()) {
                ParticleRenderer.renderParticle(target, (x + 1.5f + 1f), (float) (y + 0.4));
            }

            break;

            case "Novo 4": {

                float healthPercentage = target.getHealth() / target.getMaxHealth();
                float space = width;

                target.healthAnimation.animate(space * MathHelper.clamp_float(healthPercentage, 0, 1), 30);

                if (!shader) {
                    RenderUtils.drawRect(x - 1.0f, y + 4.0f, width, height, new Color(0, 0, 0, 150));
                    mc.fontRendererObj.drawStringWithShadow(target.getName(), x + 30.0f, y + 13.0f, -1);
                    RenderUtils.renderItemStack(target, x + 13, y + 25, 1, 0.5f);
                    GuiInventory.drawEntityOnScreen(x + 15, y + 40, 15, target.rotationYaw, -target.rotationPitch, target);
                    RenderUtils.drawRect(x - 1, y + 47, target.healthAnimation.getOutput(), 2f, ColorUtils.getHealthColor(target));
                }
            }

            break;

            case "Novo 5": {

                float healthPercentage = target.getHealth() / target.getMaxHealth();
                float space = width - Fonts.interRegular.get(17).getStringWidth("20.0") - 3 - 3.2f;

                target.healthAnimation.animate(space * MathHelper.clamp_float(healthPercentage, 0, 1), 30);

                if (!shader) {
                    RenderUtils.drawRect(x, y, width, height, setting.bgColor());
                    Fonts.interRegular.get(20).drawStringWithShadow(target.getName(), x + 40f, y + 3.2f + 1, -1);
                    String text = String.format("%.1f", target.getHealth());
                    Fonts.interRegular.get(17).drawString(text, x + target.healthAnimation.getOutput() + 3, y + 37F, -1);
                    RenderUtils.renderPlayer2D(target, x + 3.2f, y + 3.2f, 33, 0, -1);
                    RenderUtils.drawGradientRect(x + 3.2f, y + 39, target.healthAnimation.getOutput(), 4, true, setting.color(0), setting.color(90));
                    RenderUtils.renderItemStack(target, x + 40f, y + 15, 1, false, true);

                    if (setting.targetHudParticle.get()) {
                        ParticleRenderer.renderParticle(target, x + 3.2f, y + 3.2f);
                    }

                } else {
                    RenderUtils.drawRect(x, y, width, height, setting.color(0));
                }
            }

            break;

            case "Akrien": {

                float healthPercentage = target.getHealth() / target.getMaxHealth();
                float space = width - 2;

                if (!shader) {
                    target.healthAnimation.animate(space * MathHelper.clamp_float(healthPercentage, 0, 1), 30);

                    RenderUtils.drawRect(x, y + 2, width, height, setting.bgColor());
                    RenderUtils.drawBorderedRect(x + 1, y + 34.5f, space, 2.5f, 0.74f, new Color(0, 0, 0, 100).getRGB(), new Color(0, 0, 0, 100).getRGB());
                    RenderUtils.drawBorderedRect(x + 1, y + 38.5f, space, 2.5f, 0.74f, new Color(0, 0, 0, 100).getRGB(), new Color(0, 0, 0, 100).getRGB());

                    RenderUtils.drawHorizontalGradientSideways(x + 1, y + 34.5f, target.healthAnimation.getOutput(), 2.5f,
                            new Color(40, 145, 90).getRGB(),
                            new Color(170, 255, 220).getRGB());

                    if (target.getTotalArmorValue() > 0) {
                        RenderUtils.drawHorizontalGradientSideways(x + 1, y + 38.5f, target.getTotalArmorValue() * 5.75f, 2.5f,
                                new Color(40, 110, 160).getRGB(),
                                new Color(100, 225, 255).getRGB());
                    }

                    String text = String.format("%.1f", target.getHealth());
                    String text2 = String.format("%.1f", mc.thePlayer.getDistanceToEntity(target));

                    Fonts.psRegular.get(15).drawStringWithShadow("Health: " + text, x + 32.5f, y + 16f + 2, -1);
                    Fonts.psRegular.get(15).drawStringWithShadow("Distance: " + text2 + "m", x + 32.5f, y + 24.5f + 2, -1);
                    Fonts.psBold.get(21).drawStringWithShadow(target.getName(), x + 32.5f, y + 3 + 2, -1);

                    RenderUtils.renderPlayer2D(target, x + 1, y + 3, 30, 0, -1);
                } else {
                    RenderUtils.drawRect(x, y, width, height, setting.color());
                }
            }
            break;

            case "Innominate": {
                float healthPercentage = target.getHealth() / target.getMaxHealth();
                float space = width - 4;

                if (!shader) {
                    target.healthAnimation.animate(space * MathHelper.clamp_float(healthPercentage, 0, 1), 30);
                    RenderUtils.drawBorderedRect(x, y, width, height,1, Color.BLACK.getRGB(),Color.DARK_GRAY.getRGB());

                    String text = String.format("%.1f", target.getHealth());
                    Fonts.interRegular.get(16).drawStringWithShadow(target.getName() +  " | " + text, x + 2, y + 2 + 2, -1);

                    RenderUtils.renderItemStack(target,x + 2,y + 2 + Fonts.interRegular.get(16).getHeight(),1,false,0,false,false);

                    RenderUtils.drawBorderedRect(x + 2,y + 2 + Fonts.interRegular.get(16).getHeight() + 16,width - 4,5,1,Color.BLACK.getRGB(),Color.DARK_GRAY.getRGB());

                    RenderUtils.drawBorderedRect(x + 2,y + 2 + Fonts.interRegular.get(16).getHeight() + 16,target.healthAnimation.getOutput(),5,1,setting.color(0),Color.DARK_GRAY.getRGB());
                } else {
                    RenderUtils.drawBorderedRect(x, y, width, height,1, Color.BLACK.getRGB(),Color.DARK_GRAY.getRGB());
                }
            }

            break;
        }
        GlStateManager.popMatrix();
    }
}
