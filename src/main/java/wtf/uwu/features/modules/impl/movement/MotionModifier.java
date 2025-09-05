package wtf.uwu.features.modules.impl.movement;

import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.player.UpdateEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.features.values.impl.ModeValue;
import wtf.uwu.features.values.impl.SliderValue;
import wtf.uwu.utils.math.MathUtils;
import wtf.uwu.utils.math.TimerUtils;
import wtf.uwu.utils.player.MovementUtils;
import wtf.uwu.utils.player.PlayerUtils;
import net.minecraft.potion.Potion;

@ModuleInfo(name = "MotionModifier", category = ModuleCategory.Movement)
public class MotionModifier extends Module {

    private final BoolValue onlyWhileMoving = new BoolValue("Only While Moving", true, this);
    private final BoolValue onlyWhileJumpKeyPressed = new BoolValue("Only While Jump Key Pressed", false, this);
    private final BoolValue onlyWhileSprinting = new BoolValue("Only While Sprinting", false, this);
    private final ModeValue conditionLogic = new ModeValue("Condition Logic", new String[]{"AND", "OR"}, "AND", this);
    private final BoolValue notWhileKB = new BoolValue("Not While KB", false, this);
    private final BoolValue onlyInWater = new BoolValue("Only In Water", false, this);
    private final BoolValue onlyInWeb = new BoolValue("Only In Web", false, this);
    private final BoolValue onlyAtLadder = new BoolValue("Only At Ladder", false, this);
    private final BoolValue onlyInAir = new BoolValue("Only In Air", false, this);
    private final BoolValue onlyNearGround = new BoolValue("Only Near Ground", false, this);
    private final SliderValue nearGroundDistance = new SliderValue("Near Ground Distance", 3.0f, 0.5f, 10.0f, 0.1f, this, onlyNearGround::get);
    private final BoolValue onlyOverVoid = new BoolValue("Only Over Void", false, this);
    private final BoolValue notWhileUsingItem = new BoolValue("Not While Using Item", false, this);
    private final BoolValue notWithSpeedEffect = new BoolValue("Not With Speed Effect", false, this);
    private final BoolValue notWithJumpBoost = new BoolValue("Not With Jump Boost", false, this);
    private final BoolValue useDelay = new BoolValue("Use Delay", false, this);
    private final SliderValue delayTicks = new SliderValue("Delay Ticks", 5, 1, 40, 1, this, useDelay::get);
    private final BoolValue randomizeDelay = new BoolValue("Randomize Delay", false, this, useDelay::get);
    private final SliderValue randomDelayRange = new SliderValue("Random Delay Range", 3, 1, 20, 1, this, () -> useDelay.get() && randomizeDelay.get());
    private final BoolValue smoothMotion = new BoolValue("Smooth Motion", false, this);
    private final SliderValue smoothingFactor = new SliderValue("Smoothing Factor", 0.5f, 0.1f, 1.0f, 0.01f, this, smoothMotion::get);
    private final ModeValue smoothingMode = new ModeValue("Smoothing Mode", new String[]{"Linear", "Exponential", "Gaussian"}, "Linear", this, smoothMotion::get);
    private final BoolValue jump = new BoolValue("Jump", false, this);
    private final BoolValue blockJumpInput = new BoolValue("Block Jump Input", false, this, jump::get);
    private final BoolValue randomizeJump = new BoolValue("Randomize Jump", false, this, jump::get);
    private final SliderValue jumpChance = new SliderValue("Jump Chance", 100, 1, 100, 1, this, () -> jump.get() && randomizeJump.get());
    private final BoolValue editStaticY = new BoolValue("Edit Static Y", false, this);
    private final SliderValue editStaticYAmount = new SliderValue("Static Y Amount", 0.42f, -2f, 5f, 0.01f, this, editStaticY::get);
    private final BoolValue randomizeStaticY = new BoolValue("Randomize Static Y", false, this, editStaticY::get);
    private final SliderValue staticYRandomRange = new SliderValue("Static Y Random Range", 0.1f, 0.01f, 1f, 0.01f, this, () -> editStaticY.get() && randomizeStaticY.get());
    private final BoolValue editStaticXZ = new BoolValue("Edit Static XZ", false, this);
    private final SliderValue editStaticXZAmount = new SliderValue("Static XZ Amount", 0.2f, 0f, 5f, 0.01f, this, editStaticXZ::get);
    private final BoolValue randomizeStaticXZ = new BoolValue("Randomize Static XZ", false, this, editStaticXZ::get);
    private final SliderValue staticXZRandomRange = new SliderValue("Static XZ Random Range", 0.05f, 0.01f, 0.5f, 0.01f, this, () -> editStaticXZ.get() && randomizeStaticXZ.get());
    private final BoolValue editAddY = new BoolValue("Edit Add Y", false, this);
    private final SliderValue editAddYAmount = new SliderValue("Add Y Amount", 0.02f, -1f, 1f, 0.01f, this, editAddY::get);
    private final BoolValue editAddXZ = new BoolValue("Edit Add XZ", false, this);
    private final SliderValue editAddXZAmount = new SliderValue("Add XZ Amount", 0.02f, -1f, 1f, 0.01f, this, editAddXZ::get);
    private final BoolValue editMultiplyY = new BoolValue("Edit Multiply Y", false, this);
    private final SliderValue editMultiplyYAmount = new SliderValue("Multiply Y Amount", 0.98f, 0f, 2f, 0.01f, this, editMultiplyY::get);
    private final BoolValue editMultiplyXZ = new BoolValue("Edit Multiply XZ", false, this);
    private final SliderValue editMultiplyXZAmount = new SliderValue("Multiply XZ Amount", 0.98f, 0f, 2f, 0.01f, this, editMultiplyXZ::get);
    private final BoolValue editLimitY = new BoolValue("Edit Limit Y", false, this);
    private final SliderValue editLimitYAmount = new SliderValue("Limit Y Amount", 0.5f, 0f, 2f, 0.01f, this, editLimitY::get);
    private final BoolValue editLimitXZ = new BoolValue("Edit Limit XZ", false, this);
    private final SliderValue editLimitXZAmount = new SliderValue("Limit XZ Amount", 0.5f, 0f, 2f, 0.01f, this, editLimitXZ::get);
    private final BoolValue editLaunchY = new BoolValue("Edit Launch Y", false, this);
    private final SliderValue editLaunchYAmount = new SliderValue("Launch Y Amount", 0.42f, -1f, 5f, 0.01f, this, editLaunchY::get);
    private final BoolValue editLaunchXZ = new BoolValue("Edit Launch XZ", false, this);
    private final SliderValue editLaunchXZAmount = new SliderValue("Launch XZ Amount", 0.3f, 0f, 5f, 0.01f, this, editLaunchXZ::get);
    private final BoolValue editTimerSpeed = new BoolValue("Edit Timer Speed", false, this);
    private final SliderValue timerSpeedAmount = new SliderValue("Timer Speed Amount", 1f, 0.1f, 4f, 0.01f, this, editTimerSpeed::get);
    private final BoolValue randomizeTimer = new BoolValue("Randomize Timer", false, this, editTimerSpeed::get);
    private final SliderValue timerRandomRange = new SliderValue("Timer Random Range", 0.1f, 0.01f, 1f, 0.01f, this, () -> editTimerSpeed.get() && randomizeTimer.get());
    private final BoolValue strafe = new BoolValue("Strafe", false, this);
    private final BoolValue keepStrafeOnGround = new BoolValue("Keep Strafe On Ground", true, this, strafe::get);

