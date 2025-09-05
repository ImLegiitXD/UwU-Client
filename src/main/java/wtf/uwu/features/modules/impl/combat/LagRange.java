package wtf.uwu.features.modules.impl.combat;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.misc.WorldEvent;
import wtf.uwu.events.impl.packet.PacketEvent;
import wtf.uwu.events.impl.player.UpdateEvent;
import wtf.uwu.events.impl.render.Render3DEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.features.values.impl.ColorValue;
import wtf.uwu.features.values.impl.SliderValue;
import wtf.uwu.utils.math.TimerUtils;

import java.awt.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@ModuleInfo(name = "LagRange", category = ModuleCategory.Combat)
public class LagRange extends Module {

    private final SliderValue delay = new SliderValue("Delay", 550, 0, 1000, 1, this);
    private final SliderValue recoilTime = new SliderValue("Recoil Time", 750, 0, 2000, 1, this);

    private final SliderValue minDistToEnemy = new SliderValue("Min Dist To Enemy", 1.5F, 0F, 6F, 0.1F, this);
    private final SliderValue maxDistToEnemy = new SliderValue("Max Dist To Enemy", 3.5F, 0F, 6F, 0.1F, this);

    private final BoolValue blinkOnAction = new BoolValue("Blink On Action", true, this);
    private final BoolValue pauseOnNoMove = new BoolValue("Pause On No Move", true, this);
    private final BoolValue pauseOnChest = new BoolValue("Pause On Chest", false, this);

    private final BoolValue line = new BoolValue("Line", true, this);
    private final ColorValue lineColor = new ColorValue("Line Color", Color.GREEN, this);

    private final BoolValue renderModel = new BoolValue("Render Model", true, this);

    private final ConcurrentLinkedQueue<QueueData> packetQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<PositionData> positions = new ConcurrentLinkedQueue<>();
    private final TimerUtils resetTimer = new TimerUtils();

    private boolean wasNearEnemy = false;
    private boolean ignoreWholeTick = false;

    private Vec3 renderPos = new Vec3(0, 0, 0);
    private float renderYaw = 0f;
    private float renderPitch = 0f;

    @Override
    public void onEnable() {
        resetTimer.reset();
        packetQueue.clear();
        positions.clear();
        wasNearEnemy = false;
        ignoreWholeTick = false;

        if (mc.thePlayer != null) {
            renderPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            renderYaw = mc.thePlayer.rotationYaw;
            renderPitch = mc.thePlayer.rotationPitch;
        }
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            blink();
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || !isEnabled() || player.isDead || event.isCancelled() ||
                (maxDistToEnemy.get() > 0.0 && wasNearEnemy) || ignoreWholeTick) {
            return;
        }

        Packet packet = event.getPacket();

        if (pauseOnNoMove.get() && !isPlayerMoving(player)) {
            blink();
            return;
        }

        if (player.getHealth() < player.getMaxHealth() && player.hurtTime != 0) {
            blink();
            return;
        }

        if (blinkOnAction.get() && packet instanceof C02PacketUseEntity) {
            blink();
            return;
        }

        if (pauseOnChest.get() && mc.currentScreen instanceof GuiContainer) {
            blink();
            return;
        }

        if (packet instanceof C00Handshake || packet instanceof C00PacketServerQuery ||
                packet instanceof C01PacketPing || packet instanceof C01PacketChatMessage ||
                packet instanceof S01PacketPong) {
            return;
        }

        if (packet instanceof C0EPacketClickWindow || packet instanceof C0DPacketCloseWindow) {
            blink();
            return;
        }

        if (packet instanceof S08PacketPlayerPosLook || packet instanceof C08PacketPlayerBlockPlacement ||
                packet instanceof C07PacketPlayerDigging || packet instanceof C12PacketUpdateSign) {
            blink();
            return;
        }

        if (packet instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity velPacket = (S12PacketEntityVelocity) packet;
            if (player.getEntityId() == velPacket.getEntityID()) {
                blink();
                return;
            }
        }

        if (packet instanceof S27PacketExplosion) {
            S27PacketExplosion expPacket = (S27PacketExplosion) packet;
            if (expPacket.getX() != 0f || expPacket.getY() != 0f || expPacket.getZ() != 0f) {
                blink();
                return;
            }
        }

        if (!resetTimer.hasTimeElapsed((long) recoilTime.get())) {
            return;
        }

        if (mc.isSingleplayer() || mc.getCurrentServerData() == null) {
            blink();
            return;
        }

