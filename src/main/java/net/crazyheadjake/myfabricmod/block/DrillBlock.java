package net.crazyheadjake.myfabricmod.block;

import java.util.HashMap;

import org.jspecify.annotations.Nullable;

import net.crazyheadjake.myfabricmod.AutomationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.state.StateDefinition;

public class DrillBlock extends Block {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final HashMap<Block, Item> MINING_BLOCKS = new HashMap<>();
    static {
        MINING_BLOCKS.put(Blocks.STONE, Items.COBBLESTONE);
        MINING_BLOCKS.put(Blocks.COAL_ORE, Items.COAL);
        MINING_BLOCKS.put(Blocks.IRON_ORE, Items.RAW_IRON);
        MINING_BLOCKS.put(Blocks.COPPER_ORE, Items.RAW_COPPER);
        MINING_BLOCKS.put(Blocks.GOLD_ORE, Items.RAW_GOLD);
        MINING_BLOCKS.put(Blocks.REDSTONE_ORE, Items.REDSTONE);
    }
    public static final int DRILL_SPEED = 40; // Ticks between item spawns
    public DrillBlock(Properties properties) {
        super(properties.noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
        .setValue(FACING, Direction.NORTH)
        .setValue(HALF, DoubleBlockHalf.LOWER));
    }
    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        // Schedule the first tick for 40 ticks (2 seconds) later
        if (!level.isClientSide()) {
            level.scheduleTick(blockPos, this, DRILL_SPEED);
        }
    }

    @Override
	public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		if (!level.isClientSide() && (player.preventsBlockDrops() || !player.hasCorrectToolForDrops(blockState))) {
			DoubleBlockHalf doubleBlockHalf = blockState.getValue(HALF);
            if (doubleBlockHalf == DoubleBlockHalf.UPPER) {
                BlockPos blockPos2 = blockPos.below();
                BlockState blockState2 = level.getBlockState(blockPos2);
                if (blockState2.is(blockState.getBlock()) && blockState2.getValue(HALF) == DoubleBlockHalf.LOWER) {
                    BlockState blockState3 = blockState2.getFluidState().is(Fluids.WATER) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
                    level.setBlock(blockPos2, blockState3, 35);
                    level.levelEvent(player, 2001, blockPos2, Block.getId(blockState2));
                }
            }
		}

		return super.playerWillDestroy(level, blockPos, blockState, player);
	}

    @Override
	protected BlockState updateShape(
		BlockState blockState,
		LevelReader levelReader,
		ScheduledTickAccess scheduledTickAccess,
		BlockPos blockPos,
		Direction direction,
		BlockPos blockPos2,
		BlockState blockState2,
		RandomSource randomSource
	) {
		DoubleBlockHalf doubleBlockHalf = blockState.getValue(HALF);
		if (direction.getAxis() != Direction.Axis.Y || doubleBlockHalf == DoubleBlockHalf.LOWER != (direction == Direction.UP)) {
			return doubleBlockHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !blockState.canSurvive(levelReader, blockPos)
				? Blocks.AIR.defaultBlockState()
				: super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
		} else {
			return blockState2.getBlock() instanceof DrillBlock && blockState2.getValue(HALF) != doubleBlockHalf
				? blockState2.setValue(HALF, doubleBlockHalf)
				: Blocks.AIR.defaultBlockState();
		}
	}

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        // 1. Check if block below the drill is a valid mining block
        BlockState blockStateBelow = serverLevel.getBlockState(blockPos.below());
        if (!MINING_BLOCKS.containsKey(blockStateBelow.getBlock())) {
            // If not, do nothing and return
            serverLevel.scheduleTick(blockPos, this, DRILL_SPEED);
            return;
        }
        ItemStack stack = new ItemStack(MINING_BLOCKS.get(blockStateBelow.getBlock()), 1);
        // Get the position of the adjacent block the drill is facing
        Direction facing = blockState.getValue(FACING);
        BlockPos adjacentPos = blockPos.relative(facing);
        BlockEntity adjacentBlockEntity = serverLevel.getBlockEntity(adjacentPos);

        // Check the spawn location to see if it is a container
        BlockState adjacentState = serverLevel.getBlockState(adjacentPos);
        // If air, spawn item entity
        if (adjacentState.getBlock() == Blocks.AIR) {
            ItemEntity itemEntity = new ItemEntity(serverLevel, adjacentPos.getX() + 0.5, adjacentPos.getY(), adjacentPos.getZ() + 0.5, stack);
            serverLevel.addFreshEntity(itemEntity);
        }
        // If AutomationBlockEntity, try to insert
        else if (adjacentBlockEntity instanceof AutomationBlockEntity automationBlockEntity) {
            if (automationBlockEntity.hasSpaceFor(stack) > 0) {
                automationBlockEntity.insert(stack);
            }
        }
        // If WorldlyContainer, try to insert
        else if (adjacentBlockEntity instanceof Container container) {
            HopperBlockEntity.addItem(
        null,
                container,
                stack,
                facing.getOpposite()
            );
        }
        
        serverLevel.scheduleTick(blockPos, this, DRILL_SPEED);

    }
    	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		Level level = blockPlaceContext.getLevel();
		if (blockPos.getY() < level.getMaxY() && level.getBlockState(blockPos.above()).canBeReplaced(blockPlaceContext)) {
			return this.defaultBlockState()
				.setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite())
				.setValue(HALF, DoubleBlockHalf.LOWER);
		} else {
			return null;
		}
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
		level.setBlock(blockPos.above(), blockState.setValue(HALF, DoubleBlockHalf.UPPER), 3);
	}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }
}
