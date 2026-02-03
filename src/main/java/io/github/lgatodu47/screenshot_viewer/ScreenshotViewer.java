package io.github.lgatodu47.screenshot_viewer;

import io.github.lgatodu47.screenshot_viewer.screen.IconButtonWidget;
import io.github.lgatodu47.screenshot_viewer.screen.ScreenshotViewerTexts;
import io.github.lgatodu47.screenshot_viewer.screen.manage_screenshots.ManageScreenshotsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ScreenshotViewer implements ClientModInitializer {
    public static final String MODID = "screenshot_viewer";

    private static ScreenshotViewer instance;

    private ScreenshotThumbnailManager thumbnailManager;
    private KeyBinding openScreenshotsScreenKey;

    @Override
    public void onInitializeClient() {
        thumbnailManager = new ScreenshotThumbnailManager();

        initKeyBindings();
        registerEvents();
        instance = this;
    }

    private void initKeyBindings() {
        openScreenshotsScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(ScreenshotViewerTexts.translation("key", "open_screenshots_screen"), InputUtil.UNKNOWN_KEY.getCode(), KeyBinding.Category.MISC));
    }

    private static final Identifier DELAYED_PHASE = Identifier.of(MODID, "delayed");
    private static final Identifier SCREENSHOT_VIEWER_ICON = Identifier.of(MODID, "widget/icons/screenshot_viewer");

    private void registerEvents() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if(client.world != null && client.currentScreen == null && openScreenshotsScreenKey != null && !openScreenshotsScreenKey.isUnbound()) {
                if(openScreenshotsScreenKey.isPressed()) {
                    client.setScreen(new ManageScreenshotsScreen(null));
                }
            }
        });
        ScreenEvents.AFTER_INIT.register(DELAYED_PHASE, (client, screen, scaledWidth, scaledHeight) -> {
            if(screen instanceof GameMenuScreen) {
                List<ClickableWidget> buttons = Screens.getButtons(screen);
                ClickableWidget topButton = buttons.getFirst();

                int x = topButton.getX() + topButton.getWidth() + 4;
                int y = topButton.getY();
                buttons.add(Util.make(new IconButtonWidget(x, y, topButton.getHeight(), topButton.getHeight(), ScreenshotViewerTexts.MANAGE_SCREENSHOTS, SCREENSHOT_VIEWER_ICON, button -> {
                    client.setScreen(new ManageScreenshotsScreen(screen));
                }), btn -> btn.setTooltip(Tooltip.of(ScreenshotViewerTexts.MANAGE_SCREENSHOTS))));
            }
            if(screen instanceof TitleScreen) {
                List<ClickableWidget> buttons = Screens.getButtons(screen);
                Optional<ClickableWidget> accessibilityWidgetOpt = buttons.stream()
                        .filter(TextIconButtonWidget.class::isInstance)
                        .filter(widget -> widget.getMessage().equals(Text.translatable("options.accessibility")))
                        .findFirst();

                int width = accessibilityWidgetOpt.map(ClickableWidget::getWidth).orElse(20);
                int height = accessibilityWidgetOpt.map(ClickableWidget::getHeight).orElse(20);
                int x = accessibilityWidgetOpt.map(ClickableWidget::getX).map(v -> v + width + 4).orElse(screen.width / 2 + 104 + width + 4);
                int y = accessibilityWidgetOpt.map(ClickableWidget::getY).orElse(screen.height / 4 + 132);
                buttons.add(Util.make(new IconButtonWidget(x, y, width, height, ScreenshotViewerTexts.MANAGE_SCREENSHOTS, SCREENSHOT_VIEWER_ICON, button -> {
                    client.setScreen(new ManageScreenshotsScreen(screen));
                }), btn -> btn.setTooltip(Tooltip.of(ScreenshotViewerTexts.MANAGE_SCREENSHOTS))));
            }
        });
        ScreenEvents.AFTER_INIT.addPhaseOrdering(Event.DEFAULT_PHASE, DELAYED_PHASE);
    }

    public ScreenshotThumbnailManager getThumbnailManager() {
        return thumbnailManager;
    }

    public KeyBinding getOpenScreenshotsScreenKey() {
        return openScreenshotsScreenKey;
    }

    @NotNull
    public static ScreenshotViewer getInstance() {
        if(instance == null) {
            throw new IllegalStateException("Screenshot Viewer Client is not loaded yet!");
        }

        return instance;
    }
}
