package ftm._0xfmel.melsportals.handlers.events.entity;

import java.util.Optional;

import ftm._0xfmel.melsportals.capabilities.customteleport.CustomTeleportCapability;
import ftm._0xfmel.melsportals.capabilities.customteleport.ICustomTeleport;
import ftm._0xfmel.melsportals.world.CustomTeleporter;
import net.minecraft.entity.Entity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EntityChangeDimensionHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent e) {
        Entity entity = e.getEntity();
        RegistryKey<World> toDim = e.getDimension();
        if (entity.level.dimension() != World.NETHER && toDim != World.NETHER)
            return;

        Optional<ICustomTeleport> customTeleport = entity
                .getCapability(CustomTeleportCapability.CUSTOM_TELEPORT_CAPABILITY, null).resolve();

        if (customTeleport.isPresent()) {
            RegistryKey<World> mapVal = customTeleport.get().getTravelIntent();
            if (mapVal != null && mapVal == toDim) {
                customTeleport.get().setTravelIntent(null);
                return;
            }

            e.setCanceled(true);

            customTeleport.get().setTravelIntent(toDim);

            ServerWorld level = entity.getServer().getLevel(toDim);

            ThreadTaskExecutor<Runnable> executor = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);
            executor.tell(
                    new TickDelayedTask(0, () -> entity.changeDimension(level, new CustomTeleporter(level))));
        }
    }
}
