package com.danrus.rpt.core.bbmodel.fsm;

import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

public class FsmBuilder {
    private final FsmController controller = new FsmController();

    public FsmBuilder initialState(String name, Consumer<StateBuilder> stateConfig) {
        state(name, stateConfig);
        controller.setInitialState(name);
        return this;
    }

    public FsmBuilder state(String name, Consumer<StateBuilder> stateConfig) {
        StateBuilder builder = new StateBuilder(name);
        stateConfig.accept(builder);
        controller.addState(builder.build());
        return this;
    }

    public FsmController build() {
        if (controller.getInitialState() == null) {
            throw new IllegalStateException("FSM Controller must have an initial state.");
        }
        return controller;
    }

    public static class StateBuilder {
        private final String name;
        private String animationName;
        private boolean isLooping = true;
        private final java.util.List<FsmTransition> transitions = new java.util.ArrayList<>();

        public StateBuilder(String name) {
            this.name = name;
            this.animationName = name; // default to state name
        }

        public StateBuilder animation(String animationName) {
            this.animationName = animationName;
            return this;
        }

        public StateBuilder looping(boolean isLooping) {
            this.isLooping = isLooping;
            return this;
        }

        public TransitionBuilder transitionTo(String targetState, double blendDuration) {
            return new TransitionBuilder(this, targetState, blendDuration);
        }

        void addTransition(FsmTransition transition) {
            this.transitions.add(transition);
        }

        public FsmState build() {
            FsmState state = new FsmState(name, animationName, isLooping);
            for (FsmTransition transition : transitions) {
                state.addTransition(transition);
            }
            return state;
        }
    }

    public static class TransitionBuilder {
        private final StateBuilder parent;
        private final String targetState;
        private final double blendDuration;
        private FsmTrigger trigger = null;

        public TransitionBuilder(StateBuilder parent, String targetState, double blendDuration) {
            this.parent = parent;
            this.targetState = targetState;
            this.blendDuration = blendDuration;
        }

        public TransitionBuilder onTrigger(FsmTrigger trigger) {
            this.trigger = trigger;
            return this;
        }
        
        public TransitionBuilder onTrigger(String triggerId) {
            this.trigger = new FsmTriggers.SimpleTrigger(triggerId);
            return this;
        }

        public StateBuilder end() {
            parent.addTransition(new FsmTransition(targetState, blendDuration, trigger));
            return parent;
        }
    }
}
