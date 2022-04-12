package ftm._0xfmel.melsportals.world.poi;

import java.util.ArrayList;
import java.util.List;

import ftm._0xfmel.melsportals.gameobjects.blocks.ModBlocks;
import net.minecraft.village.PointOfInterestType;

public class ModPointOfInterestTypes {
    public static final List<PointOfInterestType> POINT_OF_INTEREST_TYPES = new ArrayList<PointOfInterestType>();

    public static final PointOfInterestType CUSTOM_PORTAL = new ModPointOfInterestType("custom_portal",
            PointOfInterestType.getBlockStates(ModBlocks.CUSTOM_PORTAL), 0, 1);
}
