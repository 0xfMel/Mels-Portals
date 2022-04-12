package ftm._0xfmel.melsportals.data;

import ftm._0xfmel.melsportals.data.client.ModBlockStateProvider;
import ftm._0xfmel.melsportals.data.tags.ModBlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(bus = Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent e) {
        DataGenerator gen = e.getGenerator();
        ExistingFileHelper exFileHelper = e.getExistingFileHelper();

        gen.addProvider(new ModBlockStateProvider(gen, exFileHelper));
        gen.addProvider(new ModBlockTagsProvider(gen, exFileHelper));
    }
}
