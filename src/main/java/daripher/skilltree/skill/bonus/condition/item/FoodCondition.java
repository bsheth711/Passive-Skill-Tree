package daripher.skilltree.skill.bonus.condition.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.init.PSTItemConditions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public record FoodCondition() implements ItemCondition {
  @Override
  public boolean met(ItemStack stack) {
    return stack.getFoodProperties(null) != null;
  }

  @Override
  public boolean equals(Object obj) {
    return obj.getClass() == this.getClass();
  }

  @Override
  public int hashCode() {
    return getSerializer().hashCode();
  }

  @Override
  public ItemCondition.Serializer getSerializer() {
    return PSTItemConditions.FOOD.get();
  }

  public static class Serializer implements ItemCondition.Serializer {
    @Override
    public ItemCondition deserialize(JsonObject json) throws JsonParseException {
      return new FoodCondition();
    }

    @Override
    public void serialize(JsonObject json, ItemCondition condition) {
      if (!(condition instanceof FoodCondition)) {
        throw new IllegalArgumentException();
      }
    }

    @Override
    public ItemCondition deserialize(CompoundTag tag) {
      return new FoodCondition();
    }

    @Override
    public CompoundTag serialize(ItemCondition condition) {
      if (!(condition instanceof FoodCondition)) {
        throw new IllegalArgumentException();
      }
      return new CompoundTag();
    }

    @Override
    public ItemCondition deserialize(FriendlyByteBuf buf) {
      return new FoodCondition();
    }

    @Override
    public void serialize(FriendlyByteBuf buf, ItemCondition condition) {
      if (!(condition instanceof FoodCondition)) {
        throw new IllegalArgumentException();
      }
    }
  }
}
