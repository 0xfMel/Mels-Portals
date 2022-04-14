package ftm._0xfmel.melsportals.handlers.events;

import ftm._0xfmel.melsportals.capabilities.ICustomPortalPosition;
import ftm._0xfmel.melsportals.globals.ModGlobals;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AttachCapabilityHandler {
    @SubscribeEvent
    public static void onAttachCapabilityEntity(AttachCapabilitiesEvent<Entity> e) {
        Entity entity = e.getObject();
        if (entity.level.isClientSide) {
            if (entity instanceof ClientPlayerEntity) {
                e.addCapability(new ResourceLocation(ModGlobals.MOD_ID, "custom_portal_position"),
                        new ICustomPortalPosition.Provider());
            }
        }
    }
}
