package daripher.skilltree.skill.bonus.player;

import com.google.gson.*;
import daripher.skilltree.client.screen.SkillTreeEditor;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.data.SerializationHelper;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.condition.item.ItemCondition;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

public record RepairEfficiencyBonus(ItemCondition itemCondition, float multiplier)
    implements SkillBonus<RepairEfficiencyBonus> {
  @Override
  public SkillBonus.Serializer getSerializer() {
    return PSTSkillBonuses.REPAIR_EFFICIENCY.get();
  }

  @Override
  public SkillBonus<RepairEfficiencyBonus> copy() {
    return new RepairEfficiencyBonus(itemCondition, multiplier);
  }

  @Override
  public RepairEfficiencyBonus multiply(double multiplier) {
    return new RepairEfficiencyBonus(itemCondition, (float) (multiplier * multiplier));
  }

  @Override
  public boolean canMerge(SkillBonus<?> other) {
    if (!(other instanceof RepairEfficiencyBonus otherBonus)) return false;
    return Objects.equals(otherBonus.itemCondition, this.itemCondition);
  }

  @Override
  public SkillBonus<RepairEfficiencyBonus> merge(SkillBonus<?> other) {
    if (!(other instanceof RepairEfficiencyBonus otherBonus)) {
      throw new IllegalArgumentException();
    }
    return new RepairEfficiencyBonus(itemCondition, otherBonus.multiplier + this.multiplier);
  }

  @Override
  public MutableComponent getTooltip() {
    MutableComponent itemDescription =
        TooltipHelper.getOptionalTooltip(itemCondition.getDescriptionId(), "repaired");
    double visibleMultiplier = multiplier * 100;
    if (multiplier < 0) visibleMultiplier *= -1;
    String operationDescription = multiplier > 0 ? "plus" : "take";
    operationDescription = "attribute.modifier." + operationDescription + ".1";
    String multiplierDescription = ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(visibleMultiplier);
    MutableComponent bonusDescription = Component.translatable(getDescriptionId() + ".bonus");
    bonusDescription =
        Component.translatable(operationDescription, multiplierDescription, bonusDescription)
            .withStyle(Style.EMPTY.withColor(0x7AB3E2));
    return Component.translatable(getDescriptionId(), itemDescription, bonusDescription)
        .withStyle(Style.EMPTY.withColor(0x7B7BE5));
  }

  @Override
  public void addEditorWidgets(SkillTreeEditor editor, int row) {
    // TODO: add widgets
  }

  public static class Serializer implements SkillBonus.Serializer {
    @Override
    public RepairEfficiencyBonus deserialize(JsonObject json) throws JsonParseException {
      ItemCondition condition = SerializationHelper.deserializeItemCondition(json);
      float multiplier = json.get("multiplier").getAsFloat();
      return new RepairEfficiencyBonus(condition, multiplier);
    }

    @Override
    public void serialize(JsonObject json, SkillBonus<?> bonus) {
      if (!(bonus instanceof RepairEfficiencyBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      SerializationHelper.serializeItemCondition(json, aBonus.itemCondition);
      json.addProperty("multiplier", aBonus.multiplier);
    }

    @Override
    public RepairEfficiencyBonus deserialize(CompoundTag tag) {
      ItemCondition condition = SerializationHelper.deserializeItemCondition(tag);
      float multiplier = tag.getFloat("multiplier");
      return new RepairEfficiencyBonus(condition, multiplier);
    }

    @Override
    public CompoundTag serialize(SkillBonus<?> bonus) {
      if (!(bonus instanceof RepairEfficiencyBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      CompoundTag tag = new CompoundTag();
      SerializationHelper.serializeItemCondition(tag, aBonus.itemCondition);
      tag.putFloat("multiplier", aBonus.multiplier);
      return tag;
    }

    @Override
    public RepairEfficiencyBonus deserialize(FriendlyByteBuf buf) {
      return new RepairEfficiencyBonus(NetworkHelper.readItemCondition(buf), buf.readFloat());
    }

    @Override
    public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
      if (!(bonus instanceof RepairEfficiencyBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      NetworkHelper.writeItemCondition(buf, aBonus.itemCondition);
      buf.writeFloat(aBonus.multiplier);
    }
  }
}
