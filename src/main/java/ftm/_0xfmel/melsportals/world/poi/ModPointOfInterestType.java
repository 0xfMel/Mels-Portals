package ftm._0xfmel.melsportals.world.poi;

import java.util.Set;

import net.minecraft.block.BlockState;
import net.minecraft.village.PointOfInterestType;

public class ModPointOfInterestType extends PointOfInterestType {
    public ModPointOfInterestType(String pKey, Set<BlockState> pBlockStates, int pMaxFreeTickets,
            int pValidRange) {
        super(pKey, pBlockStates, pMaxFreeTickets, pValidRange);

        this.setRegistryName(pKey);

        ModPointOfInterestTypes.POINT_OF_INTEREST_TYPES.add(this);
    }

}
