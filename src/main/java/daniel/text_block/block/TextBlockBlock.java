package daniel.text_block.block;

import daniel.text_block.block.entity.TextBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class TextBlockBlock extends BlockWithEntity {
    public TextBlockBlock() {
        super(Settings.copy(Blocks.STRUCTURE_VOID));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        if (placer instanceof PlayerEntity player) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof TextBlockEntity textBlockEntity) {
                // Get the exact hit position from the player's raycast
                HitResult hitResult = player.raycast(player.isCreative() ? 5.0 : 4.5, 0.0f, false);
                Vec3d hitPos;

                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    hitPos = hitResult.getPos();
                } else {
                    // Fallback to block center if raycast fails
                    hitPos = Vec3d.ofCenter(pos);
                }

                Vec3d playerEyes = player.getEyePos();
                Vec3d direction = playerEyes.subtract(hitPos).normalize();

                // Convert direction to rotations
                float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
                float pitch = (float) Math.toDegrees(Math.asin(-direction.y));

                float yawAligned = Math.round(yaw / 45.0f) * 45.0f;
                System.out.printf("Pitch: %f%n", pitch);
                float pitchAligned = pitch > 0 ? Math.round(pitch / 45.0f) * 45.0f :
                        pitch > -45.0f ? 0.0f : pitch < -75.0f ? -90.0f : -45.0f;

                textBlockEntity.setRotation(pitchAligned, -yawAligned, 0.0f);
            }
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            PacketByteBuf data = PacketByteBufs.create();
            data.writeBlockPos(pos);
            ServerPlayNetworking.send((ServerPlayerEntity) player, daniel.text_block.TextBlock.OPEN_TEXT_BLOCK_SCREEN_PACKET_ID, data);
        }
        return ActionResult.success(world.isClient);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.fullCube();
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TextBlockEntity(pos, state);
    }
}
