package daniel.text_block.client;

import daniel.text_block.TextBlock;
import daniel.text_block.block.entity.TextBlockEntity;
import daniel.text_block.client.render.block.TextBlockRenderer;
import daniel.text_block.client.screen.TextBlockScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.util.math.BlockPos;

import static daniel.text_block.TextBlock.TEXT_BLOCK_ITEM;

@Environment(EnvType.CLIENT)
public class TextBlockClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(TextBlock.TEXT_BLOCK_ENTITY, TextBlockRenderer::new);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            entries.add(TEXT_BLOCK_ITEM);
        });

        ClientPlayNetworking.registerGlobalReceiver(TextBlock.OPEN_TEXT_BLOCK_SCREEN_PACKET_ID, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            client.execute(() -> {
                assert client.world != null;
                BlockEntity textBlock = client.world.getBlockEntity(pos);
                if (!(textBlock instanceof TextBlockEntity)) {
                    BlockState textBlockState = client.world.getBlockState(pos);
                    textBlock = new SignBlockEntity(pos, textBlockState);
                    textBlock.setWorld(client.world);
                }
                client.setScreen(new TextBlockScreen((TextBlockEntity) textBlock));
            });
        });
    }
}
