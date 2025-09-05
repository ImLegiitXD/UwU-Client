package wtf.uwu.features.modules.impl.movement;

import net.minecraft.block.BlockAir;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import org.lwjglx.input.Keyboard;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.misc.BlockAABBEvent;
import wtf.uwu.events.impl.packet.PacketEvent;
import wtf.uwu.events.impl.player.UpdateEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.features.values.impl.ModeValue;
import wtf.uwu.features.values.impl.SliderValue;
import wtf.uwu.utils.math.TimerUtils;
import wtf.uwu.utils.player.MovementUtils;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "Fly", category = ModuleCategory.Movement)
public class Fly extends Module {

    // --- CONFIGURACIÓN DE MODOS Y VALORES ---

    public final ModeValue mode = new ModeValue("Mode", new String[]{"Vanilla", "BlocksMC", "VerusJump", "HycraftDamage", "Polar"}, "Vanilla", this);

    // Vanilla mode settings
    private final SliderValue moveSpeed = new SliderValue("Speed", 2f, 1f, 10f, 0.1f, this, () -> mode.is("Vanilla"));
    private final SliderValue upSpeed = new SliderValue("Up Speed", 2f, 0.1f, 5f, 0.1f, this, () -> mode.is("Vanilla"));
    private final SliderValue downSpeed = new SliderValue("Down Speed", 2f, 0.1f, 5f, 0.1f, this, () -> mode.is("Vanilla"));

    // VerusJump settings
    private final BoolValue boostValue = new BoolValue("Boost", false, this, () -> mode.is("VerusJump"));
    private final SliderValue verusSpeed = new SliderValue("Verus Speed", 2f, 0f, 3f, 0.1f, this, () -> mode.is("VerusJump"));
    private final SliderValue boostLength = new SliderValue("Boost Time", 500f, 300f, 1000f, 50f, this, () -> mode.is("VerusJump") && boostValue.get());
    private final BoolValue moveBeforeDamage = new BoolValue("Move Before Damage", true, this, () -> mode.is("VerusJump"));
    private final BoolValue airStrafeValue = new BoolValue("Air Strafe", true, this, () -> mode.is("VerusJump") && !boostValue.get());

    // Polar settings
    private final SliderValue polarSpeed = new SliderValue("Speed", 0.1f, 0.01f, 0.5f, 0.01f, this, () -> mode.is("Polar"));
    private final BoolValue polarPacket = new BoolValue("Packet", false, this, () -> mode.is("Polar"));

    // HycraftDamage settings
    private final SliderValue hycraftSpeed = new SliderValue("Hycraft Speed", 0.28f, 0.1f, 1.0f, 0.01f, this, () -> mode.is("HycraftDamage"));

    // --- VARIABLES DE ESTADO PARA LOS MODOS ---

    private int ticks = 0;

    // BlocksMC variables
    private int blocksMCTicks = 0;
    private double floatPos = 0.0;

    // VerusJump variables
    private int verusTimes = 0;
    private TimerUtils verusTimer = new TimerUtils();
    private double launchY = 0.0;

    // Polar variables
    private int polarOffGroundTicks = 0;
    private boolean polarActive = false;

    // HycraftDamage variables
    private boolean hycraftDamageTaken = false;
    private boolean hycraftShouldDisable = false;
    private int hycraftTicks = 0;
    private final List<Packet<?>> hycraftPacketQueue = new ArrayList<>();

    // --- MÉTODOS onEnable Y onDisable ---

    @Override
    public void onEnable() {
        ticks = 0;
        if (mc.thePlayer != null) {
            floatPos = mc.thePlayer.posY;
            launchY = mc.thePlayer.posY;
        }

        // Reset de variables de cada modo
        blocksMCTicks = 0;
        verusTimes = 0;
        verusTimer.reset();
        polarOffGroundTicks = 0;
        polarActive = false;

        // Reset de HycraftDamage
        hycraftTicks = 0;
        hycraftDamageTaken = false;
        hycraftShouldDisable = false;
        hycraftPacketQueue.clear();
        if (mode.is("HycraftDamage") && mc.thePlayer != null) {
            mc.thePlayer.sendChatMessage("§8[§c!§8] §7Modo HycraftDamage: Espera a recibir daño para volar.");
        }

        setTag(mode.get());
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            mc.thePlayer.motionY = 0;
        }
        if (mc.timer != null) {
            mc.timer.timerSpeed = 1.0F;
        }

