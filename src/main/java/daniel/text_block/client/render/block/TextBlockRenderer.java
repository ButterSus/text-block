package daniel.text_block.client.render.block;

import daniel.text_block.block.entity.TextBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TextBlockRenderer implements BlockEntityRenderer<TextBlockEntity> {
    @SuppressWarnings("unused")
    public TextBlockRenderer(BlockEntityRendererFactory.Context ctx) {
        // Register signature
    }

    @Override
    public void render(TextBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        EntityRenderDispatcher dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();

        matrices.push();

        matrices.translate(0.5f, 0.5f, 0.5f);
        Vector3f scale = entity.getScale();
        Vector3f offset = entity.getOffset();

        if (entity.isBillboard()) {
            matrices.multiply(dispatcher.getRotation());
            matrices.scale(-scale.x, -scale.y, -scale.z);
        } else {
            // Apply rotation first
            Vector3f rotation = entity.getRotation();
            matrices.multiply(
                    new Quaternionf()
                            .rotationZYX(
                                    (float) Math.toRadians(rotation.z),
                                    (float) Math.toRadians(rotation.y),
                                    (float) Math.toRadians(rotation.x)
                            )
            );

            // Apply offset in local space (after rotation)
            matrices.translate(offset.x, offset.y, offset.z);
            matrices.scale(scale.x, -scale.y, scale.z);
        }

        if (entity.isDistanceScaled()) {
            float distance = getDistance(entity, tickDelta);
            matrices.scale(distance, distance, distance);
        }

        drawFullyCenteredTextWithShadow(matrices, MinecraftClient.getInstance().textRenderer, entity.getText(), 0, 0, 0xffffff);
        matrices.pop();
    }

    private static float getDistance(TextBlockEntity entity, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        Entity cameraEntity = client.cameraEntity;

        assert cameraEntity != null;

        // Interpolate camera position smoothly between previous and current tick positions
        double interpCamX = MathHelper.lerp(tickDelta, cameraEntity.prevX, cameraEntity.getX());
        double interpCamY = MathHelper.lerp(tickDelta, cameraEntity.prevY, cameraEntity.getY());
        double interpCamZ = MathHelper.lerp(tickDelta, cameraEntity.prevZ, cameraEntity.getZ());

        // Compute squared distance from interpolated camera position to fixed block entity position
        double dx = interpCamX - entity.getPos().getX();
        double dy = interpCamY - entity.getPos().getY();
        double dz = interpCamZ - entity.getPos().getZ();
        float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        distance *= 0.2f;
        return distance;
    }

    // Custom method to draw text centered both horizontally and vertically
    @SuppressWarnings("SameParameterValue")
    private void drawFullyCenteredTextWithShadow(MatrixStack matrices, TextRenderer textRenderer, Text text, int centerX, int centerY, int color) {
        OrderedText orderedText = text.asOrderedText();
        int textWidth = textRenderer.getWidth(orderedText);
        int textHeight = textRenderer.fontHeight;

        float x = centerX - textWidth / 2.0f;
        float y = centerY - textHeight / 2.0f;

        textRenderer.drawWithShadow(matrices, orderedText, x, y, color);
    }
}
