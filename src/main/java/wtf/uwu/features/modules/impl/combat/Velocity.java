package wtf.uwu.features.modules.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.packet.PacketEvent;
import wtf.uwu.events.impl.player.*;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.modules.impl.movement.LongJump;
import wtf.uwu.features.modules.impl.movement.Scaffold;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.features.values.impl.ModeValue;
import wtf.uwu.features.values.impl.SliderValue;
import wtf.uwu.utils.packet.PacketUtils;
import wtf.uwu.utils.player.MovementUtils;
import wtf.uwu.utils.player.PlayerUtils;
import wtf.uwu.utils.player.RotationUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@ModuleInfo(name = "Velocity", category = ModuleCategory.Combat)
public class Velocity extends Module {
    private final ModeValue mode = new ModeValue("Mode", new String[]{"Cancel", "Air", "Horizontal", "Boost", "Jump Reset", "GrimAC", "Intave", "Cris", "PolarTest", "Zip"}, "Air", this);
    private final ModeValue grimMode = new ModeValue("Grim Mode", new String[]{"Reduce", "1.17"}, "Reduce", this, () -> mode.is("GrimAC"));
    private final SliderValue reverseTick = new SliderValue("Boost Tick", 1, 1, 5, 1, this, () -> mode.is("Boost"));
    private final SliderValue reverseStrength = new SliderValue("Boost Strength", 1, 0.1f, 1, 0.01f, this, () -> mode.is("Boost"));
    private final ModeValue jumpResetMode = new ModeValue("Jump Reset Mode", new String[]{"Hurt Time", "Packet"}, "Packet", this, () -> mode.is("Jump Reset"));
    private final SliderValue jumpResetHurtTime = new SliderValue("Jump Reset Hurt Time", 9, 1, 10, 1, this, () -> mode.is("Jump Reset") && jumpResetMode.is("Hurt Time"));
    private final SliderValue intaveXZOnHit = new SliderValue("XZ on Hit", 0.6f, -1, 1, 0.01f, this, () -> mode.is("Intave"));
    private final SliderValue intaveXZOnSprintHit = new SliderValue("XZ on Sprint Hit", 0.6f, -1, 1, 0.01f, this, () -> mode.is("Intave"));
    private final BoolValue intaveJumpSmart = new BoolValue("Smart Jump", false, this, () -> mode.is("Intave"));
    private final BoolValue intaveSmartTrackMotion = new BoolValue("Track Motion", true, this, () -> mode.is("Intave"));
    private final SliderValue intaveMaxAngleSmart = new SliderValue("Max Angle Smart", 40, 10, 180, 1, this, () -> mode.is("Intave") && intaveJumpSmart.get());
    private final BoolValue intaveJump = new BoolValue("Jump", false, this, () -> mode.is("Intave"));
    private final SliderValue zipDelay = new SliderValue("Zip Delay", 1000, 500, 10000, 250, this, () -> mode.is("Zip"));
    private final BoolValue zipStopOnAttack = new BoolValue("Zip Stop on Attack", true, this, () -> mode.is("Zip"));
    private boolean veloPacket = false;
    private boolean canSpoof, canCancel;
    private int idk = 0;
    private int intaveCounter = 0;
    private boolean hasSpoofed = false;
    private final Queue<Packet<INetHandlerPlayClient>> zipDelayedPackets = new ConcurrentLinkedQueue<>();
    private long zipLastVelocityTime = -1;
    private boolean zipIsDelayed = false;

    @Override
    public void onEnable() {
        super.onEnable();
        resetState();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        resetState();
    }

    private void resetState() {
        veloPacket = false;
        canSpoof = canCancel = false;
        idk = 0;
        intaveCounter = 0;
        hasSpoofed = false;
        releaseZipPackets();
    }

