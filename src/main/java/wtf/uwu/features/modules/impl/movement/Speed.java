package wtf.uwu.features.modules.impl.movement;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import org.lwjglx.input.Keyboard;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.packet.PacketEvent;
import wtf.uwu.events.impl.player.*;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.features.values.impl.ModeValue;
import wtf.uwu.features.values.impl.SliderValue;
import wtf.uwu.utils.misc.DebugUtils;
import wtf.uwu.utils.player.MovementUtils;
import wtf.uwu.utils.player.RotationUtils;

import java.lang.reflect.Field;
import java.util.Objects;

@ModuleInfo(name = "Speed", category = ModuleCategory.Movement, key = Keyboard.KEY_V)
public class Speed extends Module {
    private final ModeValue mode = new ModeValue("Mode", new String[]{"Vanilla", "EntityCollide", "BlocksMC", "Polar", "NCP", "VulcanNew", "Verus"}, "NCP", this);

    private final ModeValue polarMode = new ModeValue(
            "Polar Mode",
            new String[]{"Normal", "Lowhop"},
            "Normal",
            this,
            () -> mode.is("Polar")
    );

    private final ModeValue blocksMCMode = new ModeValue(
            "BlocksMC Mode",
            new String[]{"Ground", "Full", "Directional"},
            "Ground",
            this,
            () -> mode.is("BlocksMC")
    );
    private final SliderValue blocksMCSpeed = new SliderValue(
            "BlocksMC Speed",
            1, 0, 4, 0.1F,
            this,
            () -> mode.is("BlocksMC") && blocksMCMode.is("Ground")
    );
    private final SliderValue blocksMCOffset = new SliderValue(
            "BlocksMC Offset",
            0, -0.5F, 0.5F, 0.05F,
            this,
            () -> mode.is("BlocksMC") && blocksMCMode.is("Ground")
    );

    private final ModeValue verusMode = new ModeValue(
            "Verus Mode",
            new String[]{"Hop", "Float", "Ground", "YPort"},
            "Hop",
            this,
            () -> mode.is("Verus")
    );
    private final SliderValue yPortSpeed = new SliderValue(
            "YPort Speed",
            0.61f, 0.1f, 1f, 0.01f,
            this,
            () -> mode.is("Verus") && verusMode.is("YPort")
    );

    private final BoolValue expand = new BoolValue("More Expand", false, this, () -> Objects.equals(mode.get(), "EntityCollide"));
    private final BoolValue ignoreDamage = new BoolValue("Ignore Damage", true, this, () -> Objects.equals(mode.get(), "EntityCollide"));
    private final BoolValue pullDown = new BoolValue("Pull Down", true, this, () -> Objects.equals(mode.get(), "NCP"));
    private final SliderValue onTick = new SliderValue("On Tick", 5, 1, 10, 1, this, () -> Objects.equals(mode.get(), "NCP") && pullDown.get());
    private final BoolValue onHurt = new BoolValue("On Hurt", true, this, () -> Objects.equals(mode.get(), "NCP") && pullDown.get());
    private final BoolValue airBoost = new BoolValue("Air Boost", true, this, () -> Objects.equals(mode.get(), "NCP"));
    private final BoolValue damageBoost = new BoolValue("Damage Boost", false, this, () -> Objects.equals(mode.get(), "NCP"));
    private final SliderValue vanilla = new SliderValue("Vanilla Speed", 0.5f,0.05f,2, 0.05f,this, () -> Objects.equals(mode.get(), "Vanilla"));
    private final BoolValue vanillaPullDown = new BoolValue("Pull Down", true, this, () -> mode.is("Vanilla"));
    private final SliderValue vanillaPullDownAmount = new SliderValue("Vanilla Pull Down", 0.5f,0.05f,2, 0.05f,this, () -> Objects.equals(mode.get(), "Vanilla") && vanillaPullDown.get());
    public final BoolValue noBob = new BoolValue("No Bob", true, this);
    private final BoolValue forceStop = new BoolValue("Force Stop", true, this);
    private final BoolValue lagBackCheck = new BoolValue("Lag Back Check", true, this);
    private final BoolValue liquidCheck = new BoolValue("Liquid Check", true, this);
    private final BoolValue guiCheck = new BoolValue("Gui Check", true, this);
    private final BoolValue printOffGroundTicks = new BoolValue("Print Off Ground Ticks", true, this);

    private boolean stopVelocity;
    public boolean couldStrafe;
    private int ticksSinceTeleport;

    private boolean firstHop = false;
    private int verusTicks = 0;
    private boolean verusBaipas = false;
    private boolean verusIsInAir = false;

    private Field pressedField;

    @Override
    public void onEnable() {
        if (mode.is("Verus")) {
            verusBaipas = false;
            firstHop = false;
            verusTicks = 0;
            verusIsInAir = false;
        }

        try {
            pressedField = mc.gameSettings.keyBindJump.getClass().getDeclaredField("pressed");
            pressedField.setAccessible(true);
        } catch (Exception e) {
            try {
                pressedField = mc.gameSettings.keyBindJump.getClass().getDeclaredField("field_74513_e");
                pressedField.setAccessible(true);
            } catch (Exception ex) {
                pressedField = null;
            }
        }
    }

