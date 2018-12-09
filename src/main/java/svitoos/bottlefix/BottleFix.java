package svitoos.bottlefix;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = BottleFix.MOD_ID, name = BottleFix.MOD_NAME, version = BottleFix.VERSION /*@MCVERSIONDEP@*/)
public class BottleFix {

    public static final String MOD_ID = "@MODID@";
    public static final String MOD_NAME = "@MODNAME@";
    public static final String VERSION = "@MODVERSION@";
    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MOD_ID)
    public static BottleFix INSTANCE;

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onBottleRightClick(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        World world = player.world;
        Item item = player.getHeldItem(event.getHand()).getItem();

        if (item.equals(Items.GLASS_BOTTLE)) {
            ActionResult<ItemStack> actionResult = item.onItemRightClick(event.getWorld(), player, event.getHand());

            if (actionResult.getType() == EnumActionResult.SUCCESS) {
                player.setHeldItem(event.getHand(), actionResult.getResult());

                // Check for interaction with the water source and destroy it
                RayTraceResult raytraceresult = item.rayTrace(world, player, true);
                if (raytraceresult != null && raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK) {
                    BlockPos blockpos = raytraceresult.getBlockPos();
                    if (world.getBlockState(blockpos).getMaterial() == Material.WATER) {
                        world.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 11);
                    }
                }

                // Prevent onItemRightClick from being fired a second time
                event.setCanceled(true);
            }
        }
    }
}
