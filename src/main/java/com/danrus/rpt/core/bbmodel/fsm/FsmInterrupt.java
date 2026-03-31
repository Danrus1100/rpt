package com.danrus.rpt.core.bbmodel.fsm;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.animation.Animation;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public record FsmInterrupt(Mode mode, float exitTime) {

    public static final FsmInterrupt DONT_INTERRUPT = new FsmInterrupt(Mode.NONE, 1f);
    public static final FsmInterrupt FORCE_INTERRUPT = new FsmInterrupt(Mode.FORCE, 0f);

    public static final Codec<FsmInterrupt> CODEC = RecordCodecBuilder.create(i -> i.group(
            Mode.CODEC.fieldOf("mode").forGetter(FsmInterrupt::mode),
            Codec.FLOAT.optionalFieldOf("exit_time", 1f).forGetter(FsmInterrupt::exitTime)
    ).apply(i, FsmInterrupt::new));

    private static DataResult<Object> validatePercents(Object o) {
        if (o instanceof FsmInterrupt interrupt) {
            if (interrupt.mode != Mode.PERCENTS) return DataResult.success(interrupt);
            if (interrupt.exitTime > 1f) return DataResult.error(() -> "for \"percents\" interrupt valid \"exit_time\" values between 0.0 and 1.0!");
            return DataResult.success(interrupt);
        }
        return DataResult.error(() -> o + "is not FsmInterrupt!");
    }

    public static final Codec<FsmInterrupt> ONE_LINE_CODEC = Codec.either(Codec.BOOL, Codec.either(Codec.STRING, Codec.FLOAT)).xmap(
            either -> either.map(
                    b -> b ? FORCE_INTERRUPT : DONT_INTERRUPT,
                    n -> n.map(
                            string -> {
                                if (string.endsWith("%")) {
                                    try {
                                        return new FsmInterrupt(Mode.PERCENTS, Float.parseFloat(string.replace("%", "")) / 100f);
                                    } catch (NumberFormatException e) {
                                        return FORCE_INTERRUPT;
                                    }
                                } else {
                                    try {
                                        return new FsmInterrupt(Mode.TIME, Float.parseFloat(string));
                                    } catch (NumberFormatException e) {
                                        return FORCE_INTERRUPT;
                                    }
                                }
                            },
                            f -> new FsmInterrupt(Mode.TIME, f)
                    )
            ),
            fsmInterrupt -> switch (fsmInterrupt.mode) {
                    case NONE -> Either.left(false);
                    case FORCE -> Either.left(true);
                    case TIME -> Either.right(Either.right(fsmInterrupt.exitTime));
                    case PERCENTS -> Either.right(Either.left(fsmInterrupt.exitTime * 100f + "%"));
            }
    );

    public boolean shouldInterrupt(BbModelDocument document, FsmState state, double currentStateTime) {
        return switch (mode) {
            case NONE -> false;
            case FORCE -> true;
            case PERCENTS -> {
                Animation animation = FsmInstance.findAnimationByName(document, state.getAnimationName());
                if (animation == null) yield true;
                double progress = currentStateTime / animation.getDuration();
                yield progress >= exitTime;
            }
            case TIME -> currentStateTime >= exitTime;
        };
    }


    public enum Mode implements StringRepresentable {
        NONE("none"),
        FORCE("force"),
        PERCENTS("percents"),
        TIME("time");

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public @NonNull String getSerializedName() {
            return name;
        }
    }
}