        if (event.getState() == PacketEvent.State.OUTGOING) {
            event.setCancelled(true);

            if (packet instanceof C03PacketPlayer) {
                C03PacketPlayer playerPacket = (C03PacketPlayer) packet;
                if (playerPacket.isMoving()) {
                    synchronized (positions) {
                        positions.add(new PositionData(
                                new Vec3(playerPacket.getPositionX(), playerPacket.getPositionY(), playerPacket.getPositionZ()),
                                System.currentTimeMillis(),
                                player.renderYawOffset,
                                player.rotationYaw,
                                player.rotationPitch
                        ));
                    }
                }
            }

            synchronized (packetQueue) {
                packetQueue.add(new QueueData(packet, System.currentTimeMillis()));
            }
        }
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        if (event.newWorld() == null) {
            blink(false);
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || mc.theWorld == null) return;

        setTag(packetQueue.size() + " packets");

        if (maxDistToEnemy.get() > 0) {
            Vec3 playerPos = new Vec3(player.posX, player.posY, player.posZ);
            Vec3 serverPos = positions.isEmpty() ? playerPos : positions.peek().pos;

            AxisAlignedBB playerBox = player.getEntityBoundingBox().offset(
                    serverPos.xCoord - playerPos.xCoord,
                    serverPos.yCoord - playerPos.yCoord,
                    serverPos.zCoord - playerPos.zCoord
            );

            wasNearEnemy = false;

            for (EntityPlayer otherPlayer : mc.theWorld.playerEntities) {
                if (otherPlayer == player) continue;

                Vec3 eyes = new Vec3(otherPlayer.posX, otherPlayer.posY + otherPlayer.getEyeHeight(), otherPlayer.posZ);
                double distance = eyes.distanceTo(getNearestPointBB(eyes, playerBox));

                if (distance >= minDistToEnemy.get() && distance <= maxDistToEnemy.get()) {
                    blink();
                    wasNearEnemy = true;
                    return;
                }
            }
        }

        if (player.isDead || player.isUsingItem()) {
            blink();
            return;
        }

        if (!resetTimer.hasTimeElapsed((long) recoilTime.get())) return;

