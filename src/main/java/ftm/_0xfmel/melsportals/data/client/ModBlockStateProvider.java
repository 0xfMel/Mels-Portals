package ftm._0xfmel.melsportals.data.client;

import ftm._0xfmel.melsportals.gameobjects.blocks.CustomPortalBlock;
import ftm._0xfmel.melsportals.gameobjects.blocks.ModBlocks;
import ftm._0xfmel.melsportals.globals.ModGlobals;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.DyeColor;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, ModGlobals.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        VariantBlockStateBuilder customPortalBuilder = this.getVariantBuilder(ModBlocks.CUSTOM_PORTAL);

        PartialBlockstate purpleState = customPortalBuilder.partialState().with(CustomPortalBlock.COLOR,
                DyeColor.PURPLE);
        PartialBlockstate xState = customPortalBuilder.partialState().with(NetherPortalBlock.AXIS, Direction.Axis.X);

        ModelFile customNS = this.models().getBuilder(ModBlocks.CUSTOM_PORTAL.getRegistryName().getPath() + "_ns")
                .texture("portal", this.blockTexture(ModBlocks.CUSTOM_PORTAL))
                .texture("particle", this.blockTexture(ModBlocks.CUSTOM_PORTAL))
                .element().from(0, 0, 6).to(16, 16, 10)
                .face(Direction.NORTH).texture("#portal").tintindex(0).end()
                .face(Direction.SOUTH).texture("#portal").tintindex(0).end()
                .end();

        ModelFile customEW = this.models().getBuilder(ModBlocks.CUSTOM_PORTAL.getRegistryName().getPath() + "_ew")
                .texture("portal", this.blockTexture(ModBlocks.CUSTOM_PORTAL))
                .texture("particle", this.blockTexture(ModBlocks.CUSTOM_PORTAL))
                .element().from(6, 0, 0).to(10, 16, 16)
                .face(Direction.EAST).texture("#portal").tintindex(0).end()
                .face(Direction.WEST).texture("#portal").tintindex(0).end()
                .end();

        customPortalBuilder.forAllStates((blockState) -> {
            ModelFile modelFile;
            if (purpleState.test(blockState)) {
                modelFile = this.models()
                        .getExistingFile(this.mcLoc(xState.test(blockState) ? "nether_portal_ns" : "nether_portal_ew"));
            } else {
                modelFile = xState.test(blockState) ? customNS : customEW;
            }
            return new ConfiguredModel[] { new ConfiguredModel(modelFile) };
        });
    }
}
