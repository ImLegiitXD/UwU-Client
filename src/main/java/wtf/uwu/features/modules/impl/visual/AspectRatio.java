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

import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;
import wtf.uwu.features.values.impl.SliderValue;

@ModuleInfo(name = "test", category = ModuleCategory.Visual)
public class AspectRatio extends Module {
    public final SliderValue aspect = new SliderValue("Aspect",1.0f, 0.1f, 5.0f, 0.1f,this);
}
