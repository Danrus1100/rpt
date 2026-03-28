package com.danrus.rpt.core.bbmodel.fsm;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.animation.Animation;
import com.danrus.bb4j.model.animation.AnimationBlendState;
import com.danrus.rpt.compat.iris.IrisCompatBridge;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FsmInstance {
    private final FsmController controller;
    
    private FsmState currentState;
    private double currentStateTime = 0.0;
    
    private @Nullable FsmState previousState = null;
    private @Nullable LivingEntity captured;
    private long lastRenderGameTime = Long.MIN_VALUE;
    private boolean firstRenderTick = true;
    private double previousStateTime = 0.0;
    
    private double currentBlendTime = 0.0;
    private double totalBlendDuration = 0.0;

    private final Map<String, Double> customVariables = new HashMap<>();

    public FsmInstance(FsmController controller) {
        this.controller = controller;
        this.currentState = controller.getState(controller.getInitialState());
        if (this.currentState == null) {
            throw new IllegalStateException("Initial state '" + controller.getInitialState() + "' not found in controller.");
        }
    }
    
    public void setVariable(String name, double value) {
        customVariables.put(name, value);
    }

    public void tick(float delta, ItemDisplayContext displayContext, @Nullable ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, BbModelDocument document, Set<String> activeTriggers) {
        if (IrisCompatBridge.isShadowsPass.get()) return; //FIXME: hack, move tick to other place
        captured = entity;

        currentStateTime += delta;
        if (previousState != null) {
            previousStateTime += delta;
            currentBlendTime += delta;
            
            if (currentBlendTime >= totalBlendDuration) {
                previousState = null;
            }
        }

        if (currentState != null && document != null && currentState.getAnimationName() != null) {
            Animation animation = findAnimationByName(document, currentState.getAnimationName());
            if (animation != null && currentStateTime >= animation.getDuration()) {
                activeTriggers.add(FsmTriggers.ANIMATION_FINISHED);
            }
        }

        checkTransitions(level, entity, seed, activeTriggers);
        activeTriggers.clear();
    }

    private boolean shouldTriggerDraw(@Nullable ClientLevel level) {
        if (firstRenderTick) {
            firstRenderTick = false;
            if (level != null) {
                lastRenderGameTime = level.getGameTime();
            }
            return true;
        }

        if (level == null) {
            return false;
        }

        long now = level.getGameTime();
        boolean shouldTrigger = lastRenderGameTime != Long.MIN_VALUE && now - lastRenderGameTime > 3;
        lastRenderGameTime = now;
        return shouldTrigger;
    }

    private boolean isHandDisplayContext(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    private void checkTransitions(@Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, Set<String> activeTriggers) {
        if (currentState == null) return;

        for (FsmTransition transition : currentState.getTransitions()) {
            if (!transition.interruptible() && !activeTriggers.contains(FsmTriggers.ANIMATION_FINISHED)) {
                continue;
            }
            boolean triggered = transition.trigger() != null && transition.trigger().test(activeTriggers, customVariables, level, entity, seed);
            boolean isApplicable = transition.condition().getValueWithGame(customVariables, level, entity, seed);
            if (transition.trigger() != null && triggered && isApplicable) {
                performTransition(transition);
                break;
            }
        }
    }

    private void performTransition(FsmTransition transition) {
        FsmState targetState = controller.getState(transition.toState());
        if (targetState == null) return;

        previousState = currentState;
        previousStateTime = currentStateTime;

        currentState = targetState;
        currentStateTime = 0.0;

        totalBlendDuration = transition.blendDuration();
        currentBlendTime = 0.0;
    }

    public float getCurrentAnimationProgress(@Nullable BbModelDocument document) {
        if (currentState == null || document == null) {
            return 0.0f;
        }

        String animationName = currentState.getAnimationName();
        if (animationName == null || animationName.isBlank()) {
            return 0.0f;
        }

        Animation animation = findAnimationByName(document, animationName);
        if (animation == null) {
            return 0.0f;
        }

        double duration = animation.getDuration();
        if (duration <= 0.0) {
            return 1.0f;
        }

        double normalized = currentStateTime / duration;
        return (float) Math.max(0.0, Math.min(1.0, normalized));
    }

    public float getCurrentAnimationProgressFrom(float startProgress, @Nullable BbModelDocument document) {
        float progress = getCurrentAnimationProgress(document);
        if (startProgress <= 0.0f) {
            return progress;
        }
        if (startProgress >= 1.0f) {
            return progress >= 1.0f ? 1.0f : 0.0f;
        }
        float tailProgress = (progress - startProgress) / (1.0f - startProgress);
        return Math.max(0.0f, Math.min(1.0f, tailProgress));
    }

    private @Nullable Animation findAnimationByName(BbModelDocument document, String animationName) {
        for (Animation animation : document.getAnimations()) {
            if (animationName.equals(animation.getName())) {
                return animation;
            }
        }
        return null;
    }

    public List<AnimationBlendState> getBlendStates() {
        if (currentState == null) return Collections.emptyList();

        if (previousState == null || totalBlendDuration <= 0) {
            return List.of(new AnimationBlendState(currentState.getAnimationName(), currentStateTime, 1.0));
        }

        double progress = Math.min(1.0, currentBlendTime / totalBlendDuration);
        
        // You can easily use easing functions here later for non-linear blending
        double previousWeight = 1.0 - progress;
        double currentWeight = progress;

        return List.of(
            new AnimationBlendState(previousState.getAnimationName(), previousStateTime, previousWeight),
            new AnimationBlendState(currentState.getAnimationName(), currentStateTime, currentWeight)
        );
    }


    public @Nullable LivingEntity getCapturedEntity() {
        return captured;
    }

    public String getCurrentStateName() {
        return currentState != null ? currentState.getName() : "none";
    }
}
