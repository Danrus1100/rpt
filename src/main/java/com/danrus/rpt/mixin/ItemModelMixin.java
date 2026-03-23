package com.danrus.rpt.mixin;

import com.danrus.rpt.Rpt;
import com.danrus.rpt.core.template.RptTemplate;
import com.danrus.rpt.duck.PatchInformer;
import com.danrus.rpt.impl.model.PatchCapturedItemModel;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.item.*;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({BlockModelWrapper.Unbaked.class,
        BundleSelectedItemSpecialRenderer.Unbaked.class,
        CompositeModel.Unbaked.class,
        ConditionalItemModel.Unbaked.class,
        EmptyModel.Unbaked.class,
        RangeSelectItemModel.Unbaked.class,
        SelectItemModel.Unbaked.class,
        SpecialModelWrapper.Unbaked.class
})
public class ItemModelMixin implements PatchInformer {

    @Unique
    private ResourceLocation rpt$patch;

    @Override
    public ResourceLocation rpt$getPatchPath() {
        return rpt$patch;
    }

    @Override
    public void rpt$setPatchPath(ResourceLocation path) {
        rpt$patch = path;
    }

    @WrapMethod(
            method = "bake"
    )
    private ItemModel rpt$bake(ItemModel.BakingContext context, Operation<ItemModel> original) {
        if (rpt$patch == null) {
            return original.call(context);
        }
        RptTemplate template = Rpt.getTemplatesManager().getTemplate(context, rpt$patch);
        if (template == null) {
            throw new IllegalStateException("Unable to find tempalte" + rpt$patch + " for patch model!");
        }
        return new PatchCapturedItemModel(template);
    }
}
