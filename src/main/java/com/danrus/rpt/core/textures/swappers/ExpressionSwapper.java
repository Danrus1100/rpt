package com.danrus.rpt.core.textures.swappers;

import com.danrus.rpt.core.expression.ExpressionsCase;
import com.danrus.rpt.core.expression.GameExpressionsHelper;
import com.danrus.rpt.core.expression.NumberOrString;
import com.danrus.rpt.core.textures.TextureSwapper;
import com.danrus.rpt.core.textures.TextureSwappers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ExpressionSwapper(String main, List<ExpressionsCase<TextureSwapper>> cases, TextureSwapper fallback) implements TextureSwapper {
    @Override
    public void swap(ItemStack stack, @Nullable LivingEntity entity, List<ResourceLocation> pendingSwapperApply) {
        Level level = entity != null ? entity.level() : null;
        ClientLevel clientLevel = level != null && level.isClientSide() ? (ClientLevel) level : null;

        GameExpressionsHelper.selectValueFromCases(cases, main, Map.of(), clientLevel, entity, entity != null ? entity.getId() : 0, fallback)
                .swap(stack, entity, pendingSwapperApply);
    }

    public static record Unbaked(NumberOrString main, List<ExpressionsCase<TextureSwapper.Unbaked>> cases, Optional<TextureSwapper.Unbaked> fallback) implements TextureSwapper.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(unbakedInstance -> unbakedInstance.group(
                NumberOrString.CODEC.fieldOf("value").forGetter(Unbaked::main),
                ExpressionsCase.codec(TextureSwappers.CODEC, "swapper").listOf().fieldOf("cases").forGetter(Unbaked::cases),
                TextureSwappers.CODEC.optionalFieldOf("fallback").forGetter(Unbaked::fallback)
        ).apply(unbakedInstance, Unbaked::new));

        @Override
        public TextureSwapper bake() {
            List<ExpressionsCase<TextureSwapper>> baked = new ArrayList<>(cases.size());
            for (ExpressionsCase<TextureSwapper.Unbaked> entry : cases) {
                baked.add(new BakedExpressionsCase(entry.when(), entry.value().bake(), entry.requireAll()));
            }
            TextureSwapper bakedFallback = fallback.isPresent() ? fallback.get().bake() : EmptySwapper.INSTANCE;
            return new ExpressionSwapper(main.expression(), baked, bakedFallback);
        }

        @Override
        public MapCodec<? extends TextureSwapper.Unbaked> type() {
            return MAP_CODEC;
        }
    }

    public static class BakedExpressionsCase extends ExpressionsCase<TextureSwapper> {
        protected BakedExpressionsCase(List<NumberOrString> when, TextureSwapper value, boolean requireAll) {
            super(when, value, requireAll);
        }

        @Override
        protected Codec<TextureSwapper> createValueCodec() {
            throw new UnsupportedOperationException("TextureSwapper is not serializable!");
        }
    }
}
