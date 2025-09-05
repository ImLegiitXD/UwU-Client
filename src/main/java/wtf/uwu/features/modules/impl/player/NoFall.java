package wtf.uwu.features.modules.impl.player;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.client.C03PacketPlayer;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.player.MotionEvent;
import wtf.uwu.events.impl.render.Render2DEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.modules.impl.movement.Scaffold;
import wtf.uwu.features.values.impl.ModeValue;
import wtf.uwu.features.values.impl.SliderValue;
import wtf.uwu.utils.packet.BlinkComponent;
import wtf.uwu.utils.player.MovementUtils;
import wtf.uwu.utils.player.PlayerUtils;

@ModuleInfo(name = "NoFall", category = ModuleCategory.Player)
public class NoFall extends Module {

    public final ModeValue mode = new ModeValue("Mode", new String[]{"NoGround", "Blink", "Extra", "Verus"}, "NoGround", this);
    public final SliderValue minDistance = new SliderValue("Min Distance", 3, 0, 8, 1, this, () -> !mode.is("NoGround"));
    private boolean blinked = false;
    private boolean prevOnGround = false;
    private double fallDistance = 0;
    private boolean timed = false;
    private boolean spoof = false;
    private boolean timer = false;

    @Override
    public void onEnable() {
        if (PlayerUtils.nullCheck())
            this.fallDistance = mc.thePlayer.fallDistance;
    }

    @Override
    public void onDisable() {
        if (blinked) {
            BlinkComponent.dispatch();
            blinked = false;
        }
        mc.timer.timerSpeed = 1f;

        spoof = false;
        timer = false;
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        setTag(mode.get());
        if (!PlayerUtils.nullCheck())
            return;

        if (event.isPost())
            return;

        if (mc.thePlayer.onGround)
            fallDistance = 0;
        else {
            fallDistance += (float) Math.max(mc.thePlayer.lastTickPosY - event.getY(), 0);

            fallDistance -= MovementUtils.predictedMotionY(mc.thePlayer.motionY, 1);
        }

        if (mc.thePlayer.capabilities.allowFlying) return;
        if (isVoid()) {
            if (blinked) {
                BlinkComponent.dispatch();
                blinked = false;
            }
            return;
        }

        switch (mode.get()) {
            case "NoGround":
                event.setOnGround(false);
                break;

            case "Extra":
                if (fallDistance >= minDistance.get() && !isEnabled(Scaffold.class)) {
                    mc.timer.timerSpeed = (float) 0.5;
                    timed = true;
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                    fallDistance = 0;
                } else if (timed) {
                    mc.timer.timerSpeed = 1;
                    timed = false;
                }
                break;

            case "Verus":
                if (mc.thePlayer.lastTickPosY - mc.thePlayer.posY > 0.0D) {
                    fallDistance += 0.4D;
                }
                if (mc.thePlayer.onGround) {
                    fallDistance = 0.0F;
                }

                if (mc.thePlayer.fallDistance > 3.5D && !isVoid()) {
                    spoof = true;
                    mc.thePlayer.fallDistance = 0.0F;
                    mc.timer.timerSpeed = 0.5F;
                    timer = true;
                } else if (timer) {
                    mc.timer.timerSpeed = 1.0F;
                    timer = false;
                }
                break;

            case "Blink":
                if (mc.thePlayer.onGround) {
                    if (blinked) {
                        BlinkComponent.dispatch();
                        blinked = false;
                    }

                    this.prevOnGround = true;
                } else if (this.prevOnGround) {
                    if (shouldBlink()) {
                        if (!BlinkComponent.blinking)
                            BlinkComponent.blinking = true;
                        blinked = true;
                    }

                    prevOnGround = false;
                } else if (PlayerUtils.isBlockUnder() && BlinkComponent.blinking && (this.fallDistance - mc.thePlayer.motionY) >= minDistance.get()) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
                    this.fallDistance = 0.0F;
                }
                break;
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);
        if (mode.is("Blink")) {
            if (blinked)
                mc.fontRendererObj.drawStringWithShadow("Blinking: " + BlinkComponent.packets.size(), (float) sr.getScaledWidth() / 2.0F - (float) mc.fontRendererObj.getStringWidth("Blinking: " + BlinkComponent.packets.size()) / 2.0F, (float) sr.getScaledHeight() / 2.0F + 13.0F, -1);
        } else if (mode.is("Verus")) {
            if (spoof || timer) {
                String status = "Verus: " + (timer ? "Timer Active" : "Spoofing");
                mc.fontRendererObj.drawStringWithShadow(status, (float) sr.getScaledWidth() / 2.0F - (float) mc.fontRendererObj.getStringWidth(status) / 2.0F, (float) sr.getScaledHeight() / 2.0F + 13.0F, -1);
            }
        }
    }

    private boolean isVoid() {
        return PlayerUtils.overVoid(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }

    private boolean shouldBlink() {
        return !mc.thePlayer.onGround && !PlayerUtils.isBlockUnder((int) Math.floor(minDistance.get())) && PlayerUtils.isBlockUnder() && !getModule(Scaffold.class).isEnabled();
    }
}