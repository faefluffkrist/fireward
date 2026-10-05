package com.faefluffkrist.campfireward.configscreen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Shown after leaving the config screen when at least one change was saved. */
final class RelogNoticeScreen extends Screen {
    private final Screen returnTo;

    RelogNoticeScreen(Screen returnTo) {
        super(Component.literal("Fireward Settings Saved"));
        this.returnTo = returnTo;
    }

    @Override public void extractBackground(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        net.minecraft.client.gui.screens.Screen.extractMenuBackgroundTexture(graphics,
                net.minecraft.resources.Identifier.withDefaultNamespace("textures/block/cobbled_deepslate.png"),
                0, 0, 0, 0, width, height);
        graphics.fill(0, 0, width, height, 0x99000000);
    }
    @Override
    protected void init() {
        // Use real widgets for the notice text. In 26.2 these are collected by the
        // screen's normal render-state path, avoiding the custom text extraction
        // issue that previously left only the Okay button visible.
        Component saved = Component.literal("Settings saved!")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);

        Component relog = Component.empty()
                .append(Component.literal("Leave and rejoin")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD, ChatFormatting.ITALIC))
                .append(Component.literal(" the world to apply your changes.")
                        .withStyle(ChatFormatting.ITALIC));

        int savedWidth = font.width(saved);
        int relogWidth = font.width(relog);

        addRenderableWidget(new StringWidget(
                (width - savedWidth) / 2, height / 2 - 30, savedWidth, 12, saved, font));
        addRenderableWidget(new StringWidget(
                (width - relogWidth) / 2, height / 2 - 8, relogWidth, 12, relog, font));

        addRenderableWidget(Button.builder(Component.literal("Okay"), b -> onClose())
                .bounds(width / 2 - 60, height / 2 + 34, 120, 20)
                .build());
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(returnTo);
    }
}
