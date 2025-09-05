package wtf.uwu.features.modules.impl.movement;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemBucketMilk;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.player.MotionEvent;
import wtf.uwu.events.impl.player.SlowDownEvent;
import wtf.uwu.events.impl.player.UpdateEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.features.values.impl.ModeValue;
import wtf.uwu.features.values.impl.SliderValue;

@ModuleInfo(name = "NoSlow", category = ModuleCategory.Movement)
public class NoSlowdown extends Module {

    public final BoolValue onlyWhileMove = new BoolValue("OnlyWhileMove", true, this);
    public final ModeValue switchMode = new ModeValue("SwitchMode", new String[]{"PreAttack", "PostAttack", "PrePosition", "PostPosition", "PrePostPosition"}, "PrePosition", this);
    public final ModeValue spamMode = new ModeValue("SpamMode", new String[]{"PreAttack", "PostAttack", "PrePosition", "PostPosition", "PrePostPosition"}, "PrePosition", this);

    public final BoolValue swordEnable = new BoolValue("Enable For Swords", true, this);
    public final BoolValue swordSlowdown = new BoolValue("Sword Slowdown", false, this, swordEnable::get);
    public final BoolValue swordSprint = new BoolValue("Sword Sprint", true, this, swordEnable::get);
    public final BoolValue swordSwitch = new BoolValue("Sword Switch", false, this, swordEnable::get);
    public final BoolValue swordSpam = new BoolValue("Sword Spam", false, this, swordEnable::get);
    public final BoolValue swordPolar = new BoolValue("Sword Polar", false, this, swordEnable::get);
    public final BoolValue swordToggle = new BoolValue("Sword Toggle", false, this, swordEnable::get);
    public final SliderValue swordForward = new SliderValue("SwordForward", 0.2f, 0.2f, 1.0f, 0.01f, this, swordSlowdown::get);
    public final SliderValue swordStrafe = new SliderValue("SwordStrafe", 0.2f, 0.2f, 1.0f, 0.01f, this, swordSlowdown::get);

    public final BoolValue bowEnable = new BoolValue("Enable For Bows", true, this);
    public final BoolValue bowSlowdown = new BoolValue("Bow Slowdown", false, this, bowEnable::get);
    public final BoolValue bowSprint = new BoolValue("Bow Sprint", true, this, bowEnable::get);
    public final BoolValue bowSwitch = new BoolValue("Bow Switch", false, this, bowEnable::get);
    public final SliderValue bowForward = new SliderValue("BowForward", 0.2f, 0.2f, 1.0f, 0.01f, this, bowSlowdown::get);
    public final SliderValue bowStrafe = new SliderValue("BowStrafe", 0.2f, 0.2f, 1.0f, 0.01f, this, bowSlowdown::get);

    public final BoolValue restEnable = new BoolValue("Enable For Consumables", true, this);
    public final BoolValue restSlowdown = new BoolValue("Rest Slowdown", false, this, restEnable::get);
    public final BoolValue restSprint = new BoolValue("Rest Sprint", true, this, restEnable::get);
    public final BoolValue restSwitch = new BoolValue("Rest Switch", false, this, restEnable::get);
    public final BoolValue restSpam = new BoolValue("Rest Spam", false, this, restEnable::get);
    public final BoolValue restPolar = new BoolValue("Rest Polar", false, this, restEnable::get);
    public final BoolValue restFood = new BoolValue("Rest Food", true, this, restEnable::get);
    public final BoolValue restPotion = new BoolValue("Rest Potion", true, this, restEnable::get);
    public final BoolValue restBucketMilk = new BoolValue("Rest BucketMilk", true, this, restEnable::get);
    public final SliderValue maxItemInUseDurationPackets = new SliderValue("MaxItemInUseDurationPackets", 32, 0, 32, 1, this, restEnable::get);
    public final SliderValue restForward = new SliderValue("RestForward", 0.2f, 0.2f, 1.0f, 0.01f, this, restSlowdown::get);
    public final SliderValue restStrafe = new SliderValue("RestStrafe", 0.2f, 0.2f, 1.0f, 0.01f, this, restSlowdown::get);

