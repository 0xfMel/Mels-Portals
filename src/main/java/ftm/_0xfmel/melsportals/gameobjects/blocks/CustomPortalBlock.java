package ftm._0xfmel.melsportals.gameobjects.blocks;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

import com.google.common.cache.LoadingCache;

import org.apache.commons.lang3.tuple.Pair;

import ftm._0xfmel.melsportals.capabilities.customteleport.CustomTeleportCapability;
import ftm._0xfmel.melsportals.capabilities.customteleport.ICustomTeleport;
import ftm._0xfmel.melsportals.client.particles.ColoredPortalParticleData;
import ftm._0xfmel.melsportals.globals.ModGlobals;
import ftm._0xfmel.melsportals.utils.ExposedPatternHelper;
import ftm._0xfmel.melsportals.utils.ParticleUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.PortalSize;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.api.distmarker.Dist;

public class CustomPortalBlock extends NetherPortalBlock {
    public static EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

    public CustomPortalBlock() {
        super(AbstractBlock.Properties.of(Material.PORTAL).noCollission().randomTicks().strength(-1.0F)
                .sound(SoundType.GLASS).lightLevel((p_235463_0_) -> {
                    return 11;
                }));

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(COLOR, DyeColor.PURPLE)
                .setValue(AXIS, Direction.Axis.X));

        this.setRegistryName(ModGlobals.MOD_ID, "custom_portal");

