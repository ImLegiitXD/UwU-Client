/*
 * MoonLight Hacked Client
 *
 * A free and open-source hacked client for Minecraft.
 * Developed using Minecraft's resources.
 *
 * Repository: https://github.com/randomguy3725/MoonLight
 *
 * Author(s): [Randumbguy & wxdbie & opZywl & MukjepScarlet & lucas & eonian]
 */
package wtf.uwu.features.modules.impl.visual;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.render.Render3DEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.BoolValue;
import wtf.uwu.features.values.impl.ColorValue;
import wtf.uwu.utils.render.RenderUtils;

import java.awt.*;

@ModuleInfo(name = "ChestESP",category = ModuleCategory.Visual)
public class ChestESP extends Module {

    public final BoolValue outline = new BoolValue("Outline", false, this);
    public final BoolValue filled = new BoolValue("Filled", true, this);
    public final BoolValue syncColor = new BoolValue("Sync Color", false, this);
    public final ColorValue color = new ColorValue("Color",new Color(255,255,128),this ,() -> !syncColor.get());

    @EventTarget
    public void onRender3D(Render3DEvent event) {

        for (TileEntity tileEntity : mc.theWorld.loadedTileEntityList) {
            if (tileEntity instanceof TileEntityChest || tileEntity instanceof TileEntityEnderChest) {
                if (!tileEntity.isInvalid() && mc.theWorld.getBlockState(tileEntity.getPos()) != null) {
                    if (syncColor.get()) {
                        RenderUtils.renderBlock(tileEntity.getPos(),getModule(Interface.class).color(0),outline.get(),filled.get());
                    } else {
                        RenderUtils.renderBlock(tileEntity.getPos(),color.get().getRGB(),outline.get(),filled.get());
                    }
                }
            }
        }
    }
}
