package net.minecraft.client.gui;

import java.io.IOException;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import cn.floatingpoint.min.management.Managers;
import cn.floatingpoint.min.system.ui.components.DraggableGameView;
import cn.floatingpoint.min.utils.math.Vec2i;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ITabCompleter;
import net.minecraft.util.TabCompleter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;

public class GuiChat extends GuiScreen implements ITabCompleter {
    private String historyBuffer = "";

    /**
     * keeps position of which chat message you will select when you press up, (does not increase for duplicated
     * messages sent immediately after each other)
     */
    private int sentHistoryCursor = -1;
    private TabCompleter tabCompleter;

    /**
     * Chat entry field
     */
    protected GuiTextField inputField;

    /**
     * is the text that appears when you press the chat key and the input box appears pre-filled
     */
    private String defaultInputFieldText = "";

    private static DraggableGameView clickedDraggable;

    private int prevMouseX;
    private int prevMouseY;

    public GuiChat() {
    }

    public GuiChat(String defaultText) {
        this.defaultInputFieldText = defaultText;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.sentHistoryCursor = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
        this.inputField = new GuiTextField(0, this.fontRenderer, 4, this.height - 12, this.width - 4, 12);
        this.inputField.setMaxStringLength(256);
        this.inputField.setEnableBackgroundDrawing(false);
        this.inputField.setFocused(true);
        this.inputField.setText(this.defaultInputFieldText);
        this.inputField.setCanLoseFocus(false);
        this.tabCompleter = new GuiChat.ChatTabCompleter(this.inputField);
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        this.mc.ingameGUI.getChatGUI().resetScroll();
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen() {
        this.inputField.updateCursorCounter();
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.tabCompleter.resetRequested();

        if (keyCode == 15) {
            this.tabCompleter.complete();
        } else {
            this.tabCompleter.resetDidComplete();
        }

        if (keyCode == 1) {
            this.mc.displayGuiScreen(null);
        } else if (keyCode != 28 && keyCode != 156) {
            if (keyCode == 200) {
                this.getSentHistory(-1);
            } else if (keyCode == 208) {
                this.getSentHistory(1);
            } else if (keyCode == 201) {
                this.mc.ingameGUI.getChatGUI().scroll(this.mc.ingameGUI.getChatGUI().getLineCount() - 1);
            } else if (keyCode == 209) {
                this.mc.ingameGUI.getChatGUI().scroll(-this.mc.ingameGUI.getChatGUI().getLineCount() + 1);
            } else {
                this.inputField.textboxKeyTyped(typedChar, keyCode);
            }
        } else {
            String s = this.inputField.getText().trim();

            if (!s.isEmpty()) {
                this.sendChatMessage(s);
            }

            this.mc.displayGuiScreen(null);
        }
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();

        if (i != 0) {
            if (i > 1) {
                i = 1;
            }

            if (i < -1) {
                i = -1;
            }

            if (!isShiftKeyDown()) {
                i *= 7;
            }

            this.mc.ingameGUI.getChatGUI().scroll(i);
        }
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            ITextComponent itextcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

            if (itextcomponent != null && this.handleComponentClick(itextcomponent)) {
                return;
            }
        }

        this.inputField.mouseClicked(mouseX, mouseY, mouseButton);
        ScaledResolution scaledresolution = new ScaledResolution(mc);
        if (clickedDraggable == null && mouseButton == 0) {
            for (Map.Entry<DraggableGameView, Vec2i> entry : Managers.draggableGameViewManager.draggableMap.entrySet()) {
                DraggableGameView draggableGameView = entry.getKey();
                Vec2i position = entry.getValue();
                if (isHovered(scaledresolution.getScaledWidth() / 2 + position.x, position.y, scaledresolution.getScaledWidth() / 2 + position.x + draggableGameView.getWidth(), position.y + draggableGameView.getHeight(), mouseX, mouseY)) {
                    clickedDraggable = draggableGameView;
                    return;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Sets the text of the chat
     */
    protected void setText(String newChatText, boolean shouldOverwrite) {
        if (shouldOverwrite) {
            this.inputField.setText(newChatText);
        } else {
            this.inputField.writeText(newChatText);
        }
    }

    /**
     * input is relative and is applied directly to the sentHistoryCursor so -1 is the previous message, 1 is the next
     * message from the current cursor position
     */
    public void getSentHistory(int msgPos) {
        int i = this.sentHistoryCursor + msgPos;
        int j = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
        i = MathHelper.clamp(i, 0, j);

        if (i != this.sentHistoryCursor) {
            if (i == j) {
                this.sentHistoryCursor = j;
                this.inputField.setText(this.historyBuffer);
            } else {
                if (this.sentHistoryCursor == j) {
                    this.historyBuffer = this.inputField.getText();
                }

                this.inputField.setText(this.mc.ingameGUI.getChatGUI().getSentMessages().get(i));
                this.sentHistoryCursor = i;
            }
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);
        this.inputField.drawTextBox();
        if (clickedDraggable != null) {
            if (Mouse.isButtonDown(0)) {
                Vec2i prevPosition = Managers.draggableGameViewManager.draggableMap.get(clickedDraggable);
                ScaledResolution scaledResolution = new ScaledResolution(mc);
                int width = scaledResolution.getScaledWidth();
                int height = scaledResolution.getScaledHeight();
                int x = Math.max(prevPosition.x + mouseX - prevMouseX, -width / 2);
                int y = Math.max(prevPosition.y + mouseY - prevMouseY, 0);
                if (width / 2 + x + clickedDraggable.getWidth() > width) {
                    x = width - clickedDraggable.getWidth() - scaledResolution.getScaledWidth() / 2;
                }
                if (y + clickedDraggable.getHeight() > height) {
                    y = height - clickedDraggable.getHeight();
                }
                Managers.draggableGameViewManager.draggableMap.put(clickedDraggable, new Vec2i(x, y));
                prevMouseX = mouseX;
                prevMouseY = mouseY;
            } else {
                clickedDraggable = null;
            }
            return;
        }
        prevMouseX = mouseX;
        prevMouseY = mouseY;

        ITextComponent itextcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

        if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null) {
            this.handleComponentHover(itextcomponent, mouseX, mouseY);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    public boolean doesGuiPauseGame() {
        return false;
    }

    /**
     * Sets the list of tab completions, as long as they were previously requested.
     */
    public void setCompletions(@Nonnull String... newCompletions) {
        this.tabCompleter.setCompletions(newCompletions);
    }

    public static class ChatTabCompleter extends TabCompleter {
        private final Minecraft client = Minecraft.getMinecraft();

        public ChatTabCompleter(GuiTextField p_i46749_1_) {
            super(p_i46749_1_, false);
        }

        public void complete() {
            super.complete();

            if (this.completions.size() > 1) {
                StringBuilder stringbuilder = new StringBuilder();

                for (String s : this.completions) {
                    if (stringbuilder.length() > 0) {
                        stringbuilder.append(", ");
                    }

                    stringbuilder.append(s);
                }

                this.client.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(stringbuilder.toString()), 1);
            }
        }

        @Nullable
        public BlockPos getTargetBlockPos() {
            BlockPos blockpos = null;

            if (this.client.objectMouseOver != null && this.client.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
                blockpos = this.client.objectMouseOver.getBlockPos();
            }

            return blockpos;
        }
    }
}