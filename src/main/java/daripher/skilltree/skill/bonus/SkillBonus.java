package daripher.skilltree.skill.bonus;

import daripher.skilltree.client.screen.SkillTreeEditorScreen;
import daripher.skilltree.init.PSTRegistries;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface SkillBonus<T extends SkillBonus<T>> {
  default void onSkillLearned(ServerPlayer player, boolean firstTime) {}

  default void onSkillRemoved(ServerPlayer player) {}

  boolean canMerge(SkillBonus<?> other);

  default boolean sameBonus(SkillBonus<?> other) {
    return canMerge(other);
  }

  SkillBonus<T> merge(SkillBonus<?> other);

  SkillBonus<T> copy();

  T multiply(double multiplier);

  Serializer getSerializer();

  default String getDescriptionId() {
    ResourceLocation id = PSTRegistries.SKILL_BONUSES.get().getKey(getSerializer());
    Objects.requireNonNull(id);
    return "skill_bonus.%s.%s".formatted(id.getNamespace(), id.getPath());
  }

  MutableComponent getTooltip();

  default MutableComponent getAdvancedTooltip() {
    return Component.empty();
  }

  void addEditorWidgets(SkillTreeEditorScreen editor, int index, Consumer<T> consumer);

  interface Ticking {
    void tick(ServerPlayer player);
  }

  interface Serializer extends daripher.skilltree.data.serializers.Serializer<SkillBonus<?>> {
    SkillBonus<?> createDefaultInstance();
  }
}
