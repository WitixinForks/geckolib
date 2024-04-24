package software.bernie.geckolib.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.util.ClientUtil;

import java.util.function.Consumer;

public record BlockEntityAnimTriggerPacket(BlockPos pos, String controllerName, String animName) implements MultiloaderPacket {
    public static final ResourceLocation ID = GeckoLibConstants.id("blockentity_anim_trigger");
    public static final CustomPacketPayload.Type<BlockEntityAnimTriggerPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, BlockEntityAnimTriggerPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBlockPos(packet.pos());
                buf.writeUtf(packet.controllerName());
                buf.writeUtf(packet.animName());
            },
            buf -> new BlockEntityAnimTriggerPacket(buf.readBlockPos(), buf.readUtf(), buf.readUtf())
    );

    @Override
    public void receiveMessage(@Nullable Player sender, Consumer<Runnable> workQueue) {
        workQueue.accept(() -> {
            if (ClientUtil.getLevel().getBlockEntity(this.pos) instanceof GeoBlockEntity blockEntity)
                blockEntity.triggerAnim(this.controllerName.isEmpty() ? null : this.controllerName, this.animName);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type(){
        return TYPE;
    }
}
