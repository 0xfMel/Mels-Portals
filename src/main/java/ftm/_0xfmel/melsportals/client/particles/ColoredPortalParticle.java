package ftm._0xfmel.melsportals.client.particles;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ColoredPortalParticle extends PortalParticle {
    protected ColoredPortalParticle(ClientWorld worldIn, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed, ColoredPortalParticleData data) {
        super(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);

        float f = this.random.nextFloat() * 0.6F + 0.4F;
        this.rCol = data.getR() * f;
        this.gCol = data.getG() * f;
        this.bCol = data.getB() * f;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<ColoredPortalParticleData> {
        private final IAnimatedSprite sprite;

        public Factory(IAnimatedSprite spriteIn) {
            this.sprite = spriteIn;
        }

        public Particle createParticle(ColoredPortalParticleData data, ClientWorld world, double x,
                double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ColoredPortalParticle particle = new ColoredPortalParticle(world, x, y, z,
                    xSpeed, ySpeed, zSpeed, data);
            particle.pickSprite(this.sprite);
            return particle;
        }
    }
}