    private final TimerUtils delayTimer = new TimerUtils();
    private double prevMotionX, prevMotionY, prevMotionZ;
    private boolean wasOnGround = false;
    private int activeTicks = 0;

    @Override
    public void onEnable() {
        if (!PlayerUtils.nullCheck()) return;

        delayTimer.reset();
        activeTicks = 0;
        wasOnGround = mc.thePlayer.onGround;
        prevMotionX = mc.thePlayer.motionX;
        prevMotionY = mc.thePlayer.motionY;
        prevMotionZ = mc.thePlayer.motionZ;
    }

    @Override
    public void onDisable() {
        if (editTimerSpeed.get()) {
            mc.timer.timerSpeed = 1.0f;
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!PlayerUtils.nullCheck()) return;

        // Check all conditions
        if (!shouldActivate()) return;

        // Check delay
        if (useDelay.get()) {
            long delayTime = getRandomizedDelay();
            if (!delayTimer.hasTimeElapsed(delayTime)) return;
            delayTimer.reset();
        }

        activeTicks++;
        applyMotionModifications();

        // Store previous values
        prevMotionX = mc.thePlayer.motionX;
        prevMotionY = mc.thePlayer.motionY;
        prevMotionZ = mc.thePlayer.motionZ;
        wasOnGround = mc.thePlayer.onGround;
    }

    private boolean shouldActivate() {
        boolean[] conditions = new boolean[]{
                !onlyWhileMoving.get() || MovementUtils.isMoving(),
                !onlyWhileJumpKeyPressed.get() || mc.gameSettings.keyBindJump.isKeyDown(),
                !onlyWhileSprinting.get() || mc.thePlayer.isSprinting(),
                !notWhileKB.get() || mc.thePlayer.hurtTime == 0,
                !onlyInWater.get() || mc.thePlayer.isInWater(),
                !onlyInWeb.get() || mc.thePlayer.isInWeb,
                !onlyAtLadder.get() || mc.thePlayer.isOnLadder(),
                !onlyInAir.get() || !mc.thePlayer.onGround,
                !onlyNearGround.get() || isNearGround(),
                !onlyOverVoid.get() || PlayerUtils.overVoid(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ),
                !notWhileUsingItem.get() || !mc.thePlayer.isUsingItem(),
                !notWithSpeedEffect.get() || !mc.thePlayer.isPotionActive(Potion.moveSpeed),
                !notWithJumpBoost.get() || !mc.thePlayer.isPotionActive(Potion.jump)
        };

        if (conditionLogic.is("AND")) {
            for (boolean condition : conditions) {
                if (!condition) return false;
            }
            return true;
        } else { // OR logic
            for (boolean condition : conditions) {
                if (condition) return true;
            }
            return false;
        }
    }

