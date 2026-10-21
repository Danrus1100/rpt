package com.danrus.rpt.core.textures;

import com.danrus.rpt.core.AbstractNestedSelectorItemsReloadListener;
import com.danrus.rpt.core.selection.NestedSelector;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TextureSwappersManager
        extends AbstractNestedSelectorItemsReloadListener<Identifier> {

    private static final FileToIdConverter LISTENER = FileToIdConverter.json("rpt/swappers");

    public TextureSwappersManager() {
        super(
                LISTENER,
                Identifier.CODEC,
                true
        );
    }

    @Override
    protected String getNameOfObjective() {
        return "Swapper";
    }

    @Override
    protected Identifier prepareLocation(Identifier rawLocation) {
        String path = rawLocation.getPath().replace(".png", "");

        Identifier swapperLocation = Identifier.fromNamespaceAndPath(
                rawLocation.getNamespace(),
                path
        );

        return LISTENER.idToFile(swapperLocation);
    }


    public void swap(Identifier original, ItemStack stack, @Nullable LivingEntity entity, SwapApplier applier) {

        callback(
                original,
                stack,
                entity,
                original,
                applier::apply
        );
    }
}