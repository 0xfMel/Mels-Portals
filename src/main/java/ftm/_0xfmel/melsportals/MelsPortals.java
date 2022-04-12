package ftm._0xfmel.melsportals;

import ftm._0xfmel.melsportals.capabilities.Capabilities;
import ftm._0xfmel.melsportals.client.BlockRenderTypes;
import ftm._0xfmel.melsportals.globals.ModGlobals;
import ftm._0xfmel.melsportals.utils.Logging;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ModGlobals.MOD_ID)
public class MelsPortals {
    public MelsPortals() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent e) {
        Logging.LOGGER.info("Setting up...");
        Capabilities.registerCommonCapabilities();
    }

    private void clientSetup(final FMLClientSetupEvent e) {
        Logging.LOGGER.info("Setting up client...");
        BlockRenderTypes.setBlockRenderTypes();
    }
}
