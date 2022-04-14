package ftm._0xfmel.melsportals.utils;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class Utils {
    public static Direction relativeTo(BlockPos from, BlockPos to) {
        BlockPos normal = from.subtract(to);
        return Direction.fromNormal(normal.getX(), normal.getY(), normal.getZ());
    }
}
