package svitoos.bottlefix;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.BeeHiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.fluid.BaseFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

@SuppressWarnings("unused")
public class BottleFixMod implements ModInitializer {
  @Override
  public void onInitialize() {
    // This code runs as soon as Minecraft is in a mod-load-ready state.
    // However, some things (like resources) may still be uninitialized.
    // Proceed with mild caution.

    DispenserBlock.registerBehavior(
        Items.GLASS_BOTTLE.asItem(),
        new ItemDispenserBehavior() {
          private final ItemDispenserBehavior fallback = new ItemDispenserBehavior();

          private ItemStack fill(
              BlockPointer blockPointer, ItemStack itemStack, ItemStack filledItemStack) {
            itemStack.decrement(1);
            if (itemStack.isEmpty()) {
              return filledItemStack.copy();
            } else {
              if (((DispenserBlockEntity) blockPointer.getBlockEntity())
                      .addToFirstFreeSlot(filledItemStack.copy())
                  < 0) {
                this.fallback.dispense(blockPointer, filledItemStack.copy());
              }

              return itemStack;
            }
          }

          public ItemStack dispenseSilently(BlockPointer blockPointer, ItemStack itemStack) {
            IWorld world = blockPointer.getWorld();
            BlockPos blockPos =
                blockPointer
                    .getBlockPos()
                    .offset(blockPointer.getBlockState().get(DispenserBlock.FACING));
            BlockState blockState = world.getBlockState(blockPos);
            Block block = blockState.getBlock();

            if (block.matches(BlockTags.BEEHIVES)
                && blockState.get(BeeHiveBlock.HONEY_LEVEL) >= 5) {
              ((BeeHiveBlock) blockState.getBlock())
                  .emptyHoney(world.getWorld(), blockState, blockPos, null);
              return this.fill(
                  blockPointer, itemStack, new ItemStack(Items.HONEY_BOTTLE));
            } else {
              if (world.getFluidState(blockPos).matches(FluidTags.WATER)
                  && block instanceof FluidDrainable) {
                Fluid fluid =
                    ((FluidDrainable) block).tryDrainFluid(world, blockPos, blockState);
                if (fluid instanceof BaseFluid) {
                  return this.fill(
                      blockPointer,
                      itemStack,
                      PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER));
                }
              }
              return itemStack;
            }
          }
        });
  }
}
