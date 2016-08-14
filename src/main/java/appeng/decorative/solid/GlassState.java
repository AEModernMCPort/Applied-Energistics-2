package appeng.decorative.solid;


import java.util.EnumSet;

import net.minecraft.util.EnumFacing;


/**
 * Immutable (and thus thread-safe) class that encapsulates the rendering state required for a connected texture
 * glass block.
 */
public final class GlassState
{

	private final int x;
	private final int y;
	private final int z;

	private final EnumSet<EnumFacing> flushWith = EnumSet.noneOf( EnumFacing.class );

	public GlassState( int x, int y, int z, EnumSet<EnumFacing> flushWith )
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.flushWith.addAll( flushWith );
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public int getZ()
	{
		return z;
	}

	public boolean isFlushWith( EnumFacing side )
	{
		return flushWith.contains( side );
	}

}
