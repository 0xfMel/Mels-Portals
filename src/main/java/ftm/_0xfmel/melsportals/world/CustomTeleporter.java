package ftm._0xfmel.melsportals.world;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

import ftm._0xfmel.melsportals.utils.Logging;
import ftm._0xfmel.melsportals.world.poi.ModPointOfInterestTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.PortalInfo;
import net.minecraft.block.PortalSize;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.TeleportationRepositioner;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.village.PointOfInterest;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class CustomTeleporter extends Teleporter {
    private static final Field PORTAL_ENTRANCE_POS_FIELD = ObfuscationReflectionHelper.findField(Entity.class,
            "field_242271_ac"); // Entity.portalEntrancePos

    static {
        PORTAL_ENTRANCE_POS_FIELD.setAccessible(true);
    }

    public CustomTeleporter(ServerWorld level) {
        super(level);
    }

    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerWorld destWorld,
            Function<ServerWorld, PortalInfo> defaultPortalInfo) {
        boolean flag2 = destWorld.dimension() == World.NETHER;
        WorldBorder worldborder = destWorld.getWorldBorder();
        double d0 = Math.max(-2.9999872E7D, worldborder.getMinX() + 16.0D);
        double d1 = Math.max(-2.9999872E7D, worldborder.getMinZ() + 16.0D);
        double d2 = Math.min(2.9999872E7D, worldborder.getMaxX() - 16.0D);
        double d3 = Math.min(2.9999872E7D, worldborder.getMaxZ() - 16.0D);
        double d4 = DimensionType.getTeleportationScale(entity.level.dimensionType(),
                destWorld.dimensionType());
        BlockPos blockpos1 = new BlockPos(MathHelper.clamp(entity.getX() * d4, d0, d2), entity.getY(),
                MathHelper.clamp(entity.getZ() * d4, d1, d3));

        BlockPos portalEntrancePos;
        try {
            portalEntrancePos = (BlockPos) PORTAL_ENTRANCE_POS_FIELD.get(entity);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Getting portalEntrancePos for entity");
            CrashReportCategory entityCategory = crashreport.addCategory("Entity being teleported");
            entityCategory.setDetail("Entity", entity);
            throw new ReportedException(crashreport);
        }

        if (portalEntrancePos == null)
            return null;

        Optional<TeleportationRepositioner.Result> optional1 = this.findPortalAround(blockpos1, flag2);

        Optional<TeleportationRepositioner.Result> optional;
        if (!optional1.isPresent() && entity instanceof ServerPlayerEntity) {
            Direction.Axis direction$axis1 = entity.level.getBlockState(portalEntrancePos)
                    .getOptionalValue(NetherPortalBlock.AXIS).orElse(Direction.Axis.X);
            Optional<TeleportationRepositioner.Result> optional2 = destWorld.getPortalForcer().createPortal(blockpos1,
                    direction$axis1);
            if (!optional2.isPresent()) {
                Logging.LOGGER.error("Unable to create a portal, likely target out of worldborder");
            }

            optional = optional2;
        } else {
            optional = optional1;
        }

        return optional.map((result) -> {
            BlockState blockstate = entity.level.getBlockState(portalEntrancePos);
            Direction.Axis direction$axis;
            Vector3d vector3d;
            if (blockstate.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                direction$axis = blockstate.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                TeleportationRepositioner.Result teleportationrepositioner$result = TeleportationRepositioner
                        .getLargestRectangleAround(portalEntrancePos, direction$axis, 21, Direction.Axis.Y, 21,
                                (p_242276_2_) -> {
                                    return entity.level.getBlockState(p_242276_2_) == blockstate;
                                });
                vector3d = this.getRelativePortalPosition(entity, direction$axis, teleportationrepositioner$result);
            } else {
                direction$axis = Direction.Axis.X;
                vector3d = new Vector3d(0.5D, 0.0D, 0.0D);
            }

            return PortalSize.createPortalInfo(destWorld, result, direction$axis, vector3d,
                    entity.getDimensions(entity.getPose()), entity.getDeltaMovement(), entity.yRot, entity.xRot);
        }).orElse((PortalInfo) null);
    }

    @Override
    public Optional<TeleportationRepositioner.Result> findPortalAround(BlockPos pPos, boolean pIsNether) {
        PointOfInterestManager pointofinterestmanager = this.level.getPoiManager();
        int i = pIsNether ? 16 : 128;
        pointofinterestmanager.ensureLoadedAndValid(this.level, pPos, i);
        Optional<PointOfInterest> optional = pointofinterestmanager.getInSquare((p_242952_0_) -> {
            return p_242952_0_ == ModPointOfInterestTypes.CUSTOM_PORTAL
                    || p_242952_0_ == PointOfInterestType.NETHER_PORTAL;
        }, pPos, i, PointOfInterestManager.Status.ANY)
                .sorted(Comparator.<PointOfInterest>comparingDouble((p_242954_1_) -> {
                    return p_242954_1_.getPos().distSqr(pPos);
                }).thenComparingInt((p_242959_0_) -> {
                    return p_242959_0_.getPos().getY();
                })).filter((p_242958_1_) -> {
                    return this.level.getBlockState(p_242958_1_.getPos())
                            .hasProperty(BlockStateProperties.HORIZONTAL_AXIS);
                }).findFirst();
        return optional.map((p_242951_1_) -> {
            BlockPos blockpos = p_242951_1_.getPos();
            this.level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(blockpos), 3, blockpos);
            BlockState blockstate = this.level.getBlockState(blockpos);
            return TeleportationRepositioner.getLargestRectangleAround(blockpos,
                    blockstate.getValue(BlockStateProperties.HORIZONTAL_AXIS), 21, Direction.Axis.Y, 21,
                    (p_242953_2_) -> {
                        return this.level.getBlockState(p_242953_2_) == blockstate;
                    });
        });
    }

    private Vector3d getRelativePortalPosition(Entity entity, Direction.Axis pAxis,
            TeleportationRepositioner.Result pPortal) {
        return PortalSize.getRelativePosition(pPortal, pAxis, entity.position(),
                entity.getDimensions(entity.getPose()));
    }
}
