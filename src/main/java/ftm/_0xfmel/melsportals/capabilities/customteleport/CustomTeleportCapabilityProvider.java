package ftm._0xfmel.melsportals.capabilities.customteleport;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class CustomTeleportCapabilityProvider extends CapabilityProvider<CustomTeleportCapabilityProvider> {
    private LazyOptional<ICustomTeleport> customTeleportLazyOptional = LazyOptional
            .of(CustomTeleport::new);

    public CustomTeleportCapabilityProvider() {
        super(CustomTeleportCapabilityProvider.class);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == CustomTeleportCapability.CUSTOM_TELEPORT_CAPABILITY) {
            return this.customTeleportLazyOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void invalidateCaps() {
        super.invalidateCaps();
        customTeleportLazyOptional.invalidate();
    }
}
