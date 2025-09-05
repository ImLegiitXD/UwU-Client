package wtf.uwu.utils.packet;

import net.minecraft.network.Packet;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.packet.PacketEvent;
import wtf.uwu.utils.InstanceAccess;

public class PacketHandler implements InstanceAccess {

    public static void init() {
        // Registrar esta clase como listener en el EventManager
        INSTANCE.getEventManager().register(new PacketHandler());
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        switch (event.getState()) {
            case INCOMING:
                // Ejemplo: Cancelar packet de kick (disconnect)
                if (packet.getClass().getSimpleName().equals("S40PacketDisconnect")) {
                    event.setCancelled(true);
                }

                // Más lógica INCOMING...
                break;

            case OUTGOING:
                // Ejemplo: Cancelar movimiento si querés debuggear motion
                if (packet.getClass().getSimpleName().equals("C03PacketPlayer")) {
                    // event.setCancelled(true);
                }

                // Más lógica OUTGOING...
                break;
        }
    }

    /**
     * Envía un paquete normalmente.
     */
    public static void sendPacket(Packet<?> packet) {
        if (mc.thePlayer != null && mc.theWorld != null) {
            mc.getNetHandler().addToSendQueue(packet);
        }
    }

    /**
     * Envía un paquete sin generar eventos.
     */
    public static void sendPacketNoEvent(Packet<?> packet) {
        if (mc.thePlayer != null && mc.theWorld != null) {
            mc.getNetHandler().sendPacketNoEvent(packet);
        }
    }
}
