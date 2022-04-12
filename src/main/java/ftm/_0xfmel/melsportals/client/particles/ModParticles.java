package ftm._0xfmel.melsportals.client.particles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.particles.ParticleType;

public class ModParticles {
        public static final List<ParticleType<?>> PARTICLE_TYPES = new ArrayList<ParticleType<?>>();

        public static final ParticleType<ColoredPortalParticleData> COLORED_PORTAL = new ColoredPortalParticleType(
                        "colored_portal", false);
}
