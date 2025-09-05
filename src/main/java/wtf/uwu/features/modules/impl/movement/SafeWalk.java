package wtf.uwu.features.modules.impl.movement;

import net.minecraft.block.BlockAir;
import net.minecraft.item.ItemBlock;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.player.MoveInputEvent;
import wtf.uwu.events.impl.player.SafeWalkEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.features.values.impl.ModeValue;
import wtf.uwu.features.values.impl.SliderValue;
import wtf.uwu.utils.math.MathUtils;
import wtf.uwu.utils.player.PlayerUtils;

@ModuleInfo(name = "Legitscaffold", category = ModuleCategory.Movement)
public class SafeWalk extends Module {

    public final ModeValue mode = new ModeValue("Mode", new String[]{"Safe", "Sneak"}, "Safe", this);
    private final BoolValue heldBlocks = new BoolValue("Held Blocks Check", true, this);
    private final BoolValue pitchCheck = new BoolValue("Pitch Check", true, this);
    private final SliderValue minPitch = new SliderValue("Min Pitch", 55, 50, 90, 1, this, pitchCheck::get);
    public final SliderValue maxPitch = new SliderValue("Max Pitch", 75, 50, 90, 1, this, pitchCheck::get);

    @EventTarget
    public void onSafeWalk(SafeWalkEvent event) {
        if (canSafeWalk() && mode.is("Safe"))
            event.setCancelled(true);
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (canSafeWalk() && mode.is("Sneak") && PlayerUtils.blockRelativeToPlayer(0, -1, 0) instanceof BlockAir)
            event.setSneaking(true);
    }

    public boolean canSafeWalk() {
        return mc.thePlayer.onGround && (heldBlocks.get() && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock || !heldBlocks.get()) && (pitchCheck.get() && MathUtils.inBetween(minPitch.getMin(), maxPitch.getMax(), mc.thePlayer.rotationPitch) || !pitchCheck.get());
    }
}