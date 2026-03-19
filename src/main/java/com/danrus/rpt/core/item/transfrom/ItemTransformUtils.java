package com.danrus.rpt.core.item.transfrom;

import net.minecraft.client.renderer.block.model.ItemTransform;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class ItemTransformUtils {
    private ItemTransformUtils(){}

    public static ItemTransform merge(ItemTransform left, ItemTransform right) {
        Vector3f rotation = left.rotation().add(right.rotation(), new Vector3f());
        Vector3f translation = left.translation().add(right.translation(), new Vector3f());
        Vector3f scale = left.scale().add(right.scale(), new Vector3f());

        return new ItemTransform(rotation, translation, scale);
    }
}
