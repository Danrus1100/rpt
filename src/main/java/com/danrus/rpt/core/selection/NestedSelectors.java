package com.danrus.rpt.core.selection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class NestedSelectors {
	private static final Map<Codec<?>, Type<?>> TYPES = new IdentityHashMap<>();
	private static final Map<ResourceLocation, GenericCodecFactory> GLOBAL_FACTORIES = new LinkedHashMap<>();

	private NestedSelectors() {}

	private static <T> Type<T> create(Codec<T> valueCodec) {
		return new Type<>(valueCodec);
	}

	@SuppressWarnings("unchecked")
	public static synchronized <T> Type<T> type(Codec<T> valueCodec) {
		Type<T> existing = (Type<T>) TYPES.get(valueCodec);
		if (existing != null) {
			return existing;
		}

		Type<T> created = create(valueCodec);
		// Put into cache before factory materialization to break recursive codec initialization.
		TYPES.put(valueCodec, created);

		for (Map.Entry<ResourceLocation, GenericCodecFactory> entry : GLOBAL_FACTORIES.entrySet()) {
			created.register(entry.getKey(), entry.getValue().create(valueCodec));
		}

		return created;
	}

	public static synchronized <T> void register(Codec<T> valueCodec, ResourceLocation id, MapCodec<? extends NestedSelector.Unbaked<T>> mapCodec) {
		type(valueCodec).register(id, mapCodec);
	}

	public static synchronized void register(ResourceLocation id, GenericCodecFactory factory) {
		GLOBAL_FACTORIES.put(id, factory);
		for (Type<?> type : TYPES.values()) {
			registerForExistingType(type, id, factory);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> void registerForExistingType(Type<?> type, ResourceLocation id, GenericCodecFactory factory) {
		Type<T> typed = (Type<T>) type;
		typed.register(id, factory.create(typed.valueCodec()));
	}

	public static <T> Codec<NestedSelector.Unbaked<T>> codec(Codec<T> valueCodec) {
		return type(valueCodec).codec();
	}

	@FunctionalInterface
	public interface GenericCodecFactory {
		<T> MapCodec<? extends NestedSelector.Unbaked<T>> create(Codec<T> valueCodec);
	}

	public static final class Type<T> {
		private final Codec<T> valueCodec;
		private final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends NestedSelector.Unbaked<T>>> idMapper = new ExtraCodecs.LateBoundIdMapper<>();
		private final Codec<NestedSelector.Unbaked<T>> codec;

		private Type(Codec<T> valueCodec) {
			this.valueCodec = valueCodec;
			this.codec = idMapper
					.codec(ResourceLocation.CODEC)
					.dispatch(unbaked -> unbaked.type(this.valueCodec), mapCodec -> mapCodec);
		}

		public void register(ResourceLocation id, MapCodec<? extends NestedSelector.Unbaked<T>> mapCodec) {
			idMapper.put(id, mapCodec);
		}

		public Codec<NestedSelector.Unbaked<T>> codec() {
			return codec;
		}

		private Codec<T> valueCodec() {
			return valueCodec;
		}

		public ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends NestedSelector.Unbaked<T>>> mapper() {
			return idMapper;
		}
	}

}