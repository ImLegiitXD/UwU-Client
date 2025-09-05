package wtf.uwu.features.modules.impl.combat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MovingObjectPosition;
import org.lwjglx.input.Mouse;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.misc.TickEvent;
import wtf.uwu.events.impl.render.Render3DEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.features.values.impl.ModeValue;
import wtf.uwu.features.values.impl.SliderValue;
import wtf.uwu.utils.player.PlayerUtils;
import java.util.Random;
@ModuleInfo(name = "AutoClicker", category = ModuleCategory.Legit)
public class AutoClicker extends Module {
    private final ModeValue mode = new ModeValue("Mode", new String[]{"Legit", "Blatant"}, "Legit", this);
    private final ModeValue randomize = new ModeValue("Randomize", new String[]{"ButterFly", "Jitter", "Drag"}, "ButterFly", this);
    private final SliderValue minCPS = new SliderValue("Min CPS", 9.0F, 1.0F, 25.0F, 1.0F, this);
    private final SliderValue maxCPS = new SliderValue("Max CPS", 13.0F, 1.0F, 25.0F, 1.0F, this);
    private final BoolValue inventory = new BoolValue("Inventory", false, this);
    private final BoolValue onlyWeapon = new BoolValue("Only Weapon", false, this);
    private final BoolValue breakBlocks = new BoolValue("Break Blocks", false, this);
    private final Random random = new Random();
    private long lastClick;
    private long nextReleaseTime;
    private boolean leftDown;
    private long leftDownTime;
    private long leftUpTime;
    private long leftK;
    private long leftL;
    private double leftM;
    private boolean leftN;
    private int renderTicks = 0;

    @Override
    public void onEnable() {
        correctValues();
        setTag(mode.get());
    }

    @Override
    public void onDisable() {
        leftDownTime = leftUpTime = 0L;
        leftDown = false;
    }

    @EventTarget
    public void onTick(TickEvent event) {
        correctValues();
        setTag(mode.get());

        if (mc.currentScreen != null && !randomize.is("Jitter")) {
            return;
        }

        if (randomize.is("Jitter")) {
            handleClick();
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (mc.currentScreen != null) {
            return;
        }

        renderTicks++;

        if (randomize.is("ButterFly")) {
            handleClick();
        } else if (randomize.is("Drag") && renderTicks % 2 == 0) {
            handleClick();
        }
    }

    private void handleClick() {
        if (mode.is("Legit")) {
            ravenClick();
        } else {
            skidClick();
        }
    }

    private void skidClick() {
        if (!PlayerUtils.nullCheck() || !Mouse.isButtonDown(0) || shouldBreakBlock()) {
            return;
        }

        if (onlyWeapon.get() && !isSword()) {
            return;
        }

        double interval = 1.0D / randomDouble(minCPS.get() - 0.2D, maxCPS.get());
        double hold = interval / randomDouble(minCPS.get() - 0.02D, maxCPS.get());
        long currentTime = System.currentTimeMillis();

        if ((currentTime - lastClick) > interval * 1000.0D) {
            lastClick = currentTime;
            if (nextReleaseTime < lastClick) {
                nextReleaseTime = lastClick;
            }
            click(true);
        } else if ((currentTime - nextReleaseTime) > hold * 1000.0D) {
            click(false);
        }
    }

    private void ravenClick() {
        Mouse.poll();

        if (!Mouse.isButtonDown(0) && !leftDown) {
            click(false);
            return;
        }

        if (Mouse.isButtonDown(0) || leftDown) {
            if (onlyWeapon.get() && !isSword()) {
                return;
            }
            leftClickExecute();
        }
    }

    private void leftClickExecute() {
        if (shouldBreakBlock()) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        if (leftUpTime > 0L && leftDownTime > 0L) {
            if (currentTime > leftUpTime && leftDown) {
                click(true);
                genLeftTimings();
                leftDown = false;
            } else if (currentTime > leftDownTime) {
                click(false);
                leftDown = true;
            }
        } else {
            genLeftTimings();
        }
    }

    private void genLeftTimings() {
        double cps = ranModuleVal(minCPS.get(), maxCPS.get()) + 0.4D * random.nextDouble();
        long delay = (long)(1000.0D / cps);
        long currentTime = System.currentTimeMillis();

        if (currentTime > leftK) {
            leftN = (random.nextInt(100) >= 85);
            if (leftN) {
                leftM = 1.1D + random.nextDouble() * 0.15D;
            }
            leftK = currentTime + 500L + random.nextInt(1500);
        }

        if (leftN) {
            delay = (long)(delay * leftM);
        }

        if (currentTime > leftL) {
            if (random.nextInt(100) >= 80) {
                delay += (50 + random.nextInt(100));
            }
            leftL = currentTime + 500L + random.nextInt(1500);
        }

        leftUpTime = currentTime + delay;
        leftDownTime = currentTime + delay / 2L - random.nextInt(10);
    }

    private void click(boolean press) {
        int key = mc.gameSettings.keyBindAttack.getKeyCode();
        KeyBinding.setKeyBindState(key, press);

        if (press) {
            KeyBinding.onTick(key);
            mc.thePlayer.swingItem();
            if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null) {
                mc.playerController.attackEntity(mc.thePlayer, mc.objectMouseOver.entityHit);
            }
        }
    }

    private boolean shouldBreakBlock() {
        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (!breakBlocks.get()) {
                return true;
            }
        }
        return false;
    }

    private boolean isSword() {
        return mc.thePlayer.getHeldItem() != null &&
                mc.thePlayer.getHeldItem().getItem() instanceof net.minecraft.item.ItemSword;
    }

    private double ranModuleVal(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    private double randomDouble(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    private void correctValues() {
        if (minCPS.get() > maxCPS.get()) {
        }
    }
}