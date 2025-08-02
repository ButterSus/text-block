package daniel.text_block.client;

import daniel.text_block.block.TextBlockBlock;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWScrollCallbackI;

public class MouseScrollHandler {
    private static boolean ctrlPressed = false;
    private static boolean callbackSet = false;
    private static GLFWScrollCallbackI originalCallback = null;

    public void initialize() {
        // Register tick event to check Ctrl state and set up scroll callback
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            updateCtrlState(client);
            setupScrollCallback(client);
        });
    }

    private static void updateCtrlState(MinecraftClient client) {
        if (client.getWindow() != null) {
            ctrlPressed = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
                    GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
        }
    }

    private static void setupScrollCallback(MinecraftClient client) {
        if (callbackSet || client.getWindow() == null) {
            return;
        }

        callbackSet = true;
        long windowHandle = client.getWindow().getHandle();

        // Store the original callback before setting our own
        originalCallback = GLFW.glfwSetScrollCallback(windowHandle, null);

        // Set our custom scroll callback
        //noinspection resource
        GLFW.glfwSetScrollCallback(windowHandle, (window, xOffset, yOffset) -> {
            boolean shouldIntercept = false;

            // Only check for interception if Ctrl is pressed and there's scroll movement
            if (ctrlPressed && Math.abs(yOffset) > 0.1) {
                shouldIntercept = checkShouldInterceptScroll(client, yOffset);
            }

            // If we shouldn't intercept, call the original callback (normal scroll behavior)
            if (!shouldIntercept && originalCallback != null) {
                originalCallback.invoke(window, xOffset, yOffset);
            }
        });
    }

    private static boolean checkShouldInterceptScroll(MinecraftClient client, double scrollDelta) {
        ClientPlayerEntity player = client.player;
        World world = client.world;

        if (player == null || world == null) {
            return false;
        }

        // Perform raycast with the same distance as your block
        double reachDistance = player.isCreative() ? 5.0 : 4.5;
        HitResult hitResult = player.raycast(reachDistance, 0.0f, false);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            BlockPos hitPos = blockHitResult.getBlockPos();

            // Check if the hit block is your TextBlockBlock
            if (world.getBlockState(hitPos).getBlock() instanceof TextBlockBlock) {
                // Calculate scale change based on scroll direction
                float scaleChange = scrollDelta > 0 ? 0.1f : -0.1f;

                // Send packet to server to modify the TextBlockEntity scale
                sendScaleChangePacket(hitPos, scaleChange);

                return true; // Intercept the scroll event
            }
        }

        return false; // Don't intercept, let normal scrolling happen
    }

    private static void sendScaleChangePacket(BlockPos pos, float scaleChange) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || !(client.world.getBlockEntity(pos) instanceof daniel.text_block.block.entity.TextBlockEntity textBlockEntity)) {
            return;
        }

        PacketByteBuf data = PacketByteBufs.create();
        data.writeBlockPos(pos);

        // Write current text
        data.writeText(textBlockEntity.getText());

        // Write current boolean values
        data.writeBoolean(textBlockEntity.isBillboard());
        data.writeBoolean(textBlockEntity.isDistanceScaled());

        // Write current offset values
        data.writeFloat(textBlockEntity.getOffset().x);
        data.writeFloat(textBlockEntity.getOffset().y);
        data.writeFloat(textBlockEntity.getOffset().z);

        // Write current rotation values
        data.writeFloat(textBlockEntity.getRotation().x);
        data.writeFloat(textBlockEntity.getRotation().y);
        data.writeFloat(textBlockEntity.getRotation().z);

        // Write modified scale values (current + change, with limits)
        Vector3f currentScale = textBlockEntity.getScale();
        float newScaleX = Math.max(0.1f, Math.min(5.0f, currentScale.x + scaleChange));
        float newScaleY = Math.max(0.1f, Math.min(5.0f, currentScale.y + scaleChange));
        float newScaleZ = Math.max(0.1f, Math.min(5.0f, currentScale.z + scaleChange));

        data.writeFloat(newScaleX);
        data.writeFloat(newScaleY);
        data.writeFloat(newScaleZ);

        ClientPlayNetworking.send(daniel.text_block.TextBlock.TEXT_BLOCK_UPDATE_PACKET, data);
    }
}
