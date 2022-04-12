package ftm._0xfmel.melsportals.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.particles.IParticleData;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParticleUtil {
    public static <T extends IParticleData> void spawnParticle(
            T data,
            World worldIn,
            boolean alwaysRender,
            boolean minimizeLevel,
            double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed) {
        Minecraft mc = Minecraft.getInstance();
        ActiveRenderInfo activerenderinfo = mc.gameRenderer.getMainCamera();

        if (activerenderinfo.isInitialized() && mc.particleEngine != null) {
            ParticleStatus particlestatus = ParticleUtil.calculateParticleLevel(worldIn, minimizeLevel);

            Particle particle = mc.particleEngine.createParticle(data, xCoord, yCoord,
                    zCoord, xSpeed, ySpeed, zSpeed);

            if (alwaysRender) {
                mc.particleEngine.add(particle);
            } else if (!(activerenderinfo.getPosition().distanceToSqr(xCoord, yCoord, zCoord) > 1024.0D)
                    && particlestatus != ParticleStatus.MINIMAL) {
                mc.particleEngine.add(particle);
            }
        }
    }

    private static ParticleStatus calculateParticleLevel(World worldIn, boolean pMinimiseLevel) {
        ParticleStatus particlestatus = Minecraft.getInstance().options.particles;

        if (pMinimiseLevel && particlestatus == ParticleStatus.MINIMAL && worldIn.random.nextInt(10) == 0) {
            particlestatus = ParticleStatus.DECREASED;
        }

        if (particlestatus == ParticleStatus.DECREASED && worldIn.random.nextInt(3) == 0) {
            particlestatus = ParticleStatus.MINIMAL;
        }

        return particlestatus;
    }
}