    @Override
    public void onDisable() {
        couldStrafe = false;
        if(forceStop.get()){
            MovementUtils.stopXZ();
        }
        if (mc.timer != null) {
            mc.timer.timerSpeed = 1.0f;
        }
    }

    private void setKeyPressed(boolean pressed) {
        if (pressedField != null) {
            try {
                pressedField.setBoolean(mc.gameSettings.keyBindJump, pressed);
            } catch (Exception e) {
            }
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        String currentModeName = mode.get();
        String subModeDisplay = "";
        if (currentModeName.equals("Polar")) {
            subModeDisplay = " " + polarMode.get();
        } else if (currentModeName.equals("BlocksMC")) {
            subModeDisplay = " " + blocksMCMode.get();
        } else if (currentModeName.equals("Verus")) {
            subModeDisplay = " " + verusMode.get();
        }
        setTag(currentModeName + subModeDisplay);

        ticksSinceTeleport++;

        if(liquidCheck.get() && (mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) ||
                guiCheck.get() && mc.currentScreen instanceof GuiContainer ||
                (isEnabled(Scaffold.class) && getModule(Scaffold.class).isEnabled())) {
            return;
        }

        if (printOffGroundTicks.get()) {
            DebugUtils.sendMessage(mc.thePlayer.offGroundTicks + " Tick");
        }

        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            if (mode.is("BlocksMC") || mode.is("VulcanNew") || mode.is("Polar") || mode.is("NCP") || mode.is("Vanilla")) {
                mc.thePlayer.jump();
            }
        }

        switch (currentModeName) {
            case "NCP": {
                if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
                    MovementUtils.strafe(0.48 + MovementUtils.getSpeedEffect() * 0.07);
                    couldStrafe = true;
                }

                if (mc.thePlayer.offGroundTicks == onTick.get() && pullDown.get()) {
                    MovementUtils.strafe();
                    mc.thePlayer.motionY -= 0.1523351824467155;
                    couldStrafe = true;
                }

                if (onHurt.get() && mc.thePlayer.hurtTime >= 5 && mc.thePlayer.motionY >= 0) {
                    mc.thePlayer.motionY -= 0.1;
                }

                if (airBoost.get() && MovementUtils.isMoving() && !mc.thePlayer.onGround) {
                    mc.thePlayer.motionX *= 1f + 0.00718;
                    mc.thePlayer.motionZ *= 1f + 0.00718;
                }
                if (damageBoost.get() && mc.thePlayer.hurtTime > 0) {
                    MovementUtils.strafe(Math.max(MovementUtils.getSpeed(), 0.5));
                    couldStrafe = true;
                }
            }
            break;

            case "Vanilla": {
                MovementUtils.strafe(vanilla.get());
                couldStrafe = true;

                if (vanillaPullDown.get() && !mc.thePlayer.onGround) {
                    mc.thePlayer.motionY = -vanillaPullDownAmount.get();
                }
            }
            break;

            case "Polar": {
                if (polarMode.is("Normal")) {
                    if (mc.thePlayer.motionY > 0.03 && mc.thePlayer.isSprinting() && !mc.thePlayer.onGround) {
                        mc.thePlayer.motionX *= 1f + 0.003;
                        mc.thePlayer.motionZ *= 1f + 0.003;
                    }
                } else if (polarMode.is("Lowhop")) {
                    if (!mc.thePlayer.onGround && mc.thePlayer.motionY < 0.38 && mc.thePlayer.motionY > 0.1 && mc.thePlayer.isSprinting()) {
                        mc.thePlayer.motionX *= 1;
                        mc.thePlayer.motionZ *= 1;
                    }
                    if (mc.thePlayer.motionY < 0.1 && mc.thePlayer.motionY > -0.1 && !mc.thePlayer.onGround) {
                        mc.thePlayer.motionY -= 0.02;
                    }
                }
            }
            break;

            case "EntityCollide": {
                couldStrafe = false;
                if (mc.thePlayer.hurtTime <= 1) {
                    stopVelocity = false;
                }
                if (stopVelocity && !ignoreDamage.get()) {
                    return;
                }
                if (!MovementUtils.isMoving())
                    return;

                int collisions = 0;
                AxisAlignedBB box = expand.get() ? mc.thePlayer.getEntityBoundingBox().expand(1.0, 1.0, 1.0)
                        : mc.thePlayer.getEntityBoundingBox().expand(0.8, 0.8, 0.8);
                for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                    if (canCauseSpeed(entity) && box.intersectsWith(entity.getEntityBoundingBox())) {
                        collisions++;
                    }
                }
                double yaw = Math.toRadians(RotationUtils.shouldRotate() ? RotationUtils.currentRotation[0] : mc.thePlayer.rotationYaw);
                double boost = 0.078 * collisions;
                mc.thePlayer.addVelocity(-Math.sin(yaw) * boost, 0.0, Math.cos(yaw) * boost);
            }
            break;