    private void releaseZipPackets() {
        if (zipIsDelayed) {
            while (!zipDelayedPackets.isEmpty()) {
                Packet<INetHandlerPlayClient> packet = zipDelayedPackets.poll();
                if (packet != null && mc.getNetHandler() != null) {
                    packet.processPacket(mc.getNetHandler());
                }
            }
        }
        zipIsDelayed = false;
        zipLastVelocityTime = -1;
        zipDelayedPackets.clear();
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        setTag(mode.get());

        if (mode.is("Zip") && zipIsDelayed && System.currentTimeMillis() - zipLastVelocityTime >= zipDelay.get()) {
            releaseZipPackets();
        }

        if (mode.is("PolarTest") && mc.thePlayer.hurtTime == 9) {
            mc.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
        }

        if (mode.is("GrimAC") && grimMode.is("1.17") && canSpoof) {
            sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround));
            sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, new BlockPos(mc.thePlayer).down(), EnumFacing.DOWN));
            canSpoof = false;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (mode.is("Zip")) {
            if (packet instanceof S12PacketEntityVelocity s12 && s12.getEntityID() == mc.thePlayer.getEntityId()) {
                event.setCancelled(true);
                zipDelayedPackets.add((Packet<INetHandlerPlayClient>) packet);
                if (!zipIsDelayed) {
                    zipLastVelocityTime = System.currentTimeMillis();
                    zipIsDelayed = true;
                }
                return;
            }
            if (zipIsDelayed && packet instanceof S32PacketConfirmTransaction) {
                event.setCancelled(true);
                zipDelayedPackets.add((Packet<INetHandlerPlayClient>) packet);
                return;
            }
        }

        if (packet instanceof S12PacketEntityVelocity s12 && s12.getEntityID() == mc.thePlayer.getEntityId()) {
            switch (mode.get()) {
                case "Cancel":
                case "PolarTest":
                    event.setCancelled(true);
                    break;
                case "Air":
                    if (!isEnabled(LongJump.class)) {
                        event.setCancelled(true);
                        if (mc.thePlayer.onGround) mc.thePlayer.motionY = (double) s12.getMotionY() / 8000.0;
                    }
                    break;
                case "Horizontal":
                    if (!isEnabled(LongJump.class)) {
                        s12.motionX = (int) (mc.thePlayer.motionX * 8000);
                        s12.motionZ = (int) (mc.thePlayer.motionZ * 8000);
                    }
                    break;
                case "Boost":
                    if (mc.thePlayer.onGround) {
                        s12.motionX = (int) (mc.thePlayer.motionX * 8000);
                        s12.motionZ = (int) (mc.thePlayer.motionZ * 8000);
                    } else {
                        veloPacket = true;
                    }
                    break;
                case "Jump Reset":
                    if (jumpResetMode.is("Packet") && s12.getMotionY() > 0) veloPacket = true;
                    break;
                case "Intave":
                    final float multiplier = mc.thePlayer.isSprinting() ? intaveXZOnSprintHit.get() : intaveXZOnHit.get();
                    s12.motionX *= multiplier;
                    s12.motionZ *= multiplier;
                    break;
                case "Cris":
                    event.setCancelled(true);
                    double motionX = s12.getMotionX() / 8000.0, motionY = s12.getMotionY() / 8000.0, motionZ = s12.getMotionZ() / 8000.0;
                    double spoofX = mc.thePlayer.posX + motionX, spoofY = mc.thePlayer.posY + motionY, spoofZ = mc.thePlayer.posZ + motionZ;
                    sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(spoofX, spoofY, spoofZ, false));
                    sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(spoofX, spoofY - 0.0784000015258789, spoofZ, false));
                    hasSpoofed = true;
                    break;
                case "GrimAC":
                    handleGrimACPacket(event, s12);
                    break;
            }
        }

        if (mode.is("Cris") && packet instanceof S32PacketConfirmTransaction && hasSpoofed) {
            event.setCancelled(true);
            hasSpoofed = false;
        }

        if (mode.is("Cris") && packet instanceof S27PacketExplosion explosion) {
            event.setCancelled(true);
            double motionX = explosion.getX(), motionY = explosion.getY(), motionZ = explosion.getZ();
            double spoofX = mc.thePlayer.posX + motionX, spoofY = mc.thePlayer.posY + motionY, spoofZ = mc.thePlayer.posZ + motionZ;
            sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(spoofX, spoofY, spoofZ, false));
            sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(spoofX, spoofY - 0.0784000015258789, spoofZ, false));
            hasSpoofed = true;
        }

        if (mode.is("GrimAC") && grimMode.is("1.17") && packet instanceof S19PacketEntityStatus s19 && s19.getEntity(mc.theWorld) == mc.thePlayer) {
            canCancel = true;
        }
    }

    private void handleGrimACPacket(PacketEvent event, S12PacketEntityVelocity s12) {
        switch (grimMode.get()) {
            case "Reduce":
                if (getModule(KillAura.class).target != null && !isEnabled(Scaffold.class)) {
                    event.setCancelled(true);
                    if (!mc.thePlayer.serverSprintState) PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                    for (int i = 0; i < 8; i++) {
                        sendPacketNoEvent(new C02PacketUseEntity(getModule(KillAura.class).target, C02PacketUseEntity.Action.ATTACK));
                        sendPacketNoEvent(new C0APacketAnimation());
                    }
                    double velocityX = s12.getMotionX() / 8000.0, velocityZ = s12.getMotionZ() / 8000.0;
                    if (MathHelper.sqrt_double(velocityX * velocityX + velocityZ * velocityZ) <= 3F) {
                        mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
                    } else {
                        mc.thePlayer.motionX = velocityX * 0.078;
                        mc.thePlayer.motionZ = velocityZ * 0.078;
                    }
                    mc.thePlayer.motionY = s12.getMotionY() / 8000.0;
                }
                break;
            case "1.17":
                if (canCancel) {
                    canCancel = false;
                    canSpoof = true;
                    event.setCancelled(true);
                }
                break;
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mode.is("Boost") && veloPacket) {
            idk++;
            if (idk >= reverseTick.get()) {
                MovementUtils.strafe(MovementUtils.getSpeed() * reverseStrength.get());
                veloPacket = false;
                idk = 0;
            }
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (mode.is("Jump Reset") && (jumpResetMode.is("Packet") && veloPacket || jumpResetMode.is("Hurt Time") && mc.thePlayer.hurtTime >= jumpResetHurtTime.get())) {
            if (mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown() && !checks()) {
                mc.thePlayer.jump();
                veloPacket = false;
            }
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (mode.is("Intave") && intaveJump.get() && mc.thePlayer.hurtTime == mc.thePlayer.maxHurtTime - 1 && intaveCounter++ % 2 == 0 && mc.thePlayer.onGround) {
            boolean shouldJump = !intaveSmartTrackMotion.get() || (Math.abs(mc.thePlayer.motionX) > 0.32 || Math.abs(mc.thePlayer.motionZ) > 0.32);
            if (shouldJump) {
                if (intaveJumpSmart.get()) {
                    EntityLivingBase target = getMouseClosestTarget((float) intaveMaxAngleSmart.get());
                    if (target != null && mc.thePlayer.getDistanceToEntity(target) > 2) {
                        event.setJumping(true);
                        intaveCounter = 0;
                    }
                } else {
                    event.setJumping(true);
                    intaveCounter = 0;
                }
            }
        }
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (mode.is("Zip") && zipIsDelayed && zipStopOnAttack.get()) {
            releaseZipPackets();
        }
    }

    private EntityLivingBase getMouseClosestTarget(float maxAngle) {
        return mc.theWorld.loadedEntityList.stream()
                .filter(e -> e instanceof EntityLivingBase && e != mc.thePlayer && !e.isDead && ((EntityLivingBase) e).getHealth() > 0)
                .map(e -> (EntityLivingBase) e)
                .min(Comparator.comparingDouble(e -> {
                    float[] angles = getRotationsToEntity(e);
                    float currentYaw = RotationUtils.currentRotation != null ? RotationUtils.currentRotation[0] : mc.thePlayer.rotationYaw;
                    return getAngleDifference(currentYaw, angles[0]);
                }))
                .filter(e -> {
                    float[] angles = getRotationsToEntity(e);
                    float currentYaw = RotationUtils.currentRotation != null ? RotationUtils.currentRotation[0] : mc.thePlayer.rotationYaw;
                    return getAngleDifference(currentYaw, angles[0]) < maxAngle;
                })
                .orElse(null);
    }

    private float[] getRotationsToEntity(EntityLivingBase entity) {
        double deltaX = entity.posX - mc.thePlayer.posX;
        double deltaY = entity.posY + entity.getEyeHeight() - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double deltaZ = entity.posZ - mc.thePlayer.posZ;
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = (float) (Math.atan2(deltaZ, deltaX) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(deltaY, distance) * 180.0D / Math.PI));
        return new float[]{yaw, pitch};
    }

    private float getAngleDifference(float angle1, float angle2) {
        return Math.abs(((angle1 - angle2) % 360.0F + 540.0F) % 360.0F - 180.0F);
    }

    private boolean checks() {
        return mc.thePlayer.isInWeb || mc.thePlayer.isInLava() || mc.thePlayer.isBurning() || mc.thePlayer.isInWater();
    }
}