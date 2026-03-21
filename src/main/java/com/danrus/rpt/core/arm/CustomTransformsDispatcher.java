package com.danrus.rpt.core.arm;

import com.danrus.rpt.duck.CustomArmTransformHolder;
import com.danrus.rpt.duck.CustomTransformsDispatchedState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.Nullable;

public class CustomTransformsDispatcher {

    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart head;

    public CustomTransformsDispatcher(ModelPart rightArm, ModelPart leftArm, ModelPart head) {
        this.rightArm = rightArm;
        this.leftArm = leftArm;
        this.head = head;
    }

    public void dispatch(HumanoidRenderState state) {
        if (!(state instanceof CustomTransformsDispatchedState dispatchedState)) {
            throw new IllegalStateException(state + " is not CustomTransformsDispatchedState");
        }

        ArmContext ctx = resolveContext(state);

        ArmTransform mainTransform = getTransform(ctx.main(), ctx.mainArm());
        ArmTransform offTransform = getTransform(ctx.offhand(), ctx.offArm());

        if (shouldApply(mainTransform)) {
            applyTransform(mainTransform, ctx.mainArm(), InteractionHand.MAIN_HAND, state, dispatchedState);

            tryApplyOtherHand(
                    ctx.offhand(),
                    ctx.offArm(),
                    ctx.mainArm(),
                    InteractionHand.OFF_HAND,
                    state,
                    dispatchedState
            );

        } else if (shouldApply(offTransform)) {
            applyTransform(offTransform, ctx.offArm(), InteractionHand.OFF_HAND, state, dispatchedState);

            tryApplyOtherHand(
                    ctx.offhand(),
                    ctx.mainArm(),
                    ctx.offArm(),
                    InteractionHand.MAIN_HAND,
                    state,
                    dispatchedState
            );
        }
    }

    public boolean shouldCancelAttack(HumanoidRenderState state) {
        if (!(state instanceof PlayerRenderState player)) {
            return false;
        }

        HumanoidArm attackArm = player.attackArm;

        CustomArmTransformHolder holder = switch (attackArm) {
            case LEFT -> (CustomArmTransformHolder) ArmTransformsHelper.getLeftItem(state);
            case RIGHT -> (CustomArmTransformHolder) ArmTransformsHelper.getRightItem(state);
        };

        ArmTransform transform = getTransform(holder, attackArm);

        if (transform == null) return false;

        return transform.attack();
    }

    @Nullable
    public HumanoidModel.ArmPose getVanilla(HumanoidRenderState state, HumanoidArm arm) {
        return getVanilla(resolveContext(state), arm);
    }

    @Nullable
    private HumanoidModel.ArmPose getVanilla(ArmContext ctx, HumanoidArm arm) {
        CustomArmTransformHolder holder =
                (arm == ctx.mainArm()) ? ctx.main() : ctx.offhand();

        return getTransform(holder, arm).getVanillaOrNull();
    }

    private CustomArmTransformHolder getHolder(HumanoidRenderState state, HumanoidArm arm) {
        return switch (arm) {
            case RIGHT -> (CustomArmTransformHolder) ArmTransformsHelper.getRightItem(state);
            case LEFT -> (CustomArmTransformHolder) ArmTransformsHelper.getLeftItem(state);
        };
    }

    private HolderPair resolveHolders(HumanoidRenderState state, HumanoidArm mainHand) {
        return switch (mainHand) {
            case RIGHT -> new HolderPair(
                    (CustomArmTransformHolder) ArmTransformsHelper.getRightItem(state),
                    (CustomArmTransformHolder) ArmTransformsHelper.getLeftItem(state)
            );
            case LEFT -> new HolderPair(
                    (CustomArmTransformHolder) ArmTransformsHelper.getLeftItem(state),
                    (CustomArmTransformHolder) ArmTransformsHelper.getRightItem(state)
            );
        };
    }

    private ArmTransform getTransform(CustomArmTransformHolder holder, HumanoidArm arm) {
        return holder.getForArm(arm);
    }

    private boolean shouldApply(ArmTransform transform) {
        return !transform.isEmpty() && transform.getVanillaOrNull() == null;
    }

    private void applyTransform(
            ArmTransform transform,
            HumanoidArm arm,
            InteractionHand hand,
            HumanoidRenderState state,
            CustomTransformsDispatchedState dispatchedState
    ) {
        transform.rotateModelPart(
                selectArmPart(arm, hand),
                head,
                arm == HumanoidArm.RIGHT,
                state
        );

        dispatchedState.rpt$markAsAlreadyTransformed(arm);
    }

    private void tryApplyOtherHand(
            CustomArmTransformHolder otherHolder,
            HumanoidArm otherArm,
            HumanoidArm sourceArm,
            InteractionHand hand,
            HumanoidRenderState state,
            CustomTransformsDispatchedState dispatchedState
    ) {
        ArmTransform otherTransform = getTransform(otherHolder, otherArm);

        if (otherHolder.isBothHandsAvailable(sourceArm) && shouldApply(otherTransform)) {
            applyTransform(otherTransform, otherArm, hand, state, dispatchedState);
        }
    }

    private ModelPart selectArmPart(HumanoidArm mainHand, InteractionHand wanted) {
        return switch (wanted) {
            case MAIN_HAND -> switch (mainHand) {
                case RIGHT -> rightArm;
                case LEFT -> leftArm;
            };
            case OFF_HAND -> switch (mainHand) {
                case RIGHT -> leftArm;
                case LEFT -> rightArm;
            };
        };
    }

    private ArmContext resolveContext(HumanoidRenderState state) {
        HumanoidArm main = state.mainArm;
        HumanoidArm off = main.getOpposite();

        HolderPair pair = resolveHolders(state, main);

        return new ArmContext(main, off, pair.main(), pair.offhand());
    }

    private record HolderPair(CustomArmTransformHolder main, CustomArmTransformHolder offhand) {}

    public record ArmContext(
            HumanoidArm mainArm,
            HumanoidArm offArm,
            CustomArmTransformHolder main,
            CustomArmTransformHolder offhand
    ) {}
}