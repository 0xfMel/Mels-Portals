package ftm._0xfmel.melsportals.handlers.events.registry;

import ftm._0xfmel.melsportals.client.particles.ColoredPortalParticle;
import ftm._0xfmel.melsportals.client.particles.ModParticles;
import ftm._0xfmel.melsportals.gameobjects.blocks.CustomPortalBlock;
import ftm._0xfmel.melsportals.gameobjects.blocks.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistryHandler {
    @SubscribeEvent
    public static void onParticlesRegistry(ParticleFactoryRegisterEvent e) {
        Minecraft.getInstance().particleEngine.register(ModParticles.COLORED_PORTAL,
                ColoredPortalParticle.Factory::new);
    }

    @SubscribeEvent
    public static void registerBlockColorHandlers(ColorHandlerEvent.Block e) {
        e.getBlockColors().register(new CustomPortalBlock.BlockColorHandlerCustomPortal(), ModBlocks.CUSTOM_PORTAL);
    }
}
