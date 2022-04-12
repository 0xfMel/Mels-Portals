package ftm._0xfmel.melsportals.client.particles;

import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ColoredPortalParticleData implements IParticleData {
    private final float r;
    private final float g;
    private final float b;

    public ColoredPortalParticleData(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    @OnlyIn(Dist.CLIENT)
    public float getR() {
        return r;
    }

    @OnlyIn(Dist.CLIENT)
    public float getG() {
        return g;
    }

    @OnlyIn(Dist.CLIENT)
    public float getB() {
        return b;
    }

    @Override
    public ParticleType<?> getType() {
        return ModParticles.COLORED_PORTAL;
    }

    @Override
    public void writeToNetwork(PacketBuffer pBuffer) {
    }

    @Override
    public String writeToString() {
        return null;
    }
}