        handlePackets();
        ignoreWholeTick = false;
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || positions.isEmpty()) {
            updateRenderData(player);
            return;
        }

        updateRenderData(null);

        if (line.get()) {
            renderLine();
        }

        if (renderModel.get() && mc.gameSettings.thirdPersonView != 0) {
            renderModel();
        }
    }

    private void renderLine() {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glBegin(GL11.GL_LINE_STRIP);

        Color color = lineColor.get();
        GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

        double renderPosX = mc.getRenderManager().viewerPosX;
        double renderPosY = mc.getRenderManager().viewerPosY;
        double renderPosZ = mc.getRenderManager().viewerPosZ;

        for (PositionData posData : positions) {
            Vec3 pos = posData.pos;
            GL11.glVertex3d(pos.xCoord - renderPosX, pos.yCoord - renderPosY, pos.zCoord - renderPosZ);
        }

        GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private void renderModel() {
        if (positions.isEmpty()) return;

        double x = renderPos.xCoord - mc.getRenderManager().viewerPosX;
        double y = renderPos.yCoord - mc.getRenderManager().viewerPosY;
        double z = renderPos.zCoord - mc.getRenderManager().viewerPosZ;

        AxisAlignedBB box = mc.thePlayer.getEntityBoundingBox().expand(0.1D, 0.1, 0.1);
        AxisAlignedBB axis = new AxisAlignedBB(
                box.minX - mc.thePlayer.posX + x,
                box.minY - mc.thePlayer.posY + y,
                box.minZ - mc.thePlayer.posZ + z,
                box.maxX - mc.thePlayer.posX + x,
                box.maxY - mc.thePlayer.posY + y,
                box.maxZ - mc.thePlayer.posZ + z
        );

        GL11.glPushMatrix();
        GL11.glColor4f(0f, 0f, 0f, 0.6f);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(axis.minX, axis.minY, axis.minZ);
        GL11.glVertex3d(axis.maxX, axis.minY, axis.minZ);
        GL11.glVertex3d(axis.maxX, axis.minY, axis.minZ);
        GL11.glVertex3d(axis.maxX, axis.minY, axis.maxZ);
        GL11.glVertex3d(axis.maxX, axis.minY, axis.maxZ);
        GL11.glVertex3d(axis.minX, axis.minY, axis.maxZ);
        GL11.glVertex3d(axis.minX, axis.minY, axis.maxZ);
        GL11.glVertex3d(axis.minX, axis.minY, axis.minZ);

        GL11.glVertex3d(axis.minX, axis.maxY, axis.minZ);
        GL11.glVertex3d(axis.maxX, axis.maxY, axis.minZ);
        GL11.glVertex3d(axis.maxX, axis.maxY, axis.minZ);
        GL11.glVertex3d(axis.maxX, axis.maxY, axis.maxZ);
        GL11.glVertex3d(axis.maxX, axis.maxY, axis.maxZ);
        GL11.glVertex3d(axis.minX, axis.maxY, axis.maxZ);
        GL11.glVertex3d(axis.minX, axis.maxY, axis.maxZ);
        GL11.glVertex3d(axis.minX, axis.maxY, axis.minZ);

        GL11.glVertex3d(axis.minX, axis.minY, axis.minZ);
        GL11.glVertex3d(axis.minX, axis.maxY, axis.minZ);
        GL11.glVertex3d(axis.maxX, axis.minY, axis.minZ);
        GL11.glVertex3d(axis.maxX, axis.maxY, axis.minZ);
        GL11.glVertex3d(axis.maxX, axis.minY, axis.maxZ);
        GL11.glVertex3d(axis.maxX, axis.maxY, axis.maxZ);
        GL11.glVertex3d(axis.minX, axis.minY, axis.maxZ);
        GL11.glVertex3d(axis.minX, axis.maxY, axis.maxZ);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glPopMatrix();
    }

    private void updateRenderData(EntityPlayerSP player) {
        if (player != null) {
            renderPos = new Vec3(player.posX, player.posY, player.posZ);
            renderYaw = player.rotationYaw;
            renderPitch = player.rotationPitch;
        } else if (!positions.isEmpty()) {
            PositionData data = positions.peek();
            renderPos = lerpVec3(renderPos, data.pos, 0.1);
            renderYaw = lerpFloat(renderYaw, data.yaw, 0.1f);
            renderPitch = lerpFloat(renderPitch, data.pitch, 0.1f);
        }
    }

    private Vec3 lerpVec3(Vec3 from, Vec3 to, double factor) {
        return new Vec3(
                from.xCoord + (to.xCoord - from.xCoord) * factor,
                from.yCoord + (to.yCoord - from.yCoord) * factor,
                from.zCoord + (to.zCoord - from.zCoord) * factor
        );
    }

    private float lerpFloat(float from, float to, float factor) {
        return from + (to - from) * factor;
    }

    private boolean isPlayerMoving(EntityPlayerSP player) {
        return player.movementInput.moveForward != 0 || player.movementInput.moveStrafe != 0;
    }

    private Vec3 getNearestPointBB(Vec3 eye, AxisAlignedBB box) {
        double x = Math.max(box.minX, Math.min(eye.xCoord, box.maxX));
        double y = Math.max(box.minY, Math.min(eye.yCoord, box.maxY));
        double z = Math.max(box.minZ, Math.min(eye.zCoord, box.maxZ));
        return new Vec3(x, y, z);
    }

    private void blink() {
        blink(true);
    }

    private void blink(boolean handlePackets) {
        mc.addScheduledTask(() -> {
            if (handlePackets) {
                resetTimer.reset();
            }
            handlePackets(true);
            ignoreWholeTick = true;
        });
    }

    private void handlePackets() {
        handlePackets(false);
    }

    private void handlePackets(boolean clear) {
        synchronized (packetQueue) {
            packetQueue.removeIf(queueData -> {
                if (queueData.timestamp <= System.currentTimeMillis() - (long) delay.get() || clear) {
                    sendPacket(queueData.packet);
                    return true;
                }
                return false;
            });
        }

        synchronized (positions) {
            positions.removeIf(posData ->
                    posData.time <= System.currentTimeMillis() - (long) delay.get() || clear);
        }
    }

    private static class QueueData {
        public final Packet packet;
        public final long timestamp;

        public QueueData(Packet packet, long timestamp) {
            this.packet = packet;
            this.timestamp = timestamp;
        }
    }

    private static class PositionData {
        public final Vec3 pos;
        public final long time;
        public final float body;
        public final float yaw;
        public final float pitch;

        public PositionData(Vec3 pos, long time, float body, float yaw, float pitch) {
            this.pos = pos;
            this.time = time;
            this.body = body;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }
}