    private boolean isNearGround() {
        return PlayerUtils.isBlockUnder(nearGroundDistance.get(), true);
    }

    private long getRandomizedDelay() {
        if (!randomizeDelay.get()) {
            return (long) (delayTicks.get() * 50); // Convert ticks to ms
        }

        int baseDelay = (int) delayTicks.get();
        int randomRange = (int) randomDelayRange.get();
        int randomDelay = MathUtils.nextInt(baseDelay - randomRange/2, baseDelay + randomRange/2);
        return Math.max(1, randomDelay) * 50L;
    }

    private void applyMotionModifications() {
        // Jump modifications
        if (jump.get() && mc.thePlayer.onGround) {
            if (shouldApplyWithChance(jumpChance.get(), randomizeJump.get())) {
                if (!blockJumpInput.get()) {
                    mc.thePlayer.jump();
                }
            }
        }

        // Static motion with smoothing
        if (editStaticY.get()) {
            double staticY = getRandomizedValue(editStaticYAmount.get(), staticYRandomRange.get(), randomizeStaticY.get());
            mc.thePlayer.motionY = applySmoothingY(staticY);
        }

        if (editStaticXZ.get()) {
            double staticXZ = getRandomizedValue(editStaticXZAmount.get(), staticXZRandomRange.get(), randomizeStaticXZ.get());
            MovementUtils.strafe(applySmoothingXZ(staticXZ));
        }

        // Add motion
        if (editAddY.get()) {
            mc.thePlayer.motionY += editAddYAmount.get();
        }

        if (editAddXZ.get()) {
            MovementUtils.moveFlying(editAddXZAmount.get());
        }

        // Multiply motion
        if (editMultiplyY.get()) {
            mc.thePlayer.motionY = applySmoothingY(mc.thePlayer.motionY * editMultiplyYAmount.get());
        }

        if (editMultiplyXZ.get()) {
            double multiplier = editMultiplyXZAmount.get();
            mc.thePlayer.motionX = applySmoothingXZ(mc.thePlayer.motionX * multiplier);
            mc.thePlayer.motionZ = applySmoothingXZ(mc.thePlayer.motionZ * multiplier);
        }

        // Limit motion
        if (editLimitY.get()) {
            double limit = editLimitYAmount.get();
            mc.thePlayer.motionY = MathUtils.interpolate(-limit, limit,
                    MathUtils.normalize((float)mc.thePlayer.motionY, -2f, 2f));
        }

        if (editLimitXZ.get()) {
            double limit = editLimitXZAmount.get();
            mc.thePlayer.motionX = Math.max(-limit, Math.min(limit, mc.thePlayer.motionX));
            mc.thePlayer.motionZ = Math.max(-limit, Math.min(limit, mc.thePlayer.motionZ));
        }

        // Launch motion (only on ground)
        if (mc.thePlayer.onGround) {
            if (editLaunchY.get()) {
                mc.thePlayer.motionY = editLaunchYAmount.get();
            }
            if (editLaunchXZ.get()) {
                MovementUtils.strafe(editLaunchXZAmount.get());
            }
        }

        // Timer modifications
        if (editTimerSpeed.get()) {
            float timerSpeed = getRandomizedTimerSpeed();
            mc.timer.timerSpeed = timerSpeed;
        }

        // Strafe
        if (strafe.get()) {
            if (keepStrafeOnGround.get() || !mc.thePlayer.onGround) {
                MovementUtils.strafe();
            }
        }
    }

    private boolean shouldApplyWithChance(float chance, boolean randomize) {
        if (!randomize) return true;
        return MathUtils.nextFloat(0f, 100f) <= chance;
    }

    private double getRandomizedValue(float baseValue, float randomRange, boolean randomize) {
        if (!randomize) return baseValue;
        return MathUtils.nextDouble(baseValue - randomRange/2, baseValue + randomRange/2);
    }

    private float getRandomizedTimerSpeed() {
        if (!randomizeTimer.get()) return timerSpeedAmount.get();

        float base = timerSpeedAmount.get();
        float range = timerRandomRange.get();
        return MathUtils.nextFloat(base - range/2, base + range/2);
    }

    private double applySmoothingY(double newValue) {
        if (!smoothMotion.get()) return newValue;
        return applySmoothing(prevMotionY, newValue);
    }

    private double applySmoothingXZ(double newValue) {
        if (!smoothMotion.get()) return newValue;
        return applySmoothing(prevMotionX, newValue);
    }

    private double applySmoothing(double oldValue, double newValue) {
        float factor = smoothingFactor.get();

        return switch (smoothingMode.get()) {
            case "Linear" -> MathUtils.interpolate(oldValue, newValue, factor);
            case "Exponential" -> oldValue + (newValue - oldValue) * Math.pow(factor, 2);
            case "Gaussian" -> {
                float gaussian = MathUtils.calculateGaussianValue((float)(newValue - oldValue), factor);
                yield oldValue + (newValue - oldValue) * gaussian;
            }
            default -> newValue;
        };
    }
}