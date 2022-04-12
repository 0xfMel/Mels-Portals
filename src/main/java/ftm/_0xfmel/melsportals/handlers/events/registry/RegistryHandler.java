package ftm._0xfmel.melsportals.handlers.events.registry;

import ftm._0xfmel.melsportals.client.particles.ModParticles;
import ftm._0xfmel.melsportals.gameobjects.blocks.ModBlocks;
import ftm._0xfmel.melsportals.world.poi.ModPointOfInterestTypes;
import net.minecraft.block.Block;
import net.minecraft.particles.ParticleType;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class RegistryHandler {
    @SubscribeEvent
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> e) {
        e.getRegistry().registerAll(ModBlocks.BLOCKS.toArray(new Block[0]));
    }

    @SubscribeEvent
    public static void onParticleTypesRegistry(final RegistryEvent.Register<ParticleType<?>> e) {
        e.getRegistry().registerAll(ModParticles.PARTICLE_TYPES.toArray(new ParticleType<?>[0]));
    }

    @SubscribeEvent
    public static void onPointOfInterestRegistry(final RegistryEvent.Register<PointOfInterestType> e) {
        e.getRegistry()
                .registerAll(ModPointOfInterestTypes.POINT_OF_INTEREST_TYPES.toArray(new PointOfInterestType[0]));
    }
}
