package wtf.uwu.features.modules.impl.combat;

import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.viamcp.fixes.AttackOrder;
import net.minecraft.block.BlockAir;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.*;
import net.minecraft.util.*;
import org.apache.commons.lang3.RandomUtils;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import wtf.uwu.UwU;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.player.StrafeEvent;
import wtf.uwu.events.impl.player.UpdateEvent;
import wtf.uwu.events.impl.render.Render2DEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.modules.impl.visual.Interface;
import wtf.uwu.features.modules.impl.player.BedNuker;
import wtf.uwu.features.modules.impl.movement.Scaffold;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.features.values.impl.ModeValue;
import wtf.uwu.features.values.impl.MultiBoolValue;
import wtf.uwu.features.values.impl.SliderValue;
import wtf.uwu.utils.animations.Direction;
import wtf.uwu.utils.animations.impl.DecelerateAnimation;
import wtf.uwu.utils.math.MathUtils;
import wtf.uwu.utils.math.TimerUtils;
import wtf.uwu.utils.packet.BlinkComponent;
import wtf.uwu.utils.player.*;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@ModuleInfo(name = "KillAura", category = ModuleCategory.Combat, key = Keyboard.KEY_R)
public class KillAura extends Module {

    //region target settings
    private final ModeValue mode = new ModeValue("Mode", new String[]{"Switch", "Single", "UltimateSwitch"}, "Switch", this);
    public final SliderValue switchDelayValue = new SliderValue("SwitchDelay", 15, 0, 20, this, () -> mode.is("Switch"));
    private final ModeValue priority = new ModeValue("Priority", new String[]{"Range", "Armor", "Health", "HurtTime", "FOV", "Ultimate", "Aim to dick"}, "Health", this);
    public final SliderValue searchRange = new SliderValue("Search Range", 6.0F, 2.0F, 16F, .1f, this);
    public final SliderValue rotationRange = new SliderValue("Rotation Range", 6.0F, 2.0F, 16F, .1f, this);
    private final SliderValue fov = new SliderValue("FOV", 180, 1, 180, this);
    private final MultiBoolValue targetOption = new MultiBoolValue("Targets", Arrays.asList(new BoolValue("Players", true), new BoolValue("Mobs", false),
            new BoolValue("Animals", false), new BoolValue("Invisible", true), new BoolValue("Dead", false),
            new BoolValue("Villagers", false), new BoolValue("Armor Stands", false)), this);
    public final MultiBoolValue filter = new MultiBoolValue("Filter", Arrays.asList(new BoolValue("Teams", true), new BoolValue("Friends", true)), this);
    //endregion

    //region combat settings
    private final SliderValue minAps = new SliderValue("Min Aps", 9, 1, 30, this);
    private final SliderValue maxAps = new SliderValue("Max Aps", 11, 1, 30, this);
    private final ModeValue apsMode = new ModeValue("Aps Mode", new String[]{"Random", "Secure Random", "Full Random", "Dynamic"}, "Random", this);
    public final SliderValue attackRange = new SliderValue("Attack Range", 3.0F, 2.0F, 6F, .1f, this);
    public final SliderValue wallAttackRange = new SliderValue("Wall Attack Range", 0.0F, 0.0F, 6F, .1f, this);
    public final BoolValue packetAttack = new BoolValue("Attack", false, this);
    public final BoolValue noSwing = new BoolValue("No Swing", false, this, packetAttack::get);
    public final ModeValue rangeMode = new ModeValue("Range Mode", new String[]{"Client", "Client 2", "Client 3", "Server", "Predictive"}, "Client", this);
    public final BoolValue predictiveRange = new BoolValue("Predictive Range", false, this);
    public final SliderValue predictionFactor = new SliderValue("Prediction Factor", 1.5f, 1.0f, 3.0f, 0.1f, this, predictiveRange::get);
    public final BoolValue preHit = new BoolValue("Pre Hit", false, this);
    public final SliderValue preHitRange = new SliderValue("Pre Hit Range", 4.0F, 2.0F, 6.0F, .1f, this, preHit::get);
    public final MultiBoolValue addons = new MultiBoolValue("Addons", Arrays.asList(new BoolValue("Movement Fix", false), new BoolValue("Perfect Hit", false), new BoolValue("Ray Cast", true), new BoolValue("Hit Select", false)), this);
    public final SliderValue hitSelectRange = new SliderValue("Hit Select Range", 3.0F, 2.0F, 6F, .1f, this, () -> addons.isEnabled("Hit Select"));
    public final BoolValue auto = new BoolValue("Auto", false, this, () -> addons.isEnabled("Hit Select"));
    public final BoolValue sprintCheck = new BoolValue("Sprint Check", false, this, () -> addons.isEnabled("Hit Select") && auto.get());
    //endregion

    //region ab settings
    public final ModeValue autoBlock = new ModeValue("AutoBlock", new String[]{"None", "Vanilla", "Test", "uncp/watchdog", "Release", "Interact", "Constant", "Constant2", "Constant3", "Constant4", "Constant5"}, "None", this);
    // public final SliderValue blockRange = new SliderValue("Block Range", 5.0F, 2.0F, 16F, .1f, this); // mmh
    public final BoolValue interact = new BoolValue("Interact", false, this, () -> !autoBlock.is("None"));
    public final BoolValue via = new BoolValue("Via", false, this, () -> !autoBlock.is("None"));
    public final BoolValue slow = new BoolValue("Slowdown", false, this, () -> !autoBlock.is("None"));
    public final SliderValue releaseBlockRate = new SliderValue("Block Rate", 100, 1, 100, 1, this, () -> autoBlock.is("Release"));
    public final BoolValue forceDisplayBlocking = new BoolValue("Force Display Blocking", false, this);
    public final BoolValue onlyWhenNeeded = new BoolValue("Only When Needed", false, this, () -> !autoBlock.is("None"));
    public final BoolValue onlyRightClick = new BoolValue("Only Right Click", false, this, () -> !autoBlock.is("None"));
    public final BoolValue onlyWhileHurt = new BoolValue("Only While Hurt", false, this, () -> !autoBlock.is("None"));
    //endregion

