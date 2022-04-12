package ftm._0xfmel.melsportals.handlers.events.world.block;

import ftm._0xfmel.melsportals.gameobjects.blocks.CustomPortalBlock;
import ftm._0xfmel.melsportals.utils.WorldUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class NeighborNotifyHandler {
    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent e) {
        IWorld iworld = e.getWorld();
        if (iworld instanceof World) {
            World world = (World) iworld;
            if (WorldUtil.inPortalDimension(world)) {
                BlockState blockState = e.getState();
                if (blockState.getBlock() == Blocks.FIRE) {
                    CustomPortalBlock.trySpawnCustomPortal(world, e.getPos(), true);
                }
            }
        }
    }
}
