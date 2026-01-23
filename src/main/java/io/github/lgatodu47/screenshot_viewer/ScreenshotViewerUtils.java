package io.github.lgatodu47.screenshot_viewer;

import com.mojang.logging.LogUtils;
import io.github.lgatodu47.screenshot_viewer.screen.ScreenshotViewerTexts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ScreenshotViewerUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private static final Clipboard AWT_CLIPBOARD = tryGetAWTClipboard();
    private static final SystemToast.Type COPY_SCREENSHOT = new SystemToast.Type(3000);

    public static File getVanillaScreenshotsFolder() {
        return new File(MinecraftClient.getInstance().runDirectory, "screenshots");
    }

    public static File getDefaultThumbnailFolder() {
        return new File(MinecraftClient.getInstance().runDirectory, "screenshots/thumbnails");
    }

    public static List<File> getScreenshotFiles(File screenshotsFolder) {
        File[] files = screenshotsFolder.listFiles();
        if(files == null) {
            return List.of();
        }
        return Arrays.stream(files).filter(file -> file.isFile() && (file.getName().endsWith(".png") || file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg"))).collect(Collectors.toList());
    }

    public static void drawTexture(DrawContext context, Identifier texture, int x, int y, int width, int height, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, (float)u, (float)v, width, height, regionWidth, regionHeight, textureWidth, textureHeight);
    }
    @Nullable
    private static Clipboard tryGetAWTClipboard() {
        if(Util.getOperatingSystem() == Util.OperatingSystem.OSX) {
            return null;
        }
        try {
            return Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch (Throwable t) {
            LOGGER.error("Unable to retrieve Java AWT Clipboard instance!", t);
        }
        return null;
    }

    public static void copyImageToClipboard(File screenshotFile) {
        if(Util.getOperatingSystem() == Util.OperatingSystem.OSX) {
            ScreenshotViewerMacOsUtils.doCopyMacOS(screenshotFile.getAbsolutePath());
            return;
        }
        if(AWT_CLIPBOARD != null && screenshotFile.exists()) {
            CompletableFuture.runAsync(() -> {
                Text toastText;
                try {
                    BufferedImage img = ImageIO.read(screenshotFile);
                    BufferedImage rgbImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
                    rgbImg.createGraphics().drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
                    ImageTransferable imageTransferable = new ImageTransferable(rgbImg);
                    AWT_CLIPBOARD.setContents(imageTransferable, null);
                    toastText = ScreenshotViewerTexts.TOAST_COPY_SUCCESS;
                } catch (Throwable t) {
                    LOGGER.error("Failed to copy screenshot image to clipboard!", t);
                    toastText = ScreenshotViewerTexts.translatable("toast", "copy_fail", t.getClass().getSimpleName());
                }

                MinecraftClient client = MinecraftClient.getInstance();
                if(client != null) {
                    SystemToast.show(client.getToastManager(), COPY_SCREENSHOT, toastText, Text.literal(screenshotFile.getName()));
                }
            }, Util.getMainWorkerExecutor());
        }
    }

    public static List<TooltipComponent> toColoredComponents(MinecraftClient client, Text text) {
        return Tooltip.wrapLines(client, text).stream().map(ColoredTooltipComponent::new).collect(Collectors.toList());
    }

    public static void renderTooltip(DrawContext context, TextRenderer textRenderer, List<OrderedText> text, int posX, int posY) {
        context.drawTooltip(textRenderer, text, HoveredTooltipPositioner.INSTANCE, posX, posY, false);
    }

    public static void renderWidget(ClickableWidget widget, DrawContext context, int mouseX, int mouseY, float delta) {
        widget.render(context, mouseX, mouseY, delta);
    }

    public static void forEachDrawable(Screen screen, Consumer<Drawable> renderer) {
        forEachOfType(screen, Drawable.class, renderer);
    }

    public static <T> void forEachOfType(Screen screen, Class<T> type, Consumer<T> action) {
        screen.children().stream().filter(type::isInstance).map(type::cast).forEachOrdered(action);
    }

    static class ColoredTooltipComponent implements TooltipComponent {
        private final OrderedText text;

        public ColoredTooltipComponent(OrderedText text) {
            this.text = text;
        }

        @Override
        public int getWidth(TextRenderer textRenderer) {
            return textRenderer.getWidth(this.text);
        }

        @Override
        public int getHeight(TextRenderer textRenderer) {
            return 10;
        }

        @Override
        public void drawText(DrawContext context, TextRenderer textRenderer, int x, int y) {
            context.drawText(textRenderer, this.text, x, y, -1, true);
        }
    }

    record ImageTransferable(Image image) implements Transferable {
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {DataFlavor.imageFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @NotNull
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if(!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image();
        }
    }
}
