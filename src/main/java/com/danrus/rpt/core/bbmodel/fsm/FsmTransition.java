package com.danrus.rpt.core.bbmodel.fsm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record FsmTransition(String toState, double blendDuration,
                            @Nullable FsmTrigger trigger) {
    public static final Codec<FsmTransition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("to").forGetter(FsmTransition::toState),
            Codec.DOUBLE.optionalFieldOf("blend_duration", 0.0).forGetter(FsmTransition::blendDuration),
            FsmTriggers.FLEX_CODEC.optionalFieldOf("trigger").forGetter(t -> Optional.ofNullable(t.trigger()))
    ).apply(instance, (to, blend, triggerOpt) ->
            new FsmTransition(to, blend, triggerOpt.orElse(null))
    ));

}
