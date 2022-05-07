package ftm._0xfmel.melsportals.network;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.Lists;

import net.minecraft.item.DyeColor;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class PortalBreakMessage {
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

    public static void handle(PortalBreakMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.safeRunWhenOn(Dist.CLIENT,
                    () -> ClientOnlyNetworkMethods.handleBreakPortal(msg.axis, msg.w, msg.color, msg.blockPoints));
        });

        ctx.get().setPacketHandled(true);
    }
}
