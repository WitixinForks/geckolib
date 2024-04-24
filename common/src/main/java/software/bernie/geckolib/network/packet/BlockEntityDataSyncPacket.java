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
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.dataticket.SerializableDataTicket;
import software.bernie.geckolib.util.ClientUtil;

import java.util.function.Consumer;

public record BlockEntityDataSyncPacket<D>(BlockPos pos, SerializableDataTicket<D> dataTicket, D data) implements MultiloaderPacket {
    public static final ResourceLocation ID = GeckoLibConstants.id("blockentity_data_sync");
    public static final CustomPacketPayload.Type<BlockEntityDataSyncPacket<?>> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, BlockEntityDataSyncPacket> CODEC = StreamCodec.of(
            BlockEntityDataSyncPacket::encode,
            BlockEntityDataSyncPacket::decode
    );

    private static <T> void encode(FriendlyByteBuf buf, BlockEntityDataSyncPacket<T> packet) {
        buf.writeBlockPos(packet.pos());
        buf.writeUtf(packet.dataTicket().id());
        packet.dataTicket().encode(packet.data(), buf);
    }

    private static <T> BlockEntityDataSyncPacket<T> decode(FriendlyByteBuf buf) {
        final BlockPos pos = buf.readBlockPos();
        final SerializableDataTicket<T> dataTicket = (SerializableDataTicket<T>) DataTickets.byName(buf.readUtf());
        return new BlockEntityDataSyncPacket<>(pos, dataTicket, dataTicket.decode(buf));
    }

    @Override
    public void receiveMessage(@Nullable Player sender, Consumer<Runnable> workQueue) {
        workQueue.accept(() -> {
            if (ClientUtil.getLevel().getBlockEntity(this.pos) instanceof GeoBlockEntity blockEntity)
                blockEntity.setAnimData(this.dataTicket, this.data);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type(){
        return TYPE;
    }
}
