package ftm._0xfmel.melsportals.capabilities;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;

@OnlyIn(Dist.CLIENT)
public interface ICustomPortalPosition {
    BlockPos getPortalPos();

    void setPortalPos(BlockPos portalPos);

    @OnlyIn(Dist.CLIENT)
    public class Provider extends CapabilityProvider<Provider> {
        private LazyOptional<ICustomPortalPosition> customPortalPositionLazyOptional = LazyOptional
                .of(CustomPortalPosition::new);

        public Provider() {
            super(Provider.class, true);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            if (cap == CustomPortalPositionCapability.CUSTOM_PORTAL_POSITION_CAPABILITY) {
                return this.customPortalPositionLazyOptional.cast();
            }
            return super.getCapability(cap, side);
        }

        @Override
        protected void invalidateCaps() {
            super.invalidateCaps();
            this.customPortalPositionLazyOptional.invalidate();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class CustomPortalPosition implements ICustomPortalPosition {
        private BlockPos portalPos;

        @Override
        public BlockPos getPortalPos() {
            return this.portalPos;
        }

        @Override
        public void setPortalPos(BlockPos portalPos) {
            this.portalPos = portalPos;
        }
    }
}
