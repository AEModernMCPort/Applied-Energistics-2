package appeng.client.render.model;


import java.util.EnumMap;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;


final class RenderHelper
{

	private static EnumMap<EnumFacing, List<Vec3d>> cornersForFacing = generateCornersForFacings();

	private RenderHelper() {

	}

	static List<Vec3d> getFaceCorners( EnumFacing side )
	{
		return cornersForFacing.get( side );
	}

	private static EnumMap<EnumFacing, List<Vec3d>> generateCornersForFacings()
	{
		EnumMap<EnumFacing, List<Vec3d>> result = new EnumMap<>( EnumFacing.class );

		for( EnumFacing facing : EnumFacing.values() )
		{
			List<Vec3d> corners;

			float offset = ( facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ) ? 0 : 1;

			switch( facing.getAxis() )
			{
				default:
				case X:
					corners = Lists.newArrayList(
							new Vec3d( offset, 1, 1 ),
							new Vec3d( offset, 0, 1 ),
							new Vec3d( offset, 0, 0 ),
							new Vec3d( offset, 1, 0 )
					);
					break;
				case Y:
					corners = Lists.newArrayList(
							new Vec3d( 1, offset, 1 ),
							new Vec3d( 1, offset, 0 ),
							new Vec3d( 0, offset, 0 ),
							new Vec3d( 0, offset, 1 )
					);
					break;
				case Z:
					corners = Lists.newArrayList(
							new Vec3d( 0, 1, offset ),
							new Vec3d( 0, 0, offset ),
							new Vec3d( 1, 0, offset ),
							new Vec3d( 1, 1, offset )
					);
					break;
			}

			if (facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
				corners = Lists.reverse( corners );
			}

			result.put( facing, ImmutableList.copyOf( corners ) );
		}

		return result;
	}

	private static Vec3d adjust( Vec3d vec, EnumFacing.Axis axis, double delta )
	{
		switch( axis )
		{
			default:
			case X:
				return new Vec3d( vec.xCoord + delta, vec.yCoord, vec.zCoord );
			case Y:
				return new Vec3d( vec.xCoord, vec.yCoord + delta, vec.zCoord );
			case Z:
				return new Vec3d( vec.xCoord, vec.yCoord, vec.zCoord + delta );
		}
	}
}
