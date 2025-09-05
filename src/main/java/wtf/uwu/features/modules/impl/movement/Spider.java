package wtf.uwu.features.modules.impl.movement;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.player.UpdateEvent;
import wtf.uwu.events.impl.misc.BlockAABBEvent;
import wtf.uwu.events.impl.misc.PushOutOfBlockEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.ModeValue;
import wtf.uwu.features.values.impl.SliderValue;
import wtf.uwu.utils.player.MovementUtils;

@ModuleInfo(name = "Spider", category = ModuleCategory.Movement)
public class Spider extends Module {

    private final ModeValue mode = new ModeValue("Mode", new String[]{"Vanilla", "Polar", "PolarSpider"}, "Vanilla", this);
    private final SliderValue climbSpeed = new SliderValue("Climb Speed", 0.2f, 0.05f, 1.0f, 0.05f, this, () -> mode.is("Vanilla"));

    @EventTarget
    public void onUpdate(UpdateEvent e) {
        if (!isEnabled() || mc.thePlayer == null || mc.theWorld == null) return;

        switch (mode.get()) {
            case "Vanilla":
                handleVanilla();
                break;
            case "Polar":
                handlePolar();
                break;
            case "PolarSpider":
                handlePolarSpider();
                break;
        }
    }

    @EventTarget
    public void onBlockAABB(BlockAABBEvent event) {
        if (!isEnabled() || mc.thePlayer == null) return;

        if (mode.is("Polar") && isInsideBlock()) {
            BlockPos playerPos = new BlockPos(mc.thePlayer);
            BlockPos blockPos = event.getBlockPos();

            // Nullify bounding box for blocks above the player when inside a block
            if (blockPos.getY() > playerPos.getY()) {
                event.setBoundingBox(null);
            }
        } else if (mode.is("PolarSpider")) {
            // Simular el comportamiento del CollideEvent usando BlockAABB
            EntityPlayerSP player = mc.thePlayer;
            AxisAlignedBB bb = event.getBoundingBox();

            if (bb != null && event.getBlockPos().getY() >= (int) player.posY) {
                if (!player.isSneaking() || !player.onGround) {
                    event.setBoundingBox(new AxisAlignedBB(
                            bb.minX + 0.0001, bb.minY, bb.minZ + 0.0001,
                            bb.maxX - 0.0001, bb.maxY, bb.maxZ - 0.0001
                    ));
                }
            }
        }
    }

    @EventTarget
    public void onPushOutOfBlock(PushOutOfBlockEvent event) {
        if (!isEnabled() || mc.thePlayer == null || !mode.is("Polar")) return;

        // Cancel the push out of block event when inside a block
        if (isInsideBlock()) {
            event.setCancelled(true);
        }
    }

    private void handleVanilla() {
        if (mc.thePlayer.isCollidedHorizontally && mc.thePlayer.onGround) {
            mc.thePlayer.motionY = climbSpeed.get();
        }
    }

    private void handlePolar() {
        if (mc.thePlayer.isCollidedHorizontally && !isInsideBlock()) {
            double yaw = getDirection();
            mc.thePlayer.setPosition(
                    mc.thePlayer.posX + -MathHelper.sin((float) yaw) * 0.05,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ + MathHelper.cos((float) yaw) * 0.05
            );
            MovementUtils.stop();
        }
    }

    private void handlePolarSpider() {
        EntityPlayerSP player = mc.thePlayer;

        // Simular el comportamiento del MotionEvent
        if (player.isCollidedHorizontally && !player.isSneaking()) {
            if (player.motionY < 0.2) {
                player.motionY = 0.2;
            }
            // Simular setGround(true) - forzar que el jugador esté en el suelo para efectos de movimiento
            player.onGround = true;
        }
    }

    private double getDirection() {
        float yaw = mc.thePlayer.rotationYaw;
        if (mc.thePlayer.moveForward < 0f) yaw += 180f;

        float forward = 1f;
        if (mc.thePlayer.moveForward < 0f) forward = -0.5f;
        else if (mc.thePlayer.moveForward > 0f) forward = 0.5f;

        if (mc.thePlayer.moveStrafing > 0f) yaw -= 90f * forward;
        if (mc.thePlayer.moveStrafing < 0f) yaw += 90f * forward;

        return Math.toRadians(yaw);
    }

    private boolean isInsideBlock() {
        for (int x = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minX); x < MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().maxX) + 1; x++) {
            for (int y = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minY); y < MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().maxY) + 1; y++) {
                for (int z = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minZ); z < MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().maxZ) + 1; z++) {
                    if (mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock().isCollidable()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        // Aplicar la lógica de onDisable del PolarSpider cuando se desactiva en ese modo
        if (mode.is("PolarSpider") && mc.thePlayer != null) {
            mc.thePlayer.motionY = Math.min(mc.thePlayer.motionY, 0);
        }
    }
}