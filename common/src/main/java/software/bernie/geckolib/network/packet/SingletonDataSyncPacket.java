package software.bernie.geckolib.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.dataticket.SerializableDataTicket;
import software.bernie.geckolib.util.ClientUtil;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public record SingletonDataSyncPacket<D>(String syncableId, long instanceId, SerializableDataTicket<D> dataTicket, D data) implements MultiloaderPacket {
    public static final ResourceLocation ID = GeckoLibConstants.id("singleton_data_sync");
    public static final CustomPacketPayload.Type<SingletonDataSyncPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SingletonDataSyncPacket> CODEC = StreamCodec.of(
        SingletonDataSyncPacket::encode,
        SingletonDataSyncPacket::decode
    );

    private static <T> void encode(FriendlyByteBuf buf, SingletonDataSyncPacket<T> packet) {
        buf.writeUtf(packet.syncableId());
        buf.writeVarLong(packet.instanceId());
        buf.writeUtf(packet.dataTicket().id());
        packet.dataTicket().encode(packet.data(), buf);
    }

    public static <T> SingletonDataSyncPacket<T> decode(FriendlyByteBuf buf) {
        final String syncableId = buf.readUtf();
        final long instanceId = buf.readVarLong();
        final SerializableDataTicket<T> dataTicket = (SerializableDataTicket<T>) DataTickets.byName(buf.readUtf());

        return new SingletonDataSyncPacket<>(syncableId, instanceId, dataTicket, dataTicket.decode(buf));
    }

    @Override
    public void receiveMessage(@Nullable Player sender, Consumer<Runnable> workQueue) {
        workQueue.accept(() -> {
            GeoAnimatable animatable = GeckoLibUtil.getSyncedAnimatable(this.syncableId);

            if (animatable instanceof SingletonGeoAnimatable singleton)
                singleton.setAnimData(ClientUtil.getClientPlayer(), this.instanceId, this.dataTicket, this.data);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type(){
        return TYPE;
    }
}
