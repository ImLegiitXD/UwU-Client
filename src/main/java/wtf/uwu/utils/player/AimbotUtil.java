package wtf.uwu.utils.player;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import wtf.uwu.utils.InstanceAccess;

public class AimbotUtil implements InstanceAccess {

    public static float[] getRotationsToEntity(EntityLivingBase entity) {
        return getRotationsToPosition(entity.getPositionEyes(1.0F));
    }

    public static float[] getRotationsToPosition(Vec3 targetPos) {
        Vec3 playerPos = mc.thePlayer.getPositionEyes(1.0F);

        double deltaX = targetPos.xCoord - playerPos.xCoord;
        double deltaY = targetPos.yCoord - playerPos.yCoord;
        double deltaZ = targetPos.zCoord - playerPos.zCoord;

        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float yaw = (float) (Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F);
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, distance));

        return new float[]{
                MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw)),
                mc.thePlayer.rotationPitch + (pitch - mc.thePlayer.rotationPitch)
        };
    }

    public static Vec3 getPredictedPosition(EntityLivingBase entity, double time) {
        if (entity == null) return null;

        double predX = entity.posX + (entity.motionX * time);
        double predY = entity.posY + (entity.motionY * time);
        double predZ = entity.posZ + (entity.motionZ * time);

        return new Vec3(predX, predY, predZ);
    }

    public static double getAngleDifference(EntityLivingBase entity) {
        float[] rotations = getRotationsToEntity(entity);
        float yawDiff = MathHelper.wrapAngleTo180_float(rotations[0] - mc.thePlayer.rotationYaw);
        return Math.abs(yawDiff);
    }

    public static boolean canSeeEntity(EntityLivingBase entity) {
        Vec3 eyePos = mc.thePlayer.getPositionEyes(1.0F);
        Vec3 targetPos = new Vec3(entity.posX, entity.posY + entity.getEyeHeight() / 2, entity.posZ);

        MovingObjectPosition rayTrace = mc.theWorld.rayTraceBlocks(eyePos, targetPos, false, true, false);
        return rayTrace == null || (rayTrace.entityHit != null && rayTrace.entityHit.equals(entity));
    }

    public static boolean isInFOV(EntityLivingBase entity, double fovAngle) {
        double angle = getAngleDifference(entity);
        return angle <= fovAngle / 2.0;
    }

    public static float[] applySmoothing(float[] currentRotations, float[] targetRotations, float smoothing) {
        if (smoothing <= 1.0f) {
            return targetRotations;
        }

        float yawDiff = MathHelper.wrapAngleTo180_float(targetRotations[0] - currentRotations[0]);
        float pitchDiff = targetRotations[1] - currentRotations[1];

        float newYaw = currentRotations[0] + yawDiff / smoothing;
        float newPitch = currentRotations[1] + pitchDiff / smoothing;

        return new float[]{newYaw, MathHelper.clamp_float(newPitch, -90.0F, 90.0F)};
    }

    public static boolean isTeamMate(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer && mc.thePlayer.getTeam() != null && ((EntityPlayer) entity).getTeam() != null) {
            return mc.thePlayer.getTeam().isSameTeam(((EntityPlayer) entity).getTeam());
        }
        return false;
    }
}