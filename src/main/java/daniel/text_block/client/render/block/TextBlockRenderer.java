package daniel.text_block.client.render.block;

import daniel.text_block.block.entity.TextBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TextBlockRenderer implements BlockEntityRenderer<TextBlockEntity> {
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
            assert MinecraftClient.getInstance().cameraEntity != null;
            float distance = MathHelper.sqrt((float) MinecraftClient.getInstance().cameraEntity.squaredDistanceTo(entity.getPos().getX(), entity.getPos().getY(), entity.getPos().getZ()));
            distance *= 0.2f;
            matrices.scale(distance, distance, distance);
        }

        DrawableHelper.drawCenteredTextWithShadow(matrices, MinecraftClient.getInstance().textRenderer, entity.getText(), 0, 0, 0xffffff);
        matrices.pop();
    }
}
