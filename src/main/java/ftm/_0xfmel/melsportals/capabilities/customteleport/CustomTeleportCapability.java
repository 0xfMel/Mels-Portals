package ftm._0xfmel.melsportals.capabilities.customteleport;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CustomTeleportCapability {
    @CapabilityInject(ICustomTeleport.class)
    public static final Capability<ICustomTeleport> CUSTOM_TELEPORT_CAPABILITY = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(ICustomTeleport.class, new Capability.IStorage<ICustomTeleport>() {
            @Override
            public INBT writeNBT(Capability<ICustomTeleport> capability, ICustomTeleport instance, Direction side) {
                return null;
            }

            @Override
            public void readNBT(Capability<ICustomTeleport> capability, ICustomTeleport instance, Direction side,
                    INBT nbt) {
            }
        }, CustomTeleport::new);
    }
}
