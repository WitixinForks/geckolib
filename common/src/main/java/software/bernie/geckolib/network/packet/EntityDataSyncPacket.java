package software.bernie.geckolib.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.GeckoLibConstants;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.dataticket.SerializableDataTicket;
import software.bernie.geckolib.util.ClientUtil;
import software.bernie.geckolib.util.RenderUtil;

import java.util.function.Consumer;

public record EntityDataSyncPacket<D>(int entityId, boolean isReplacedEntity, SerializableDataTicket<D> dataTicket, D data) implements MultiloaderPacket {
    public static final ResourceLocation ID = GeckoLibConstants.id("entity_data_sync");
    public static final CustomPacketPayload.Type<EntityDataSyncPacket<?>> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, EntityDataSyncPacket> CODEC = StreamCodec.of(
            EntityDataSyncPacket::encode,
            EntityDataSyncPacket::decode
    );

    public static <T> void encode(FriendlyByteBuf buffer, EntityDataSyncPacket<T> packet) {
        buffer.writeVarInt(packet.entityId());
        buffer.writeBoolean(packet.isReplacedEntity());
        buffer.writeUtf(packet.dataTicket().id());
        packet.dataTicket().encode(packet.data(), buffer);
    }

    public static <T> EntityDataSyncPacket<T> decode(FriendlyByteBuf buffer) {
        final int entityId = buffer.readVarInt();
        final boolean isReplacedEntity = buffer.readBoolean();
        final SerializableDataTicket<T> dataTicket = (SerializableDataTicket<T>)DataTickets.byName(buffer.readUtf());

        return new EntityDataSyncPacket<>(entityId, isReplacedEntity, dataTicket, dataTicket.decode(buffer));
    }

    @Override
    public void receiveMessage(@Nullable Player sender, Consumer<Runnable> workQueue) {
        workQueue.accept(() -> {
            Entity entity = ClientUtil.getLevel().getEntity(this.entityId);

            if (entity == null)
                return;

            if (!this.isReplacedEntity) {
                if (entity instanceof GeoEntity geoEntity)
                    geoEntity.setAnimData(this.dataTicket, this.data);

                return;
            }

            if (RenderUtil.getReplacedAnimatable(entity.getType()) instanceof GeoReplacedEntity replacedEntity)
                replacedEntity.setAnimData(entity, this.dataTicket, this.data);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type(){
        return TYPE;
    }
}
