package com.danrus.rpt.core.textures;

import com.danrus.rpt.core.AbstractNestedSelectorItemsReloadListener;
import com.danrus.rpt.core.selection.NestedSelector;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TextureSwappersManager
        extends AbstractNestedSelectorItemsReloadListener<ResourceLocation>
        implements IdentifiableResourceReloadListener {

    private static final FileToIdConverter LISTENER = FileToIdConverter.json("rpt/swappers");

    public TextureSwappersManager() {
        super(
                LISTENER,
                ResourceLocation.CODEC,
                true
        );
    }

    @Override
    protected String getNameOfObjective() {
        return "Swapper";
    }

    @Override
    protected ResourceLocation prepareLocation(ResourceLocation rawLocation) {
        String path = rawLocation.getPath().replace(".png", "");

        ResourceLocation swapperLocation = ResourceLocation.fromNamespaceAndPath(
                rawLocation.getNamespace(),
                path
        );

        return LISTENER.idToFile(swapperLocation);
    }


    public void swap(ResourceLocation original, ItemStack stack, @Nullable LivingEntity entity, SwapApplier applier) {

        callback(
                original,
                stack,
                entity,
                original,
                applier::apply
        );
    }

    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath("rpt", "texture_swappers");
    }
}