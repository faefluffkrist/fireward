package com.faefluffkrist.campfireward;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public final class ConfigNetworking {
    private ConfigNetworking() {}
    public record ConfigPayload(String json) implements CustomPacketPayload {
        public static final Type<ConfigPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("fireward","settings"));
        public static final StreamCodec<RegistryFriendlyByteBuf,ConfigPayload> CODEC = StreamCodec.of(
            (buffer,payload) -> buffer.writeUtf(payload.json,1048576), buffer -> new ConfigPayload(buffer.readUtf(1048576)));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    public static void initialize() {
        PayloadTypeRegistry.clientboundPlay().registerLarge(ConfigPayload.TYPE,ConfigPayload.CODEC,4194304);
        ServerPlayConnectionEvents.JOIN.register((handler,sender,server) -> {
            if (ServerPlayNetworking.canSend(handler.player,ConfigPayload.TYPE))
                ServerPlayNetworking.send(handler.player,new ConfigPayload(FirewardConfig.toJson(FirewardConfig.active)));
        });
    }
}
