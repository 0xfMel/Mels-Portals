package ftm._0xfmel.melsportals.network;

import java.awt.Point;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.google.common.collect.EvictingQueue;
import com.google.common.util.concurrent.AtomicDouble;

import ftm._0xfmel.melsportals.gameobjects.blocks.CustomPortalBlock;
import ftm._0xfmel.melsportals.gameobjects.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.item.DyeColor;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ClientOnlyNetworkMethods {
    private static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    private static final VoxelShape Z_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    private static final Field PARTICLES_FIELD = ObfuscationReflectionHelper.findField(ParticleManager.class,
            "field_78876_b"); // ParticleManager.particles

    static {
        PARTICLES_FIELD.setAccessible(true);
    }

    @SuppressWarnings("unchecked")
    public static DistExecutor.SafeRunnable handleBreakPortal(Direction.Axis msgAxis, int msgW, DyeColor msgColor,
            List<Point> msgBlockPoints) {

        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                Minecraft mc = Minecraft.getInstance();
                BlockPos.Mutable pos = new BlockPos.Mutable();

                Boolean flag = msgAxis == Direction.Axis.X;

                BiConsumer<Integer, Integer> setBlockPos = flag
                        ? (v, y) -> pos.set(v, y, msgW)
                        : (v, y) -> pos.set(msgW, y, v);

                VoxelShape voxelshape = flag ? X_AXIS_AABB : Z_AXIS_AABB;

                BlockState state = ModBlocks.CUSTOM_PORTAL.defaultBlockState()
                        .setValue(NetherPortalBlock.AXIS, msgAxis)
                        .setValue(CustomPortalBlock.COLOR, msgColor);

                Map<IParticleRenderType, EvictingQueue<Particle>> particles;

                try {
                    particles = (Map<IParticleRenderType, EvictingQueue<Particle>>) PARTICLES_FIELD
                            .get(mc.particleEngine);
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.forThrowable(throwable, "Getting particles map");
                    throw new ReportedException(crashreport);
                }

                Vector3d cameraPos = mc.gameRenderer.getMainCamera().getPosition();

                AtomicDouble minDist = new AtomicDouble(4096);

                List<Point> orderedPoints = msgBlockPoints.stream().map((blockPoint) -> {
                    setBlockPos.accept(blockPoint.x, blockPoint.y);
                    double dist = cameraPos.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
                    if (minDist.get() > dist) {
                        minDist.set(dist);
                    }
                    return new Tuple<>(dist, blockPoint);
                }).sorted(Comparator.comparingDouble(Tuple::getA)).map(el -> el.getB()).collect(Collectors.toList());

                double priorityFactor = MathHelper.clampedLerp(0.5, 0, MathHelper.inverseLerp(minDist.get(), 0, 512));

                int particlePriorityBlocks = (int) Math.floor(
                        ((double) particles.get(IParticleRenderType.TERRAIN_SHEET).remainingCapacity())
                                * (priorityFactor) / 32D);

                int orderedPointsLen = orderedPoints.size();
                if (orderedPointsLen > 0) {
                    AtomicInteger remainingParticles = new AtomicInteger(
                            (int) ((particles.get(IParticleRenderType.TERRAIN_SHEET).remainingCapacity()
                                    - particlePriorityBlocks * 32)
                                    * (0.9 - priorityFactor)));

                    MathHelper.lerp2(1, 2, 3, 4, 5, 6);

                    voxelshape.forAllBoxes(
                            (p_228348_3_, p_228348_5_, p_228348_7_, p_228348_9_, p_228348_11_, p_228348_13_) -> {
                                double d1 = Math.min(1.0D, p_228348_9_ - p_228348_3_);
                                double d2 = Math.min(1.0D, p_228348_11_ - p_228348_5_);
                                double d3 = Math.min(1.0D, p_228348_13_ - p_228348_7_);
                                int i = Math.max(2, MathHelper.ceil(d1 / 0.25D));
                                int j = Math.max(2, MathHelper.ceil(d2 / 0.25D));
                                int k = Math.max(2, MathHelper.ceil(d3 / 0.25D));

                                for (int l = 0; l < i; ++l) {
                                    for (int i1 = 0; i1 < j; ++i1) {
                                        for (int j1 = 0; j1 < k; ++j1) {
                                            double d4 = ((double) l + 0.5D) / (double) i;
                                            double d5 = ((double) i1 + 0.5D) / (double) j;
                                            double d6 = ((double) j1 + 0.5D) / (double) k;
                                            double d7 = d4 * d1 + p_228348_3_;
                                            double d8 = d5 * d2 + p_228348_5_;
                                            double d9 = d6 * d3 + p_228348_7_;

                                            for (int opi = 0; opi < orderedPointsLen; opi++) {
                                                Point blockPoint = orderedPoints.get(opi);
                                                setBlockPos.accept(blockPoint.x, blockPoint.y);
                                                double pX = (double) pos.getX() + d7;
                                                double pY = (double) pos.getY() + d8;
                                                double pZ = (double) pos.getZ() + d9;

                                                boolean flag1 = false;
                                                boolean dist = cameraPos.distanceToSqr(pX, pY, pZ) <= 4096.0D;

                                                if (opi >= particlePriorityBlocks && dist) {
                                                    if (remainingParticles.getAndDecrement() > 0) {
                                                        flag1 = true;
                                                    } else {
                                                        break;
                                                    }
                                                } else if (dist) {
                                                    flag1 = true;
                                                }

                                                if (flag1) {
                                                    mc.particleEngine.add((new DiggingParticle(
                                                            mc.level,
                                                            pX, pY, pZ,
                                                            d4 - 0.5D,
                                                            d5 - 0.5D,
                                                            d6 - 0.5D,
                                                            state)).init());
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                }

                orderedPoints.stream().limit(Math.min(20, msgBlockPoints.size())).forEach(blockPoint -> {
                    setBlockPos.accept(blockPoint.x, blockPoint.y);
                    mc.level.playLocalSound(pos, SoundType.GLASS.getBreakSound(),
                            SoundCategory.BLOCKS, (SoundType.GLASS.getVolume() + 1.0F) / 2.0F,
                            SoundType.GLASS.getPitch() * 0.8F, false);
                });
            }
        };
    }
}
