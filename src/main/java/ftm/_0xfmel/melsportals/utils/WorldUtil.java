package ftm._0xfmel.melsportals.utils;

import net.minecraft.world.World;

public class WorldUtil {
    public static boolean inPortalDimension(World world) {
        return world.dimension() == World.OVERWORLD || world.dimension() == World.NETHER;
    }
}
