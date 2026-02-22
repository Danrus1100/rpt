package com.danrus.rpt.mixin.load;

import com.danrus.rpt.duck.BakingContextSource;
import com.danrus.rpt.duck.ModelBakerSource;
import com.danrus.rpt.mixin.accessor.ModelBakerImplInvoker;
import com.danrus.rpt.mixin.accessor.PartCacheImplInvoker;
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

    //? if >= 1.21.10 {
    @Shadow
    @Final
    private MaterialSet materials;

    @Shadow
    @Final
    private net.minecraft.client.renderer.PlayerSkinRenderCache playerSkinRenderCache;
    //?}

    @Override
    public ItemModel.BakingContext rpt$createBakingContext(ModelBaker baker) {
        return new ItemModel.BakingContext(
                baker,
                entityModelSet,
                //? if >= 1.21.10
                materials, playerSkinRenderCache,
                ModelBakery.MissingModels.bake(missingModel, baker.sprites()
                        //? if >=1.21.11
                        , PartCacheImplInvoker.rpt$create()
                ).item(),
                null
        );
    }

    @Override
    public ModelBaker rpt$createModelBaker(SpriteGetter spriteGetter) {
        //? if >= 1.21.11
        ModelBakery.PartCacheImpl parts = PartCacheImplInvoker.rpt$create();
        return ModelBakerImplInvoker.rpt$create(
                (ModelBakery) (Object) this,
                spriteGetter
                //? >= 1.21.11 {
                , parts,
                ModelBakery.MissingModels.bake(this.missingModel, spriteGetter, parts)
                //? }
        );
    }
}