        ModBlocks.BLOCKS.add(this);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(COLOR, AXIS);
    }

    @Override
    public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if (!entityIn.isPassenger() && !entityIn.isVehicle() && entityIn.canChangeDimensions()) {
            entityIn.handleInsidePortal(pos);

            Optional<ICustomTeleport> customTeleport = entityIn
                    .getCapability(CustomTeleportCapability.CUSTOM_TELEPORT_CAPABILITY, null).resolve();

            if (customTeleport.isPresent()
                    && (customTeleport.get().getPortalEntrancePos() == null
                            || !customTeleport.get().getPortalEntrancePos().equals(pos))) {
                customTeleport.get().setPortalEntrancePos(pos.immutable());
            }
        }
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos,
            PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
        ItemStack itemStack = playerIn.getItemInHand(hand);

        if (itemStack.getItem() instanceof DyeItem) {
            DyeColor newColor = DyeColor.getColor(itemStack);
            DyeColor color = state.getValue(COLOR);
            if (newColor != color) {
                if (!worldIn.isClientSide) {
                    AnyShape portal = new AnyShape(worldIn, pos, state.getValue(NetherPortalBlock.AXIS));
                    if (portal.isValid()) {
                        portal.placePortalBlocks(newColor);

                        if (!playerIn.isCreative()) {
                            itemStack.shrink(1);
                        }
                    }

                    return ActionResultType.CONSUME;
                }

                return ActionResultType.SUCCESS;
            }
        }

        return ActionResultType.PASS;
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel,
            BlockPos pCurrentPos, BlockPos pFacingPos) {
        Direction.Axis direction$axis = pFacing.getAxis();
        Direction.Axis direction$axis1 = pState.getValue(AXIS);
        boolean flag = direction$axis1 != direction$axis && direction$axis.isHorizontal();
        AnyShape anyShape = new AnyShape(pLevel, pCurrentPos, direction$axis1);
        return !flag && !pFacingState.is(this)
                && !(anyShape.isValid && anyShape.portalBlocks.size() == anyShape.portalBlockCount)
                        ? Blocks.AIR.defaultBlockState()
                        : pState;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (rand.nextInt(100) == 0) {
            worldIn.playLocalSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D,
                    (double) pos.getZ() + 0.5D, SoundEvents.PORTAL_AMBIENT, SoundCategory.BLOCKS, 0.5F,
                    rand.nextFloat() * 0.4F + 0.8F, false);
        }

        for (int i = 0; i < 4; ++i) {
            double d0 = (double) pos.getX() + rand.nextDouble();
            double d1 = (double) pos.getY() + rand.nextDouble();
            double d2 = (double) pos.getZ() + rand.nextDouble();
            double d3 = ((double) rand.nextFloat() - 0.5D) * 0.5D;
            double d4 = ((double) rand.nextFloat() - 0.5D) * 0.5D;
            double d5 = ((double) rand.nextFloat() - 0.5D) * 0.5D;
            int j = rand.nextInt(2) * 2 - 1;
            if (!worldIn.getBlockState(pos.west()).is(this)
                    && !worldIn.getBlockState(pos.east()).is(this)) {
                d0 = (double) pos.getX() + 0.5D + 0.25D * (double) j;
                d3 = (double) (rand.nextFloat() * 2.0F * (float) j);
            } else {
                d2 = (double) pos.getZ() + 0.5D + 0.25D * (double) j;
                d5 = (double) (rand.nextFloat() * 2.0F * (float) j);
            }

            DyeColor particleColor = stateIn.getValue(COLOR);

            if (particleColor == DyeColor.PURPLE) {
                worldIn.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
            } else {
                int color = particleColor.getColorValue();
                float red = (float) ((color & 0x00FF0000) >> 16) / 255;
                float green = (float) ((color & 0x0000FF00) >> 8) / 255;
                float blue = (float) (color & 0x000000FF) / 255;

                ParticleUtil.spawnParticle(
                        new ColoredPortalParticleData(red, green, blue),
                        worldIn, false, false,
                        d0, d1, d2, d3, d4, d5);

            }
        }
    }

    public static ExposedPatternHelper createCustomPatternHelper(World worldIn, BlockPos p_181089_2_) {
        Direction.Axis enumfacing$axis = Direction.Axis.Z;
        AnyShape blockportal$size = new AnyShape(worldIn, p_181089_2_, Direction.Axis.X);
        LoadingCache<BlockPos, CachedBlockInfo> loadingcache = BlockPattern.createLevelCache(worldIn, true);

        if (!blockportal$size.isValid()) {
            enumfacing$axis = Direction.Axis.X;
            blockportal$size = new AnyShape(worldIn, p_181089_2_, Direction.Axis.Z);
        }

        if (!blockportal$size.isValid()) {
            return new ExposedPatternHelper(p_181089_2_, Direction.NORTH, Direction.UP, loadingcache, 1, 1, 1);
        } else {
            int[] aint = new int[Direction.AxisDirection.values().length];
            Direction enumfacing = blockportal$size.rightDir.getCounterClockWise();
            BlockPos blockpos = blockportal$size.getBottomLeft().above(blockportal$size.getHeight() - 1);

            for (Direction.AxisDirection enumfacing$axisdirection : Direction.AxisDirection.values()) {
                BlockPattern.PatternHelper blockpattern$patternhelper = new BlockPattern.PatternHelper(
                        enumfacing.getAxisDirection() == enumfacing$axisdirection ? blockpos
                                : blockpos.relative(blockportal$size.rightDir, blockportal$size.getWidth() - 1),
                        Direction.fromAxisAndDirection(enumfacing$axis, enumfacing$axisdirection), Direction.UP,
                        loadingcache, blockportal$size.getWidth(), blockportal$size.getHeight(), 1);

                for (int i = 0; i < blockportal$size.getWidth(); ++i) {
                    for (int j = 0; j < blockportal$size.getHeight(); ++j) {
                        CachedBlockInfo blockworldstate = blockpattern$patternhelper.getBlock(i, j, 1);

                        if (blockworldstate.getState() != null
                                && blockworldstate.getState().getMaterial() != Material.AIR) {
                            ++aint[enumfacing$axisdirection.ordinal()];
                        }
                    }
                }
            }

            Direction.AxisDirection enumfacing$axisdirection1 = Direction.AxisDirection.POSITIVE;

            for (Direction.AxisDirection enumfacing$axisdirection2 : Direction.AxisDirection.values()) {
                if (aint[enumfacing$axisdirection2.ordinal()] < aint[enumfacing$axisdirection1.ordinal()]) {
                    enumfacing$axisdirection1 = enumfacing$axisdirection2;
                }
            }

            return new ExposedPatternHelper(
                    enumfacing.getAxisDirection() == enumfacing$axisdirection1 ? blockpos
                            : blockpos.relative(blockportal$size.rightDir, blockportal$size.getWidth() - 1),
                    Direction.fromAxisAndDirection(enumfacing$axis, enumfacing$axisdirection1), Direction.UP,
                    loadingcache, blockportal$size.getWidth(), blockportal$size.getHeight(), 1);
        }
    }

    public static boolean trySpawnCustomPortal(World worldIn, BlockPos pos, boolean placeIfCustom) {
        PortalSize blockportal$sizeVanilla = new PortalSize(worldIn, pos, Direction.Axis.X);
        if (blockportal$sizeVanilla.isValid())
            return false;
        PortalSize blockportal$size1Vanilla = new PortalSize(worldIn, pos, Direction.Axis.Z);
        if (blockportal$size1Vanilla.isValid())
            return false;

        AnyShape anyShapePortal = new AnyShape(worldIn, pos, Direction.Axis.X);

        if (anyShapePortal.isValid() && anyShapePortal.portalBlockCount == 0) {
            Optional<PortalSize> portalSize = ForgeEventFactory.onTrySpawnPortal(worldIn, pos,
                    Optional.of(blockportal$sizeVanilla));
            if (!portalSize.isPresent() || !portalSize.get().isValid()) {
                if (placeIfCustom) {
                    anyShapePortal.placePortalBlocks();
                }
                return true;
            }
        }

        AnyShape anyShapePortal1 = new AnyShape(worldIn, pos,
                Direction.Axis.Z);

        if (anyShapePortal1.isValid() && anyShapePortal1.portalBlockCount == 0) {
            Optional<PortalSize> portalSize = ForgeEventFactory.onTrySpawnPortal(worldIn, pos,
                    Optional.of(blockportal$size1Vanilla));
            if (!portalSize.isPresent() || !portalSize.get().isValid()) {
                if (placeIfCustom) {
                    anyShapePortal1.placePortalBlocks();
                }
                return true;
            }
        }

        return false;
    }

    public static class BlockColorHandlerCustomPortal implements IBlockColor {
        @Override
        public int getColor(BlockState state, IBlockDisplayReader bdr, BlockPos pos, int tintIndex) {
            DyeColor color = state.getValue(COLOR);
            if (color == DyeColor.PURPLE)
                return -1;
            return color.getColorValue();
        }
    }

    public static class AnyShape {
        private final IWorld world;
        private final Direction.Axis axis;
        private final Direction rightDir;
        private final List<Point> portalBlocks;
        private boolean isValid = false;
        private final BlockPos startPos;
        private int portalBlockCount = 0;
        private boolean hasCustomFrame = false;

        private enum EnumTravelDirection {
            NEITHER,
            LEFT,
            RIGHT
        }

        public AnyShape(IWorld worldIn, BlockPos startPos, Direction.Axis axis) {
            this.startPos = startPos;
            this.world = worldIn;
            this.axis = axis;
            this.portalBlocks = new ArrayList<Point>();

            Direction leftDir;
            if (axis == Direction.Axis.X) {
                leftDir = Direction.EAST;
                rightDir = Direction.WEST;
            } else {
                leftDir = Direction.NORTH;
                rightDir = Direction.SOUTH;
            }

            List<Point> visited = new ArrayList<Point>();
            Queue<Pair<Point, EnumTravelDirection>> toVisit = new LinkedList<Pair<Point, EnumTravelDirection>>();

            Stack<Point> upStack = new Stack<Point>();
            Stack<Point> leftStack = new Stack<Point>();
            Stack<Point> rightStack = new Stack<Point>();

            BlockPos.Mutable pos = new BlockPos.Mutable().set(startPos);

            for (int y = startPos.getY(); y >= 0; y = pos.move(Direction.DOWN).getY()) {
                BlockState blockState = worldIn.getBlockState(pos);
                Point current = AnyShape.getPoint(pos, this.axis);
                visited.add(current);
                if (this.isEmptyBlock(blockState)) {
                    if (AnyShape.isPortal(blockState)) {
                        this.portalBlockCount++;
                    }
                    this.portalBlocks.add(current);
                    upStack.push(current);
                } else if (this.isFrameBlock(blockState)) {
                    break;
                } else {
                    return;
                }
            }

            pos.set(startPos).move(Direction.UP);

            for (int y = startPos.getY(); y < 256; y = pos.move(Direction.UP).getY()) {
                BlockState blockState = worldIn.getBlockState(pos);
                Point current = AnyShape.getPoint(pos, this.axis);
                visited.add(current);
                if (this.isEmptyBlock(blockState)) {
                    if (AnyShape.isPortal(blockState)) {
                        this.portalBlockCount++;
                    }
                    this.portalBlocks.add(current);
                    upStack.push(current);
                } else {
                    if (this.isFrameBlock(blockState)) {
                        toVisit.add(Pair.of(AnyShape.getPoint(pos.move(Direction.DOWN), this.axis),
                                EnumTravelDirection.NEITHER));
                        upStack.pop();
                        break;
                    } else {
                        return;
                    }
                }
            }

            if (toVisit.size() == 0)
                return;

            Direction[] leftFacings = new Direction[] { Direction.UP, leftDir, Direction.DOWN, rightDir };
            Direction[] rightFacings = new Direction[] { Direction.UP, rightDir, Direction.DOWN, leftDir };

            visitLoop: while (toVisit.size() > 0) {
                Pair<Point, EnumTravelDirection> currentPair = toVisit.remove();
                Point current = currentPair.getKey();
                EnumTravelDirection currentGoing = currentPair.getValue();
                Stack<Point> goingStack;
                switch (currentGoing) {
                    case LEFT:
                        goingStack = leftStack;
                        break;
                    case RIGHT:
                        goingStack = rightStack;
                        break;
                    case NEITHER:
                        goingStack = upStack;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + currentGoing);
                }
                AnyShape.changePos(pos, current.x, current.y, axis);

                boolean i = false;
                do {
                    EnumTravelDirection going = (currentGoing == EnumTravelDirection.NEITHER && i)
                            || currentGoing == EnumTravelDirection.RIGHT ? EnumTravelDirection.RIGHT
                                    : EnumTravelDirection.LEFT;
                    Direction[] facings = going == EnumTravelDirection.LEFT ? leftFacings : rightFacings;
                    boolean addedToVisit = false;
                    for (Direction facing : facings) {
                        BlockPos offsetPos = pos.relative(facing);
                        Point offsetPoint = AnyShape.getPoint(offsetPos, this.axis);

                        if ((facing == leftDir || facing == rightDir) && !this.isBlockChunkLoaded(offsetPos)) {
                            return;
                        }

                        if (!visited.contains(offsetPoint)) {
                            BlockState offsetState = worldIn.getBlockState(offsetPos);
                            if (this.isEmptyBlock(offsetState) && offsetPoint.y < 256) {
                                if (AnyShape.isPortal(offsetState)) {
                                    this.portalBlockCount++;
                                }
                                this.portalBlocks.add(offsetPoint);
                                toVisit.add(Pair.of(offsetPoint, going));
                                visited.add(offsetPoint);
                                addedToVisit = true;
                                break;
                            } else if (!this.isFrameBlock(offsetState)) {
                                return;
                            }
                        }
                    }
                    if (!addedToVisit) {
                        if (!goingStack.empty()) {
                            toVisit.add(Pair.of(goingStack.pop(), currentGoing));
                        } else if (!upStack.isEmpty()) {
                            toVisit.add(Pair.of(upStack.pop(), currentGoing));
                        }
                        continue visitLoop;
                    }
                } while ((i = !i) && currentGoing == EnumTravelDirection.NEITHER);

                goingStack.push(current);
            }

            isValid = true;
        }

        private static int getHorizontalCoord(BlockPos pos, Direction.Axis axis) {
            if (axis == Direction.Axis.X) {
                return pos.getX();
            }
            return pos.getZ();
        }

        private static Point getPoint(BlockPos pos, Direction.Axis axis) {
            return new Point(AnyShape.getHorizontalCoord(pos, axis), pos.getY());
        }

        private static void changePos(BlockPos.Mutable pos, int h, int y, Direction.Axis axis) {
            if (axis == Direction.Axis.X) {
                pos.set(h, y, pos.getZ());
            } else {
                pos.set(pos.getX(), y, h);
            }
        }

        private boolean isBlockChunkLoaded(BlockPos pos) {
            int x = pos.getX() / 16;
            int z = pos.getZ() / 16;

            return this.world.getChunkSource().hasChunk(x, z);
        }

        protected boolean isEmptyBlock(BlockState state) {
            Block block = state.getBlock();
            return state.getMaterial() == Material.AIR || block == Blocks.FIRE || block == ModBlocks.CUSTOM_PORTAL
                    || block == Blocks.NETHER_PORTAL;
        }

        private boolean isFrameBlock(BlockState state) {
            return state.getBlock() == Blocks.OBSIDIAN;
        }

        public boolean isValid() {
            return isValid;
        }

        public BlockPos getBottomLeft() {
            if (!this.isValid)
                return null;

            Integer minX = null;
            Integer maxX = null;
            Integer minY = null;

            for (Point point : this.portalBlocks) {
                if (maxX == null || maxX < point.x) {
                    maxX = point.x;
                }
                if (minX == null || minX > point.x) {
                    minX = point.x;
                }
                if (minY == null || minY > point.y) {
                    minY = point.y;
                }
            }

            BlockPos.Mutable pos = new BlockPos.Mutable().set(this.startPos);
            AnyShape.changePos(pos, this.axis == Direction.Axis.X ? maxX : minX, minY, this.axis);
            return pos;
        }

        public int getHeight() {
            if (!this.isValid)
                return 0;

            Integer maxY = null;
            Integer minY = null;

            for (Point point : this.portalBlocks) {
                if (maxY == null || maxY < point.y) {
                    maxY = point.y;
                }
                if (minY == null || minY > point.y) {
                    minY = point.y;
                }
            }

            return maxY - minY + 1;
        }

        public int getWidth() {
            if (!this.isValid)
                return 0;

            Integer maxX = null;
            Integer minX = null;

            for (Point point : this.portalBlocks) {
                if (maxX == null || maxX < point.x) {
                    maxX = point.x;
                }
                if (minX == null || minX > point.x) {
                    minX = point.x;
                }
            }

            return maxX - minX + 1;
        }

        public static boolean isPortal(BlockState state) {
            Block block = state.getBlock();
            return block == ModBlocks.CUSTOM_PORTAL || block == Blocks.NETHER_PORTAL;
        }

        public boolean isValidVanilla() {
            if (!this.isValid || this.hasCustomFrame)
                return false;

            Integer maxX = null;
            Integer maxY = null;
            Integer minX = null;
            Integer minY = null;

            for (Point point : this.portalBlocks) {
                if (maxX == null || maxX < point.x) {
                    maxX = point.x;
                }
                if (minX == null || minX > point.x) {
                    minX = point.x;
                }

                if (maxY == null || maxY < point.y) {
                    maxY = point.y;
                }
                if (minY == null || minY > point.y) {
                    minY = point.y;
                }
            }

            int width = maxX - minX + 1;
            int height = maxY - minY + 1;

            if (!(width >= 2 && width <= 21 && height >= 3 && height <= 21)) {
                return false;
            }

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    Point p = new Point(x, y);
                    if (!this.portalBlocks.contains(p))
                        return false;
                }
            }
            return true;
        }

        private void placePortalBlocks(BlockState placeBlockState) {
            BlockPos.Mutable pos = new BlockPos.Mutable().set(this.startPos);
            for (Point point : this.portalBlocks) {
                AnyShape.changePos(pos, point.x, point.y, this.axis);
                this.world.setBlock(pos, placeBlockState, 18);
            }
        }

        public void placePortalBlocks() {
            this.placePortalBlocks(DyeColor.PURPLE);
        }

        public void placePortalBlocks(DyeColor color) {
            if (color == DyeColor.PURPLE && this.isValidVanilla()) {
                this.placePortalBlocks(
                        Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, this.axis));
            } else {
                this.placePortalBlocks(
                        ModBlocks.CUSTOM_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, this.axis)
                                .setValue(COLOR, color));
            }
        }
    }
}
