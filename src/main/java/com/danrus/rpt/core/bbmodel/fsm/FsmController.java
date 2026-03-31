package com.danrus.rpt.core.bbmodel.fsm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.Map;

//TODO: global_transitions

public class FsmController {
    public static final Codec<FsmController> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("initial_state").forGetter(FsmController::getInitialState),
            Codec.unboundedMap(Codec.STRING, FsmState.CODEC).fieldOf("states").forGetter(FsmController::getStatesMap)
    ).apply(instance, (initial, statesMap) -> {
        FsmController controller = new FsmController();
        controller.setInitialState(initial);
        statesMap.forEach((name, state) -> {
            state.setName(name);
            if (state.getAnimationName() == null) {
                state.setAnimationName(name);
            }
            controller.addState(state);
        });
        return controller;
    }));

    private final Map<String, FsmState> states = new HashMap<>();
    private String initialState;

    public void addState(FsmState state) {
        states.put(state.getName(), state);
    }

    public void setInitialState(String initialState) {
        this.initialState = initialState;
    }

    public String getInitialState() {
        return initialState;
    }

    public FsmState getState(String name) {
        return states.get(name);
    }

    public Map<String, FsmState> getStatesMap() {
        return states;
    }
}
