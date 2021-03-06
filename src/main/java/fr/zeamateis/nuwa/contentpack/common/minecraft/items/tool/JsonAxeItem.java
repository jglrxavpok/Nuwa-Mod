package fr.zeamateis.nuwa.contentpack.common.minecraft.items.tool;

import fr.zeamateis.nuwa.contentpack.common.minecraft.items.base.IJsonItem;
import fr.zeamateis.nuwa.contentpack.common.minecraft.util.RegistryUtil;
import net.minecraft.item.AxeItem;
import net.minecraft.item.IItemTier;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

//TODO Check if it's necessary to replace the effective_on set
public class JsonAxeItem extends AxeItem implements IJsonItem {

    public JsonAxeItem(IItemTier tier, float attackDamageIn, float attackSpeedIn, Properties builder, @Nonnull ResourceLocation registryNameIn) {
        super(tier, attackDamageIn, attackSpeedIn, builder);
        RegistryUtil.forceRegistryName(this, registryNameIn);
    }
}
