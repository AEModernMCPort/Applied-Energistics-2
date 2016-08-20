package appeng.bootstrap;


import java.util.function.Supplier;

import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.block.AEBaseTileBlock;
import appeng.core.CreativeTab;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockStackSrc;
import appeng.core.features.TileDefinition;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;


public class TileDefinitionBuilder extends DefinitionBuilder<TileDefinitionBuilder>
{

	private final Supplier<AEBaseTileBlock> blockSupplier;

	@SideOnly( Side.CLIENT )
	private RenderingCustomizer renderingCustomizer;

	TileDefinitionBuilder( FeatureFactory registry, String id, Supplier<AEBaseTileBlock> blockSupplier )
	{
		super( registry, id );
		this.blockSupplier = blockSupplier;
	}

	public TileDefinitionBuilder rendering( RenderingCustomizerCallback callback )
	{
		if( Platform.isClient() )
		{
			customizeForClient( callback );
		}

		return this;
	}

	@SideOnly( Side.CLIENT )
	private void customizeForClient( RenderingCustomizerCallback callback )
	{
		if( renderingCustomizer == null )
		{
			renderingCustomizer = new RenderingCustomizer();
		}
		callback.customize( renderingCustomizer );
	}

	public TileDefinition build()
	{
		if( !isActive() )
		{
			return new TileDefinition( registryName, null );
		}

		AEBaseTileBlock block = blockSupplier.get();
		block.setRegistryName( resourceLocation );

		TileDefinition definition = new TileDefinition( registryName, block );
		ItemBlock item = definition.maybeItemBlock().get();
		item.setRegistryName( resourceLocation );

		block.setCreativeTab( CreativeTab.instance );
		block.setUnlocalizedName( "appliedenergistics2." + registryName );

		registry.addPreInit( side ->
		{
			GameRegistry.register( block );
			GameRegistry.register( item );
		} );

		Class<? extends AEBaseTile> tileEntityClass = block.getTileEntityClass();
		AEBaseTile.registerTileItem( tileEntityClass, new BlockStackSrc( block, 0, ActivityState.Enabled ) );

		GameRegistry.registerTileEntity( tileEntityClass, registryName );

		if( Platform.isClient() && renderingCustomizer != null )
		{
			renderingCustomizer.apply( registry, block, item, tileEntityClass );
		}

		return definition;
	}
}
