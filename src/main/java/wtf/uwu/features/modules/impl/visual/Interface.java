package wtf.uwu.features.modules.impl.visual;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import wtf.uwu.UwU;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.misc.TickEvent;
import wtf.uwu.events.impl.misc.WorldEvent;
import wtf.uwu.events.impl.packet.PacketEvent;
import wtf.uwu.events.impl.render.Render2DEvent;
import wtf.uwu.events.impl.render.RenderGuiEvent;
import wtf.uwu.events.impl.render.Shader2DEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.modules.impl.combat.KillAura;
import wtf.uwu.features.modules.impl.player.Stealer;
import wtf.uwu.features.modules.impl.visual.island.IslandRenderer;
import wtf.uwu.features.values.impl.*;
import wtf.uwu.gui.click.neverlose.NeverLose;
import wtf.uwu.gui.font.FontRenderer;
import wtf.uwu.gui.font.Fonts;
import wtf.uwu.utils.animations.Direction;
import wtf.uwu.utils.animations.impl.DecelerateAnimation;
import wtf.uwu.utils.player.MovementUtils;
import wtf.uwu.utils.render.ColorUtils;
import wtf.uwu.utils.render.RenderUtils;
import wtf.uwu.utils.render.RoundedUtils;

import java.awt.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.util.EnumChatFormatting.*;
import static wtf.uwu.gui.click.neverlose.NeverLose.*;

@ModuleInfo(name = "Interface", category = ModuleCategory.Visual)
public class Interface extends Module {

    private static final ResourceLocation INVENTORY_RESOURCE = new ResourceLocation("textures/gui/container/inventory.png");
    private static final Pattern LINK_PATTERN = Pattern.compile("(http(s)?://.)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[A-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)");
    private static final int SCOREBOARD_TEXT_COLOR = 553648127;
    private static final int SCOREBOARD_BACKGROUND_COLOR = 1342177280;
    private static final int SCOREBOARD_TITLE_BACKGROUND_COLOR = 1610612736;

    public final TextValue clientName = new TextValue("Client Name", "UwU Client BETA", this);

    public final MultiBoolValue elements = new MultiBoolValue("Elements", Arrays.asList(
            new BoolValue("Watermark", true),
            new BoolValue("Island", true),
            new BoolValue("Module List", true),
            new BoolValue("Info", true),
            new BoolValue("Health", true),
            new BoolValue("Potion HUD", true),
            new BoolValue("Target HUD", true),
            new BoolValue("Inventory", true),
            new BoolValue("Notification", true),
            new BoolValue("Pointer", true),
            new BoolValue("Session Info", true),
            new BoolValue("Key Bind", true),
            new BoolValue("Version Info", true),
            new BoolValue("Radar", true)
    ), this);

    public final ModeValue color = new ModeValue("Color Setting", new String[]{"Custom", "Rainbow", "Dynamic", "Fade", "Astolfo", "NeverLose", "Pulsing"}, "NeverLose", this);
    private final ColorValue mainColor = new ColorValue("Main Color", new Color(128, 128, 255), this, () -> !color.is("NeverLose"));
    private final ColorValue secondColor = new ColorValue("Second Color", new Color(128, 255, 255), this, () -> color.is("Fade") || color.is("Pulsing"));
    public final SliderValue fadeSpeed = new SliderValue("Fade Speed", 1, 1, 10, 1, this, () -> color.is("Dynamic") || color.is("Fade") || color.is("Pulsing"));

    public final BoolValue cFont = new BoolValue("C Fonts", true, this, () -> elements.isEnabled("Module List"));
    public final ModeValue fontMode = new ModeValue("C Fonts Mode", new String[]{"Bold", "Semi Bold", "Medium", "Regular", "Tahoma", "SFUI"}, "Semi Bold", this, () -> cFont.canDisplay() && cFont.get());
    public final SliderValue fontSize = new SliderValue("Font Size", 15, 10, 25, this, cFont::get);
    public final SliderValue animSpeed = new SliderValue("anim Speed", 200, 100, 400, 25, this, () -> elements.isEnabled("Module List"));
    public final ModeValue animation = new ModeValue("Animation", new String[]{"ScaleIn", "MoveIn", "Slide In"}, "ScaleIn", this, () -> elements.isEnabled("Module List"));
    public final SliderValue textHeight = new SliderValue("Text Height", 2, 0, 10, this, () -> elements.isEnabled("Module List"));
    public final ModeValue tags = new ModeValue("Suffix", new String[]{"None", "Simple", "Bracket", "Dash"}, "None", this, () -> elements.isEnabled("Module List"));
    public final BoolValue line = new BoolValue("Line", true, this, () -> elements.isEnabled("Module List"));
    public final BoolValue outLine = new BoolValue("Outline", true, this, () -> line.canDisplay() && line.get());

    public final BoolValue background = new BoolValue("Background", true, this, () -> elements.isEnabled("Module List"));
    public final ModeValue bgColor = new ModeValue("Background Color", new String[]{"Dark", "Synced", "Custom", "NeverLose"}, "Synced", this, background::get);
    private final ColorValue bgCustomColor = new ColorValue("Background Custom Color", new Color(32, 32, 64), this, () -> bgColor.canDisplay() && bgColor.is("Custom"));
    private final SliderValue bgAlpha = new SliderValue("Background Alpha", 100, 1, 255, 1, this);

