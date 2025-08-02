package daniel.text_block.mixin;

import daniel.text_block.TextBlock;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Final
    @Shadow
    @Mutable
    private static Set<Item> BLOCK_MARKER_ITEMS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void replaceBlockMarkerItems(CallbackInfo ci) {
        // Replace the set with your custom items
        BLOCK_MARKER_ITEMS = Set.of(
                Items.BARRIER,
                Items.LIGHT,
                TextBlock.TEXT_BLOCK_ITEM
        );
    }
}