    //region rotation settings
    private final ModeValue aimMode = new ModeValue("Aim Position", new String[]{"Head", "Torso", "Legs", "Nearest", "Test", "BestHitVec"}, "Nearest", this);
    private final BoolValue inRange = new BoolValue("Rotation In Range", false, this);
    private final SliderValue minAimRange = new SliderValue("Lowest Aim Range", 1, 0, 1, 0.05f, this, inRange::get);
    private final SliderValue maxAimRange = new SliderValue("Highest Aim Range", 1, 0, 1, 0.05f, this, inRange::get);
    private final BoolValue pauseRotations = new BoolValue("Pause Rotations", false, this);
    private final SliderValue pauseRange = new SliderValue("Pause Range", 0.5f, 0.1f, 6, 0.1f, this, pauseRotations::get);
    private final BoolValue smartRotation = new BoolValue("Smart Rotation", true, this);
    private final BoolValue smartVec = new BoolValue("Smart Vec", true, this);
    private final BoolValue heuristics = new BoolValue("Heuristics", false, this);
    private final BoolValue bruteforce = new BoolValue("Bruteforce", true, this);

    // smoothing
    private final BoolValue customRotationSetting = new BoolValue("Custom Rotation Setting", false, this);
    private final ModeValue smoothMode = new ModeValue("Rotations Smooth", RotationUtils.smoothModes, RotationUtils.smoothModes[0], this, () -> customRotationSetting.canDisplay() && customRotationSetting.get());
    private final SliderValue minYawRotSpeed = new SliderValue("Min Yaw Rotation Speed", 45, 1, 180, 1, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get());
    private final SliderValue minPitchRotSpeed = new SliderValue("Min Pitch Rotation Speed", 45, 1, 180, 1, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get());
    private final SliderValue maxYawRotSpeed = new SliderValue("Max Yaw Rotation Speed", 90, 1, 180, 1, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get());
    private final SliderValue maxPitchRotSpeed = new SliderValue("Max Pitch Rotation Speed", 90, 1, 180, 1, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get());
    private final SliderValue keepLength = new SliderValue("Keep Length", 1, 0, 20, 1, this);
    public final BoolValue smoothlyResetRotation = new BoolValue("Smoothly Reset Rotation", true, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get());
    private final SliderValue bezierP0 = new SliderValue("Bezier P0", 0f, 0f, 1f, 0.01f, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtils.smoothModes[1]) || smoothMode.is(RotationUtils.smoothModes[8])));
    private final SliderValue bezierP1 = new SliderValue("Bezier P1", 0.05f, 0f, 1f, 0.01f, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtils.smoothModes[1]) || smoothMode.is(RotationUtils.smoothModes[8])));
    private final SliderValue bezierP2 = new SliderValue("Bezier P2", 0.2f, 0f, 1f, 0.01f, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtils.smoothModes[1]) || smoothMode.is(RotationUtils.smoothModes[8])));
    private final SliderValue bezierP3 = new SliderValue("Bezier P3", 0.4f, 0f, 1f, 0.01f, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtils.smoothModes[1]) || smoothMode.is(RotationUtils.smoothModes[8])));
    private final SliderValue bezierP4 = new SliderValue("Bezier P4", 0.6f, 0f, 1f, 0.01f, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtils.smoothModes[1]) || smoothMode.is(RotationUtils.smoothModes[8])));
    private final SliderValue bezierP5 = new SliderValue("Bezier P5", 0.8f, 0f, 1f, 0.01f, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtils.smoothModes[1]) || smoothMode.is(RotationUtils.smoothModes[8])));
    private final SliderValue bezierP6 = new SliderValue("Bezier P6", 0.95f, 0f, 1f, 0.01f, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtils.smoothModes[1]) || smoothMode.is(RotationUtils.smoothModes[8])));
    private final SliderValue bezierP7 = new SliderValue("Bezier P7", 1.0f, 0f, 1f, 0.01f, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && (smoothMode.is(RotationUtils.smoothModes[1]) || smoothMode.is(RotationUtils.smoothModes[8])));
    private final SliderValue elasticity = new SliderValue("Elasticity", 0.3f, 0.1f, 1f, 0.01f, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && smoothMode.is(RotationUtils.smoothModes[7]));
    private final SliderValue dampingFactor = new SliderValue("Damping Factor", 0.5f, 0.1f, 1f, 0.01f, this, () -> customRotationSetting.canDisplay() && customRotationSetting.get() && smoothMode.is(RotationUtils.smoothModes[7]));

    // randomization
    private final BoolValue randomize = new BoolValue("Randomize", false, this);
    public final ModeValue randomizerot = new ModeValue("RandomizeRotation", new String[]{"Random", "Advanced", "Noise"}, "Random", this, randomize::get);
    public final SliderValue yawStrength = new SliderValue("YawStrength", 5f, 1, 35f, this, () -> this.randomize.get() && this.randomizerot.is("Random"));
    public final SliderValue pitchStrength = new SliderValue("PitchStrength", 5f, 1, 35f, this, () -> this.randomize.get() && this.randomizerot.is("Random"));
    public final MultiBoolValue rdadvanceaddons = new MultiBoolValue("Random Addons", Arrays.asList(new BoolValue("Sin Cos Random", true),
            new BoolValue("Randomize", false)), this, () -> this.randomize.get() && this.randomizerot.is("Advanced"));
    public final SliderValue frequency = new SliderValue("SpeedSinCos", 1.5f, 0f, 5.0f, 0.01f, this, () -> this.randomizerot.is("Advanced") && this.rdadvanceaddons.isEnabled("Sin Cos Random"));
    public final SliderValue yStrengthAimPattern = new SliderValue("YStrengthAmplitudeSinCos", 3.5f, 0f, 15.0f, 0.01f, this, () -> this.randomizerot.is("Advanced") && this.rdadvanceaddons.isEnabled("Sin Cos Random"));
    public final SliderValue xStrengthAimPattern = new SliderValue("XStrengthAmplitudeSinCos", 3.5f, 0f, 15.0f, 0.01f, this, () -> this.randomizerot.is("Advanced") && this.rdadvanceaddons.isEnabled("Sin Cos Random"));
    public final SliderValue yawStrengthAddon = new SliderValue("Yaw Strength Randomize", 5f, 1, 35f, this, () -> this.randomizerot.is("Advanced") && this.rdadvanceaddons.isEnabled("Randomize"));
    public final SliderValue pitchStrengthAddon = new SliderValue("Pitch Strength Randomize", 5f, 1, 35f, this, () -> this.randomizerot.is("Advanced") && this.rdadvanceaddons.isEnabled("Randomize"));
    public final SliderValue noiseAimSpeed = new SliderValue("AimSpeed", 55, 1, 100, 1, this, () -> this.randomize.get() && this.randomizerot.is("Noise"));
    public final SliderValue noisearrivalAggressionYaw = new SliderValue("Yaw ArrivalAggression", 0.35f, 0, 1, 0.05f, this, () -> this.randomize.get() && this.randomizerot.is("Noise"));
    public final SliderValue noisecurvatureStrengthYaw = new SliderValue("Yaw CurvatureStrength", 0.35f, 0, 1, 0.05f, this, () -> this.randomize.get() && this.randomizerot.is("Noise"));
    public final SliderValue noiseHowManyDegreesForNearNervoussnessYaw = new SliderValue("Yaw NearNervousness Degrees", 12, 0.1f, 25, 0.1f, this, () -> this.randomize.get() && this.randomizerot.is("Noise"));
    public final SliderValue noiseNervoussnessDuringRotationYaw = new SliderValue("Yaw NervousnessDuringRotation", 0.25f, 0.1f, 1, 0.05f, this, () -> this.randomize.get() && this.randomizerot.is("Noise"));
    public final SliderValue noiseNearNervoussnessYaw = new SliderValue("Yaw NervousnessNearTarget", 0.25f, 0.1f, 1, 0.05f, this, () -> this.randomize.get() && this.randomizerot.is("Noise"));
    public final SliderValue noiseAttentionYaw = new SliderValue("Yaw Attention", 0.75f, 0, 2, 0.05f, this, () -> this.randomize.get() && this.randomizerot.is("Noise"));
    public final SliderValue noisearrivalAggressionPitch = new SliderValue("Pitch ArrivalAggression", 0.55f, 0, 1, 0.05f, this, () -> this.randomize.get() && this.randomizerot.is("Noise"));
    public final SliderValue noisecurvatureStrengthPitch = new SliderValue("Pitch CurvatureStrength", 0.27f, 0, 1, 0.05f, this, () -> this.randomize.get() && this.randomizerot.is("Noise"));
    public final SliderValue noiseHowManyDegreesForNearNervoussnessPitch = new SliderValue("Pitch NearNervousness Degrees", 12, 0.1f, 25, 0.1f, this, () -> this.randomize.get() && this.randomizerot.is("Noise"));
    public final SliderValue noiseNervoussnessDuringRotationPitch = new SliderValue("Pitch NervousnessDuringRotation", 0.20f, 0.1f, 1, 0.05f, this, () -> this.randomize.get() && this.randomizerot.is("Noise"));
    public final SliderValue noiseNearNervoussnessPitch = new SliderValue("Pitch NervousnessNearTarget", 0.12f, 0.1f, 1, 0.05f, this, () -> this.randomize.get() && this.randomizerot.is("Noise"));
    public final SliderValue noiseAttentionPitch = new SliderValue("Pitch Attention", 0.45f, 0, 2, 0.05f, this, () -> this.randomize.get() && this.randomizerot.is("Noise"));

    // fix
    public final ModeValue movementFix = new ModeValue("Movement", new String[]{"Silent", "Strict"}, "Silent", this, () -> addons.isEnabled("Movement Fix"));
    //endregion

    //region misc
    public final MultiBoolValue weaponFilter = new MultiBoolValue("Weapon Filter", Arrays.asList(
            new BoolValue("Swords", true),
            new BoolValue("Axes", true),
            new BoolValue("Rods", true),
            new BoolValue("Sticks", true),
            new BoolValue("Fists", true),
            new BoolValue("Rest", true)
    ), this);
    public final MultiBoolValue slotFilter = new MultiBoolValue("Slot Filter", Arrays.asList(
            new BoolValue("Slot 1", true),
            new BoolValue("Slot 2", true),
            new BoolValue("Slot 3", true),
            new BoolValue("Slot 4", true),
            new BoolValue("Slot 5", true),
            new BoolValue("Slot 6", true),
            new BoolValue("Slot 7", true),
            new BoolValue("Slot 8", true),
            new BoolValue("Slot 9", true)
    ), this);
    public final BoolValue noScaffold = new BoolValue("No Scaffold", false, this);
    public final BoolValue noInventory = new BoolValue("No Inventory", false, this);
    public final BoolValue noBedNuker = new BoolValue("No Bed Nuker", false, this);
    //endregion

    public final List<EntityLivingBase> targets = new ArrayList<>();
    public EntityLivingBase target;
    private final TimerUtils attackTimer = new TimerUtils();
    private final TimerUtils switchTimer = new TimerUtils();
    private final TimerUtils perfectHitTimer = new TimerUtils();
    private final TimerUtils afterHitSelectTimer = new TimerUtils();
    private long randomDelay = 50L;
    private int clickTiming = 0;
    private boolean firstHitHappened = false;

    private int index;
    private int clicks;
    private int maxClicks;
    public boolean isBlocking;
    public boolean renderBlocking;
    public boolean blinked;
    public int blinkTicks;
    public float[] prevRotation;
    public Vec3 prevVec;
    public Vec3 currentVec;
    public Vec3 targetVec;
    public boolean doHitSelect = false;
    public boolean autoHitSelect;

    public float[] rotation;
    private float[] currentRotation = new float[2];
    private float[] previousRotation = new float[2];

    private float noiseYaw;
    private float noisePitch;
    private float noiseYawAcc;
    private float noisePitchAcc;

    private void resetNoise() {
        if (mc.thePlayer != null) {
            noiseYaw = mc.thePlayer.rotationYaw;
            noisePitch = mc.thePlayer.rotationPitch;
        }
        noiseYawAcc = 0;
        noisePitchAcc = 0;
    }

    @Override
    public void onEnable() {
        clicks = 0;
        attackTimer.reset();
        clickTiming = 0;
        firstHitHappened = false;
        randomDelay = getRandomClickDelay();
        resetNoise();

        this.currentRotation = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
        this.previousRotation = new float[]{mc.thePlayer.prevRotationYaw, mc.thePlayer.prevRotationPitch};
    }

    @Override
    public void onDisable() {
        unblock();
        if (renderBlocking) {
            renderBlocking = false;
        }
        if (blinked) {
            BlinkComponent.dispatch();
        }
        target = null;
        targets.clear();
        index = 0;
        switchTimer.reset();
        prevRotation = rotation = null;
        prevVec = currentVec = targetVec = null;
        blinkTicks = 0;
        clickTiming = 0;
        resetNoise();

        if (mc.thePlayer != null && mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), Mouse.isButtonDown(1));
        }

        Iterator<Map.Entry<EntityPlayer, DecelerateAnimation>> iterator = getModule(Interface.class).animationEntityPlayerMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<EntityPlayer, DecelerateAnimation> entry = iterator.next();
            DecelerateAnimation animation = entry.getValue();

            animation.setDirection(Direction.BACKWARDS);
            if (animation.finished(Direction.BACKWARDS)) {
                iterator.remove();
            }
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        EntityLivingBase lastTarget = this.target;

        targets.clear();

        if ((target == null || !shouldBlock()) && renderBlocking) {
            renderBlocking = false;
        }

        setTag(mode.get());

        if (((isEnabled(Scaffold.class) && noScaffold.get() ||
                !noScaffold.get() && isEnabled(Scaffold.class) && mc.theWorld.getBlockState(getModule(Scaffold.class).data.blockPos).getBlock() instanceof BlockAir) ||
                noInventory.get() && mc.currentScreen instanceof GuiContainer ||
                (noBedNuker.get() && isEnabled(BedNuker.class) && getModule(BedNuker.class).bedPos != null || !noBedNuker.get() && isEnabled(BedNuker.class) && getModule(BedNuker.class).rotate)
        ) && target != null) {
            if (blinked) {
                BlinkComponent.dispatch();
                blinked = false;
            }
        }

        getTargetsImproved();

        if (!targets.isEmpty()) {
            sortTargetsByPriority();

            if (switchTimer.hasTimeElapsed((long) (switchDelayValue.get() * 100L)) && targets.size() > 1) {
                ++index;
                switchTimer.reset();
            }

            if (index >= targets.size()) {
                index = 0;
            }

            switch (mode.get()) {
                case "Switch":
                    target = targets.get(index);
                    break;
                case "Single":
                    target = targets.get(0);
                    break;
                case "UltimateSwitch":
                    target = getUltimateTarget(targets);
                    break;
                default:
                    target = targets.get(0);
                    break;
            }

        } else {
            target = null;
            prevRotation = rotation = null;
            prevVec = currentVec = targetVec = null;
            unblock();
            if (blinked) {
                BlinkComponent.dispatch();
                blinked = false;
            }
            clicks = 0;
            clickTiming = 0;
            blinkTicks = 0;
        }

        if (target != lastTarget) {
            resetNoise();
        }

        if (target == null) {
            stopBlocking();
            return;
        }

        if (mc.thePlayer.isSpectator() || mc.thePlayer.isDead || !isCurrentSlotAllowed() || !isAllowedWeapon(mc.thePlayer.getHeldItem()) ||
                (isEnabled(Scaffold.class) && noScaffold.get() ||
                        !noScaffold.get() && isEnabled(Scaffold.class) && mc.theWorld.getBlockState(getModule(Scaffold.class).data.blockPos).getBlock() instanceof BlockAir) ||
                noInventory.get() && mc.currentScreen instanceof GuiContainer ||
                (noBedNuker.get() && isEnabled(BedNuker.class) && getModule(BedNuker.class).bedPos != null || !noBedNuker.get() && isEnabled(BedNuker.class) && getModule(BedNuker.class).rotate)
        ) return;

        if (target != null) {

            if (PlayerUtils.getDistanceToEntityBox(target) < rotationRange.get()) {
                if (PlayerUtils.getDistanceToEntityBox(target) < pauseRange.get() && pauseRotations.get()) {
                    return;
                }

                rotation = calcToEntity(target);
                if (customRotationSetting.canDisplay() && customRotationSetting.get()) {
                    RotationUtils.setRotation(rotation,
                            smoothMode.get(),
                            addons.isEnabled("Movement Fix") ? movementFix.is("Strict") ? MovementCorrection.STRICT : MovementCorrection.SILENT : MovementCorrection.OFF,
                            minYawRotSpeed.get(), maxYawRotSpeed.get(), minPitchRotSpeed.get(), maxPitchRotSpeed.get(),
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
                            (int) keepLength.get(), smoothlyResetRotation.get());
                } else {
                    RotationUtils.setRotation(rotation, addons.isEnabled("Movement Fix") ? movementFix.is("Strict") ? MovementCorrection.STRICT : MovementCorrection.SILENT : MovementCorrection.OFF, (int) keepLength.get());
                }

                if (!autoBlock.is("None") && !isBlocking && RotationUtils.isLookingAtEntity(target, rotationRange.get())) {
                    if (shouldBlockImproved()) {
                        renderBlocking = true;
                        startBlocking();
                    }
                }

                this.previousRotation = this.currentRotation.clone();
                this.currentRotation = rotation.clone();
                prevRotation = rotation;

                if (preHit.get() && !shouldAttack() && getDistanceToEntity(target) <= preHitRange.get()) {
                    if (RotationUtils.isLookingAtEntity(target, preHitRange.get())) {
                        mc.thePlayer.swingItem();
                    }
                }

                if (!shouldAttack() && getDistanceToEntity(target) <= rotationRange.get()) {
                    if (!autoBlock.is("None") && shouldBlockImproved()) {
                        renderBlocking = true;
                        startBlocking();
                    }
                }

            }

            if (shouldBlock()) {
                renderBlocking = true;
            }

            if (preTickBlock()) return;

            if (clicks == 0 && clickTiming == 0) return;

            if (!autoBlock.is("None") && target != null && getDistanceToEntity(target) <= rotationRange.get()) {
                if (shouldBlockImproved()) {
                    if (!isBlocking) {
                        block(interact.get());
                        isBlocking = true;
                    }
                    renderBlocking = true;
                } else {
                    stopBlocking();
                }
            }


            if (isBlocking || autoBlock.is("HYT"))
                if (preAttack()) return;

            if (shouldAttack()) {
                maxClicks = Math.max(clicks, clickTiming);
                for (int i = 0; i < maxClicks; i++) {
                    attack();
                    if (clicks > 0) clicks--;
                    if (clickTiming > 0) clickTiming--;
                }
            }

            boolean isInRotationRange = PlayerUtils.getDistanceToEntityBox(target) < rotationRange.get();

            if (isInRotationRange && !autoBlock.is("None")) {
                boolean isConstantMode = autoBlock.get().toLowerCase().contains("constant");

                if (isConstantMode) {
                    if (shouldBlockImproved()) {
                        startBlocking();
                    } else {
                        stopBlocking();
                    }
                } else {
                    if (shouldBlock() || autoBlock.is("HYT")) {
                        if (Mouse.isButtonDown(2)) {
                            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                        }
                        postAttack();
                    }
                }
            } else {
                stopBlocking();
            }
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (auto.canDisplay() && auto.get() && target != null && shouldAttack() && target.hurtTime < 6 && !mc.gameSettings.keyBindJump.isKeyDown() && !checks() && mc.thePlayer.onGround && (sprintCheck.get() && MovementUtils.canSprint(true) || !sprintCheck.get())) {
            mc.thePlayer.jump();
            if (mc.thePlayer.offGroundTicks >= 4)
                autoHitSelect = true;
        } else {
            autoHitSelect = false;
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (target == null) return;
        if (!shouldAttack()) return;


        if (shouldClick()) {
            if (maxAps.get() > 0) {
                clickTiming++;
                clicks++;
            }
            attackTimer.reset();
            randomDelay = getRandomClickDelay();
        }
    }

    private void getTargetsImproved() {
        if (mc.currentScreen != null) return;

        List<EntityLivingBase> potentialTargets = mc.theWorld.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityLivingBase && canAttackTarget((EntityLivingBase) entity))
                .map(entity -> (EntityLivingBase) entity)
                .collect(Collectors.toList());

        targets.addAll(potentialTargets);
    }

    private void sortTargetsByPriority() {
        if (targets.size() <= 1) return;

        switch (priority.get()) {
            case "Armor":
                targets.sort(Comparator.comparingInt(EntityLivingBase::getTotalArmorValue));
                break;
            case "Range":
                targets.sort(Comparator.comparingDouble(this::getDistanceToEntity));
                break;
            case "Health":
                targets.sort(Comparator.comparingDouble(EntityLivingBase::getHealth));
                break;
            case "HurtTime":
                targets.sort(Comparator.comparingInt(entity -> entity.hurtTime));
                break;
            case "FOV":
                targets.sort(Comparator.comparingDouble(RotationUtils::distanceFromYaw));
                break;
            case "Ultimate":
                targets.sort(Comparator.comparingDouble(this::ultimateTarget));
                break;
        }
    }

    private EntityLivingBase getUltimateTarget(List<EntityLivingBase> targets) {
        return targets.stream()
                .min(Comparator.comparingDouble(this::ultimateTarget))
                .orElse(targets.get(0));
    }

    private double ultimateTarget(EntityLivingBase entity) {
        double distance = getDistanceToEntity(entity);
        double hurtTime = entity.hurtTime * 6;
        return hurtTime + distance;
    }

    private boolean shouldClick() {
        return attackTimer.hasTimeElapsed(randomDelay) || attackTimer.hasTimeElapsed(1000);
    }

    private long getRandomClickDelay() {
        double min = minAps.get();
        double max = maxAps.get();

        switch (apsMode.get()) {
            case "Random":
                return (long) (1000L / MathUtils.nextInt((int) min, (int) max));
            case "Secure Random":
                double time = MathHelper.clamp_double(
                        min + ((max - min) * new SecureRandom().nextDouble()), min, max);
                return (long) (1000L / time);
            case "Full Random":
                min *= MathUtils.nextDouble(0, 1);
                max *= MathUtils.nextDouble(0, 1);
                double randomTime = (max / min) * (MathUtils.nextDouble(min, max));
                return (long) (1000L / randomTime);
            case "Dynamic":
                double dynamicFactor = 1.0;
                if (target != null) {
                    double dist = getDistanceToEntity(target);
                    dynamicFactor = dist > 3.0 ? 0.8 : (dist < 2.0 ? 1.2 : 1.0);
                }
                return (long) (1000L / ((min + max) / 2 * dynamicFactor));
            default:
                return 50L;
        }
    }

    private boolean preTickBlock() {
        switch (autoBlock.get()) {
            case "Watchdog":
                if (blinkTicks >= 3) {
                    blinkTicks = 0;
                }
                blinkTicks++;
                switch (blinkTicks) {
                    case 0:
                        return true;
                    case 1:
                        if (isBlocking) {
                            BlinkComponent.blinking = true;
                            unblock();
                            blinked = true;
                            return true;
                        }
                    case 2:
                        return false;
                }
                break;
        }
        return false;
    }

    private boolean preAttack() {
        switch (autoBlock.get()) {
            case "Release":
                if (clicks + clickTiming + 1 == maxClicks) {
                    if (!(releaseBlockRate.get() > 0 && RandomUtils.nextInt(0, 100) <= releaseBlockRate.get()))
                        break;
                    block();
                    isBlocking = true;
                }
                break;

            case "Interact":
                if (isBlocking) {
                    unblock();
                    return true;
                }
                break;
            case "HYT":
                if (this.isBlocking && !getModule(AutoGap.class).eating) {
                    unblock();
                }

                if (isBlocking) {
                    unblock();
                }
                break;

            case "Constant":
            case "Constant3":
            case "Constant2":
            case "Constant4":
            case "Constant5":
                if (isBlocking) {
                    stopBlocking();
                }
                break;
        }
        return false;
    }

    private void postAttack() {
        switch (autoBlock.get()) {
            case "Vanilla":
                block();
                break;
            case "Interact":
                block(true);
                break;
            case "HYT":
                if (!shouldBlock() && isBlocking)
                    unblock();
                break;
            case "Watchdog":
                block(true);
                BlinkComponent.dispatch();
                break;
        }
    }

    private void block() {
        block(interact.get());
    }

    public void block(boolean interact) {
        if (!isBlocking) {

            if (interact) {
                sendPacket(new C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT));
            }

            if (via.get()) {
                if (!getModule(AutoGap.class).eating && ViaLoadingBase.getInstance().getTargetVersion().getVersion() > 47) {
                    sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                    PacketWrapper useItem = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                    useItem.write(Type.VAR_INT, 1);
                    com.viaversion.viarewind.utils.PacketUtil.sendToServer(useItem, Protocol1_8To1_9.class, true, true);
                }
            } else {
                sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
            }
            isBlocking = true;
        }
    }

    public void doAutoBlock() {
        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
            sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
            isBlocking = true;
        }
    }

    private void startBlocking() {
        if (!isBlocking && isHoldingSword()) {
            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
            isBlocking = true;
        }
    }

    private void stopBlocking() {
        if (isBlocking) {
            mc.playerController.onStoppedUsingItem(mc.thePlayer);
            isBlocking = false;
        }
    }

    public void unblock() {
        if (isBlocking) {
            if (autoBlock.is("HYT")) {
                sendPacket(new C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 1) % 8));
                sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            } else if (autoBlock.is("Constant") || autoBlock.is("Constant2") || autoBlock.is("Constant4") || autoBlock.is("Constant3") || autoBlock.is("Constant5")) {
                mc.playerController.onStoppedUsingItem(mc.thePlayer);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), Mouse.isButtonDown(1));
            } else {
                sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            }
            isBlocking = false;
        }
    }

    public void attack() {
        if (autoBlock.is("Release"))
            unblock();

        boolean canSeeTarget = RotationUtils.isLookingAtEntity(target, attackRange.get());

        if (canAttack(target) && (addons.isEnabled("Ray Cast") && canSeeTarget || !addons.isEnabled("Ray Cast"))) {

            if (packetAttack.get()) {
                sendPacket(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
                if (!noSwing.get()) {
                    mc.thePlayer.swingItem();
                }
            } else {
                if (getModule(AutoGap.class).isEnabled() && getModule(AutoGap.class).alwaysAttack.get() && getModule(AutoGap.class).eating) {
                    AttackOrder.sendFixedAttackNoPacketEvent(mc.thePlayer, target);
                } else {
                    AttackOrder.sendFixedAttack(mc.thePlayer, target);
                }
            }

            firstHitHappened = true;
            perfectHitTimer.reset();
        }
    }

    public boolean canAttack(EntityLivingBase entity) {
        if (addons.isEnabled("Hit Select")) {
            if (mc.thePlayer.hurtTime < 5 || autoHitSelect) {
                if (this.getDistanceToEntity(this.target) < this.hitSelectRange.get()) {
                    this.doHitSelect = true;
                }
            } else {
                this.doHitSelect = false;
            }
            if (this.getDistanceToEntity(this.target) > this.hitSelectRange.get()) {
                this.doHitSelect = false;
            } else if (this.afterHitSelectTimer.hasTimeElapsed(900L)) {
                this.doHitSelect = false;
                this.afterHitSelectTimer.reset();
            }

            if (!doHitSelect)
                return false;
        }

        if (addons.isEnabled("Perfect Hit"))
            return (entity.hurtTime <= 2 || perfectHitTimer.hasTimeElapsed(900));

        return true;
    }

    public boolean isHoldingSword() {
        return mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }

    private boolean checks() {
        return mc.thePlayer.isInLava() || mc.thePlayer.isBurning() || mc.thePlayer.isInWater() || mc.thePlayer.isInWeb;
    }

    public double getDistanceToEntity(Entity entity) {
        switch (rangeMode.get()) {
            case "Client":
                return PlayerUtils.getDistanceToEntityBox(entity);
            case "Server":
                double x = (double) entity.serverPosX / 32.0D;
                double y = (double) entity.serverPosY / 32.0D;
                double z = (double) entity.serverPosZ / 32.0D;
                return new Vec3(x, y, z).getDistanceAtEyeByVec(mc.thePlayer, mc.thePlayer.posX + mc.thePlayer.getCollisionBorderSize(), mc.thePlayer.posY, mc.thePlayer.posZ + mc.thePlayer.getCollisionBorderSize());
            case "Client 2":
                return entity.getDistance(mc.thePlayer.getPositionEyes(1));
            case "Client 3":
                return PlayerUtils.calculatePerfectRangeToEntity(entity);
            case "Predictive":
                return getEffectiveRange(entity);
        }
        return 0;
    }

    private double getEffectiveRange(Entity entity) {
        double predictBaseRange = 4.0;
        double predictMaxRange = 5.5;

        int targetPing = 0;
        double motionPrediction = 0.0;

        if (entity instanceof EntityPlayer) {
            EntityPlayer targetPlayer = (EntityPlayer) entity;

            NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(targetPlayer.getUniqueID());
            if (info != null) {
                targetPing = info.getResponseTime();
            }

            double motionX = targetPlayer.posX - targetPlayer.prevPosX;
            double motionZ = targetPlayer.posZ - targetPlayer.prevPosZ;
            double horizontalSpeed = Math.sqrt(motionX * motionX + motionZ * motionZ);

            double timeInSeconds = targetPing / 1000.0;
            motionPrediction = horizontalSpeed * timeInSeconds * predictionFactor.get();
        }

        double pingFactor = MathHelper.clamp_float(targetPing / 250f, 0f, 1f);
        double predictedRange = predictBaseRange + (predictMaxRange - predictBaseRange) * pingFactor;

        return predictedRange + motionPrediction;
    }

    private boolean canAttackTarget(EntityLivingBase targetToCheck) {
        if (!(targetToCheck instanceof EntityLivingBase))
            return false;
        if (!isAllowedWeapon(mc.thePlayer.getHeldItem()))
            return false;

        if (targetToCheck == mc.thePlayer || targetToCheck.ticksExisted < 1 || targetToCheck.isDead || targetToCheck.deathTime > 1)
            return false;

        boolean validType = (targetToCheck instanceof EntityPlayer && targetOption.isEnabled("Players"))
                || (targetToCheck instanceof EntityMob && targetOption.isEnabled("Mobs"))
                || (targetToCheck instanceof EntityAnimal && targetOption.isEnabled("Animals"))
                || (targetToCheck instanceof EntityVillager && targetOption.isEnabled("Villagers"))
                || (targetToCheck instanceof EntityArmorStand && targetOption.isEnabled("Armor Stands"));

        if (filter.isEnabled("Teams") && PlayerUtils.isInTeam(targetToCheck)) {
            return false;
        }

        if (filter.isEnabled("Friends") && targetToCheck instanceof EntityPlayer) {
            if (UwU.INSTANCE.getFriendManager().isFriend((EntityPlayer) targetToCheck))
                return false;
        }

        if (targetToCheck instanceof EntityPlayer && isEnabled(AntiBot.class)) {
            if (getModule(AntiBot.class).isBot((EntityPlayer) targetToCheck))
                return false;
        }

        if (!targetOption.isEnabled("Invisible") && targetToCheck.isInvisible()) {
            return false;
        }

        double distance = rangeMode.is("Predictive") ? getEffectiveRange(targetToCheck) : getDistanceToEntity(targetToCheck);
        if (distance > searchRange.get()) {
            return false;
        }

        if (fov.get() < 180 && RotationUtils.getRotationDifference(targetToCheck) > fov.get()) {
            return false;
        }

        return validType;
    }

    private boolean isAllowedWeapon(ItemStack stack) {
        if (stack == null)
            return weaponFilter.isEnabled("Fists");

        Item item = stack.getItem();

        if (item instanceof ItemSword && weaponFilter.isEnabled("Swords"))
            return true;
        if (item instanceof ItemAxe && weaponFilter.isEnabled("Axes"))
            return true;
        if (item instanceof ItemFishingRod && weaponFilter.isEnabled("Rods"))
            return true;
        if (item == Items.stick && weaponFilter.isEnabled("Sticks"))
            return true;

        return weaponFilter.isEnabled("Rest");
    }

    private boolean isCurrentSlotAllowed() {
        int slot = mc.thePlayer.inventory.currentItem;
        String slotName = "Slot " + (slot + 1);
        return slotFilter.isEnabled(slotName);
    }

    public boolean isValid(Entity entity) {
        return canAttackTarget((EntityLivingBase) entity);
    }

    public boolean shouldAttack() {
        return getDistanceToEntity(target) <= (!mc.thePlayer.canEntityBeSeen(target) ? wallAttackRange.get() : attackRange.get());
    }

    public boolean shouldBlock() {
        return getDistanceToEntity(target) <= rotationRange.get() && isHoldingSword();
    }

    private boolean shouldBlockImproved() {
        if (!isHoldingSword()) {
            return false;
        }
        if (getDistanceToEntity(target) > rotationRange.get()) {
            return false;
        }
        if (onlyRightClick.get() && !Mouse.isButtonDown(1)) {
            return false;
        }
        if (onlyWhileHurt.get() && mc.thePlayer.hurtTime <= 0) {
            return false;
        }
        if (onlyWhenNeeded.get() && getDistanceToEntity(target) > rotationRange.get()) {
            return false;
        }

        return true;
    }

    private float[] getRawRotations(Vec3 vec) {
        Vec3 playerPos = mc.thePlayer.getPositionEyes(1);
        double deltaX = vec.xCoord - playerPos.xCoord;
        double deltaY = vec.yCoord - playerPos.yCoord;
        double deltaZ = vec.zCoord - playerPos.zCoord;

        float yaw = (float) (Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F);
        float pitch = (float) (-Math.toDegrees(Math.atan2(deltaY, Math.hypot(deltaX, deltaZ))));

        return new float[]{yaw, pitch};
    }

    public float[] calcToEntity(EntityLivingBase entity) {

        prevVec = currentVec;

        Vec3 playerPos = mc.thePlayer.getPositionEyes(1);
        Vec3 entityPos = entity.getPositionVector();
        AxisAlignedBB boundingBox = entity.getEntityBoundingBox();

        switch (aimMode.get()) {
            case "Head":
                targetVec = entityPos.add(0.0, entity.getEyeHeight(), 0.0);
                break;
            case "Torso":
                targetVec = entityPos.add(0.0, entity.height * 0.75, 0.0);
                break;
            case "Legs":
                targetVec = entityPos.add(0.0, entity.height * 0.45, 0.0);
                break;
            case "Nearest":
                targetVec = RotationUtils.getBestHitVec(entity);
                break;
            case "BestHitVec":
                targetVec = getBestHitVecImproved(entity);
                break;
            case "Test":
                Vec3 test = new Vec3(entity.posX, entity.posY, entity.posZ);
                double diffY;
                for (diffY = boundingBox.minY + 0.7D; diffY < boundingBox.maxY - 0.1D; diffY += 0.1D) {
                    if (mc.thePlayer.getPositionEyes(1).distanceTo(new Vec3(entity.posX, diffY, entity.posZ)) < mc.thePlayer.getPositionEyes(1).distanceTo(test)) {
                        test = new Vec3(entity.posX, diffY, entity.posZ);
                    }
                }
                targetVec = test;
                break;
            default:
                targetVec = entityPos;
        }

        if (heuristics.get()) {
            targetVec = RotationUtils.heuristics(entity, targetVec);
        }

        if (bruteforce.get()) {
            if (!RotationUtils.isLookingAtEntity(RotationUtils.getRotations(targetVec), rotationRange.get())) {
                final double xWidth = boundingBox.maxX - boundingBox.minX;
                final double zWidth = boundingBox.maxZ - boundingBox.minZ;
                final double height = boundingBox.maxY - boundingBox.minY;
                for (double x = 0.0; x < 1.0; x += 0.2) {
                    for (double y = 0.0; y < 1.0; y += 0.2) {
                        for (double z = 0.0; z < 1.0; z += 0.2) {
                            final Vec3 hitVec = new Vec3(boundingBox.minX + xWidth * x, boundingBox.minY + height * y, boundingBox.minZ + zWidth * z);
                            if (RotationUtils.isLookingAtEntity(RotationUtils.getRotations(hitVec), rotationRange.get())) {
                                targetVec = hitVec;
                            }
                        }
                    }
                }
            }
        }

        if (inRange.get()) {
            double minAimY = entity.posY + entity.getEyeHeight() * minAimRange.get();
            double maxAimY = entity.posY + entity.getEyeHeight() * maxAimRange.get();

            if (RotationUtils.getBestHitVec(entity).yCoord < minAimY) {
                targetVec.yCoord = minAimY;
            }

            if (RotationUtils.getBestHitVec(entity).yCoord > maxAimY) {
                targetVec.yCoord = maxAimY;
            }
        }

        currentVec = targetVec;

        if (smartVec.get()) {
            boolean test = RotationUtils.isLookingAtEntity(RotationUtils.getRotations(prevVec), rotationRange.get());
            if (test) {
                currentVec = prevVec;
            }
        }

        double deltaX = currentVec.xCoord - playerPos.xCoord;
        double deltaY = currentVec.yCoord - playerPos.yCoord;
        double deltaZ = currentVec.zCoord - playerPos.zCoord;

        float yaw = (float) (Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F);
        float pitch = (float) (-Math.toDegrees(Math.atan2(deltaY, Math.hypot(deltaX, deltaZ))));

        if (this.randomize.get()) {
            switch (this.randomizerot.get()) {
                case "Random": {
                    yaw += MathUtils.randomizeDouble(-this.yawStrength.get(), this.yawStrength.get());
                    pitch += MathUtils.randomizeDouble(-this.pitchStrength.get(), this.pitchStrength.get());
                    break;
                }
                case "Advanced": {
                    if (rdadvanceaddons.isEnabled("Sin Cos Random")) {
                        double time = System.currentTimeMillis() / 1000.0D;
                        double frequency = this.frequency.get();
                        double yawAmplitude = this.xStrengthAimPattern.get();
                        double pitchAmplitude = this.yStrengthAimPattern.get();

                        yaw += (Math.sin(time * frequency) * yawAmplitude);
                        pitch += (float) (Math.cos(time * frequency) * pitchAmplitude);
                    }

                    if (rdadvanceaddons.isEnabled("Randomize")) {
                        yaw += MathUtils.randomizeDouble(-this.yawStrengthAddon.get(), this.yawStrengthAddon.get());
                        pitch += MathUtils.randomizeDouble(-this.pitchStrengthAddon.get(), this.pitchStrengthAddon.get());
                    }
                    break;
                }
                case "Noise": {
                    float[] rawRots = new float[]{yaw, pitch};

                    float deltaYaw = MathHelper.wrapAngleTo180_float(rawRots[0] - this.noiseYaw);
                    float deltaPitch = rawRots[1] - this.noisePitch;

                    float yawNervousness = Math.abs(deltaYaw) > noiseHowManyDegreesForNearNervoussnessYaw.get() ? noiseNervoussnessDuringRotationYaw.get() : noiseNearNervoussnessYaw.get();
                    float pitchNervousness = Math.abs(deltaPitch) > noiseHowManyDegreesForNearNervoussnessPitch.get() ? noiseNervoussnessDuringRotationPitch.get() : noiseNearNervoussnessPitch.get();

                    float yawAttention = Math.max(0, 2.0f - noiseAttentionYaw.get() * 2.0f);
                    float pitchAttention = Math.max(0, 2.0f - noiseAttentionPitch.get() * 2.0f);

                    deltaYaw += MathUtils.randomizeDouble(-yawNervousness, yawNervousness) * yawAttention;
                    deltaPitch += MathUtils.randomizeDouble(-pitchNervousness, pitchNervousness) * pitchAttention;

                    float yawStep = deltaYaw * (noiseAimSpeed.get() / 100.0f) * noisearrivalAggressionYaw.get();
                    float pitchStep = deltaPitch * (noiseAimSpeed.get() / 100.0f) * noisearrivalAggressionPitch.get();

                    this.noiseYawAcc = this.noiseYawAcc * noisecurvatureStrengthYaw.get() + yawStep * (1.0f - noisecurvatureStrengthYaw.get());
                    this.noisePitchAcc = this.noisePitchAcc * noisecurvatureStrengthPitch.get() + pitchStep * (1.0f - noisecurvatureStrengthPitch.get());

                    this.noiseYaw += this.noiseYawAcc;
                    this.noisePitch += this.noisePitchAcc;

                    yaw = this.noiseYaw;
                    pitch = this.noisePitch;
                    break;
                }
            }
        }

        if (smartRotation.get() && prevRotation != null) {
            boolean test = RotationUtils.isLookingAtEntity(prevRotation, rotationRange.get());
            boolean test2 = RotationUtils.isLookingAtEntity(new float[]{yaw, prevRotation[1]}, rotationRange.get());
            boolean test3 = RotationUtils.isLookingAtEntity(new float[]{prevRotation[0], pitch}, rotationRange.get());

            if (test) {
                return prevRotation;
            }

            if (test2) {
                return new float[]{yaw, prevRotation[1]};
            }
            if (test3) {
                return new float[]{prevRotation[0], pitch};
            }
        }

        pitch = MathHelper.clamp_float(pitch, -90, 90);

        return new float[]{yaw, pitch};
    }

    private Vec3 getBestHitVecImproved(EntityLivingBase entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        Vec3 eyePos = mc.thePlayer.getPositionEyes(1.0F);

        double xWidth = bb.maxX - bb.minX;
        double yHeight = bb.maxY - bb.minY;
        double zWidth = bb.maxZ - bb.minZ;

        Vec3 bestVec = new Vec3(entity.posX, entity.posY + entity.getEyeHeight() / 2, entity.posZ);
        double bestDistance = eyePos.distanceTo(bestVec);

        for (double x = 0.0; x <= 1.0; x += 0.1) {
            for (double y = 0.0; y <= 1.0; y += 0.1) {
                for (double z = 0.0; z <= 1.0; z += 0.1) {
                    Vec3 testVec = new Vec3(
                            bb.minX + xWidth * x,
                            bb.minY + yHeight * y,
                            bb.minZ + zWidth * z
                    );

                    double distance = eyePos.distanceTo(testVec);
                    MovingObjectPosition mop = mc.theWorld.rayTraceBlocks(eyePos, testVec, false, true, false);
                    if (mop == null && distance < bestDistance) {
                        bestDistance = distance;
                        bestVec = testVec;
                    }
                }
            }
        }

        return bestVec;
    }
}