    public final ModeValue watemarkMode = new ModeValue("Watermark Mode", new String[]{"Text", "Styles", "Rect", "Exhi", "Exhi 2", "Nursultan", "NeverLose", "Novo", "OneTap"}, "NeverLose", this, () -> elements.isEnabled("Watermark"));
    public final ModeValue infoMode = new ModeValue("Info Mode", new String[]{"Exhi", "Moon", "Tenacity", "Astolfo"}, "Default", this, () -> elements.isEnabled("Info"));
    public final ModeValue versionMode = new ModeValue("Version Mode", new String[]{"Default", "Exhi"}, "Default", this, () -> elements.isEnabled("Version Info"));
    public final ModeValue potionHudMode = new ModeValue("Potion Mode", new String[]{"Default", "Nursultan", "Exhi", "Sexy", "Type 1", "NeverLose", "Mod"}, "Mod", this, () -> elements.isEnabled("Potion HUD"));
    public final ModeValue targetHudMode = new ModeValue("TargetHUD Mode", new String[]{"Astolfo", "Type 1", "Type 2", "Felix", "Exhi", "Adjust", "Moon", "Augustus", "New", "Novo", "Akrien", "Innominate"}, "Astolfo", this, () -> elements.isEnabled("Target HUD"));
    public final BoolValue targetHudParticle = new BoolValue("TargetHUD Particle", true, this, () -> elements.isEnabled("Target HUD"));
    public final ModeValue notificationMode = new ModeValue("Notification Mode", new String[]{"Default", "Test", "Type 2", "Type 3", "Type 4", "Type 5", "Test2", "Exhi"}, "Exhi", this, () -> elements.isEnabled("Notification"));
    public final BoolValue centerNotif = new BoolValue("Center Notification", true, this, () -> notificationMode.is("Exhi"));
    public final ModeValue keyBindMode = new ModeValue("Key Bind Mode", new String[]{"Type 1"}, "Type 1", this, () -> elements.isEnabled("Key Bind"));
    public final ModeValue sessionInfoMode = new ModeValue("Session Info Mode", new String[]{"Default", "Exhi", "Rise", "Moon", "Opai", "Novo", "Novo 1"}, "Exhi", this, () -> elements.isEnabled("Session Info"));
    public final SliderValue radarSize = new SliderValue("Radar Size", 70, 25, 200, this, () -> elements.isEnabled("Radar"));
    public final ModeValue radarMode = new ModeValue("Radar Mode", new String[]{"Default", "Exhi", "Astolfo"}, "Default", this, () -> elements.isEnabled("Radar"));

    public final BoolValue armorBg = new BoolValue("Armor Background", true, this, () -> elements.isEnabled("Armor"));
    public final BoolValue armorEnchanted = new BoolValue("Armor Enchanted", true, this, () -> elements.isEnabled("Armor"));
    public final BoolValue armorInfo = new BoolValue("Armor Info", true, this, () -> elements.isEnabled("Armor"));

    public final BoolValue customScoreboard = new BoolValue("Custom Scoreboard", true, this);
    public final BoolValue hideScoreboard = new BoolValue("Hide Scoreboard", false, this, () -> !customScoreboard.get());
    public final BoolValue hideScoreRed = new BoolValue("Hide Scoreboard Red Points", true, this, customScoreboard::get);
    public final BoolValue fixHeight = new BoolValue("Fix Height", true, this, customScoreboard::get);
    public final BoolValue hideBackground = new BoolValue("Hide Background", false, this, customScoreboard::get);

    public final BoolValue chatCombine = new BoolValue("Chat Combine", true, this);
    public final BoolValue newButton = new BoolValue("New Button", true, this);
    public final BoolValue hotBar = new BoolValue("New Hot Bar", false, this);

    public final BoolValue cape = new BoolValue("Cape", true, this);
    public final ModeValue capeMode = new ModeValue("Cape Mode", new String[]{"Default", "Sexy", "Test"}, "Default", this);
    public final BoolValue wavey = new BoolValue("Wavey Cape", true, this);
    public final BoolValue enchanted = new BoolValue("Enchanted", true, this, () -> cape.get() && !wavey.get());

    private final DecimalFormat bpsFormat = new DecimalFormat("0.00");
    private final DecimalFormat xyzFormat = new DecimalFormat("0");
    private final DecimalFormat fpsFormat = new DecimalFormat("0");
    private final DecimalFormat healthFormat = new DecimalFormat("0.#", new DecimalFormatSymbols(Locale.ENGLISH));
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm");
    private final DateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss");

    public final Map<EntityPlayer, DecelerateAnimation> animationEntityPlayerMap = new HashMap<>();
    private final DecelerateAnimation sessionInfoAnimation = new DecelerateAnimation(300, 1, Direction.BACKWARDS);

    public int lost = 0, killed = 0, won = 0;
    public int prevMatchKilled = 0, matchKilled = 0, match;
    private final Random random = new Random();
    public int scoreBoardHeight = 0;

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        final ScaledResolution sr = event.scaledResolution();

        if (elements.isEnabled("Island")) {
            IslandRenderer.INSTANCE.render(sr, false);
        }

