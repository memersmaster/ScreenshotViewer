package io.github.lgatodu47.screenshot_viewer.mixin;

import io.github.lgatodu47.screenshot_viewer.ScreenshotViewer;
import io.github.lgatodu47.screenshot_viewer.config.ScreenshotViewerOptions;
import io.github.lgatodu47.screenshot_viewer.screen.manage_screenshots.ManageScreenshotsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Inject(method = "handleClickEvent", at = @At("HEAD"), cancellable = true)
    private static void screenshot_viewer$inject_handleClickEvent(ClickEvent clickEvent, MinecraftClient client, @Nullable Screen screenAfterRun, CallbackInfo ci) {
        if (!ScreenshotViewer.getInstance().getConfig().getOrFallback(ScreenshotViewerOptions.REDIRECT_SCREENSHOT_CHAT_LINKS, false)) {
            return;
        }
        if (!(clickEvent instanceof ClickEvent.OpenFile openFile)) {
            return;
        }

        File file = openFile.file();
        if (!file.isFile()) {
            return;
        }

        String name = file.getName().toLowerCase();
        if (!name.endsWith(".png") && !name.endsWith(".jpg") && !name.endsWith(".jpeg") &&
            !name.endsWith(".bmp") && !name.endsWith(".tga") && !name.endsWith(".tiff")) {
            return;
        }

        client.setScreen(new ManageScreenshotsScreen(screenAfterRun, file));
        ci.cancel();
    }
}
