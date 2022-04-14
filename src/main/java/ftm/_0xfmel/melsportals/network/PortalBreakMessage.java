package ftm._0xfmel.melsportals.network;

import java.awt.Point;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
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
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

public class PortalBreakMessage {
    private static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    private static final VoxelShape Z_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    private static final int PACKED_V_LENGTH = 26;
    private static final int PACKED_Y1_LENGTH = 8;
    private static final int PACKED_Y2_LENGTH = 4;
    private static final long PACKED_V_MASK = (1L << PACKED_V_LENGTH) - 1L;
    private static final long PACKED_Y1_MASK = (1L << PACKED_Y1_LENGTH) - 1L;
    private static final long PACKED_Y2_MASK = (1L << PACKED_Y2_LENGTH) - 1L;
    private static final int Y1_OFFSET = PACKED_Y2_LENGTH;
    private static final int V2_OFFSET = PACKED_Y1_LENGTH + PACKED_Y2_LENGTH;
    private static final int V1_OFFSET = PACKED_Y1_LENGTH + PACKED_Y2_LENGTH + PACKED_V_LENGTH;

    private static final int Y22_MASK = 0xf;

    private final Direction.Axis axis;
    private final DyeColor color;
    private final int w;
    private final List<Point> blockPoints;

    public PortalBreakMessage(Direction.Axis axis, DyeColor color, List<Point> blockPoints, int w) {
        this.axis = axis;
        this.color = color;
        this.blockPoints = blockPoints;
        this.w = w;
    }

    public static PortalBreakMessage decode(PacketBuffer buf) {
        Direction.Axis axis = Direction.Axis.byName(Character.toString(buf.readChar()));
        DyeColor color = DyeColor.byId(buf.readShort());
        int w = buf.readInt();
        int blocksLen = buf.readInt();
        int blocksDataLen = (int) Math.ceil(((double) blocksLen) / 2);
        int blockY2sLen = Math.floorDiv(blocksLen, 2);

        long[] blocks = IntStream.range(0, blocksDataLen).mapToLong((i) -> buf.readLong()).toArray();
        int[] blocksY2 = IntStream.range(0, (int) Math.ceil(((double) blockY2sLen) / 2)).flatMap((i) -> {
            byte bufVal = buf.readByte();
            return IntStream.of((bufVal & 0xff) >>> 4, bufVal & Y22_MASK);
        }).toArray();

        List<Point> blockPoints = new ArrayList<>();

        int li = blocksDataLen != blockY2sLen ? blocksDataLen - 1 : blocksDataLen;
        for (int i = 0; i < blocksDataLen; i++) {
            long packed = blocks[i];
            int v1 = (int) (packed << 64 - V1_OFFSET - PACKED_V_LENGTH >> 64 - PACKED_V_LENGTH);
            int y1 = (int) (packed << 64 - Y1_OFFSET - PACKED_Y1_LENGTH >> 64 - PACKED_Y1_LENGTH);
            blockPoints.add(new Point(v1, y1));

            if (i != li) {
                int v2 = (int) (packed << 64 - V2_OFFSET - PACKED_V_LENGTH >> 64 - PACKED_V_LENGTH);
                int y21 = (int) (packed << 64 - PACKED_Y2_LENGTH >> 64 - PACKED_Y2_LENGTH);
                blockPoints.add(new Point(v2, ((y21 << 4) | blocksY2[i])));
            }
        }

        return new PortalBreakMessage(axis, color, blockPoints, w);
    }

