package ftm._0xfmel.melsportals.client.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;

import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;

public class ColoredPortalParticleType extends ParticleType<ColoredPortalParticleData> {
    private static final IParticleData.IDeserializer<ColoredPortalParticleData> DESERIALIZER = new IParticleData.IDeserializer<ColoredPortalParticleData>() {
        @Override
        public ColoredPortalParticleData fromCommand(ParticleType<ColoredPortalParticleData> pParticleType,
                StringReader pReader)
                throws CommandSyntaxException {
            return null;
        }

        @Override
        public ColoredPortalParticleData fromNetwork(ParticleType<ColoredPortalParticleData> pParticleType,
                PacketBuffer pBuffer) {
            return null;
        }
    };

    public ColoredPortalParticleType(String name, boolean overrideLimiter) {
        super(overrideLimiter, DESERIALIZER);

        this.setRegistryName(name);

        ModParticles.PARTICLE_TYPES.add(this);
    }

    @Override
    public Codec<ColoredPortalParticleData> codec() {
        return null;
    }
}
