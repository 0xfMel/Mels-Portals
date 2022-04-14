package ftm._0xfmel.melsportals.capabilities;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

@OnlyIn(Dist.CLIENT)
public class CustomPortalPositionCapability {
    @CapabilityInject(ICustomPortalPosition.class)
    public static final Capability<ICustomPortalPosition> CUSTOM_PORTAL_POSITION_CAPABILITY = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(ICustomPortalPosition.class,
                new Capability.IStorage<ICustomPortalPosition>() {
                    @Override
                    public INBT writeNBT(Capability<ICustomPortalPosition> capability, ICustomPortalPosition instance,
                            Direction side) {
                        return null;
                    }

                    @Override
                    public void readNBT(Capability<ICustomPortalPosition> capability, ICustomPortalPosition instance,
                            Direction side,
                            INBT nbt) {
                    }
                }, ICustomPortalPosition.CustomPortalPosition::new);
    }
}
