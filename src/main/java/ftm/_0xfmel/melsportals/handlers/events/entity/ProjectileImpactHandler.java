package ftm._0xfmel.melsportals.handlers.events.entity;

import ftm._0xfmel.melsportals.gameobjects.blocks.ModBlocks;
import net.minecraft.block.BlockState;
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
            if (blockstate.is(ModBlocks.CUSTOM_PORTAL)) {
                entity.handleInsidePortal(blockpos);

                e.setCanceled(true);
            }
        }
    }
}
