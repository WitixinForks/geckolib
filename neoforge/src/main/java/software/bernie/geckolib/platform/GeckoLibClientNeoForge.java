package software.bernie.geckolib.platform;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.service.GeckoLibClient;

/**
 * NeoForge service implementation for clientside functionalities
 */
public class GeckoLibClientNeoForge implements GeckoLibClient {
    /**
     * Helper method for retrieving an (ideally) cached instance of the armor model for a given Item
     * <p>
     * If no custom model applies to this item, the {@code defaultModel} is returned
     */
    @NotNull
    @Override
    public <T extends LivingEntity & GeoAnimatable> HumanoidModel<?> getArmorModelForItem(T animatable, ItemStack stack, EquipmentSlot slot, HumanoidModel<LivingEntity> defaultModel) {
        Item item = stack.getItem();
        HumanoidModel<?> model = IClientItemExtensions.of(item).getHumanoidArmorModel(animatable, stack, slot, defaultModel);

        if (model == defaultModel && GeoRenderProvider.of(item).getGeoArmorRenderer(animatable, stack, slot, defaultModel) instanceof GeoArmorRenderer<?> geoArmorRenderer)
            return geoArmorRenderer;

        return model;
    }

    /**
     * Helper method for retrieving an (ideally) cached instance of the GeoModel for the given item
     *
     * @return The GeoModel for the item, or null if not applicable
     */
    @Nullable
    @Override
    public GeoModel<?> getGeoModelForItem(ItemStack item) {
        if (IClientItemExtensions.of(item).getCustomRenderer() instanceof GeoRenderer<?> geoRenderer)
            return geoRenderer.getGeoModel();

        if (GeoRenderProvider.of(item).getGeoItemRenderer() instanceof GeoRenderer<?> geoRenderer)
            return geoRenderer.getGeoModel();

        return null;
    }

    /**
     * Helper method for retrieving an (ideally) cached instance of the GeoModel for the given item's armor renderer
     *
     * @return The GeoModel for the item, or null if not applicable
     */
    @Nullable
    @Override
    public GeoModel<?> getGeoModelForArmor(ItemStack armour) {
        if (IClientItemExtensions.of(armour).getHumanoidArmorModel(null, armour, null, null) instanceof GeoArmorRenderer<?> armorRenderer)
            return armorRenderer.getGeoModel();

        if (GeoRenderProvider.of(armour).getGeoArmorRenderer(null, armour, null, null) instanceof GeoArmorRenderer<?> armorRenderer)
            return armorRenderer.getGeoModel();

        return null;
    }
}
