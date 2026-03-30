package com.danrus.rpt.core.bbmodel;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public interface DynamicSpecialModel<T> extends SpecialModelRenderer<T> {
    default T extractArgument(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        return extractArgument(stack);
    }
    default void updateAndGetExtends(T patterns, ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed,
                                     //? <=1.21.10
                                     // Set<Vector3f>
                                     //? >=1.21.11
                                     Consumer<Vector3fc>
                                    output) {
        getExtents(output);
    }

    public interface Unbaked extends SpecialModelRenderer.Unbaked {
        @Nullable
        SpecialModelRenderer<?> bake(ItemModel.BakingContext context);

        @Override
        @Nullable
        default SpecialModelRenderer<?> bake(BakingContext context) {
            return null;
        }

        void resolveDependencies(ResolvableModel.Resolver resolver);
    }
}
