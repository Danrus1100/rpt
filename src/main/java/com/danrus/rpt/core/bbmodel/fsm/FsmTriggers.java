package com.danrus.rpt.core.bbmodel.fsm;

import com.danrus.rpt.core.expression.GameExpressionsHelper;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class FsmTriggers {

    public static final String DRAW = "draw";
    public static final String ATTACK = "attack";
    public static final String ATTACK1 = "attack_1";
    public static final String ATTACK2 = "attack_2";
    public static final String USE = "use";
    public static final String ANIMATION_FINISHED = "animation_finished";
//    public static final String RELOAD = "reload";
//    public static final String START_SPRINT = "start_sprint";
//    public static final String STOP_SPRINT = "stop_sprint";
//    public static final String START_SNEAK = "start_sneak";
//    public static final String STOP_SNEAK = "stop_sneak";
//    public static final String JUMP = "jump";

    public static final ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends FsmTrigger>> ID_MAPPER = new ExtraCodecs.LateBoundIdMapper<>();
    public static final Codec<FsmTrigger> CODEC;
    public static final Codec<FsmTrigger> FLEX_CODEC;

    public static void bootstrap() {
        ID_MAPPER.put(Identifier.withDefaultNamespace("simple"), SimpleTrigger.MAP_CODEC);
        ID_MAPPER.put(Identifier.withDefaultNamespace("conditional"), ConditionalTrigger.MAP_CODEC);
        ID_MAPPER.put(Identifier.withDefaultNamespace("keypress"), InputPressed.MAP_CODEC);
    }

    static {
        CODEC = ID_MAPPER.codec(Identifier.CODEC).dispatch(FsmTrigger::type, (mapCodec) -> mapCodec);
        FLEX_CODEC = Codec.either(Codec.STRING, CODEC).xmap(
                either -> either.map(SimpleTrigger::new, trigger -> trigger),
                trigger -> {
                    if (trigger instanceof SimpleTrigger simple) {
                        return Either.left(simple.id());
                    }
                    return Either.right(trigger);
                }
        );
    }

    public record SimpleTrigger(String id) implements FsmTrigger {
        public static final MapCodec<SimpleTrigger> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(SimpleTrigger::id)
        ).apply(instance, SimpleTrigger::new));

        @Override
        public boolean test(Set<String> activeTriggers, Map<String, Double> customVariables, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            return activeTriggers.contains(id);
        }

        @Override
        public MapCodec<? extends FsmTrigger> type() {
            return MAP_CODEC;
        }
    }

    public record ConditionalTrigger(String condition) implements FsmTrigger {
        public static final MapCodec<ConditionalTrigger> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("condition").forGetter(ConditionalTrigger::condition)
        ).apply(instance, ConditionalTrigger::new));

        @Override
        public boolean test(Set<String> activeTriggers, Map<String, Double> customVariables, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            return GameExpressionsHelper.evaluateCondition(condition, 0, customVariables, level, entity, seed);
        }

        @Override
        public MapCodec<? extends FsmTrigger> type() {
            return MAP_CODEC;
        }
    }

    public record InputPressed(int input) implements FsmTrigger {

        public static final MapCodec<InputPressed> MAP_CODEC = RecordCodecBuilder.mapCodec(inputPressedInstance -> inputPressedInstance.group(
                Codec.INT.fieldOf("key").forGetter(InputPressed::input)
        ).apply(inputPressedInstance, InputPressed::new));

        @Override
        public boolean test(Set<String> activeTriggers, Map<String, Double> customVariables, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow()
                    //? <=1.21.8
                    //.getWindow()
                    , input);
        }

        @Override
        public MapCodec<? extends FsmTrigger> type() {
            return MAP_CODEC;
        }
    }
}
