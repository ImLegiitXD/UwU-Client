package wtf.uwu.features.modules.impl.combat;

import org.lwjglx.input.Mouse;
import wtf.uwu.UwU;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.player.UpdateEvent;
import wtf.uwu.events.impl.render.Render2DEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.features.values.impl.ModeValue;
import wtf.uwu.features.values.impl.MultiBoolValue;
import wtf.uwu.features.values.impl.SliderValue;
import wtf.uwu.utils.player.AimbotUtil;
import wtf.uwu.utils.player.PlayerUtils;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemSword;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ModuleInfo(name = "Aimbot", category = ModuleCategory.Legit)
public class Aimbot extends Module {

    // insane bad
    private final ModeValue aimMode = new ModeValue("Aim Mode", new String[]{"Adaptive", "Lock"}, "Adaptive", this);
    private final SliderValue range = new SliderValue("Range", 6.0F, 3.0F, 16.0F, this);
    private final SliderValue fov = new SliderValue("FOV", 90, 1, 360, this);
    private final ModeValue smoothMode = new ModeValue("Smooth Mode", new String[]{"Linear", "Exponential", "Gaussian", "Hyperbolic"}, "Exponential", this);
    private final SliderValue smoothing = new SliderValue("Smoothing", 4.0F, 1.1F, 20.0F, this);
    private final BoolValue interpolation = new BoolValue("Interpolation", true, this);
    private final SliderValue acceleration = new SliderValue("Acceleration", 1.2F, 1.0F, 3.0F, this, () -> aimMode.is("Adaptive"));
    private final ModeValue aimPart = new ModeValue("Aim Part", new String[]{"Head", "Chest", "Legs"}, "Chest", this);
    private final BoolValue prediction = new BoolValue("Prediction", true, this);
    private final SliderValue predictionFactor = new SliderValue("Prediction Factor", 2, 0, 10, this, prediction::get);
    private final BoolValue onClick = new BoolValue("Only On Click", true, this);
    private final BoolValue weaponCheck = new BoolValue("Weapon Check", true, this);
    private final BoolValue disableOnBreak = new BoolValue("Disable on Break", true, this);
    private final BoolValue verticalAim = new BoolValue("Vertical Aim", true, this);
    private final SliderValue verticalSpeed = new SliderValue("Vertical Speed", 1.0F, 0.1F, 1.0F, this, verticalAim::get);
    private final MultiBoolValue targets = new MultiBoolValue("Targets", Arrays.asList(
            new BoolValue("Players", true),
            new BoolValue("Mobs", false),
            new BoolValue("Animals", false),
            new BoolValue("Invisibles", true)
    ), this);
    private final MultiBoolValue filters = new MultiBoolValue("Filters", Arrays.asList(
            new BoolValue("Teams", true),
            new BoolValue("Friends", true),
            new BoolValue("Visibility Check", true)
    ), this);
    private EntityLivingBase target;
    private float lastYaw, lastPitch;
    private float serverYaw, serverPitch;
    private static Field isHittingBlockField;

    static {
        try {
            isHittingBlockField = PlayerControllerMP.class.getDeclaredField("isHittingBlock");
            isHittingBlockField.setAccessible(true);
        } catch (NoSuchFieldException e) {
        }
    }

    @Override
    public void onEnable() {
        this.target = null;
        if (mc.thePlayer != null) {
            this.lastYaw = mc.thePlayer.rotationYaw;
            this.lastPitch = mc.thePlayer.rotationPitch;
            this.serverYaw = mc.thePlayer.rotationYaw;
            this.serverPitch = mc.thePlayer.rotationPitch;
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!shouldRun()) {
            this.target = null;
            return;
        }

        findTarget();

        if (this.target != null) {
            calculateRotations();
            if (!interpolation.get()) {
                mc.thePlayer.rotationYaw = serverYaw;
                mc.thePlayer.rotationPitch = serverPitch;
            }
        }

        this.lastYaw = mc.thePlayer.rotationYaw;
        this.lastPitch = mc.thePlayer.rotationPitch;
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (interpolation.get() && target != null && shouldRun()) {
            final float partialTicks = mc.timer.renderPartialTicks;
            mc.thePlayer.rotationYaw = interpolateRotation(mc.thePlayer.rotationYaw, serverYaw, partialTicks);
            mc.thePlayer.rotationPitch = interpolateRotation(mc.thePlayer.rotationPitch, serverPitch, partialTicks);
        }
    }

    private boolean shouldRun() {
        if (onClick.get() && !Mouse.isButtonDown(0)) return false;
        if (weaponCheck.get() && !isHoldingWeapon()) return false;
        if (disableOnBreak.get() && isBreakingBlock()) return false;
        return true;
    }

    private void findTarget() {
        if (aimMode.is("Lock") && this.target != null && isValidTarget(this.target)) {
            return;
        }

        List<EntityLivingBase> possibleTargets = mc.theWorld.loadedEntityList.stream()
                .filter(e -> e instanceof EntityLivingBase)
                .map(e -> (EntityLivingBase) e)
                .filter(this::isValidTarget)
                .sorted(Comparator.comparingDouble(AimbotUtil::getAngleDifference))
                .collect(Collectors.toList());

        this.target = possibleTargets.isEmpty() ? null : possibleTargets.get(0);
    }

