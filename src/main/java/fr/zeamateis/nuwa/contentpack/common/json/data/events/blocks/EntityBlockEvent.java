package fr.zeamateis.nuwa.contentpack.common.json.data.events.blocks;

import fr.zeamateis.nuwa.contentpack.common.json.data.events.base.ProcessEvent;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EntityBlockEvent extends ProcessEvent {

    private List<String> affectedEntities = new ArrayList<>();

    public List<EntityType<?>> getAffectedEntities() {
        return affectedEntities
                .stream().map(ResourceLocation::new).collect(Collectors.toList())
                .stream().map(ForgeRegistries.ENTITIES::getValue).collect(Collectors.toList());
    }

}