    public void encode(PacketBuffer buf) {
        long[] blocks; // Each long = 2 V coords & 1 1/2 Y coords
        int[] blocksY2; // Each int = other half of the 2nd Y coord

        List<List<Point>> partitionedBlocks = Lists.partition(this.blockPoints, 2);

        int blocksLen = this.blockPoints.size();

        blocks = new long[partitionedBlocks.size()];
        blocksY2 = new int[Math.floorDiv(blocksLen, 2)];

        int len = blocks.length;
        int lastIndex = blocks.length - 1;
        List<Point> last = partitionedBlocks.get(lastIndex);

        if (last.size() == 1) {
            len--;
            Point lastPos = last.get(0);
            long l = ((long) lastPos.x & PACKED_V_MASK) << V1_OFFSET;
            blocks[lastIndex] = l | ((long) lastPos.y & PACKED_Y1_MASK) << Y1_OFFSET;
        }

        for (int i = 0; i < len; i++) {
            List<Point> blocks1 = partitionedBlocks.get(i);
            Point pos1 = blocks1.get(0);
            Point pos2 = blocks1.get(1);
            long l = ((long) pos1.x & PACKED_V_MASK) << V1_OFFSET;
            l = l | ((long) pos2.x & PACKED_V_MASK) << V2_OFFSET;
            l = l | ((long) pos1.y & PACKED_Y1_MASK) << Y1_OFFSET;
            blocks[i] = l | ((long) (pos2.y >>> 4) & PACKED_Y2_MASK);
            blocksY2[i] = (byte) (pos2.y & Y22_MASK);
        }

        buf.writeChar(this.axis.getName().charAt(0));
        buf.writeShort(this.color.getId());
        buf.writeInt(this.w);
        buf.writeInt(blocksLen);
        Arrays.stream(blocks).forEachOrdered(buf::writeLong);
        List<List<Integer>> partitionedBlockY2s = Lists
                .partition(Arrays.stream(blocksY2).boxed().collect(Collectors.toList()), 2);
        int lastIndex1 = partitionedBlockY2s.size() - 1;
        Integer oddY = null;
        if (lastIndex1 >= 0 && partitionedBlockY2s.get(lastIndex1).size() == 1) {
            oddY = partitionedBlockY2s.get(lastIndex1).get(0);
            partitionedBlockY2s = partitionedBlockY2s.subList(0, lastIndex1);
        }
        partitionedBlockY2s.stream()
                .forEachOrdered(b -> buf.writeByte((b.get(0) << 4) | b.get(1)));
        if (oddY != null) {
            buf.writeByte(oddY << 4);
        }
    }

    @SuppressWarnings("unchecked")
    public static void handle(PortalBreakMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            BlockPos.Mutable pos = new BlockPos.Mutable();

            Boolean flag = msg.axis == Direction.Axis.X;

            BiConsumer<Integer, Integer> setBlockPos = flag
                    ? (v, y) -> pos.set(v, y, msg.w)
                    : (v, y) -> pos.set(msg.w, y, v);

            VoxelShape voxelshape = flag ? X_AXIS_AABB : Z_AXIS_AABB;

            BlockState state = ModBlocks.CUSTOM_PORTAL.defaultBlockState()
                    .setValue(NetherPortalBlock.AXIS, msg.axis)
                    .setValue(CustomPortalBlock.COLOR, msg.color);

            Map<IParticleRenderType, EvictingQueue<Particle>> particles;

            try {
                Field f = ParticleManager.class.getDeclaredField("particles");

                f.setAccessible(true);
                particles = (Map<IParticleRenderType, EvictingQueue<Particle>>) f
                        .get(mc.particleEngine);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Getting particles map");
                throw new ReportedException(crashreport);
            }

            Vector3d cameraPos = mc.gameRenderer.getMainCamera().getPosition();

            AtomicDouble minDist = new AtomicDouble(4096);

            List<Point> orderedPoints = msg.blockPoints.stream().map((blockPoint) -> {
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

            orderedPoints.stream().limit(Math.min(20, msg.blockPoints.size())).forEach(blockPoint -> {
                setBlockPos.accept(blockPoint.x, blockPoint.y);
                mc.level.playLocalSound(pos, SoundType.GLASS.getBreakSound(),
                        SoundCategory.BLOCKS, (SoundType.GLASS.getVolume() + 1.0F) / 2.0F,
                        SoundType.GLASS.getPitch() * 0.8F, false);
            });
        });

        ctx.get().setPacketHandled(true);
    }
}
