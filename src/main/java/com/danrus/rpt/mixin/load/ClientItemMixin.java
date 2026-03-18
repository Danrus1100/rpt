package com.danrus.rpt.mixin.load;

import com.danrus.rpt.duck.RptClientItem;
import com.danrus.rpt.core.item.RptItemParams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModels;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(ClientItem.class)
public class ClientItemMixin implements RptClientItem {
    @Unique
    @Nullable
    private RptItemParams rpt$params;

    @Override
    public Optional<RptItemParams> rpt$getParams() {
        return Optional.ofNullable(this.rpt$params);
    }

    @Override
    public void rpt$setParams(@Nullable RptItemParams params) {
        this.rpt$params = params;
    }

    @Redirect(
            method = "<clinit>",
            at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;")
    )
    private static Codec<ClientItem> rpt$modifyCodec(java.util.function.Function<RecordCodecBuilder.Instance<ClientItem>, ? extends com.mojang.datafixers.kinds.App<RecordCodecBuilder.Mu<ClientItem>, ClientItem>> builder) {
        return RecordCodecBuilder.create(instance -> instance.group(
            ItemModels.CODEC.fieldOf("model").forGetter(ClientItem::model),
                ClientItem.Properties.MAP_CODEC.forGetter(ClientItem::properties),
                RptItemParams.CODEC.optionalFieldOf("rpt").forGetter(item -> RptClientItem.class.cast(item).rpt$getParams())
        ).apply(instance, (unbaked, properties, rptItemParams) -> {
            ClientItem item = new ClientItem(unbaked, properties);
            RptClientItem.class.cast(item).rpt$setParams(rptItemParams.orElse(null));
            return item;
        }));
    }
}
