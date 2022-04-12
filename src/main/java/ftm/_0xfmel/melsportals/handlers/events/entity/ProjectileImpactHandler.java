package ftm._0xfmel.melsportals.handlers.events.entity;

import java.util.Optional;

import ftm._0xfmel.melsportals.capabilities.customteleport.CustomTeleportCapability;
import ftm._0xfmel.melsportals.capabilities.customteleport.ICustomTeleport;
import ftm._0xfmel.melsportals.gameobjects.blocks.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.util.math.BlockRayTraceResult;

@Mod.EventBusSubscriber
public class ProjectileImpactHandler {
    @SubscribeEvent
    public static void onThrowableProjectileImpact(ProjectileImpactEvent.Throwable e) {
        RayTraceResult raytraceresult = e.getRayTraceResult();
        if (raytraceresult.getType() == RayTraceResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockRayTraceResult) raytraceresult).getBlockPos();
            Entity entity = e.getEntity();
            BlockState blockstate = entity.level.getBlockState(blockpos);
            if (blockstate.is(ModBlocks.CUSTOM_PORTAL) || blockstate.is(Blocks.NETHER_PORTAL)) {
                Optional<ICustomTeleport> customTeleport = entity
                        .getCapability(CustomTeleportCapability.CUSTOM_TELEPORT_CAPABILITY, null).resolve();

                if (!entity.isOnPortalCooldown()) {
                    entity.handleInsidePortal(blockpos);

                    if (customTeleport.isPresent()
                            && (customTeleport.get().getPortalEntrancePos() == null
                                    || !customTeleport.get().getPortalEntrancePos().equals(blockpos))) {
                        customTeleport.get().setPortalEntrancePos(blockpos.immutable());
                    }

                    customTeleport.get().setEntityIsInsidePortal(true);
                }

                e.setCanceled(true);
            }
        }
    }
}
