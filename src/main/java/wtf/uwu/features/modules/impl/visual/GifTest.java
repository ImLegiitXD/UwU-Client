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

import net.minecraft.util.ResourceLocation;
import wtf.uwu.events.annotations.EventTarget;
import wtf.uwu.events.impl.render.Render2DEvent;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.utils.render.GifRenderer;

@ModuleInfo(name = "GifTest", category = ModuleCategory.Visual)
public class GifTest extends Module {

    GifRenderer gif = new GifRenderer(new ResourceLocation("uwu client/texture/gif/test.gif"));
    @EventTarget
    public void onRender2D(Render2DEvent event){
        gif.drawTexture((float) event.scaledResolution().getScaledWidth() / 2, (float) event.scaledResolution().getScaledHeight() / 2,this.gif.getWidth(),this.gif.getHeight());
        gif.update();
    }
}