    @EventTarget
    public void onNoSlow(SlowDownEvent event) {
        if (!isUsingItem() || !onlyMoveCheck()) return;

        ItemStack currentItem = mc.thePlayer.getHeldItem();
        Item itemInUse = currentItem.getItem();

        if (itemInUse instanceof ItemSword && swordEnable.get()) {
            event.setSprinting(swordSprint.get());
            if (swordSlowdown.get()) {
                event.setForward(swordForward.get());
                event.setStrafe(swordStrafe.get());
            }
        }
        else if (itemInUse instanceof ItemBow && bowEnable.get()) {
            event.setSprinting(bowSprint.get());
            if (bowSlowdown.get()) {
                event.setForward(bowForward.get());
                event.setStrafe(bowStrafe.get());
            }
        }
        else if (restEnable.get()) {
            if (!correctRestItem(itemInUse)) return;
            event.setSprinting(restSprint.get());
            if (restSlowdown.get()) {
                event.setForward(restForward.get());
                event.setStrafe(restStrafe.get());
            }
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!isUsingItem() || !onlyMoveCheck()) return;

        if (event.isPre()) {
            handlePacketLogic("PreAttack");
        } else if (event.isPost()) {
            handlePacketLogic("PostAttack");
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (!isUsingItem() || !onlyMoveCheck()) return;

        if (event.isPre()) {
            handlePacketLogic("PrePosition");
        } else if (event.isPost()) {
            handlePacketLogic("PostPosition");
        }
    }

    private void handlePacketLogic(String timing) {
        ItemStack currentItem = mc.thePlayer.getHeldItem();
        Item itemInUse = currentItem.getItem();

        boolean isSwitchTime = switchMode.is(timing) || (switchMode.is("PrePostPosition") && (timing.equals("PrePosition") || timing.equals("PostPosition")));
        boolean isSpamTime = spamMode.is(timing) || (spamMode.is("PrePostPosition") && (timing.equals("PrePosition") || timing.equals("PostPosition")))
                || (spamMode.is("PrePostAttack") && (timing.equals("PreAttack") || timing.equals("PostAttack")));

        if (itemInUse instanceof ItemSword && swordEnable.get()) {
            if (swordSwitch.get() && isSwitchTime) {
                switchItemLogic();
            }
            if (swordSpam.get() && isSpamTime) {
                sendPacketNoEvent(new C08PacketPlayerBlockPlacement(currentItem));
            }
            if (swordToggle.get()) {
                if (timing.equals("PrePosition")) {
                    sendPacketNoEvent(new C07PacketPlayerDigging(Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                } else if (timing.equals("PostPosition")) {
                    sendPacketNoEvent(new C08PacketPlayerBlockPlacement(currentItem));
                }
            }
            if (swordPolar.get() && timing.equals("PostPosition")) {
                polarMethod(currentItem);
            }
        }
        else if (itemInUse instanceof ItemBow && bowEnable.get()) {
            if (bowSwitch.get() && isSwitchTime) {
                switchItemLogic();
            }
        }
        else if (restEnable.get()) {
            if (!isWithinMaxItemDuration() || !correctRestItem(itemInUse)) return;

            if (restSwitch.get() && isSwitchTime) {
                switchItemLogic();
            }
            if (restSpam.get() && isSpamTime) {
                sendPacketNoEvent(new C08PacketPlayerBlockPlacement(currentItem));
            }
            if (restPolar.get() && timing.equals("PostPosition")) {
                polarMethod(currentItem);
            }
        }
    }

    private void switchItemLogic() {
        int currentSlot = mc.thePlayer.inventory.currentItem;
        int slotToSwitch = currentSlot >= 7 ? currentSlot - 2 : currentSlot + 2;
        sendPacketNoEvent(new C09PacketHeldItemChange(slotToSwitch));
        sendPacketNoEvent(new C09PacketHeldItemChange(currentSlot));
    }

    private void polarMethod(ItemStack currentItem) {
        int currentSlot = mc.thePlayer.inventory.currentItem;
        int nextSlot = (currentSlot + 1) % 9;
        sendPacketNoEvent(new C07PacketPlayerDigging(Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        sendPacketNoEvent(new C09PacketHeldItemChange(nextSlot));
        sendPacketNoEvent(new C09PacketHeldItemChange(currentSlot));
        sendPacketNoEvent(new C08PacketPlayerBlockPlacement(
                BlockPos.ORIGIN, 255, currentItem, 0.5F, 0.5F, 0.5F
        ));
    }

    private boolean isUsingItem() {
        return mc.thePlayer != null && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem() != null;
    }

    private boolean isWithinMaxItemDuration() {
        int itemUseDuration = mc.thePlayer.getItemInUseDuration();
        return itemUseDuration <= maxItemInUseDurationPackets.get();
    }

    private boolean correctRestItem(Item currentItem) {
        if (currentItem == null) return false;
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (heldItem == null) return false;

        boolean isConsumablePotion = currentItem instanceof ItemPotion && !ItemPotion.isSplash(heldItem.getMetadata());

        return (restFood.get() && currentItem instanceof ItemFood) ||
                (restPotion.get() && isConsumablePotion) ||
                (restBucketMilk.get() && currentItem instanceof ItemBucketMilk);
    }

    private boolean onlyMoveCheck() {
        return !onlyWhileMove.get() || (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f);
    }
}