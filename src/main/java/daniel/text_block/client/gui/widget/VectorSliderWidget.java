package daniel.text_block.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class VectorSliderWidget extends ClickableWidget {

    private final int dimensions;
    private final int[] values;

    private int selectedIndex;
    private boolean isDragging = false;

    private static final String[] COMPONENT_NAMES = new String[] {"X: ", "Y: ", "Z: ", "W: "};
    private static final int[] COMPONENT_COLORS = new int[] {
            0xff0000,
            0x00ff00,
            0x0000ff,
            0xffff00
    };

    public VectorSliderWidget(int x, int y, int width, int height, Text message, int dimensions) {
        super(x, y, width, height, message);
        this.dimensions = dimensions;
        this.values = new int[dimensions];
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.isHovered() && !isDragging) {
            for (int i = 0; i < this.dimensions; i++) {
                int width = this.width / (this.dimensions * 2) - 2;
                int x = this.getX() + width * i * 2 + i * 5;

                if (mouseX > x && mouseX < x + width * 2) {
                    selectedIndex = i;
                }
            }
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        //System.out.println(selectedIndex);
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        TextRenderer textRenderer = minecraftClient.textRenderer;

        DrawableHelper.drawTextWithShadow(matrices, textRenderer, this.getMessage(), this.getX(), this.getY() - 9, 0xffffff);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        int width = this.width / (this.dimensions * 2) - 2;
        int y = this.getY();
        for (int i = 0; i < this.dimensions; i++) {
            int x = this.getX() + width * i * 2 + i * 5;
            int hoverOffset = (selectedIndex == i && (isHovered() || isDragging)) ? 40 : 0;

            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
            RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
            drawTexture(matrices, x, y, 0, 46 + hoverOffset, width, this.height);
            drawTexture(matrices, x + width, y, this.width - width, 46 + hoverOffset, width, this.height);
            //textRenderer.draw(matrices, compNames[i], x, this.y + (this.height - 8) / 2, 0xffffff);
            DrawableHelper.drawCenteredTextWithShadow(matrices, textRenderer, COMPONENT_NAMES[i] + values[i], x + width, y + (this.height - 8) / 2, COMPONENT_COLORS[i]);
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        System.out.println(button);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        this.isDragging = true;
        this.values[selectedIndex] += Math.round(deltaX * 1000) / 1000f;
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        System.out.println("moved");
        return super.isMouseOver(mouseX, mouseY);
    }

    public int getX() {
        return this.values[0];
    }

    public int getY() {
        return this.values[1];
    }

    public int getZ() {
        return this.values[2];
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
