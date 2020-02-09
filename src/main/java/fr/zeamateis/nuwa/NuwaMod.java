package fr.zeamateis.nuwa;

import api.contentpack.ContentPack;
import api.contentpack.PackManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.zeamateis.nuwa.common.network.C2SContentPackInfoPacket;
import fr.zeamateis.nuwa.common.network.S2CContentPackInfoPacket;
import fr.zeamateis.nuwa.contentpack.client.minecraft.assets.ContentPackFinder;
import fr.zeamateis.nuwa.contentpack.common.data.*;
import fr.zeamateis.nuwa.contentpack.common.json.adapter.IProcessAdapter;
import fr.zeamateis.nuwa.contentpack.common.json.adapter.ItemStackAdapter;
import fr.zeamateis.nuwa.contentpack.common.json.data.events.processes.base.IProcess;
import fr.zeamateis.nuwa.contentpack.common.minecraft.registries.ItemGroupType;
import fr.zeamateis.nuwa.init.NuwaRegistries;
import fr.zeamateis.nuwa.proxy.ClientProxy;
import fr.zeamateis.nuwa.proxy.CommonProxy;
import fr.zeamateis.nuwa.proxy.ServerProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Predicate;

//TODO Fix content packs not closed after loading.
@Mod(Constant.MODID)
public class NuwaMod implements ISelectiveResourceReloadListener {
    public static final Logger LOGGER = LogManager.getLogger();
    private static final String PROTOCOL_VERSION = String.valueOf(1);
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(Constant.MODID, "nuwa_channel"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();
    private static final CommonProxy PROXY = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);
    private static NuwaMod instance;
    private final PackManager packManager;

    public NuwaMod() {
        instance = this;
        this.packManager = new PackManager(Constant.DATA_VERSION, LOGGER, PROXY.getPackDir().toPath());
        this.packManager.setGson(this.nuwaGsonInstance());

        this.packManager.registerData(new ResourceLocation(Constant.MODID, "processes_data"), ProcessesData.class, NuwaRegistries.PROCESS);

        this.packManager.registerData(new ResourceLocation(Constant.MODID, "item_group_data"), ItemGroupData.class, NuwaRegistries.ITEM_GROUP);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> this::registerVanillaGroup);

        this.packManager.registerData(new ResourceLocation(Constant.MODID, "block_data"), BlocksData.class, ForgeRegistries.BLOCKS);
        this.packManager.registerData(new ResourceLocation(Constant.MODID, "item_data"), ItemsData.class, ForgeRegistries.ITEMS);
        this.packManager.registerData(new ResourceLocation(Constant.MODID, "ores_generation_data"), OresGenerationData.class);
        this.packManager.registerData(new ResourceLocation(Constant.MODID, "armor_material_data"), ArmorMaterialData.class, NuwaRegistries.ARMOR_MATERIAL);
        this.packManager.registerData(new ResourceLocation(Constant.MODID, "tool_material_data"), ToolMaterialData.class, NuwaRegistries.TOOL_MATERIAL);
        this.packManager.registerData(new ResourceLocation(Constant.MODID, "sounds_data"), SoundsData.class, ForgeRegistries.SOUND_EVENTS);

        this.packManager.loadPacks();

        MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> this::registerAssetsManager);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
    }

    static CommonProxy getProxy() {
        return PROXY;
    }

    public static PackManager getPackManager() {
        return instance.packManager;
    }

    public static SimpleChannel getNetwork() {
        return CHANNEL;
    }

    private Gson nuwaGsonInstance() {
        return new GsonBuilder()
                .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
                .registerTypeAdapter(IProcess.class, new IProcessAdapter())
               // .registerTypeAdapter(ICondition.class, new IConditionAdapter())
                .create();
    }

    @OnlyIn(Dist.CLIENT)
    private void registerVanillaGroup() {
        NuwaRegistries.ITEM_GROUP.register(new ItemGroupType(new ResourceLocation("minecraft:blocks"), ItemGroup.BUILDING_BLOCKS));
        NuwaRegistries.ITEM_GROUP.register(new ItemGroupType(new ResourceLocation("minecraft:brewing"), ItemGroup.BREWING));
        NuwaRegistries.ITEM_GROUP.register(new ItemGroupType(new ResourceLocation("minecraft:combat"), ItemGroup.COMBAT));
        NuwaRegistries.ITEM_GROUP.register(new ItemGroupType(new ResourceLocation("minecraft:decoration"), ItemGroup.DECORATIONS));
        NuwaRegistries.ITEM_GROUP.register(new ItemGroupType(new ResourceLocation("minecraft:food"), ItemGroup.FOOD));
        NuwaRegistries.ITEM_GROUP.register(new ItemGroupType(new ResourceLocation("minecraft:misc"), ItemGroup.MISC));
        NuwaRegistries.ITEM_GROUP.register(new ItemGroupType(new ResourceLocation("minecraft:redstone"), ItemGroup.REDSTONE));
        NuwaRegistries.ITEM_GROUP.register(new ItemGroupType(new ResourceLocation("minecraft:tools"), ItemGroup.TOOLS));
        NuwaRegistries.ITEM_GROUP.register(new ItemGroupType(new ResourceLocation("minecraft:transportation"), ItemGroup.TRANSPORTATION));
    }

    @OnlyIn(Dist.CLIENT)
    private void registerAssetsManager() {
        for (ContentPack contentPack : packManager.getPacks()) {
            Minecraft.getInstance().getResourcePackList().addPackFinder(new ContentPackFinder(packManager, contentPack));
        }
        ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(this);
    }

    private void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        for (ContentPack contentPack : getPackManager().getPacks()) {
            event.getServer().getResourcePacks().addPackFinder(new ContentPackFinder(packManager, contentPack));
        }
    }


    /**
     * A version of onResourceManager that selectively chooses {@link IResourceType}s
     * to reload.
     * When using this, the given predicate should be called to ensure the relevant resources should
     * be reloaded at this time.
     *
     * @param resourceManager   the resource manager being reloaded
     * @param resourcePredicate predicate to test whether any given resource type should be reloaded
     */
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        for (ContentPack contentPack : getPackManager().getPacks()) {
            Minecraft.getInstance().getResourcePackList().addPackFinder(new ContentPackFinder(packManager, contentPack));
        }
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        CHANNEL.messageBuilder(S2CContentPackInfoPacket.class, 0)
                .encoder(S2CContentPackInfoPacket::encode)
                .decoder(S2CContentPackInfoPacket::decode)
                .consumer(S2CContentPackInfoPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SContentPackInfoPacket.class, 1)
                .encoder(C2SContentPackInfoPacket::encode)
                .decoder(C2SContentPackInfoPacket::decode)
                .consumer(C2SContentPackInfoPacket::handle)
                .add();
    }
}