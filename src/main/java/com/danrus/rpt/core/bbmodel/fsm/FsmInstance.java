package com.danrus.rpt.core.bbmodel.fsm;

import com.danrus.bb4j.model.animation.AnimationBlendState;
import com.danrus.rpt.compat.iris.IrisCompatBridge;
import com.danrus.rpt.core.bbmodel.fsm.FsmTriggers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
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
    
    private final Set<String> activeTriggers = new HashSet<>();
    private final Map<String, Double> customVariables = new HashMap<>();

    public FsmInstance(FsmController controller) {
        this.controller = controller;
        this.currentState = controller.getState(controller.getInitialState());
        if (this.currentState == null) {
            throw new IllegalStateException("Initial state '" + controller.getInitialState() + "' not found in controller.");
        }
    }

    public void trigger(String trigger, @Nullable LivingEntity entity) {
        if (entity != null && captured != null && entity.getId() != captured.getId()) return;
        activeTriggers.add(trigger);
    }
    
    public void setVariable(String name, double value) {
        customVariables.put(name, value);
    }

    public void tick(float delta, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, com.danrus.bb4j.model.BbModelDocument document) {
        if (IrisCompatBridge.isShadowsPass.get()) return; //FIXME: hack, move tick to other place
        captured = entity;

        if (isHandDisplayContext(displayContext) && shouldTriggerDraw(level)) {
            activeTriggers.add(FsmTriggers.DRAW);
        }

        currentStateTime += delta;
        if (previousState != null) {
            previousStateTime += delta;
            currentBlendTime += delta;
            
            if (currentBlendTime >= totalBlendDuration) {
                previousState = null;
            }
        }

        if (currentState != null && document != null && currentState.getAnimationName() != null) {
            for (com.danrus.bb4j.model.animation.Animation anim : document.getAnimations()) {
                if (currentState.getAnimationName().equals(anim.getName())) {
                    if (currentStateTime >= anim.getDuration()) {
                        activeTriggers.add(FsmTriggers.ANIMATION_FINISHED);
                    }
                    break;
                }
            }
        }

        checkTransitions(level, entity, seed);
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
        boolean shouldTrigger = lastRenderGameTime != Long.MIN_VALUE && now - lastRenderGameTime > 1;
        lastRenderGameTime = now;
        return shouldTrigger;
    }

    private boolean isHandDisplayContext(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    private void checkTransitions(@Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        if (currentState == null) return;

        for (FsmTransition transition : currentState.getTransitions()) {
            if (transition.trigger() != null && transition.trigger().test(activeTriggers, customVariables, level, entity, seed)) {
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
