package io.github.lgatodu47.screenshot_viewer.mixin;

import io.github.lgatodu47.screenshot_viewer.screen.ScreenshotViewerTexts;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.File;
import java.util.function.Consumer;

@Mixin(ScreenshotRecorder.class)
public class ScreenshotRecorderMixin {
    @ModifyVariable(
            method = "saveScreenshot(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/client/gl/Framebuffer;ILjava/util/function/Consumer;)V",
            at = @org.spongepowered.asm.mixin.injection.At("HEAD"),
            argsOnly = true,
            index = 4
    )
    private static Consumer<Text> screenshot_viewer$wrapMessageReceiver(Consumer<Text> messageReceiver, File gameDirectory, String fileName, Framebuffer framebuffer, int downscaleFactor) {
        return message -> {
            // Hardcoded: false
            if (false && message instanceof MutableText mutable) {
                mutable.styled(style -> style.withHoverEvent(new HoverEvent.ShowText(ScreenshotViewerTexts.REDIRECT_TO_SCREENSHOT_MANAGER)));
            }
            messageReceiver.accept(message);
        };
    }
}
