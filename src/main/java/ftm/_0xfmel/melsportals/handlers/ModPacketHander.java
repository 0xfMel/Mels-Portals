package ftm._0xfmel.melsportals.handlers;

import java.util.Optional;

import ftm._0xfmel.melsportals.globals.ModGlobals;
import ftm._0xfmel.melsportals.network.PortalBreakMessage;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class ModPacketHander {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ModGlobals.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void registerToClientNetworkPackets() {
        int discrim = 0;

        ModPacketHander.INSTANCE.<PortalBreakMessage>registerMessage(
                discrim++,
                PortalBreakMessage.class,
                PortalBreakMessage::encode,
                PortalBreakMessage::decode,
                PortalBreakMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