    private void calculateRotations() {
        if (aimMode.is("Adaptive")) {
            float yawChange = Math.abs(mc.thePlayer.rotationYaw - lastYaw);
            float pitchChange = Math.abs(mc.thePlayer.rotationPitch - lastPitch);
            if (yawChange < 0.1f && pitchChange < 0.1f) return;
        }

        float[] targetRotations = AimbotUtil.getRotationsToPosition(getAimPosition());

        if (!verticalAim.get()) {
            targetRotations[1] = mc.thePlayer.rotationPitch;
        } else {
            float pitchDiff = targetRotations[1] - mc.thePlayer.rotationPitch;
            targetRotations[1] = mc.thePlayer.rotationPitch + (pitchDiff * verticalSpeed.get());
        }

        float[] smoothedRotations = getSmoothedRotations(targetRotations);

        this.serverYaw = smoothedRotations[0];
        this.serverPitch = MathHelper.clamp_float(smoothedRotations[1], -90, 90);
    }

    private boolean isTargetTypeAllowed(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer && targets.isEnabled("Players")) return true;
        if (entity instanceof EntityMob && targets.isEnabled("Mobs")) return true;
        if (entity instanceof EntityAnimal && targets.isEnabled("Animals")) return true;
        return false;
    }

    private boolean isValidTarget(EntityLivingBase entity) {
        if (entity == mc.thePlayer || !entity.isEntityAlive() || entity.getHealth() <= 0) return false;
        if (mc.thePlayer.getDistanceToEntity(entity) > range.get()) return false;
        if (!isTargetTypeAllowed(entity)) return false;
        if (entity.isInvisible() && !targets.isEnabled("Invisibles")) return false;
        if (filters.isEnabled("Teams") && PlayerUtils.isInTeam(entity)) return false;
        if (filters.isEnabled("Friends") && entity instanceof EntityPlayer && UwU.INSTANCE.getFriendManager().isFriend((EntityPlayer) entity)) return false;
        if (filters.isEnabled("Visibility Check") && !AimbotUtil.canSeeEntity(entity)) return false;
        if (!AimbotUtil.isInFOV(entity, fov.get())) return false;

        return true;
    }

    private float[] getSmoothedRotations(float[] targetRotations) {
        float currentYaw = interpolation.get() ? this.serverYaw : mc.thePlayer.rotationYaw;
        float currentPitch = interpolation.get() ? this.serverPitch : mc.thePlayer.rotationPitch;

        float yawDiff = MathHelper.wrapAngleTo180_float(targetRotations[0] - currentYaw);
        float pitchDiff = targetRotations[1] - currentPitch;
        float smooth = smoothing.get();
        float accel = aimMode.is("Adaptive") ? acceleration.get() : 1.0f;

        float finalYaw = currentYaw;
        float finalPitch = currentPitch;

        switch (smoothMode.get()) {
            case "Linear":
                finalYaw = currentYaw + (yawDiff / smooth) * accel;
                finalPitch = currentPitch + (pitchDiff / smooth) * accel;
                break;
            case "Exponential":
                finalYaw += (yawDiff * (1 / smooth)) * accel;
                finalPitch += (pitchDiff * (1 / smooth)) * accel;
                break;
            case "Gaussian":
                double yawDist = Math.abs(yawDiff);
                double pitchDist = Math.abs(pitchDiff);
                float yawFactor = (float) (Math.exp(-Math.pow(yawDist, 2) / (2 * Math.pow(smooth, 2))));
                float pitchFactor = (float) (Math.exp(-Math.pow(pitchDist, 2) / (2 * Math.pow(smooth, 2))));
                finalYaw += yawDiff * (1 - yawFactor) * accel;
                finalPitch += pitchDiff * (1 - pitchFactor) * accel;
                break;
            case "Hyperbolic":
                finalYaw += (yawDiff / (1 + (Math.abs(yawDiff) / (180 / smooth)))) * accel;
                finalPitch += (pitchDiff / (1 + (Math.abs(pitchDiff) / (90 / smooth)))) * accel;
                break;
        }

        return new float[]{finalYaw, finalPitch};
    }

    private Vec3 getAimPosition() {
        Vec3 basePos;
        if (prediction.get()) {
            basePos = AimbotUtil.getPredictedPosition(this.target, predictionFactor.get());
        } else {
            basePos = this.target.getPositionVector();
        }

        if (aimPart.is("Head")) return basePos.addVector(0, this.target.getEyeHeight(), 0);
        if (aimPart.is("Chest")) return basePos.addVector(0, this.target.height * 0.5, 0);
        if (aimPart.is("Legs")) return basePos.addVector(0, this.target.height * 0.2, 0);

        return basePos.addVector(0, this.target.getEyeHeight(), 0);
    }

    private boolean isHoldingWeapon() {
        if (mc.thePlayer.getCurrentEquippedItem() == null) return false;
        return mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemSword ||
                mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemAxe;
    }

    private boolean isBreakingBlock() {
        if (isHittingBlockField == null) return false;

        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            try {
                return (boolean) isHittingBlockField.get(mc.playerController);
            } catch (IllegalAccessException e) {
            }
        }
        return false;
    }

    private float interpolateRotation(float prev, float cur, float partialTicks) {
        float f = MathHelper.wrapAngleTo180_float(cur - prev);
        return prev + f * partialTicks;
    }
}