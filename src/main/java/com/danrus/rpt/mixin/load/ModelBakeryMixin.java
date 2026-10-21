package com.danrus.rpt.mixin.load;

import com.danrus.rpt.duck.BakingContextSource;
import com.danrus.rpt.duck.ModelBakerSource;
import com.danrus.rpt.mixin.accessor.InternerImplInvoker;
import com.danrus.rpt.mixin.accessor.ModelBakerImplInvoker;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.*;
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


    @Shadow
    @Final
    private net.minecraft.client.renderer.PlayerSkinRenderCache playerSkinRenderCache;

    @Shadow
    @Final
    private net.minecraft.client.resources.model.sprite.SpriteGetter sprites;

    @Override
    public ItemModel.BakingContext rpt$createBakingContext(
            ModelBaker baker
            , net.minecraft.client.resources.model.sprite.MaterialBaker materials
    ) {
        return new ItemModel.BakingContext(
                baker,
                entityModelSet,
                sprites,
                playerSkinRenderCache,
                ModelBakery.MissingModels.bake(missingModel
                        , materials, InternerImplInvoker.rpt$create()
                ).item(),
                null
        );
    }

    @Override
    public ModelBaker rpt$createModelBaker(
            net.minecraft.client.resources.model.sprite.MaterialBaker sprites
    ) {
        var parts = InternerImplInvoker.rpt$create();
        return ModelBakerImplInvoker.rpt$create(
                (ModelBakery) (Object) this,
                sprites
                , parts
                , ModelBakery.MissingModels.bake(this.missingModel, sprites, parts)
        );
    }
}
