
package appeng.api.definitions;


import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.ImmutablePair;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Optional;


public interface ITileDefinition<T extends TileEntity> extends IDefinition< Class<T> >
{

	/**
	 * @return block of this tile
	 */
	@Nonnull
	<B extends Block> Optional<IBlockDefinition<B>> maybeBlock();

	/**
	 * Compare tile in world with this.
	 *
	 * @param world of tile
	 * @param pos of tile
	 *
	 * @return whether the tile at the location is the same
	 */
	default boolean isSameAs( IBlockAccess world, BlockPos pos )
	{
		return isSameAs( new ImmutablePair( world, pos ) );
	}

}
