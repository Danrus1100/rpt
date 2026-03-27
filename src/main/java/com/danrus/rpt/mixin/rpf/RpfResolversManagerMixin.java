package com.danrus.rpt.mixin.rpf;

import com.danrus.rpf.core.item.ModelUpdateContext;
import com.danrus.rpf.core.item.RpfResolversManager;
import com.danrus.rpt.RptHooks;
import com.danrus.rpt.core.OwnerHolder;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RpfResolversManager.class)
public class RpfResolversManagerMixin {

    @WrapMethod(method = "resolve")
    private void rpt$hookResolve(ModelUpdateContext context, ItemStack stack, ItemOwner entity, Operation<Void> vanilla, Operation<Void> original) {
        RptHooks.preItemResolve(context, stack, entity == null ? null : entity.asLivingEntity());
        original.call(context, stack, entity, vanilla);
        RptHooks.postItemResolve(context, stack, entity == null ? null : entity.asLivingEntity());
    }
}
