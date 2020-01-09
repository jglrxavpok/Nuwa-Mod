package api.contentpack.common.minecraft.items;

import api.contentpack.common.minecraft.RegistryUtil;
import api.contentpack.common.minecraft.items.base.IJsonItem;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class JsonArmor extends ArmorItem implements IJsonItem {

    public JsonArmor(IArmorMaterial materialIn, EquipmentSlotType slot, Properties builder, @Nonnull ResourceLocation registryNameIn) {
        super(materialIn, slot, builder);
        RegistryUtil.forceRegistryName(this, registryNameIn);
    }


}