        // Liberar paquetes de HycraftDamage si quedaron en cola
        if (!hycraftPacketQueue.isEmpty()) {
            for (final Packet<?> packet : hycraftPacketQueue) {
                mc.getNetHandler().addToSendQueue(packet);
            }
            hycraftPacketQueue.clear();
        }

        // Resetear todas las variables de estado
        ticks = 0;
        blocksMCTicks = 0;
        verusTimes = 0;
        polarOffGroundTicks = 0;
        polarActive = false;
        hycraftDamageTaken = false;
        hycraftShouldDisable = false;
    }

    // --- MANEJO DE EVENTOS ---

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        setTag(mode.get());

        if (ticks > 0) {
            ticks--;
        }

        switch (mode.get()) {
            case "Vanilla":
                handleVanilla();
                break;
            case "BlocksMC":
                handleBlocksMC();
                break;
            case "VerusJump":
                handleVerusJump();
                break;
            case "HycraftDamage":
                handleHycraftDamage();
                break;
            case "Polar":
                handlePolar();
                break;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        Packet<?> packet = event.getPacket();

        // Lógica de paquetes para VerusJump
        if (mode.is("VerusJump") && boostValue.get()) {
            if (packet instanceof C03PacketPlayer) {
                C03PacketPlayer playerPacket = (C03PacketPlayer) packet;
                try {
                    java.lang.reflect.Field onGroundField = C03PacketPlayer.class.getDeclaredField("onGround");
                    onGroundField.setAccessible(true);
                    onGroundField.set(playerPacket, (verusTimes >= 5 && !verusTimer.hasTimeElapsed((long) boostLength.get())));
                } catch (Exception e) {
                    // Ignorar error
                }
            }
        }

        // Lógica de paquetes para Polar
        if (mode.is("Polar")) {
            if (packet instanceof S08PacketPlayerPosLook && !polarActive) {
                polarActive = true;
            }
        }

        // Lógica de paquetes para HycraftDamage
        if (mode.is("HycraftDamage")) {
            // Este bypass solo manipula paquetes recibidos del servidor
            if (event.getState() != PacketEvent.State.INCOMING) {
                return;
            }

            if (packet instanceof S12PacketEntityVelocity) {
                final S12PacketEntityVelocity velocityPacket = (S12PacketEntityVelocity) packet;
                if (velocityPacket.getEntityID() == mc.thePlayer.getEntityId()) {
                    hycraftDamageTaken = true;
                    hycraftShouldDisable = true;
                    hycraftTicks = 40; // Inicia 2 segundos de vuelo
                    return; // Dejamos que el paquete pase para recibir el impulso
                }
            }

            if (hycraftDamageTaken && hycraftTicks > 0) {
                if (packet instanceof S00PacketKeepAlive) {
                    event.setCancelled(true); // Congelamos el paquete de "ping"
                    hycraftPacketQueue.add(packet); // Lo guardamos para enviarlo después
                }
            }
        }
    }

    @EventTarget
    public void onBlockAABB(BlockAABBEvent event) {
        if (mode.is("VerusJump")) {
            if (event.getBlock() instanceof BlockAir && event.getBlockPos().getY() <= launchY) {
                event.setBoundingBox(AxisAlignedBB.fromBounds(
                        event.getBlockPos().getX(), event.getBlockPos().getY(), event.getBlockPos().getZ(),
                        event.getBlockPos().getX() + 1.0, launchY, event.getBlockPos().getZ() + 1.0
                ));
            }
        }
    }

    // --- MÉTODOS DE MANEJO PARA CADA MODO ---

    private void handleVanilla() {
        if (!mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.thePlayer.motionY = 0.0D;
        }
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.thePlayer.motionY = upSpeed.get();
        }
        if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            mc.thePlayer.motionY = -downSpeed.get();
        }
        MovementUtils.strafe(moveSpeed.get());
    }

    private void handleBlocksMC() {
        if (blocksMCTicks == 6) mc.thePlayer.posY = floatPos + 0.42D;
        if (mc.thePlayer.onGround && blocksMCTicks == 5) {
            blocksMCTicks++;
            if (MovementUtils.isMoving()) {
                mc.thePlayer.jump();
                MovementUtils.strafe(mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 0.6D : 0.49D);
            }
        } else if (!mc.thePlayer.onGround && blocksMCTicks == 5) this.toggle();
        else if (mc.thePlayer.offGroundTicks != 6) {
            if (mc.thePlayer.offGroundTicks == 1) {
                mc.timer.timerSpeed = 1.05F;
                MovementUtils.strafe(MovementUtils.getSpeed() * 1.08D);
            } else if (mc.thePlayer.offGroundTicks == 2) {
                mc.timer.timerSpeed = 1.15F;
                MovementUtils.strafe(MovementUtils.getSpeed() * 1.08D);
            } else if (mc.thePlayer.offGroundTicks == 3) {
                mc.timer.timerSpeed = 1.25F;
                MovementUtils.strafe(MovementUtils.getSpeed() * 1.06D);
            } else if (mc.thePlayer.offGroundTicks >= 4) {
                mc.timer.timerSpeed = 2.5F;
                MovementUtils.strafe(MovementUtils.getSpeed() * 1.02D);
            }
        }
        if (mc.thePlayer.offGroundTicks >= 10) this.toggle();
        if (blocksMCTicks < 5) blocksMCTicks++;
        if (blocksMCTicks < 4) {
            mc.timer.timerSpeed = 0.5F;
            mc.thePlayer.setSprinting(true);
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
        }
        MovementUtils.strafe();
        if (mc.thePlayer.hurtTime > 0) MovementUtils.strafe(0.4D);
    }

    private void handleVerusJump() {
        if (boostValue.get()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
            if (verusTimes < 5 && !moveBeforeDamage.get()) MovementUtils.strafe(0f);
            if (mc.thePlayer.onGround && verusTimes < 5) {
                verusTimes++;
                verusTimer.reset();
                if (verusTimes < 5) {
                    mc.thePlayer.jump();
                    MovementUtils.strafe(0.48f);
                }
            }
            if (verusTimes >= 5) {
                if (!verusTimer.hasTimeElapsed((long) boostLength.get())) MovementUtils.strafe(verusSpeed.get());
                else verusTimes = 0;
            }
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), GameSettings.isKeyDown(mc.gameSettings.keyBindJump));
            if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
                mc.thePlayer.jump();
                MovementUtils.strafe(0.48f);
            } else if (airStrafeValue.get()) {
                MovementUtils.strafe();
            }
        }
    }

    private void handlePolar() {
        if (mc.thePlayer.onGround) polarOffGroundTicks = 0;
        else polarOffGroundTicks++;
        if (polarActive) mc.timer.timerSpeed = 0.3F;
        if (mc.thePlayer.ticksExisted < 20) return;
        if (mc.thePlayer.onGround) {
            if (!mc.gameSettings.keyBindSneak.isKeyDown()) mc.thePlayer.jump();
        } else if (polarOffGroundTicks >= 9) {
            if (polarOffGroundTicks % 2 == 0) {
                if (polarPacket.get()) {
                    try {
                        mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                    } catch (Exception ignored) {}
                }
                double randomOffset = 0.09 + (Math.random() * 0.03);
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + polarSpeed.get(), mc.thePlayer.posZ + randomOffset);
            } else {
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + polarSpeed.get(), mc.thePlayer.posZ);
            }
            mc.thePlayer.motionY = 0.0;
            if (MovementUtils.isMoving() && polarActive) MovementUtils.strafe(polarSpeed.get() / 10);
            else if (!polarActive) MovementUtils.strafe(0);
        }
    }

    private void handleHycraftDamage() {
        if (hycraftTicks > 0) {
            hycraftTicks--;
            mc.thePlayer.motionY = 0; // Evita que caigamos
            MovementUtils.strafe(hycraftSpeed.get()); // Moverse a la velocidad configurada
        }

        if (hycraftShouldDisable && hycraftTicks <= 0) {
            this.toggle(); // Desactiva el módulo Fly automáticamente
        }
    }
}