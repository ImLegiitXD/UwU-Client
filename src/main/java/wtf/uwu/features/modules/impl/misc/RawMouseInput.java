package wtf.uwu.features.modules.impl.misc;

import org.lwjgl.glfw.GLFW;
import org.lwjglx.input.Mouse;
import org.lwjglx.opengl.Display;
import wtf.uwu.features.modules.Module;
import wtf.uwu.features.modules.ModuleCategory;
import wtf.uwu.features.modules.ModuleInfo;

@ModuleInfo(name = "RawMouseInput",category = ModuleCategory.Misc)
public class RawMouseInput extends Module {

    @Override
    public void onEnable(){
        if (Mouse.isCreated()) {
            if (GLFW.glfwRawMouseMotionSupported()) {
                GLFW.glfwSetInputMode(Display.getWindow(), GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_FALSE);
            }
        }
    }

    @Override
    public void onDisable(){
        if (Mouse.isCreated()) {
            if (GLFW.glfwRawMouseMotionSupported()) {
                GLFW.glfwSetInputMode(Display.getWindow(), GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_TRUE);
            }
        }
    }
}
