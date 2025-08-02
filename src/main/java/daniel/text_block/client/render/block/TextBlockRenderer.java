package daniel.text_block.client.render.block;

import daniel.text_block.block.entity.TextBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
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

        matrices.translate(0.5f, 1, 0.5f);
        Vector3f scale = entity.getScale();
        Vector3f offset = entity.getOffset();
        if (entity.isBillboard()) {
            matrices.multiply(dispatcher.getRotation());
            matrices.scale(-scale.x, -scale.y, -scale.z);
        } else {
            matrices.translate(offset.x, offset.y, offset.z);

            float rotX = entity.getRotation().x;
            float rotY = entity.getRotation().y;
            float rotZ = entity.getRotation().z;
            matrices.multiply(
                    new Quaternionf()
                            .rotationXYZ(
                                    (float) Math.toRadians(rotX),
                                    (float) Math.toRadians(rotY),
                                    (float) Math.toRadians(rotZ)
                            )
            );
            matrices.scale(scale.x, -scale.y, scale.z);
        }

        if (entity.isDistanceScaled()) {
            float distance = getDistance(entity, tickDelta);
            matrices.scale(distance, distance, distance);
        }

        DrawableHelper.drawCenteredTextWithShadow(matrices, MinecraftClient.getInstance().textRenderer, entity.getText(), 0, 0, 0xffffff);
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
}
