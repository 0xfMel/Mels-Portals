package ftm._0xfmel.melsportals.handlers.events;

import ftm._0xfmel.melsportals.capabilities.customteleport.CustomTeleportCapabilityProvider;
import ftm._0xfmel.melsportals.globals.ModGlobals;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AttachCapabilityHandler {
    @SubscribeEvent
    public static void onAttachCapabilityEntity(AttachCapabilitiesEvent<Entity> e) {
        e.addCapability(new ResourceLocation(ModGlobals.MOD_ID, "custom_teleport"),
                new CustomTeleportCapabilityProvider());
    }
}
