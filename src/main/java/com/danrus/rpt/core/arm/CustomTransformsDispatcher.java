package com.danrus.rpt.core.arm;

import com.danrus.rpt.duck.RptItemRenderState;
import com.danrus.rpt.duck.CustomTransformsDispatchedState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
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
        if (rightArm == null || leftArm == null || head == null) {
            return;
        }

        if (!(state instanceof CustomTransformsDispatchedState dispatchedState)) {
            throw new IllegalStateException(state + " is not CustomTransformsDispatchedState");
        }

        ArmContext ctx = resolveContext(state);

        ArmTransform mainTransform = getTransform(ctx.main(), ctx.mainArm());
        ArmTransform offTransform = getTransform(ctx.offhand(), ctx.offArm());

        if (shouldApply(mainTransform)) {
            applyTransform(mainTransform, ctx.mainArm(), state, dispatchedState);

            tryApplyCompanionFromSameHolder(
                ctx.main(),
                ctx.offArm(),
                state,
                dispatchedState
            );

            tryApplyOtherHand(
                    ctx.offhand(),
                    ctx.offArm(),
                    ctx.mainArm(),
                    state,
                    dispatchedState
            );

        }
        if (shouldApply(offTransform)) {
            applyTransform(offTransform, ctx.offArm(), state, dispatchedState);

            tryApplyCompanionFromSameHolder(
                ctx.offhand(),
                ctx.mainArm(),
                state,
                dispatchedState
            );

            tryApplyOtherHand(
                    ctx.main(),
                    ctx.mainArm(),
                    ctx.offArm(),
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
        ArmTransform transform = getPlayerTransform(state, attackArm);

        if (transform == null) return false;

        return !transform.attack();
    }

    public boolean shouldCancelBob(HumanoidRenderState state, HumanoidArm arm) {
        ArmTransform transform = getPlayerTransform(state, arm);
        if (transform == null) return false;
        return !transform.bob();
    }

    @Nullable
    private ArmTransform getPlayerTransform(HumanoidRenderState state, HumanoidArm arm) {
        if (!(state instanceof PlayerRenderState player)) {
            return null;
        }

        RptItemRenderState holder = getHolder(state, arm);

        return getTransform(holder, arm);
    }

    @Nullable
    public HumanoidModel.ArmPose getVanilla(HumanoidRenderState state, HumanoidArm arm) {
        return getVanilla(resolveContext(state), arm);
    }

    @Nullable
    private HumanoidModel.ArmPose getVanilla(ArmContext ctx, HumanoidArm arm) {
        RptItemRenderState holder =
                (arm == ctx.mainArm()) ? ctx.main() : ctx.offhand();

        return getTransform(holder, arm).getVanillaOrNull();
    }

    private RptItemRenderState getHolder(HumanoidRenderState state, HumanoidArm arm) {
        return switch (arm) {
            case RIGHT -> (RptItemRenderState) ArmTransformsHelper.getRightItem(state);
            case LEFT -> (RptItemRenderState) ArmTransformsHelper.getLeftItem(state);
        };
    }

    private HolderPair resolveHolders(HumanoidRenderState state, HumanoidArm mainHand) {
        return switch (mainHand) {
            case RIGHT -> new HolderPair(
                    (RptItemRenderState) ArmTransformsHelper.getRightItem(state),
                    (RptItemRenderState) ArmTransformsHelper.getLeftItem(state)
            );
            case LEFT -> new HolderPair(
                    (RptItemRenderState) ArmTransformsHelper.getLeftItem(state),
                    (RptItemRenderState) ArmTransformsHelper.getRightItem(state)
            );
        };
    }

    private ArmTransform getTransform(RptItemRenderState holder, HumanoidArm arm) {
        return holder.getForArm(arm);
    }

    private boolean shouldApply(ArmTransform transform) {
        return !transform.isEmpty() && transform.getVanillaOrNull() == null;
    }

    private void applyTransform(
            ArmTransform transform,
            HumanoidArm arm,
            HumanoidRenderState state,
            CustomTransformsDispatchedState dispatchedState
    ) {
        transform.rotateModelPart(
            selectArmPart(arm),
                head,
                arm == HumanoidArm.RIGHT,
                state
        );
    }

    private void tryApplyOtherHand(
            RptItemRenderState otherHolder,
            HumanoidArm otherArm,
            HumanoidArm sourceArm,
            HumanoidRenderState state,
            CustomTransformsDispatchedState dispatchedState
    ) {
        ArmTransform otherTransform = getTransform(otherHolder, otherArm);

        if (otherHolder.isBothHandsAvailable(sourceArm) && shouldApply(otherTransform)) {
            applyTransform(otherTransform, otherArm, state, dispatchedState);
            dispatchedState.rpt$markAsAlreadyTransformed(otherArm);
        }
    }

    private void tryApplyCompanionFromSameHolder(
            RptItemRenderState holder,
            HumanoidArm otherArm,
            HumanoidRenderState state,
            CustomTransformsDispatchedState dispatchedState
    ) {
        ArmTransform otherTransform = getTransform(holder, otherArm);

        if (shouldApply(otherTransform)) {
            applyTransform(otherTransform, otherArm, state, dispatchedState);
        }
    }

    private ModelPart selectArmPart(HumanoidArm arm) {
        return switch (arm) {
            case RIGHT -> rightArm;
            case LEFT -> leftArm;
        };
    }

    private ArmContext resolveContext(HumanoidRenderState state) {
        HumanoidArm main = state.mainArm;
        HumanoidArm off = main.getOpposite();

        HolderPair pair = resolveHolders(state, main);

        return new ArmContext(main, off, pair.main(), pair.offhand());
    }

    private record HolderPair(RptItemRenderState main, RptItemRenderState offhand) {}

    private record ArmContext(
            HumanoidArm mainArm,
            HumanoidArm offArm,
            RptItemRenderState main,
            RptItemRenderState offhand
    ) {}
}