package com.danrus.rpt.core.bbmodel.fsm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record FsmTransition(String toState, double blendDuration,
                            @Nullable FsmTrigger trigger,
                            FsmInterrupt interruptible, FsmCondition condition) {
    public static final Codec<FsmTransition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("to").forGetter(FsmTransition::toState),
            Codec.DOUBLE.optionalFieldOf("blend_duration", 0.0).forGetter(FsmTransition::blendDuration),
            FsmTriggers.FLEX_CODEC.optionalFieldOf("trigger").forGetter(t -> Optional.ofNullable(t.trigger())),
            FsmInterrupt.ONE_LINE_CODEC.optionalFieldOf("interruptible", FsmInterrupt.DONT_INTERRUPT).forGetter(FsmTransition::interruptible),
            FsmCondition.CODEC.optionalFieldOf("condition", FsmCondition.TRUE).forGetter(FsmTransition::condition)
    ).apply(instance, (to, blend, triggerOpt, interruptible, condition) ->
            new FsmTransition(to, blend, triggerOpt.orElse(null), interruptible, condition)
    ));

}
