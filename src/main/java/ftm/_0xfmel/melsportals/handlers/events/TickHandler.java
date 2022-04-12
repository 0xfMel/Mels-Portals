package ftm._0xfmel.melsportals.handlers.events;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.stream.Stream;

import ftm._0xfmel.melsportals.capabilities.customteleport.CustomTeleportCapability;
import ftm._0xfmel.melsportals.capabilities.customteleport.ICustomTeleport;
import ftm._0xfmel.melsportals.gameobjects.blocks.ModBlocks;
import ftm._0xfmel.melsportals.world.CustomTeleporter;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TickHandler {
    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.world instanceof ServerWorld))
            return;
        Stream<Entity> entityStream = ((ServerWorld) e.world).getEntities();
        entityStream.forEach((entity) -> {
            checkEntityPortalCollisions(entity);
            checkThrowableProjectileTick(entity);
        });
    }

    private static void checkThrowableProjectileTick(Entity entity) {
        if (entity instanceof ThrowableEntity) {
            Optional<ICustomTeleport> customTeleport = entity.getCapability(
                    CustomTeleportCapability.CUSTOM_TELEPORT_CAPABILITY, null).resolve();

            if (customTeleport.isPresent()) {
                try {
                    tickForgeBugHack(entity, customTeleport.get());
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking ThrowableProjectile");
                    throw new ReportedException(crashreport);
                }
            }
        }
    }

    private static void checkEntityPortalCollisions(Entity entity) {
        AxisAlignedBB axisalignedbb = entity.getBoundingBox();
        BlockPos blockpos = new BlockPos(axisalignedbb.minX + 0.001D, axisalignedbb.minY + 0.001D,
                axisalignedbb.minZ + 0.001D);
        BlockPos blockpos1 = new BlockPos(axisalignedbb.maxX - 0.001D, axisalignedbb.maxY - 0.001D,
                axisalignedbb.maxZ - 0.001D);
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
        if (entity.level.hasChunksAt(blockpos, blockpos1)) {
            for (int i = blockpos.getX(); i <= blockpos1.getX(); ++i) {
                for (int j = blockpos.getY(); j <= blockpos1.getY(); ++j) {
                    for (int k = blockpos.getZ(); k <= blockpos1.getZ(); ++k) {
                        blockpos$mutable.set(i, j, k);
                        BlockState blockstate = entity.level.getBlockState(blockpos$mutable);

                        try {
                            if (blockstate.getBlock() == Blocks.NETHER_PORTAL
                                    || blockstate.getBlock() == ModBlocks.CUSTOM_PORTAL) {

                                if (!entity.isPassenger() && !entity.isVehicle() && entity.canChangeDimensions()) {

                                    Optional<ICustomTeleport> customTeleport = entity.getCapability(
                                            CustomTeleportCapability.CUSTOM_TELEPORT_CAPABILITY, null).resolve();

                                    if (customTeleport.isPresent()) {

                                        if (blockstate.getBlock() == Blocks.NETHER_PORTAL
                                                && (customTeleport.get().getPortalEntrancePos() == null
                                                        || !customTeleport.get().getPortalEntrancePos()
                                                                .equals(blockpos$mutable))) {

                                            customTeleport.get().setPortalEntrancePos(blockpos$mutable.immutable());
                                        }

                                        tickForgeBugHack(entity, customTeleport.get());
                                    }
                                }
                            }
                        } catch (Throwable throwable) {
                            CrashReport crashreport = CrashReport.forThrowable(throwable,
                                    "Colliding entity with block");
                            CrashReportCategory crashreportcategory = crashreport
                                    .addCategory("Block being collided with");
                            CrashReportCategory.populateBlockDetails(crashreportcategory,
                                    blockpos$mutable,
                                    blockstate);
                            throw new ReportedException(crashreport);
                        }
                    }
                }
            }
        }
    }

    // Terrible hack to replace vanilla portal handling while forge's event is
    // bugged for other entities than ServerPlayerEntity.
    private static void tickForgeBugHack(Entity entity, ICustomTeleport customTeleport) throws IllegalAccessException {
        if (!(entity instanceof ServerPlayerEntity)) {
            if (entity.level instanceof ServerWorld) {
                int ii = entity.getPortalWaitTime();
                ServerWorld serverworld = (ServerWorld) entity.level;

                if (customTeleport.getEntityIsInsidePortal()) {
                    MinecraftServer minecraftserver = serverworld.getServer();
                    RegistryKey<World> registrykey = entity.level.dimension() == World.NETHER
                            ? World.OVERWORLD
                            : World.NETHER;
                    ServerWorld serverworld1 = minecraftserver.getLevel(registrykey);
                    if (serverworld1 != null && minecraftserver.isNetherEnabled() && !entity.isPassenger()) {
                        int portalTime = customTeleport.getEntityPortalTime();
                        customTeleport.setEntityPortalTime(portalTime + 1);

                        if (portalTime >= ii) {
                            ThreadTaskExecutor<Runnable> executor = LogicalSidedProvider.WORKQUEUE
                                    .get(LogicalSide.SERVER);
                            executor.tell(
                                    new TickDelayedTask(0, () -> {
                                        entity.level.getProfiler().push("portal");
                                        customTeleport.setEntityPortalTime(ii);
                                        entity.setPortalCooldown();
                                        entity.changeDimension(serverworld1, new CustomTeleporter(serverworld1));

                                        entity.level.getProfiler().pop();
                                    }));
                        }
                    }

                    customTeleport.setEntityIsInsidePortal(false);
                } else {
                    int portalTime = customTeleport.getEntityPortalTime();
                    if (portalTime > 0) {
                        customTeleport.setEntityPortalTime(portalTime -= 4);
                    }

                    if (portalTime < 0) {
                        customTeleport.setEntityPortalTime(0);
                    }
                }
            }

            Class<?> c = entity.getClass();
            while (c != null && c != Object.class && c != Entity.class) {
                c = c.getSuperclass();
            }

            if (c == Entity.class) {
                try {
                    Field field = c.getDeclaredField("isInsidePortal");
                    field.setAccessible(true);
                    field.set(entity, false);
                } catch (NoSuchFieldException e) {
                    // ignore
                }
            }

            if (!entity.isOnPortalCooldown()) {
                customTeleport.setEntityIsInsidePortal(true);
            }
        }
    }
}
