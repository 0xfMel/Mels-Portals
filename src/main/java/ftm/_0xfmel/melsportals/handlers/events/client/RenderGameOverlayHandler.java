package ftm._0xfmel.melsportals.handlers.events.client;

import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;

import ftm._0xfmel.melsportals.capabilities.CustomPortalPositionCapability;
import ftm._0xfmel.melsportals.capabilities.ICustomPortalPosition;
import ftm._0xfmel.melsportals.gameobjects.blocks.CustomPortalBlock;
import ftm._0xfmel.melsportals.gameobjects.blocks.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class RenderGameOverlayHandler {
    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public static void onRenderGamePortalOverlayPre(RenderGameOverlayEvent.Pre e) {
        if (e.getType() != RenderGameOverlayEvent.ElementType.PORTAL)
            return;
        Minecraft mc = Minecraft.getInstance();

        float timeInPortal = mc.player.oPortalTime
                + (mc.player.portalTime - mc.player.oPortalTime) * e.getPartialTicks();

        if (timeInPortal > 0.0F) {
            Optional<ICustomPortalPosition> customPortalPosition = mc.player
                    .getCapability(CustomPortalPositionCapability.CUSTOM_PORTAL_POSITION_CAPABILITY, null).resolve();

            if (customPortalPosition.isPresent()) {

                BlockPos pos = customPortalPosition.get().getPortalPos();
                if (pos != null) {
                    BlockState blockState = mc.level.getBlockState(pos);
                    if (!(blockState.getBlock() == ModBlocks.CUSTOM_PORTAL)
                            || blockState.getValue(CustomPortalBlock.COLOR) == DyeColor.PURPLE) {
                        return;
                    }

                    e.setCanceled(true);

                    if (timeInPortal < 1.0F) {
                        timeInPortal = timeInPortal * timeInPortal;
                        timeInPortal = timeInPortal * timeInPortal;
                        timeInPortal = timeInPortal * 0.8F + 0.2F;
                    }

                    RenderSystem.disableAlphaTest();
                    RenderSystem.disableDepthTest();
                    RenderSystem.depthMask(false);
                    RenderSystem.defaultBlendFunc();
                    int k = mc.getBlockColors().getColor(blockState, mc.level, pos, 0);
                    float c = (float) (k >> 16 & 255) / 255.0f;
                    float c1 = (float) (k >> 8 & 255) / 255.0f;
                    float c2 = (float) (k & 255) / 255.0f;
                    RenderSystem.color4f(c, c1, c2, timeInPortal);
                    mc.getTextureManager().bind(AtlasTexture.LOCATION_BLOCKS);
                    TextureAtlasSprite textureatlassprite = mc.getBlockRenderer().getBlockModelShaper()
                            .getTexture(blockState, mc.level, pos);
                    float f = textureatlassprite.getU0();
                    float f1 = textureatlassprite.getV0();
                    float f2 = textureatlassprite.getU1();
                    float f3 = textureatlassprite.getV1();
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferbuilder = tessellator.getBuilder();
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                    MainWindow window = e.getWindow();
                    bufferbuilder.vertex(0.0D, (double) window.getGuiScaledHeight(), -90.0D).uv(f, f3).endVertex();
                    bufferbuilder
                            .vertex((double) window.getGuiScaledWidth(), (double) window.getGuiScaledHeight(), -90.0D)
                            .uv(f2, f3)
                            .endVertex();
                    bufferbuilder.vertex((double) window.getGuiScaledWidth(), 0.0D, -90.0D).uv(f2, f1).endVertex();
                    bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(f, f1).endVertex();
                    tessellator.end();
                    RenderSystem.depthMask(true);
                    RenderSystem.enableDepthTest();
                    RenderSystem.enableAlphaTest();
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        }
    }
}
