package ftm._0xfmel.melsportals.data.tags;

import ftm._0xfmel.melsportals.gameobjects.blocks.ModBlocks;
import ftm._0xfmel.melsportals.globals.ModGlobals;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(DataGenerator pGenerator, ExistingFileHelper exFileHelper) {
        super(pGenerator, ModGlobals.MOD_ID, exFileHelper);
    }

    @Override
    protected void addTags() {
        this.tag(BlockTags.PORTALS).add(ModBlocks.CUSTOM_PORTAL);
        this.tag(BlockTags.HOGLIN_REPELLENTS).add(ModBlocks.CUSTOM_PORTAL);
    }
}
