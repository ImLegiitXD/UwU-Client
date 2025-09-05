package wtf.uwu.utils.discord;

import lombok.Getter;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiSelectWorld;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.impl.visual.Interface;
import wtf.uwu.utils.InstanceAccess;
import wtf.uwu.utils.concurrent.Workers;
import wtf.uwu.utils.misc.ServerUtils;

public class DiscordInfo implements InstanceAccess {
    private boolean running = true;
    private long timeElapsed = 0;
    @Getter
    private String name;
    @Getter
    private String id;
    @Getter
    private String smallImageText;

    public int getTotal() {
        return INSTANCE.getModuleManager().getModules().size();
    }

    public long getCount() {
        return INSTANCE.getModuleManager().getModules().stream().filter(Module::isEnabled).count();
    }

    public void init() {
        this.timeElapsed = System.currentTimeMillis();
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler(discordUser -> {
            System.out.println("[Discord] Conectado como " + discordUser.username + "#" + discordUser.discriminator);
            if (discordUser.userId != null) {
                name = discordUser.username + (discordUser.discriminator.equals("0") ? "" : discordUser.discriminator);
            } else {
                System.exit(0);
            }
        }).build();

        DiscordRPC.discordInitialize("1384041076058493079", handlers, true);
        Workers.IO.execute(() -> {
            while (running) {
                int killed = INSTANCE.getModuleManager().getModule(Interface.class).killed;
                int win = INSTANCE.getModuleManager().getModule(Interface.class).won;

                if (mc.thePlayer != null) {
                    if (mc.isSingleplayer()) {
                        update("Usuario: " + detectUsername(), "en un jugador", true);
                        updateSmallImageText(getCount() + "/" + getTotal() + " modulos activos");
                    } else if (mc.getCurrentServerData() != null) {
                        if (ServerUtils.isOnHypixel()) {
                            update("Usuario: " + detectUsername(), "Kills: " + killed + " Wins: " + win, true);
                            updateSmallImageText("Jugando en '" + mc.getCurrentServerData().serverIP + "' con " + getCount() + "/" + getTotal() + " modulos activos");
                        } else {
                            update("ig: " + detectUsername(), "jugando en " + mc.getCurrentServerData().serverIP, true);
                            updateSmallImageText("en partida multijugador | " + getCount() + "/" + getTotal() + " modulos activos");
                        }
                    } else if (mc.currentScreen instanceof GuiDownloadTerrain) {
                        update("Cargando mundo...", "", false);
                    }
                } else {
                    if (mc.currentScreen instanceof GuiSelectWorld) {
                        update("Seleccionando mundo...", "", false);
                    } else if (mc.currentScreen instanceof GuiMultiplayer) {
                        update("Seleccionando servidor...", "", false);
                    } else if (mc.currentScreen instanceof GuiDownloadTerrain) {
                        update("Cargando mundo...", "", false);
                    } else {
                        update("Pensandote...", "", false);
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }

                DiscordRPC.discordRunCallbacks();
            }
        });
    }

    public void stop() {
        running = false;
        DiscordRPC.discordShutdown();
    }

    public String detectUsername() {
        return mc.thePlayer.getName();
    }

    public void update(String line1, String line2, Boolean smallImage) {
        DiscordRichPresence.Builder rpc = new DiscordRichPresence.Builder(line2)
                .setDetails(line1)
                .setBigImage("logo", "uwu client [#" + INSTANCE.version + "]");

        if (smallImage) {
            rpc.setSmallImage("closer", smallImageText);
        }

        rpc.setStartTimestamps(timeElapsed);
        DiscordRPC.discordUpdatePresence(rpc.build());
    }

    public void updateSmallImageText(String text) {
        smallImageText = text;
    }
}
