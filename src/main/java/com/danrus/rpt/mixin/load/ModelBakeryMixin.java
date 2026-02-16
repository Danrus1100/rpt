package com.danrus.rpt.mixin.load;

import com.danrus.rpt.duck.BakingContextSource;
import com.danrus.rpt.duck.ModelBakerSource;
import com.danrus.rpt.mixin.accessor.ModelBakerImplInvoker;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SpriteGetter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ModelBakery.class)
public class ModelBakeryMixin implements BakingContextSource, ModelBakerSource {

    @Shadow
    @Final
    private EntityModelSet entityModelSet;

    @Shadow
    @Final
    ResolvedModel missingModel;

    @Override
    public ItemModel.BakingContext rpt$createBakingContext(ModelBaker baker) {
        return new ItemModel.BakingContext(
                baker,
                entityModelSet,
                ModelBakery.MissingModels.bake(missingModel, baker.sprites()).item(),
                null
        );
    }

    @Override
    public ModelBaker rpt$createModelBaker(SpriteGetter spriteGetter) {
        return ModelBakerImplInvoker.rpt$create((ModelBakery) (Object) this, spriteGetter);
    }
}
