package daripher.skilltree.item.quiver;

import java.util.List;
import java.util.Optional;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.item.ItemHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingGetProjectileEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

@EventBusSubscriber(modid = SkillTreeMod.MOD_ID)
public class QuiverItem extends Item implements ICurioItem {
	private static final String ARROWS_TAG = "Arrows";
	private static final String ARROWS_COUNT_TAG = "ArrowsCount";
	private final int capacity;

	public QuiverItem(int capacity) {
		super(new Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1).durability(capacity));
		this.capacity = capacity;
	}

	public QuiverItem() {
		this(250);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> components, TooltipFlag tooltipFlag) {
		Component capacity = Component.literal("" + getCapacity(stack)).withStyle(ChatFormatting.BLUE);
		components.add(Component.translatable("quiver.capacity", capacity).withStyle(ChatFormatting.YELLOW));
		if (!containsArrows(stack)) return;
		ItemStack arrows = getArrows(stack);
		Component arrowName = Component.empty().append(arrows.getHoverName()).withStyle(ChatFormatting.GRAY);
		Component contents = Component.translatable("quiver.contents", arrowName).withStyle(ChatFormatting.YELLOW);
		components.add(contents);
		arrows.getItem().appendHoverText(arrows, level, components, tooltipFlag);
	}

	@Override
	public int getDamage(ItemStack stack) {
		if (!containsArrows(stack)) return 0;
		return getCapacity(stack) - getArrowsCount(stack);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return false;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide) return InteractionResultHolder.success(stack);
		int arrowsLeft = getArrowsCount(stack);
		while (arrowsLeft >= 64) {
			dropArrows(player, stack, 64);
			arrowsLeft -= 64;
		}
		if (arrowsLeft > 0) dropArrows(player, stack, arrowsLeft);
		setArrows(stack, ItemStack.EMPTY, 0);
		return InteractionResultHolder.success(stack);
	}

	@SubscribeEvent
	public static void storeArrowsOnPickup(EntityItemPickupEvent event) {
		ItemStack arrowsStack = event.getItem().getItem();
		if (!(arrowsStack.getItem() instanceof ArrowItem arrow)) return;
		Optional<SlotResult> quiverCurio = CuriosApi.getCuriosHelper().findFirstCurio(event.getEntity(), QuiverItem::isQuiver);
		quiverCurio.map(SlotResult::stack).ifPresent(quiver -> {
			if (isFull(quiver)) return;
			if (!containsArrows(quiver)) {
				setArrows(quiver, arrowsStack.copy(), arrowsStack.getCount());
				event.getItem().setItem(ItemStack.EMPTY);
				event.setResult(Result.ALLOW);
			} else if (ItemStack.isSame(getArrows(quiver), arrowsStack)) {
				int capacity = getCapacity(quiver);
				int arrowsTaken = Math.min(capacity - getArrowsCount(quiver), arrowsStack.getCount());
				addArrows(quiver, arrowsTaken);
				if (arrowsStack.getCount() == arrowsTaken) event.getItem().setItem(ItemStack.EMPTY);
				else arrowsStack.shrink(arrowsTaken);
				event.setResult(Result.ALLOW);
			}
		});
	}

	@SubscribeEvent
	public static void takeArrowFromQuiver(LivingGetProjectileEvent event) {
		Optional<SlotResult> quiverCurio = CuriosApi.getCuriosHelper().findFirstCurio(event.getEntity(), QuiverItem::isQuiver);
		quiverCurio.map(SlotResult::stack).ifPresent(quiver -> {
			if (containsArrows(quiver)) event.setProjectileItemStack(getArrows(quiver).copy());
		});
	}

	@SubscribeEvent
	public static void removeArrowFromQuiver(ArrowLooseEvent event) {
		if (event.getEntity().isCreative()) return;
		Optional<SlotResult> quiverCurio = CuriosApi.getCuriosHelper().findFirstCurio(event.getEntity(), QuiverItem::isQuiver);
		quiverCurio.map(SlotResult::stack).ifPresent(quiver -> {
			if (containsArrows(quiver)) addArrows(quiver, -1);
		});
	}

	private void dropArrows(Player player, ItemStack stack, int count) {
		ItemStack arrowsStack = getArrows(stack).copy();
		arrowsStack.setCount(count);
		player.spawnAtLocation(arrowsStack);
	}

	private static boolean isFull(ItemStack quiver) {
		return getArrowsCount(quiver) == getCapacity(quiver);
	}

	private static int getCapacity(ItemStack quiver) {
		int capacity = ((QuiverItem) quiver.getItem()).capacity;
		if (ItemHelper.hasBonus(quiver, ItemHelper.CAPACITY)) {
			capacity *= 1 + ItemHelper.getBonus(quiver, ItemHelper.CAPACITY);
		}
		return capacity;
	}

	public static boolean containsArrows(ItemStack stack) {
		return stack.hasTag() && stack.getTag().contains(ARROWS_TAG) && !getArrows(stack).isEmpty() && getArrowsCount(stack) > 0;
	}

	public static ItemStack getArrows(ItemStack stack) {
		return ItemStack.of((CompoundTag) stack.getOrCreateTag().get(ARROWS_TAG));
	}

	public static int getArrowsCount(ItemStack stack) {
		return stack.getOrCreateTag().getInt(ARROWS_COUNT_TAG);
	}

	public static void setArrows(ItemStack stack, ItemStack arrows, int count) {
		arrows = arrows.copy();
		arrows.setCount(1);
		stack.getOrCreateTag().put(ARROWS_TAG, arrows.save(new CompoundTag()));
		setArrowsCount(stack, count);
	}

	public static void setArrows(ItemStack stack, ItemStack arrow) {
		stack.getOrCreateTag().put(ARROWS_TAG, arrow.save(new CompoundTag()));
	}

	public static void setArrowsCount(ItemStack stack, int count) {
		stack.getOrCreateTag().putInt(ARROWS_COUNT_TAG, count);
	}

	public static void addArrows(ItemStack stack, int count) {
		stack.getOrCreateTag().putInt(ARROWS_COUNT_TAG, getArrowsCount(stack) + count);
	}

	public static boolean isQuiver(ItemStack stack) {
		return stack.getItem() instanceof QuiverItem;
	}
}