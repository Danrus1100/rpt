package com.danrus.rpt.impl.model;

import com.danrus.rpf.api.RpfItemModel;
import com.danrus.rpf.api.TestsResultCollector;
import com.danrus.rpf.api.codec.RpfModelsCodecsExtends;
import com.danrus.rpf.core.item.ModelUpdateContext;
import com.danrus.rpt.core.OwnerHolder;
import com.danrus.rpt.core.arm.ArmTransform;
import com.danrus.rpt.core.item.RptField;
import com.danrus.rpt.duck.CustomArmTransformHolder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ArmTransformWrapper extends AbstractRpfItemModel {

    private final ArmTransform transform;
    private final Optional<ArmTransform> otherTransform;
    public final ItemModel model;

    public ArmTransformWrapper(ArmTransform transform, Optional<ArmTransform> otherTransform, ItemModel model) {
        this.transform = transform;
        this.otherTransform = otherTransform;
        this.model = model;
    }

    @Override
    boolean rpf$doDelegate(ModelUpdateContext context, ItemStack stack, OwnerHolder owner, @Nullable ItemModel prev, TestsResultCollector collector) {
        return RpfItemModel.class.cast(model).rpf$doDelegate(context, stack, owner.get(), this, collector);
    }

    @Override
    void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, OwnerHolder owner, int seed) {
        if (renderState instanceof CustomArmTransformHolder holder) {

            switch (displayContext) {
                case THIRD_PERSON_RIGHT_HAND -> {
                    holder.rpt$setRightArmTransform(resolveForHand(true, stack));
                }
                case THIRD_PERSON_LEFT_HAND -> {
                    holder.rpt$setLeftArmTransform(resolveForHand(false, stack));
                }
            }
        }

        model.update(renderState, stack, itemModelResolver, displayContext, level, owner.get(), seed);
    }

    private ArmTransform resolveForHand(boolean rightHand, ItemStack stack) {
        ArmTransform primary = new ArmTransform(transform, RptField.fromItemStack(stack));

        if (otherTransform.isEmpty()) {
            return primary;
        }

        ArmTransform secondary = new ArmTransform(otherTransform.get(), RptField.fromItemStack(stack));

        return rightHand ? primary : secondary;
    }

    public static record Unbaked(ItemModel.Unbaked model, Optional<ArmTransform> otherTransform, ArmTransform transform) implements ItemModel.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                ItemModels.CODEC.fieldOf("model").forGetter(Unbaked::model),
                ArmTransform.CODEC.optionalFieldOf("second_arm").forGetter(Unbaked::otherTransform),
                ArmTransform.CODEC.optionalFieldOf("transform", ArmTransform.EMPTY).forGetter(Unbaked::transform)
        ).apply(i, Unbaked::new));

        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("rpt", "arm_transform");

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return RpfModelsCodecsExtends.getInstance().wrap(ID, MAP_CODEC);
        }

        @Override
        public ItemModel bake(BakingContext context) {
            return new ArmTransformWrapper(transform, otherTransform, model.bake(context));
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            model.resolveDependencies(resolver);
        }
    }
}
