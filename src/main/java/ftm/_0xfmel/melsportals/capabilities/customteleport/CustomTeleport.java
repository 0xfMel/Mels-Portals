package ftm._0xfmel.melsportals.capabilities.customteleport;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CustomTeleport implements ICustomTeleport {
    private BlockPos portalEntrancePos;
    private RegistryKey<World> travelIntent;
    private boolean isInsidePortal;
    private int portalTime;

    @Override
    public BlockPos getPortalEntrancePos() {
        return this.portalEntrancePos;
    }

    @Override
    public void setPortalEntrancePos(BlockPos portalEntrancePos) {
        this.portalEntrancePos = portalEntrancePos;
    }

    @Override
    public RegistryKey<World> getTravelIntent() {
        return this.travelIntent;
    }

    @Override
    public void setTravelIntent(RegistryKey<World> travelIntent) {
        this.travelIntent = travelIntent;
    }

    @Override
    public boolean getEntityIsInsidePortal() {
        return this.isInsidePortal;
    }

    @Override
    public void setEntityIsInsidePortal(boolean isInsidePortal) {
        this.isInsidePortal = isInsidePortal;
    }

    @Override
    public int getEntityPortalTime() {
        return this.portalTime;
    }

    @Override
    public void setEntityPortalTime(int portalTime) {
        this.portalTime = portalTime;
    }
}