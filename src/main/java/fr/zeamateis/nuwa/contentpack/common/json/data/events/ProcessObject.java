package fr.zeamateis.nuwa.contentpack.common.json.data.events;

import net.minecraft.util.ResourceLocation;

public class ProcessObject {

    private String processClass;
    private String registryName;

    public ProcessObject(String processClass, String registryName) {
        this.processClass = processClass;
        this.registryName = registryName;
    }

    public String getProcessClass() {
        return processClass;
    }

    public ResourceLocation getRegistryName() {
        return new ResourceLocation(registryName);
    }
}