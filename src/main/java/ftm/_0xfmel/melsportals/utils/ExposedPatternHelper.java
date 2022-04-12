package ftm._0xfmel.melsportals.utils;

import com.google.common.cache.LoadingCache;

import net.minecraft.block.pattern.BlockPattern.PatternHelper;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class ExposedPatternHelper extends PatternHelper {
    public final int width;
    public final int height;
    public final int depth;

    public ExposedPatternHelper(BlockPos p_i46378_1_, Direction p_i46378_2_, Direction p_i46378_3_,
            LoadingCache<BlockPos, CachedBlockInfo> p_i46378_4_, int width, int height, int depth) {
        super(p_i46378_1_, p_i46378_2_, p_i46378_3_, p_i46378_4_, width, height, depth);

        this.width = width;
        this.height = height;
        this.depth = depth;
    }
}
