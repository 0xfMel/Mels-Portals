package ftm._0xfmel.melsportals.handlers.events.entity.interact.player;

import java.util.Random;

import ftm._0xfmel.melsportals.gameobjects.blocks.CustomPortalBlock;
import ftm._0xfmel.melsportals.utils.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PlayerInteractHandler {
    private static Random handlerRand = new Random();

    @SubscribeEvent
    public static void onPlayerInteractPortal(PlayerInteractEvent.RightClickBlock e) {
        World worldIn = e.getWorld();
        BlockPos pos = e.getPos();
        BlockState blockState = worldIn.getBlockState(pos);
        Block block = blockState.getBlock();

        ItemStack itemStack = e.getItemStack();
        Item item = itemStack.getItem();

        if (item instanceof DyeItem && block == Blocks.NETHER_PORTAL) {
            PlayerEntity player = e.getPlayer();
            DyeColor color = DyeColor.getColor(itemStack);

            if (!player.isCrouching() && color != DyeColor.PURPLE) {
                e.setCanceled(true);

                if (worldIn.isClientSide) {
                    e.setCancellationResult(ActionResultType.SUCCESS);
                } else {
                    CustomPortalBlock.AnyShape portal = new CustomPortalBlock.AnyShape(worldIn, pos,
                            blockState.getValue(NetherPortalBlock.AXIS));
                    if (portal.isValid()) {
                        portal.placePortalBlocks(color);

                        if (!player.isCreative()) {
                            itemStack.shrink(1);
                        }
                    }

                    e.setCancellationResult(ActionResultType.CONSUME);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInteractObsidian(PlayerInteractEvent.RightClickBlock e) {
        World worldIn = e.getWorld();
        if (!WorldUtil.inPortalDimension(worldIn))
            return;
        BlockPos pos = e.getPos();
        BlockState blockState = worldIn.getBlockState(pos);
        Block block = blockState.getBlock();

        ItemStack itemStack = e.getItemStack();
        Item item = itemStack.getItem();

        Direction face = e.getFace();

        if ((item == Items.FLINT_AND_STEEL)
                || (item == Items.FIRE_CHARGE) && block == Blocks.OBSIDIAN && face != null) {
            BlockPos firePos = pos.relative(face);

            BlockState fireState = worldIn.getBlockState(firePos);
            if (fireState.getMaterial() == Material.AIR
                    && !((FireBlock) Blocks.FIRE).canSurvive(fireState, worldIn, firePos)) {

                if (CustomPortalBlock.trySpawnCustomPortal(worldIn, firePos, !worldIn.isClientSide)) {
                    if (worldIn.isClientSide) {
                        worldIn.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 18);
                    }

                    if (item == Items.FIRE_CHARGE) {
                        worldIn.playSound((PlayerEntity) null, pos, SoundEvents.FIRECHARGE_USE,
                                SoundCategory.BLOCKS, 1.0F,
                                (handlerRand.nextFloat() - handlerRand.nextFloat()) * 0.2F + 1.0F);

                        if (!e.getPlayer().isCreative()) {
                            itemStack.shrink(1);
                        }
                    } else {
                        worldIn.playSound((PlayerEntity) null, pos, SoundEvents.FLINTANDSTEEL_USE,
                                SoundCategory.BLOCKS,
                                1.0F, handlerRand.nextFloat() * 0.4F + 0.8F);

                        PlayerEntity player = e.getPlayer();

                        if (!player.isCreative()) {
                            itemStack.hurtAndBreak(1, player, (p_219998_1_) -> {
                                p_219998_1_.broadcastBreakEvent(e.getHand());
                            });
                        }
                    }

                    e.setCanceled(true);
                    e.setCancellationResult(ActionResultType.sidedSuccess(worldIn.isClientSide));
                }
            }
        }
    }
}
