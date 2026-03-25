package com.danrus.rpt.core.fpa;

import com.danrus.rpt.core.AbstractNestedSelectorItemsReloadListener;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.atomic.AtomicReference;

public class FirstPersonAnimManager
        extends AbstractNestedSelectorItemsReloadListener<CustomFirstPersonTransforms>
        implements IdentifiableResourceReloadListener {

    private static final FileToIdConverter LISTENER = FileToIdConverter.json("rpt/first_person");

    public FirstPersonAnimManager() {
        super(LISTENER, CustomFirstPersonTransforms.CODEC, false);
    }

    @Override
    protected String getNameOfObjective() {
        return "First person transforms";
    }

    @Override
    protected Identifier prepareLocation(Identifier rawLocation) {
        return LISTENER.idToFile(rawLocation);
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.fromNamespaceAndPath("rpt", "first_person_transforms");
    }

    public CustomFirstPersonTransforms getTransforms(ItemStack stack) {
        Identifier raw = stack.get(DataComponents.ITEM_MODEL);

        if (raw == null) {
            return CustomFirstPersonTransforms.DEFAULT;
        }

        AtomicReference<CustomFirstPersonTransforms> transforms = new AtomicReference<>(CustomFirstPersonTransforms.DEFAULT);

        callback(
                raw,
                stack,
                Minecraft.getInstance().player,
                CustomFirstPersonTransforms.DEFAULT,
                transforms::set
        );

        return transforms.get();
    }
}
