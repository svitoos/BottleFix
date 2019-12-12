package svitoos.bottlefix.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlassBottleItem.class)
@SuppressWarnings("unused")
public class GlassBottleMixin extends Item {

  public GlassBottleMixin(Settings item$Settings_1) {
    super(item$Settings_1);
  }

  @Inject(
      method =  "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",
      at = @At(value = "INVOKE", ordinal = 1,
          target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"
      ),
      cancellable = true
  )
  @SuppressWarnings("unused")
  private void removeWaterBlock(
      World world,
      PlayerEntity playerEntity,
      Hand hand,
      CallbackInfoReturnable<TypedActionResult<ItemStack>> ci) {
    ItemStack itemStack = playerEntity.getStackInHand(hand);
    HitResult hitResult = rayTrace(world, playerEntity, RayTraceContext.FluidHandling.SOURCE_ONLY);
    if (hitResult.getType() == HitResult.Type.BLOCK) {
      BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
      if (world.canPlayerModifyAt(playerEntity, blockPos)) {
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.getBlock() instanceof FluidDrainable) {
          Fluid fluid =
              ((FluidDrainable) blockState.getBlock()).tryDrainFluid(world, blockPos, blockState);
          if (fluid != Fluids.EMPTY) {
            return;
          }
        }
      }
    }
    ci.setReturnValue(TypedActionResult.pass(itemStack));
  }
}
