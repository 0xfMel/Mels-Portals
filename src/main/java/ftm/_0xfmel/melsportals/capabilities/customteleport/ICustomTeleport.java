package ftm._0xfmel.melsportals.capabilities.customteleport;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ICustomTeleport {
    RegistryKey<World> getTravelIntent();

    void setTravelIntent(RegistryKey<World> travelIntent);

    BlockPos getPortalEntrancePos();

    void setPortalEntrancePos(BlockPos portalEntrancePos);

    // All temporary below
    boolean getEntityIsInsidePortal();

    void setEntityIsInsidePortal(boolean isInsidePortal);

    int getEntityPortalTime();

    void setEntityPortalTime(int portalTime);
}
