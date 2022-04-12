package ftm._0xfmel.melsportals.client;

import ftm._0xfmel.melsportals.gameobjects.blocks.ModBlocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;

public class BlockRenderTypes {
    public static void setBlockRenderTypes() {
        RenderType translucentRenderType = RenderType.translucent();
        RenderTypeLookup.setRenderLayer(ModBlocks.CUSTOM_PORTAL, translucentRenderType);
    }
}
