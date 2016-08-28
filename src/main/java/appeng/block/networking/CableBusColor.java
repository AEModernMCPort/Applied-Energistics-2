package appeng.block.networking;


import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.parts.CableBusContainer;


@SideOnly( Side.CLIENT )
public class CableBusColor implements IBlockColor
{

	@Override
	public int colorMultiplier( IBlockState state, IBlockAccess worldIn, BlockPos pos, int color )
	{

		boolean active = true;
		AEColor busColor = AEColor.Transparent;

		if( state instanceof IExtendedBlockState )
		{
			AEPartLocation side = AEPartLocation.fromOrdinal( ( color >> 2 ) & 7 );
			CableBusContainer bus = ( (IExtendedBlockState) state ).getValue( BlockCableBus.cableBus );
			if( bus != null )
			{
				active = bus.getGridNode( side ) != null && bus.getGridNode( side ).isActive();
				busColor = bus.getColor();
			}
		}

		switch( color & 3 )
		{
			case 0:
				return active ? 0xffffff : 0;
			case 1:
				return busColor.blackVariant;
			case 2:
				return busColor.mediumVariant;
			case 3:
				return busColor.whiteVariant;
			default:
				return color;
		}
	}
}
