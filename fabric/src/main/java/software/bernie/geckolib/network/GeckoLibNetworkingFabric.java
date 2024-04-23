package software.bernie.geckolib.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.GeckoLibClient;
import software.bernie.geckolib.GeckoLibServices;
import software.bernie.geckolib.network.packet.MultiloaderPacket;
import software.bernie.geckolib.service.GeckoLibNetworking;

/**
 * Fabric service implementation for GeckoLib's networking functionalities
 */
public final class GeckoLibNetworkingFabric implements GeckoLibNetworking {
    /**
     * Register a GeckoLib packet for use
     * <p>
     * <b><u>FOR GECKOLIB USE ONLY</u></b>
     */
    @Override
    public <P extends MultiloaderPacket> void registerPacketInternal(ResourceLocation id, boolean isClientBound, Class<P> messageType, StreamCodec<FriendlyByteBuf, P> codec) {
        if (isClientBound) {
            //See: https://fabricmc.net/2024/04/19/1205.html
            if (GeckoLibServices.PLATFORM.isPhysicalClient()) {
                final CustomPacketPayload.Type<P> packetId = new CustomPacketPayload.Type<>(id);
                PayloadTypeRegistry.playS2C().register(packetId, codec);
                GeckoLibClient.registerPacket(packetId, codec);
            }
        }
        else {
            final CustomPacketPayload.Type<P> packetId = new CustomPacketPayload.Type<>(id);
            PayloadTypeRegistry.playC2S().register(packetId, codec);
            ServerPlayNetworking.registerGlobalReceiver(packetId, (packet, context) -> packet.receiveMessage(context.player(), context.player().server::execute));
        }
    }

    /**
     * Send a packet to all players currently tracking a given entity
     * <p>
     * Good as a shortcut for sending a packet to all players that may have an interest in a given entity or its dealings
     * <p>
     * Will also send the packet to the entity itself if the entity is also a player
     */
    @Override
    public void sendToAllPlayersTrackingEntity(MultiloaderPacket packet, Entity trackingEntity) {
        if (trackingEntity instanceof ServerPlayer pl)
            sendToPlayer(packet, pl);

        for (ServerPlayer player : PlayerLookup.tracking(trackingEntity)) {
            sendToPlayer(packet, player);
        }
    }

    /**
     * Send a packet to all players tracking a given block position
     */
    @Override
    public void sendToAllPlayersTrackingBlock(MultiloaderPacket packet, ServerLevel level, BlockPos pos) {
        for (ServerPlayer player : PlayerLookup.tracking(level, pos)) {
            sendToPlayer(packet, player);
        }
    }

    /**
     * Send a packet to the given player
     */
    @Override
    public void sendToPlayer(MultiloaderPacket packet, ServerPlayer player) {
        FriendlyByteBuf buffer = PacketByteBufs.create();

        packet.write(buffer);
        ServerPlayNetworking.send(player, packet.id(), buffer);
    }
}
