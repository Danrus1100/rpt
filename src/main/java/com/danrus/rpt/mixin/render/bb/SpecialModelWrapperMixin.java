package com.danrus.rpt.mixin.render.bb;

import com.danrus.rpt.core.bbmodel.DynamicSpecialModel;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@Mixin(SpecialModelWrapper.class)
public class SpecialModelWrapperMixin<T> {

    @Shadow
    @Final
    private SpecialModelRenderer<T> specialRenderer;

    @WrapOperation(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/special/SpecialModelRenderer;extractArgument(Lnet/minecraft/world/item/ItemStack;)Ljava/lang/Object;")
    )
    private T rpt$extractArgument(SpecialModelRenderer instance, ItemStack stack, Operation<T> original, @Local(argsOnly = true) ItemStackRenderState renderState, @Local(argsOnly = true) ItemStack stack1, @Local(argsOnly = true) ItemModelResolver itemModelResolver, @Local(argsOnly = true) ItemDisplayContext displayContext, @Local(argsOnly = true) @Nullable ClientLevel level,
                                  //? <=1.21.8
                                  //@Local(argsOnly = true) @Nullable LivingEntity owner,
                                  //? >=1.21.10
                                  @Local(argsOnly = true) @Nullable ItemOwner owner,
                                  @Local(argsOnly = true) int seed) {
        if (specialRenderer instanceof DynamicSpecialModel<T>) {
            DynamicSpecialModel<T> dynamicExtendsGetter = (DynamicSpecialModel<T>) specialRenderer;
            return dynamicExtendsGetter.extractArgument(
                    renderState,
                    stack1,
                    itemModelResolver,
                    displayContext,
                    level,
                    owner
                    //? >=1.21.10
                    != null ? owner.asLivingEntity() : null
                    ,
                    seed);
        }
        return original.call(instance, stack);
    }

    @SuppressWarnings("unchecked")
    @WrapOperation(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState$LayerRenderState;setExtents(Ljava/util/function/Supplier;)V")
    )
    private void rpt$getDynamicExtends(
            ItemStackRenderState.LayerRenderState instance, Supplier<Vector3f[]> extents, Operation<Void> original, @Local(argsOnly = true) ItemStackRenderState renderState, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true) ItemModelResolver itemModelResolver, @Local(argsOnly = true) ItemDisplayContext displayContext, @Local(argsOnly = true) @Nullable ClientLevel level,
            //? <=1.21.8
            //@Local(argsOnly = true) @Nullable LivingEntity owner,
            //? >=1.21.10
            @Local(argsOnly = true) @Nullable ItemOwner owner,
            @Local(argsOnly = true) int seed, @Local T pattern
    ) {
        if (specialRenderer instanceof DynamicSpecialModel<?>) {
            DynamicSpecialModel<T> dynamicExtendsGetter = (DynamicSpecialModel<T>) specialRenderer;
            Set<
                    //? <=1.21.10
                    //Vector3f
                    //? >=1.21.11
                    Vector3fc
                    > set = new HashSet();
            dynamicExtendsGetter.getExtends(
                    pattern,
                    renderState,
                    stack,
                    itemModelResolver,
                    displayContext,
                    level,
                    owner
                    //? >=1.21.10
                    != null ? owner.asLivingEntity() : null
                    ,
                    seed,
                    //? <=1.21.11
                    //set
                    //? >=1.21.11
                    set::add
            );
            Supplier<Vector3f[]> newExtends = () -> (Vector3f[])set.toArray(new Vector3f[0]);
            original.call(instance, newExtends);
        } else {
            original.call(instance, extents);
        }
    }

}
