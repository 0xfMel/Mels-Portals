package ftm._0xfmel.melsportals.capabilities;

import ftm._0xfmel.melsportals.capabilities.customteleport.CustomTeleportCapability;

public class Capabilities {
    public static void registerCommonCapabilities() {
        CustomTeleportCapability.register();
    }
}
