package ftm._0xfmel.melsportals.handlers.events.world;

import java.lang.reflect.Field;

import ftm._0xfmel.melsportals.world.CustomTeleporter;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@Mod.EventBusSubscriber
public class WorldHandler {
    private static final Field PORTAL_FORCER_FIELD = ObfuscationReflectionHelper.findField(ServerWorld.class,
            "field_85177_Q");

    static {
        PORTAL_FORCER_FIELD.setAccessible(true);
    }

    @SuppressWarnings("resource")
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load e) {
        IWorld world = e.getWorld();
        if (!(world instanceof ServerWorld))
            return;

        ServerWorld serverWorld = (ServerWorld) world;
        RegistryKey<World> dimension = serverWorld.dimension();

        if (dimension != World.OVERWORLD && dimension != World.NETHER)
            return;

        try {
            PORTAL_FORCER_FIELD.set(serverWorld, new CustomTeleporter(serverWorld));
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Setting custom teleporter");
            throw new ReportedException(crashreport);
        }
    }
}
