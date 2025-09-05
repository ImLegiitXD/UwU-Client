
package wtf.uwu.features.modules.impl.combat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemStack;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.player.UpdateEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.modules.impl.movement.Freeze;
import wtf.uwu.features.modules.impl.movement.Scaffold;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.features.values.impl.ModeValue;
import wtf.uwu.features.values.impl.SliderValue;
import wtf.uwu.utils.math.TimerUtils;
import wtf.uwu.utils.misc.SpoofSlotUtils;
import wtf.uwu.utils.player.MovementCorrection;
import wtf.uwu.utils.player.PlayerUtils;
import wtf.uwu.utils.player.RotationUtils;

import java.util.Objects;

import static net.minecraft.init.Items.egg;
import static net.minecraft.init.Items.snowball;

@ModuleInfo(name = "AutoProjectile", category = ModuleCategory.Combat)
public class AutoProjectile extends Module {
    private final ModeValue mode = new ModeValue("Mode", new String[]{"Silent", "Always"}, "Silent", this);
    private final SliderValue fov = new SliderValue("FOV",180,1,180,this);
    private final SliderValue range = new SliderValue("Range", 8F, 1F, 20F, 1, this);
    private final SliderValue delay = new SliderValue("Delay", 100, 0, 2000, 25, this);
    private final SliderValue switchBackDelay = new SliderValue("Switch Back Delay", 500, 50, 2000, 25, this);
    private final SliderValue predictSize = new SliderValue("Predict Size", 2, 0.1f, 5, 0.1f, this);
    private final BoolValue customRotationSetting = new BoolValue("Custom Rotation Setting", false, this);
    private final ModeValue smoothMode = new ModeValue("Rotations Smooth", RotationUtils.smoothModes, RotationUtils.smoothModes[0], this, customRotationSetting::get);
    private final SliderValue minYawRotSpeed = new SliderValue("Min Yaw Rotation Speed", 45, 1,180,1, this, customRotationSetting::get);
    private final SliderValue minPitchRotSpeed = new SliderValue("Min Pitch Rotation Speed", 45, 1,180,1, this, customRotationSetting::get);
    private final SliderValue maxYawRotSpeed = new SliderValue("Max Yaw Rotation Speed", 90, 1,180,1, this, customRotationSetting::get);
    private final SliderValue maxPitchRotSpeed = new SliderValue("Max Pitch Rotation Speed", 90, 1,180,1, this, customRotationSetting::get);
    private final SliderValue bezierP0 = new SliderValue("Bezier P0", 0f, 0f, 1f,1, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtils.smoothModes[1]) || smoothMode.is(RotationUtils.smoothModes[8])));
    private final SliderValue bezierP1 = new SliderValue("Bezier P1", 0.05f, 0f, 1f,1, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtils.smoothModes[1]) || smoothMode.is(RotationUtils.smoothModes[8])));
    private final SliderValue bezierP2 = new SliderValue("Bezier P2", 0.2f, 0f, 1f,1, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtils.smoothModes[1]) || smoothMode.is(RotationUtils.smoothModes[8])));
    private final SliderValue bezierP3 = new SliderValue("Bezier P3", 0.4f, 0f, 1f,1, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtils.smoothModes[1]) || smoothMode.is(RotationUtils.smoothModes[8])));
    private final SliderValue bezierP4 = new SliderValue("Bezier P4", 0.6f, 0f, 1f,1, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtils.smoothModes[1]) || smoothMode.is(RotationUtils.smoothModes[8])));
    private final SliderValue bezierP5 = new SliderValue("Bezier P5", 0.8f, 0f, 1f,1, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtils.smoothModes[1]) || smoothMode.is(RotationUtils.smoothModes[8])));
    private final SliderValue bezierP6 = new SliderValue("Bezier P6", 0.95f, 0f, 1f,1, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtils.smoothModes[1]) || smoothMode.is(RotationUtils.smoothModes[8])));
    private final SliderValue bezierP7 = new SliderValue("Bezier P7", 0.1f, 0f, 1f,1, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtils.smoothModes[1]) || smoothMode.is(RotationUtils.smoothModes[8])));
    private final SliderValue elasticity = new SliderValue("Elasticity", 0.3f, 0.1f, 1f,0.01f, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && smoothMode.is(RotationUtils.smoothModes[7]));
    private final SliderValue dampingFactor = new SliderValue("Damping Factor", 0.5f, 0.1f, 1f,0.01f, this,() -> customRotationSetting.canDisplay() && customRotationSetting.get() && smoothMode.is(RotationUtils.smoothModes[7]));
    public final BoolValue smoothlyResetRotation = new BoolValue("Smoothly Reset Rotation", true, this, customRotationSetting::get);
    private final BoolValue moveFix = new BoolValue("Move Fix", true, this);
    public final ModeValue moveFixMode = new ModeValue("Move Fix Mode", new String[]{"Silent", "Strict"}, "Silent", this);
    private final TimerUtils projectilePullTimer = new TimerUtils();
    private final TimerUtils delayTimer = new TimerUtils();
    private boolean projectileInUse;
    private int switchBack;
    private EntityPlayer target;
    private boolean wasThrowing;

    @Override
    public void onDisable() {
        SpoofSlotUtils.stopSpoofing();
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        boolean usingProjectile = (mc.thePlayer.isUsingItem() && (mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() instanceof ItemSnowball || mc.thePlayer.getHeldItem().getItem() instanceof ItemEgg))) || this.projectileInUse;

        target = PlayerUtils.getTarget(range.get());

        if (getModule(Scaffold.class).isEnabled() || getModule(KillAura.class).target != null || isEnabled(Freeze.class) || !mc.thePlayer.canEntityBeSeen(target) || mc.thePlayer.isUsingItem()) {
            return;
        }

        if (target != null && (RotationUtils.getRotationDifference(target) <= fov.get() || fov.get() == 180)) {
            if (mode.is("Always") && findProjectile() != -1) {
                rotate();
            }

            if (usingProjectile) {
                if (this.projectilePullTimer.hasTimeElapsed((long) this.switchBackDelay.get()) || switchBackDelay.get() == 0) {
                    if (this.switchBack != -1 && mc.thePlayer.inventory.currentItem != this.switchBack) {
                        mc.thePlayer.inventory.currentItem = this.switchBack;
                        mc.playerController.updateController();
                    } else {
                        mc.thePlayer.stopUsingItem();
                    }
                    SpoofSlotUtils.stopSpoofing();

                    this.switchBack = -1;
                    this.projectileInUse = false;
                }
            } else {

                if (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemSnowball || mc.thePlayer.getHeldItem().getItem() instanceof ItemEgg)) {
                    int projectile = this.findProjectile();

                    if (projectile == -1) {
                        return;
                    }

                    this.switchBack = mc.thePlayer.inventory.currentItem;
                    SpoofSlotUtils.startSpoofing(switchBack);
                    mc.thePlayer.inventory.currentItem = projectile - 36;
                    mc.playerController.updateController();
                }

                this.throwProjectile();
                wasThrowing = true;

            }
        }

        if ((this.projectilePullTimer.hasTimeElapsed((long) this.switchBackDelay.get()) || switchBackDelay.get() == 0) && wasThrowing) {
            if (this.switchBack != -1 && mc.thePlayer.inventory.currentItem != this.switchBack) {
                mc.thePlayer.inventory.currentItem = this.switchBack;
                mc.playerController.updateController();
            } else {
                mc.thePlayer.stopUsingItem();
            }
            SpoofSlotUtils.stopSpoofing();

            this.switchBack = -1;
            this.projectileInUse = false;
            wasThrowing = false;
        }
    }

    private int findProjectile() {
        for (int i = 36; i < 45; i++) {
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack != null) {
                if (stack.getItem() == snowball || stack.getItem() == egg) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void throwProjectile() {
        int projectile = findProjectile();

        if (mode.is("Silent")) {
            rotate();
        }

        if (delayTimer.hasTimeElapsed((long) delay.get())) {
            mc.thePlayer.inventory.currentItem = projectile - 36;

            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventoryContainer.getSlot(projectile).getStack());

            projectileInUse = true;
            projectilePullTimer.reset();
            delayTimer.reset();
        }
    }
    
    private void rotate() {

        float[] finalRotation = RotationUtils.faceTrajectory(target, true, predictSize.get(), 0.03f, 0.5f);

        if (customRotationSetting.get()) {
            RotationUtils.setRotation(finalRotation,smoothMode.get(), moveFix.get() ? (Objects.equals(moveFixMode.get(), "Silent") ? MovementCorrection.SILENT : MovementCorrection.STRICT) : MovementCorrection.OFF, minYawRotSpeed.get(), maxYawRotSpeed.get(), minPitchRotSpeed.get(), maxPitchRotSpeed.get(),
                    bezierP0.get(),
                    bezierP1.get(),
                    bezierP2.get(),
                    bezierP3.get(),
                    bezierP4.get(),
                    bezierP5.get(),
                    bezierP6.get(),
                    bezierP7.get(),
                    elasticity.get(),
                    dampingFactor.get(),
                    smoothlyResetRotation.get());
        } else {
            RotationUtils.setRotation(finalRotation, moveFix.get() ? (Objects.equals(moveFixMode.get(), "Silent") ? MovementCorrection.SILENT : MovementCorrection.STRICT) : MovementCorrection.OFF);
        }
    }
}
