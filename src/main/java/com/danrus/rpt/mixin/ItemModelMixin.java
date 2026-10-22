package com.danrus.rpt.mixin;

import com.danrus.rpt.Rpt;
import com.danrus.rpt.core.template.RptTemplate;
import com.danrus.rpt.duck.PatchInformer;
import com.danrus.rpt.impl.model.PatchCapturedItemModel;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.item.*;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({CuboidItemModelWrapper.Unbaked.class,
        BundleSelectedItemSpecialRenderer.Unbaked.class,
        CompositeModel.Unbaked.class,
        ConditionalItemModel.Unbaked.class,
        EmptyModel.Unbaked.class,
        RangeSelectItemModel.Unbaked.class,
        SelectItemModel.Unbaked.class,
        SpecialModelWrapper.Unbaked.class
})
public class ItemModelMixin implements PatchInformer {

    private static final Logger log = LoggerFactory.getLogger(ItemModelMixin.class);
    private static final Matrix4fc IDENTITY = new Matrix4f();

    @Unique
    private Identifier rpt$patch;

    @Override
    public Identifier rpt$getPatchPath() {
        return rpt$patch;
    }

    @Override
    public void rpt$setPatchPath(Identifier path) {
        rpt$patch = path;
    }

    @WrapMethod(
            method = "bake"
    )
    private ItemModel rpt$bake(ItemModel.BakingContext context,
                               Matrix4fc transformations,
                               Operation<ItemModel> original) {
        if (rpt$patch == null) {
            return original.call(context
                    , transformations
            );
        }
        RptTemplate template = Rpt.getTemplatesManager().getTemplate(context, rpt$patch);
        if (template == null) {
            throw new IllegalStateException("Unable to find template " + rpt$patch + " for patch model!");
        }
        if (!IDENTITY.equals(transformations)) {
            log.warn("rpt$patch {} is applied on a transformed model; the parent transformation is ignored", rpt$patch);
        }
        return new PatchCapturedItemModel(template);
    }
}
