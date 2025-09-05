package wtf.uwu.features.modules.impl.player;

import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;

@ModuleInfo(name = "Noclip", category = ModuleCategory.Player)
public class Noclip extends Module {

   /* private final BoolValue block = new BoolValue("Block", false, this);

    @Override
    public void onDisable() {
        mc.thePlayer.noClip = false;
    }

    @EventLink
    public final Listener<BlockAABBEvent> onBlockAABB = event -> {
        if (PlayerUtils.insideBlock()) {
            event.setBoundingBox(null);

            if (!(event.getBlock() instanceof BlockAir) && !mc.gameSettings.keyBindSneak.isKeyDown()) {
                final double x = event.getBlockPos().getX(), y = event.getBlockPos().getY(), z = event.getBlockPos().getZ();

                if (y < mc.thePlayer.posY) {
                    event.setBoundingBox(AxisAlignedBB.fromBounds(-15, -1, -15, 15, 1, 15).offset(x, y, z));
                }
            }
        }
    };

    @EventLink
    public final Listener<PushOutOfBlockEvent> onPushOutOfBlock = CancellableEvent::setCancelled;

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        mc.thePlayer.noClip = true;

        if (block.getValue()) {
            final int slot = SlotUtil.findBlock();

            if (slot == -1 || PlayerUtils.insideBlock()) {
                return;
            }

            getComponent(Slot.class).setSlot(slot);

            otationComponent.setRotations(new Vector2f(mc.thePlayer.rotationYaw, 90), 2 + Math.random(), MovementFix.NORMAL);

            if (RotationComponent.rotations.y >= 89 &&
                    mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK &&
                    mc.thePlayer.posY == mc.objectMouseOver.getBlockPos().up().getY()) {

                mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, getComponent(Slot.class).getItemStack(),
                        mc.objectMouseOver.getBlockPos(), mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec);

                mc.thePlayer.swingItem();
            }
        }
    }; */
}