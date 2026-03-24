package com.danrus.rpt.core.bbmodel.fsm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public class FsmState {
    public static final Codec<FsmState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("animation").forGetter(s -> Optional.ofNullable(s.getAnimationName())),
            Codec.BOOL.optionalFieldOf("looping", true).forGetter(FsmState::isLooping),
            FsmTransition.CODEC.listOf().optionalFieldOf("transitions", List.of()).forGetter(FsmState::getTransitions)
    ).apply(instance, (animOpt, looping, transitions) -> {
        FsmState state = new FsmState("", animOpt.orElse(null), looping);
        for (FsmTransition t : transitions) {
            state.addTransition(t);
        }
        return state;
    }));

    private String name;
    private String animationName;
    private final boolean isLooping;
    private final List<FsmTransition> transitions = new java.util.ArrayList<>();

    public FsmState(String name, String animationName, boolean isLooping) {
        this.name = name;
        this.animationName = animationName;
        this.isLooping = isLooping;
    }

    public void addTransition(FsmTransition transition) {
        this.transitions.add(transition);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAnimationName(String animationName) {
        this.animationName = animationName;
    }

    public String getName() {
        return name;
    }

    public String getAnimationName() {
        return animationName;
    }

    public boolean isLooping() {
        return isLooping;
    }

    public List<FsmTransition> getTransitions() {
        return transitions;
    }
}