            case "BlocksMC": {
                if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
                    if (blocksMCMode.is("Ground")) {
                        if (!mc.thePlayer.isUsingItem()) {
                            MovementUtils.strafe(mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 0.55f : (float) (0.4 * blocksMCSpeed.get() + blocksMCOffset.get()));
                            couldStrafe = true;
                        }
                    }
                }

                if (blocksMCMode.is("Full") && MovementUtils.isMoving()) {
                    MovementUtils.strafe();
                    couldStrafe = true;
                }

                switch (mc.thePlayer.offGroundTicks) {
                    case 1:
                        if (blocksMCMode.is("Directional") && MovementUtils.isMoving()) {
                            MovementUtils.strafe();
                            couldStrafe = true;
                        }
                        break;
                    case 4:
                        if (mc.thePlayer.hurtTime == 0) {
                            mc.thePlayer.motionY = -0.09800000190734863;
                        }
                        break;
                }
            }
            break;

            case "VulcanNew": {
                if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
                    mc.thePlayer.motionX *= 1.05F;
                    mc.thePlayer.motionZ *= 1.05F;
                }

                if (mc.thePlayer.offGroundTicks == 1 && MovementUtils.isMoving()) {
                    mc.thePlayer.motionX *= 0.97F;
                    mc.thePlayer.motionZ *= 0.97F;
                }
            }
            break;

            case "Verus": {
                switch (verusMode.get()) {
                    case "Hop": {
                        if (MovementUtils.isMoving()) {
                            setKeyPressed(false);
                            if (mc.thePlayer.onGround) {
                                mc.thePlayer.jump();
                                MovementUtils.strafe(0.48f);
                                couldStrafe = true;
                            }
                            MovementUtils.strafe();
                            couldStrafe = true;
                        }
                    }
                    break;
                }
            }
            break;
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mode.is("Verus")) {
            switch (verusMode.get()) {
                case "Ground": {
                    if (mc.thePlayer.onGround) {
                        if (mc.thePlayer.ticksExisted % 12 == 0) {
                            firstHop = false;
                            MovementUtils.strafe(0.69f);
                            mc.thePlayer.jump();
                            mc.thePlayer.motionY = 0.0;
                            MovementUtils.strafe(0.69f);
                            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ, false));
                            MovementUtils.strafe(0.41f);
                            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                            couldStrafe = true;
                        } else if (!firstHop) {
                            MovementUtils.strafe(1.01f);
                            couldStrafe = true;
                        }
                    }
                }
                break;

                case "Float": {
                    verusTicks++;
                    if (!mc.gameSettings.keyBindJump.isKeyDown()) {
                        if (mc.thePlayer.onGround) {
                            verusTicks = 0;
                            MovementUtils.strafe(0.44f);
                            mc.thePlayer.motionY = 0.42;
                            mc.timer.timerSpeed = 2.1f;
                            verusIsInAir = true;
                            couldStrafe = true;
                        } else if (verusIsInAir) {
                            if (verusTicks >= 10) {
                                verusBaipas = true;
                                MovementUtils.strafe(0.2865f);
                                verusIsInAir = false;
                                couldStrafe = true;
                            }

                            if (verusBaipas) {
                                if (verusTicks <= 1) {
                                    MovementUtils.strafe(0.45f);
                                    couldStrafe = true;
                                }

                                if (verusTicks >= 2) {
                                    MovementUtils.strafe(0.69f - (verusTicks - 2) * 0.019f);
                                    couldStrafe = true;
                                }
                            }

                            mc.thePlayer.motionY = 0.0;
                            mc.timer.timerSpeed = 0.9f;
                            mc.thePlayer.onGround = true;
                        }
                    }
                }
                break;
            }
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if(liquidCheck.get() && (mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) || guiCheck.get() && mc.currentScreen instanceof GuiContainer)
            return;
    }

    @EventTarget
    public void onPostStrafe(PostStrafeEvent event) {
    }

    @EventTarget
    public void onMove(MoveEvent event){
        if (mode.is("Verus") && verusMode.is("YPort")) {
            if (MovementUtils.isMoving()) {
                setKeyPressed(false);
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                    mc.thePlayer.motionY = 0.0;
                    MovementUtils.strafe(yPortSpeed.get());
                    event.y = 0.41999998688698;
                    couldStrafe = true;
                } else {
                    MovementUtils.strafe();
                    couldStrafe = true;
                }
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            ticksSinceTeleport = 0;
            if (lagBackCheck.get()) {
                toggle();
            }
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event){
        if(!mode.is("EntityCollide")) {
            if (mc.thePlayer.onGround && (mode.is("NCP") || mode.is("BlocksMC") || mode.is("Polar") || mode.is("Vanilla") || mode.is("VulcanNew"))) {
                if (MovementUtils.isMoving()) {
                    event.setJumping(false);
                }
            }
            if (mode.is("Verus")) {
                if (MovementUtils.isMoving()) {
                    event.setJumping(false);
                }
            }
        }
    }

    private boolean canCauseSpeed(Entity entity) {
        return entity != mc.thePlayer && entity instanceof EntityLivingBase && !(entity instanceof EntityArmorStand);
    }
}