        if (elements.isEnabled("Watermark")) {
            renderWatermark(sr);
        }

        if (elements.isEnabled("Info")) {
            renderInfo(sr);
        }

        if (elements.isEnabled("Version Info")) {
            renderVersionInfo(sr);
        }

        if (elements.isEnabled("Armor")) {
            renderArmor(sr);
        }

        if (elements.isEnabled("Potion HUD")) {
            renderPotionHud(sr);
        }

        if (elements.isEnabled("Health")) {
            renderHealth(sr, false);
        }

        if (elements.isEnabled("Session Info")) {
            renderSessionInfo(sr);
        }

        if (elements.isEnabled("Notification")) {
            UwU.INSTANCE.getNotificationManager().publish(sr, false);
        }
    }

    @EventTarget
    public void onShader2D(Shader2DEvent event) {
        final ScaledResolution sr = new ScaledResolution(mc);

        if (elements.isEnabled("Island")) {
            IslandRenderer.INSTANCE.render(sr, true);
        }

        if (elements.isEnabled("Notification")) {
            UwU.INSTANCE.getNotificationManager().publish(sr, true);
        }
    }

    @EventTarget
    public void onRenderGui(RenderGuiEvent event) {
        if (elements.isEnabled("Health")) {
            if (mc.currentScreen instanceof GuiInventory || (mc.currentScreen instanceof GuiChest && !getModule(Stealer.class).isEnabled()) || mc.currentScreen instanceof GuiContainerCreative) {
                renderHealth(new ScaledResolution(mc), true);
            }
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        mainColor.setRainbow(color.is("Rainbow"));
        KillAura aura = getModule(KillAura.class);

        if (aura.isEnabled()) {
            animationEntityPlayerMap.entrySet().removeIf(entry -> entry.getKey().isDead || (!aura.targets.contains(entry.getKey()) && entry.getKey() != mc.thePlayer));
        } else if (aura.target == null && !(mc.currentScreen instanceof GuiChat)) {
            animationEntityPlayerMap.values().forEach(anim -> anim.setDirection(Direction.BACKWARDS));
            animationEntityPlayerMap.entrySet().removeIf(entry -> entry.getValue().finished(Direction.BACKWARDS));
        }

        if (!aura.targets.isEmpty() && !(mc.currentScreen instanceof GuiChat)) {
            for (EntityLivingBase entity : aura.targets) {
                if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
                    animationEntityPlayerMap.computeIfAbsent((EntityPlayer) entity, k -> new DecelerateAnimation(175, 1)).setDirection(Direction.FORWARDS);
                }
            }
        }

        if (mc.currentScreen instanceof GuiChat) {
            animationEntityPlayerMap.computeIfAbsent(mc.thePlayer, k -> new DecelerateAnimation(175, 1)).setDirection(Direction.FORWARDS);
        } else if (animationEntityPlayerMap.containsKey(mc.thePlayer)) {
            animationEntityPlayerMap.get(mc.thePlayer).setDirection(Direction.BACKWARDS);
            if (animationEntityPlayerMap.get(mc.thePlayer).finished(Direction.BACKWARDS)) {
                animationEntityPlayerMap.remove(mc.thePlayer);
            }
        }

        boolean shouldShowSessionInfo = elements.isEnabled("Session Info") && sessionInfoMode.is("Exhi");
        sessionInfoAnimation.setDirection(shouldShowSessionInfo ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        prevMatchKilled = matchKilled;
        matchKilled = 0;
        match = Math.min(match + 1, 6);
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof S02PacketChat) {
            final String chatMessage = ((S02PacketChat) packet).getChatComponent().getUnformattedText();
            if (chatMessage.contains("was killed by " + mc.thePlayer.getName())) {
                killed++;
                prevMatchKilled = matchKilled;
                matchKilled++;
            }
            if (chatMessage.contains("You Died! Want to play again?")) {
                lost++;
            }
        }

        if (packet instanceof S45PacketTitle) {
            final S45PacketTitle s45 = (S45PacketTitle) packet;
            if (s45.getType() == S45PacketTitle.Type.TITLE) {
                final String titleText = s45.getMessage().getUnformattedText();
                if (titleText.contains("VICTORY!")) {
                    won++;
                } else if (titleText.contains("GAME OVER!") || titleText.contains("DEFEAT!") || titleText.contains("YOU DIED!")) {
                    lost++;
                }
            }
        }
    }

    private void renderWatermark(ScaledResolution sr) {
        switch (watemarkMode.get()) {
            case "Text": {
                Fonts.interBold.get(30).drawStringWithShadow(clientName.get(), 10, 10, color(0));
                break;
            }
            case "Styles 2": {
                final String dateString = dateFormat2.format(new Date());
                final String serverString = mc.isSingleplayer() ? "singleplayer" : mc.getCurrentServerData().serverIP.toLowerCase();
                final String text = String.format("uwu%s | %s | %d FPS | %s | %s", WHITE, mc.thePlayer.getName(), Minecraft.getDebugFPS(), serverString, dateString);

                final float x = 7, y = 7;
                final FontRenderer font = Fonts.interSemiBold.get(17);
                final float width = font.getStringWidth(text) + 8;
                final float height = font.getHeight() + 6;

                RoundedUtils.drawRound(x, y, width, height, 4, new Color(bgColor(), true));
                font.drawString(text, x + 4, y + 4.5f, color(1));
                break;
            }
            case "Exhi":
            case "Exhi 2": {
                final boolean useExhi2Style = watemarkMode.is("Exhi 2");
                final String version = useExhi2Style ? "§7[§f" + ViaLoadingBase.getInstance().getTargetVersion().getName() + "§7]§r " : "";
                final String text = (clientName.get().charAt(0) + "§f" + clientName.get().substring(1)) +
                        " §r" + version + "§7[§f" + Minecraft.getDebugFPS() + " FPS§7]§r";
                mc.fontRendererObj.drawStringWithShadow(text, 4.0f, 4.0f, color());
                break;
            }
            case "Nursultan 2": {
                final float posX = 7f, posY = 7.5f, fontSize = 15f, padding = 5.0f, barWidth = 10.0f;
                float currentX = posX;

                currentX += renderInfoBlock(currentX, posY, fontSize, padding, barWidth, "S", " | MoonLight", true) + padding;
                currentX += renderInfoBlock(currentX, posY, fontSize, padding, barWidth, "W", mc.thePlayer.getName(), false) + padding;
                renderInfoBlock(currentX, posY, fontSize, padding, barWidth, "X", Minecraft.getDebugFPS() + " Fps", false);

                final float posY2 = posY + 15 + padding;
                currentX = posX;

                String pos = String.format("%d %d %d", (int) mc.thePlayer.posX, (int) mc.thePlayer.posY, (int) mc.thePlayer.posZ);
                currentX += renderInfoBlock(currentX, posY2, fontSize, padding, barWidth, "F", pos, false) + padding;
                String ping = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()).getResponseTime() + " Ping";
                renderInfoBlock(currentX, posY2, fontSize, padding, barWidth, "Q", ping, false);
                break;
            }
            case "NeverLose": {
                final FontRenderer titleFont = Fonts.interBold.get(20);
                final FontRenderer infoFont = Fonts.interSemiBold.get(16);
                final FontRenderer iconFont = Fonts.nursultan.get(20);

                final float x = 3, y = 5;
                final float height = titleFont.getHeight() + 4;
                final float textY = y + (height - titleFont.getHeight()) / 2f + 1f;
                final float iconTextY = y + (height - infoFont.getHeight()) / 2f + 1f;
                final float iconY = y + (height - iconFont.getHeight()) / 2f + 1f;

                final String clientText = clientName.getText();
                final float titleBoxWidth = titleFont.getStringWidth(clientText) + 10;
                RoundedUtils.drawRound(x, y, titleBoxWidth, height, 4, ColorUtils.applyOpacity(NeverLose.bgColor, 1f));
                titleFont.drawOutlinedString(clientText, x + 5, textY, textRGB, outlineTextRGB);

                final float infoBoxX = x + titleBoxWidth + 4;
                final String[] icons = {"W", "X", "V"};
                final String[] infoTexts = {mc.thePlayer.getName(), Minecraft.getDebugFPS() + "fps", dateFormat.format(new Date())};
                float infoBoxContentsWidth = 0;
                for (int i = 0; i < icons.length; i++) {
                    infoBoxContentsWidth += iconFont.getStringWidth(icons[i]) + infoFont.getStringWidth(infoTexts[i]) + 7;
                }

                final float infoBoxTotalWidth = infoBoxContentsWidth + 3;
                RoundedUtils.drawRound(infoBoxX, y, infoBoxTotalWidth, height, 4, ColorUtils.applyOpacity(NeverLose.bgColor, 1f));

                float currentInfoX = infoBoxX + 5;
                for (int i = 0; i < icons.length; i++) {
                    iconFont.drawString(icons[i], currentInfoX, iconY, iconRGB);
                    currentInfoX += iconFont.getStringWidth(icons[i]);
                    infoFont.drawString(infoTexts[i], currentInfoX, iconTextY, textRGB);
                    currentInfoX += infoFont.getStringWidth(infoTexts[i]) + 7;
                }
                break;
            }
            case "Novo": {
                final String name = clientName.get();
                final FontRenderer font = Fonts.sfui.get(20);
                float x = 1;
                for (int i = 0; i < name.length(); i++) {
                    String c = String.valueOf(name.charAt(i));
                    font.drawStringWithShadow(c, x, 4.0F, color(i * 10));
                    x += font.getStringWidth(c);
                }
                font.drawStringWithShadow(GRAY + " (" + WHITE + dateFormat.format(new Date()) + GRAY + ")", x, 4.0F, -1);
                break;
            }
            case "Rect": {
                final String rectText = WHITE + " - " + dateFormat.format(new Date()) + " - " + mc.thePlayer.getName() + " - " + fpsFormat.format(Minecraft.getDebugFPS()) + " FPS";
                final String name = clientName.get();
                final FontRenderer font = Fonts.sfui.get(18);
                final float totalWidth = font.getStringWidth(name + rectText) + 4;
                final float xStart = 9.0F;

                RenderUtils.drawRect(xStart - 2.5f, 5.5f, totalWidth, 12, bgColor());
                RenderUtils.drawHorizontalGradientSideways(xStart - 2.5f, 5.5f, totalWidth, 1.5, color(0), color(90));

                float currentX = xStart - 1.0f;
                font.drawStringWithShadow(String.valueOf(name.charAt(0)), currentX, 9.0f, color(0));
                currentX += font.getStringWidth(String.valueOf(name.charAt(0)));
                font.drawStringWithShadow(WHITE + name.substring(1), currentX, 9.0f, -1);
                currentX += font.getStringWidth(WHITE + name.substring(1));
                font.drawStringWithShadow(rectText, currentX, 9.0f, -1);
                break;
            }
            case "OneTap": {
                final String serverIP = mc.isSingleplayer() ? "localhost" : mc.getCurrentServerData().serverIP;
                final String text = String.format("uwu | %s | %s | delay: %dms | %s",
                        UwU.INSTANCE.getDiscordRP().getName(), serverIP,
                        mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()).getResponseTime(),
                        dateFormat2.format(new Date()));

                final FontRenderer font = Fonts.interSemiBold.get(14);
                final float textWidth = font.getStringWidth(text);
                final float x = 5, y = 5;
                final float width = textWidth + 6, height = 13.5f;

                RenderUtils.drawRect(x, y, width, height, bgColor());
                RenderUtils.drawGradientRect(x, y, x + width, y + 1.5f, true, color(0), color(180));
                font.drawStringWithShadow(text, x + 3, y + 5f, Color.WHITE.getRGB());
                break;
            }
        }
    }

    private float renderInfoBlock(float x, float y, float fontSize, float padding, float barWidth, String icon, String text, boolean useThemeColor) {
        final FontRenderer textFont = Fonts.interMedium.get(fontSize);
        final FontRenderer iconFont = Fonts.nursultan.get(18);
        final float textWidth = textFont.getStringWidth(text);
        final float blockWidth = barWidth + padding * 2.5f + textWidth;

        RoundedUtils.drawRound(x, y, blockWidth, 15, 4.0F, new Color(bgColor(), true));
        iconFont.drawString(icon, x + padding, y + 3.5f, color());
        textFont.drawString(text, x + barWidth + padding * 1.5f, y + barWidth / 2.0F + 1.5F, useThemeColor ? color() : -1);
        return blockWidth;
    }

    private void renderInfo(ScaledResolution sr) {
        final float yOffset = mc.currentScreen instanceof GuiChat ? -14.0f : 0f;
        final float yBase = sr.getScaledHeight() - mc.fontRendererObj.FONT_HEIGHT + yOffset;

        switch (infoMode.get()) {
            case "Exhi":
                final String exhiText = String.format("XYZ: %s%s %s %s %sBPS: %s%s",
                        WHITE, xyzFormat.format(mc.thePlayer.posX), xyzFormat.format(mc.thePlayer.posY), xyzFormat.format(mc.thePlayer.posZ),
                        RESET, WHITE, bpsFormat.format(MovementUtils.getBPS()));
                mc.fontRendererObj.drawStringWithShadow(exhiText, 2, yBase, color(0));
                break;
            case "Moon":
                final String moonText = "FPS: " + WHITE + Minecraft.getDebugFPS();
                mc.fontRendererObj.drawStringWithShadow(moonText, 2, yBase, color(0));
                break;
            case "Astolfo":
                final FontRenderer astolfoFont = Fonts.sfui.get(18);
                astolfoFont.drawStringWithShadow(String.format("%s, %s, %s", xyzFormat.format(mc.thePlayer.posX), xyzFormat.format(mc.thePlayer.posY), xyzFormat.format(mc.thePlayer.posZ)), 2, sr.getScaledHeight() - 10, -1);
                astolfoFont.drawStringWithShadow(bpsFormat.format(MovementUtils.getBPS()) + " b/s", 2, sr.getScaledHeight() - 20, -1);
                astolfoFont.drawStringWithShadow("FPS: " + fpsFormat.format(Minecraft.getDebugFPS()), 2, sr.getScaledHeight() - 30, -1);
                break;
            case "Tenacity":
                final FontRenderer boldFont = Fonts.psBold.get(19);
                final FontRenderer regularFont = Fonts.psRegular.get(19);
                float y = sr.getScaledHeight() - 9;
                boldFont.drawStringWithShadow("XYZ: ", 2, y, color(0));
                regularFont.drawStringWithShadow(WHITE + xyzFormat.format(mc.thePlayer.posX) + " " + xyzFormat.format(mc.thePlayer.posY) + " " + xyzFormat.format(mc.thePlayer.posZ), 26, y, -1);
                y -= 9;
                boldFont.drawStringWithShadow("Speed:", 2, y, color(0));
                regularFont.drawStringWithShadow(WHITE + bpsFormat.format(MovementUtils.getBPS()), 35, y, -1);
                y -= 9;
                boldFont.drawStringWithShadow("FPS:", 2, y, color(0));
                regularFont.drawStringWithShadow(WHITE + fpsFormat.format(Minecraft.getDebugFPS()), 24, y, -1);
                break;
        }
    }

    private void renderVersionInfo(ScaledResolution sr) {
        final float yOffset = mc.currentScreen instanceof GuiChat ? -14.0f : 0f;
        final float y = sr.getScaledHeight() - mc.fontRendererObj.FONT_HEIGHT + yOffset;
        String text;

        switch (versionMode.get()) {
            case "Default":
                text = String.format("%s%s §7- %s §7- 1.0", WHITE, UwU.INSTANCE.getVersion(), UwU.INSTANCE.getDiscordRP().getName());
                final FontRenderer font = Fonts.interMedium.get(17);
                font.drawStringWithShadow(text, sr.getScaledWidth() - font.getStringWidth(text) - 2.0f, y, color(0));
                break;
            case "Exhi":
                text = String.format("%s%s Build §7- §f§l112519§r §7- %s", GRAY, INSTANCE.getVersion(), INSTANCE.getDiscordRP().getName().replace(".", "").replace("_", "").replace("eoniann", "Eonian"));
                mc.fontRendererObj.drawStringWithShadow(text, sr.getScaledWidth() - mc.fontRendererObj.getStringWidth(text) - 2.0f, y, color(0));
                break;
        }
    }

    private void renderArmor(ScaledResolution sr) {
        final boolean onWater = mc.thePlayer.isEntityAlive() && mc.thePlayer.isInsideOfMaterial(Material.water);
        final float x = sr.getScaledWidth() / 2f - 4;
        final float y = sr.getScaledHeight() - (onWater ? 65 : 55) + (mc.thePlayer.capabilities.isCreativeMode ? 14 : 0);
        RenderUtils.renderItemStack(mc.thePlayer, x, y, 1, armorEnchanted.get(), 0.5f, armorBg.get(), armorInfo.get());
    }

    private void renderPotionHud(ScaledResolution sr) {
        final Collection<PotionEffect> activeEffects = mc.thePlayer.getActivePotionEffects();
        if (activeEffects.isEmpty()) return;

        switch (potionHudMode.get()) {
            case "Exhi": {
                ArrayList<PotionEffect> potions = new ArrayList<>(activeEffects);
                potions.sort(Comparator.comparingDouble(e -> -mc.fontRendererObj.getStringWidth(I18n.format(Potion.potionTypes[e.getPotionID()].getName()))));
                float y = (sr.getScaledHeight() - (elements.isEnabled("Version Info") ? 18 : 9)) + (mc.currentScreen instanceof GuiChat ? -14.0f : 0f);

                for (PotionEffect effect : potions) {
                    final Potion potion = Potion.potionTypes[effect.getPotionID()];
                    String name = I18n.format(potion.getName());
                    if (effect.getAmplifier() > 0) {
                        name += " " + intToRomanByGreedy(effect.getAmplifier() + 1);
                    }
                    final String duration = Potion.getDurationString(effect);
                    final String fullText = name + " §f" + duration;
                    final float width = mc.fontRendererObj.getStringWidth(fullText);

                    mc.fontRendererObj.drawStringWithShadow(fullText, sr.getScaledWidth() - width - 2, y, potion.getLiquidColor());
                    y -= 9.0f;
                }
                break;
            }
            case "Mod": {
                GL11.glPushMatrix();
                float x = 25;
                float y = sr.getScaledHeight() / 2f - 75f;
                float yOffset = 0;
                final FontRenderer font = Fonts.interMedium.get(16);

                for (final PotionEffect effect : activeEffects) {
                    final Potion potion = Potion.potionTypes[effect.getPotionID()];
                    final String effectName = I18n.format(potion.getName()) + " " + intToRomanByGreedy(effect.getAmplifier() + 1);
                    final String durationStr = "§f" + Potion.getDurationString(effect);
                    final float height = font.getHeight() * 2 + 4;
                    final float width = Math.max(font.getStringWidth(effectName), font.getStringWidth(durationStr)) + 25;

                    RoundedUtils.drawRound(x - 22, y + yOffset - 7, width, height, 3, new Color(bgColor(0, 150), true));
                    if (potion.hasStatusIcon()) {
                        drawPotionIconGL(potion, x - 20F, y + yOffset - 5);
                    }
                    font.drawString(effectName, x + 2f, y + yOffset - 4f, potion.getLiquidColor());
                    font.drawString(durationStr, x + 2f, y + yOffset + 7f, -1);

                    yOffset += height + 4;
                }
                GL11.glPopMatrix();
                break;
            }
        }
    }

    private void renderSessionInfo(ScaledResolution sr) {
        if (sessionInfoMode.is("Exhi")) {
            final String time = RenderUtils.sessionTime();
            final float width = mc.fontRendererObj.getStringWidth(time);
            final float x = sr.getScaledWidth() / 2.0f - width / 2.0f;
            final float y = BossStatus.bossName != null && BossStatus.statusBarTime > 0 ? 47 : 30.0f;

            final double animProgress = sessionInfoAnimation.getOutput();
            if (animProgress > 0.01) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(x + width / 2f, y, 0);
                GlStateManager.scale(animProgress, animProgress, 1);
                GlStateManager.translate(-(x + width / 2f), -y, 0);
                mc.fontRendererObj.drawStringWithShadow(time, x, y, ColorUtils.applyOpacity(-1, (float) animProgress));
                GlStateManager.popMatrix();
            }
        }
    }

    private void renderHealth(ScaledResolution sr, boolean inGui) {
        final float absorption = mc.thePlayer.getAbsorptionAmount();
        final String healthString = healthFormat.format(mc.thePlayer.getHealth() / 2.0f) + "§c❤" + (absorption <= 0.0f ? "" : " §e" + healthFormat.format(absorption / 2.0f) + "§6❤");

        int yOffset = 0;
        if (inGui) {
            GuiScreen screen = mc.currentScreen;
            if (screen instanceof GuiInventory) yOffset = 70;
            else if (screen instanceof GuiContainerCreative) yOffset = 80;
            else if (screen instanceof GuiChest) yOffset = ((GuiChest) screen).ySize / 2 - 15;
        }

        final int x = sr.getScaledWidth() / 2 - mc.fontRendererObj.getStringWidth(healthString) / 2;
        final int y = sr.getScaledHeight() / 2 + 25 + yOffset;

        mc.fontRendererObj.drawString(healthString, x, y, ColorUtils.getHealthColor(mc.thePlayer), true);

        GlStateManager.pushMatrix();
        mc.getTextureManager().bindTexture(Gui.icons);
        random.setSeed(mc.ingameGUI.getUpdateCounter() * 312871L);

        final float heartsStartX = sr.getScaledWidth() / 2.0f - 45;
        final float maxHealth = mc.thePlayer.getMaxHealth();
        final int health = MathHelper.ceiling_float_int(mc.thePlayer.getHealth());
        final int lastHealth = mc.ingameGUI.lastPlayerHealth;
        final boolean highlight = mc.ingameGUI.healthUpdateCounter > (long) mc.ingameGUI.getUpdateCounter() && (mc.ingameGUI.healthUpdateCounter - (long) mc.ingameGUI.getUpdateCounter()) / 3L % 2L == 1L;
        final int regenIndex = mc.thePlayer.isPotionActive(Potion.regeneration) ? mc.ingameGUI.getUpdateCounter() % MathHelper.ceiling_float_int(maxHealth + 5.0f) : -1;

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        for (int i = 0; i < MathHelper.ceiling_float_int(maxHealth / 2.0f); ++i) {
            int heartX = (int) heartsStartX + i * 8;
            int heartY = sr.getScaledHeight() / 2 + 15 + yOffset;

            if (health <= 4) heartY += random.nextInt(2);
            if (i == regenIndex) heartY -= 2;

            int iconU = 16;
            if (mc.thePlayer.isPotionActive(Potion.poison)) iconU += 36;
            else if (mc.thePlayer.isPotionActive(Potion.wither)) iconU += 72;

            int iconV = mc.theWorld.getWorldInfo().isHardcoreModeEnabled() ? 5 * 9 : 0;

            Gui.drawTexturedModalRect(heartX, heartY, 16 + (highlight ? 1 : 0) * 9, iconV, 9, 9);

            if (highlight) {
                if (i * 2 + 1 < lastHealth) Gui.drawTexturedModalRect(heartX, heartY, iconU + 54, iconV, 9, 9);
                if (i * 2 + 1 == lastHealth) Gui.drawTexturedModalRect(heartX, heartY, iconU + 63, iconV, 9, 9);
            }

            if (i * 2 + 1 < health) Gui.drawTexturedModalRect(heartX, heartY, iconU + 36, iconV, 9, 9);
            else if (i * 2 + 1 == health) Gui.drawTexturedModalRect(heartX, heartY, iconU + 45, iconV, 9, 9);
        }
        GlStateManager.popMatrix();
    }

    public void drawScoreboard(ScaledResolution sr, ScoreObjective objective, Scoreboard scoreboard, Collection<Score> sortedScores) {
        Collection<Score> scores = scoreboard.getSortedScores(objective);
        List<Score> list = Lists.newArrayList(Iterables.filter(scores, s -> s.getPlayerName() != null && !s.getPlayerName().startsWith("#")));
        scores = list.size() > 15 ? Lists.newArrayList(Iterables.skip(list, list.size() - 15)) : list;

        int maxWidth = mc.fontRendererObj.getStringWidth(objective.getDisplayName());
        for (Score score : scores) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            String entry = ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()) + ": " + RED + score.getScorePoints();
            maxWidth = Math.max(maxWidth, mc.fontRendererObj.getStringWidth(entry));
        }

        final int listHeight = scores.size() * mc.fontRendererObj.FONT_HEIGHT;
        int baseY = sr.getScaledHeight() / 2 + listHeight / 3;
        if (this.fixHeight.get()) {
            baseY = Math.max(baseY, scoreBoardHeight + listHeight + mc.fontRendererObj.FONT_HEIGHT + 17);
        }

        final int xStart = sr.getScaledWidth() - maxWidth - 3;
        final int xEnd = sr.getScaledWidth() - 1;

        int i = 0;
        for (Score score : scores) {
            i++;
            final ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            String playerName = ScorePlayerTeam.formatPlayerName(team, score.getPlayerName());
            final String points = RED + "" + score.getScorePoints();
            final int y = baseY - i * mc.fontRendererObj.FONT_HEIGHT;

            if (!hideBackground.get()) {
                RenderUtils.drawRect(xStart - 2, y, xEnd, y + mc.fontRendererObj.FONT_HEIGHT, SCOREBOARD_BACKGROUND_COLOR);
            }

            final Matcher linkMatcher = LINK_PATTERN.matcher(playerName);
            if (linkMatcher.find()) {
                playerName = "wesk.top";
                mc.fontRendererObj.drawGradientWithShadow(playerName, xStart, y, (index) -> new Color(this.color(index)));
            } else {
                mc.fontRendererObj.drawString(playerName, xStart, y, SCOREBOARD_TEXT_COLOR, true);
            }

            if (!this.hideScoreRed.get()) {
                mc.fontRendererObj.drawString(points, xEnd - mc.fontRendererObj.getStringWidth(points), y, SCOREBOARD_TEXT_COLOR, true);
            }
        }

        final String title = objective.getDisplayName();
        final int titleY = baseY - listHeight - mc.fontRendererObj.FONT_HEIGHT;
        if (!hideBackground.get()) {
            RenderUtils.drawRect(xStart - 2, titleY - 2, xEnd, titleY + mc.fontRendererObj.FONT_HEIGHT, SCOREBOARD_TITLE_BACKGROUND_COLOR);
            RenderUtils.drawGradientRect(xStart - 2, titleY - 2, xEnd, titleY - 1, false, color(0), color(180));
        }
        mc.fontRendererObj.drawString(title, xStart + maxWidth / 2 - mc.fontRendererObj.getStringWidth(title) / 2, titleY, SCOREBOARD_TEXT_COLOR, true);
    }

    private void drawPotionIconGL(Potion potion, float x, float y) {
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        mc.getTextureManager().bindTexture(INVENTORY_RESOURCE);
        final int iconIndex = potion.getStatusIconIndex();
        Gui.drawTexturedModalRect((int) x, (int) y, iconIndex % 8 * 18, 198 + iconIndex / 8 * 18, 18, 18);

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private String intToRomanByGreedy(int num) {
        if (num <= 0) return "";
        if (num > 3999) return String.valueOf(num);
        final int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        final String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length && num > 0; i++) {
            while (num >= values[i]) {
                num -= values[i];
                sb.append(symbols[i]);
            }
        }
        return sb.toString();
    }

    public FontRenderer getFr() {
        return switch (fontMode.get()) {
            case "Bold" -> Fonts.interBold.get(fontSize.get());
            case "Semi Bold" -> Fonts.interSemiBold.get(fontSize.get());
            case "Medium" -> Fonts.interMedium.get(fontSize.get());
            case "Regular" -> Fonts.interRegular.get(fontSize.get());
            case "Tahoma" -> Fonts.Tahoma.get(fontSize.get());
            case "SFUI" -> Fonts.sfui.get(fontSize.get());
            default -> Fonts.interRegular.get(fontSize.get());
        };
    }

    public Color getMainColor() {
        return mainColor.get();
    }

    public Color getSecondColor() {
        return secondColor.get();
    }

    public int getRainbow(int counter) {
        return Color.HSBtoRGB(getRainbowHSB(counter)[0], getRainbowHSB(counter)[1], getRainbowHSB(counter)[2]);
    }

    public static int astolfoRainbow(final int offset, final float saturation, final float brightness) {
        double rainbowState = Math.ceil((System.currentTimeMillis() + (long) offset * 100L) / 20.0);
        rainbowState %= 360.0;
        return Color.getHSBColor((float) (rainbowState / 360.0), saturation, brightness).getRGB();
    }

    public float[] getRainbowHSB(int counter) {
        double rainbowState = Math.ceil((System.currentTimeMillis() + (long) counter * 100L) / 20.0);
        rainbowState %= 360;
        return new float[]{(float) (rainbowState / 360), mainColor.getSaturation(), mainColor.getBrightness()};
    }

    public int color() {
        return color(0);
    }

    public int color(int counter) {
        return color(counter, getMainColor().getAlpha());
    }

    public int color(int counter, int alpha) {
        int baseRGB = switch (color.get()) {
            case "Rainbow" -> getRainbow(counter);
            case "Dynamic" -> ColorUtils.colorSwitch(getMainColor(), new Color(ColorUtils.darker(getMainColor().getRGB(), 0.25F)), 2000.0F, counter, 75L, fadeSpeed.get()).getRGB();
            case "Fade" -> ColorUtils.colorSwitch(getMainColor(), getSecondColor(), 2000.0F, counter, 75L, fadeSpeed.get()).getRGB();
            case "Astolfo" -> astolfoRainbow(counter, mainColor.getSaturation(), mainColor.getBrightness());
            case "Pulsing" -> ColorUtils.fade(mainColor.get(), 100, counter).getRGB();
            case "NeverLose" -> iconRGB;
            default -> mainColor.get().getRGB();
        };
        return ColorUtils.swapAlpha(baseRGB, alpha);
    }

    public int bgColor() {
        return bgColor(0);
    }

    public int bgColor(int counter) {
        return bgColor(counter, (int) bgAlpha.get());
    }

    public int bgColor(int counter, int alpha) {
        return switch (bgColor.get()) {
            case "Dark" -> new Color(21, 21, 21, alpha).getRGB();
            case "Synced" -> ColorUtils.applyOpacity(new Color(color(counter, 255)).darker().darker().getRGB(), alpha / 255f);
            case "Custom" -> ColorUtils.swapAlpha(bgCustomColor.get().getRGB(), alpha);
            case "NeverLose" -> ColorUtils.swapAlpha(NeverLose.bgColor.getRGB(), alpha);
            default -> new Color(0, 0, 0, 0).getRGB();
        };
    }
}