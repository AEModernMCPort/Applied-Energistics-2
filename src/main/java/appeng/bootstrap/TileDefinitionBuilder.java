package appeng.bootstrap;


import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

import appeng.block.AEBaseTileBlock;
import appeng.bootstrap.components.BlockColorRegistration;
import appeng.bootstrap.components.ItemColorRegistration;
import appeng.bootstrap.components.ItemModelRegistrationComponent;
import appeng.bootstrap.components.ItemVariantsComponent;
import appeng.bootstrap.components.TesrComponent;
import appeng.client.render.model.CachingRotatingBakedModel;
import appeng.core.CreativeTab;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockStackSrc;
import appeng.core.features.TileDefinition;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;


public class TileDefinitionBuilder extends DefinitionBuilder<TileDefinitionBuilder>
{

	private final Supplier<AEBaseTileBlock> blockSupplier;

	private BiFunction<ModelResourceLocation, IBakedModel, IBakedModel> modelCustomizer;

	private IBlockColor blockColor;

	private IItemColor itemColor;

	TileDefinitionBuilder( FeatureFactory registry, String id, Supplier<AEBaseTileBlock> blockSupplier )
	{
		super( registry, id );
		this.blockSupplier = blockSupplier;
	}

	public TileDefinitionBuilder modelCustomizer( BiFunction<ModelResourceLocation, IBakedModel, IBakedModel> customizer )
	{
		this.modelCustomizer = customizer;
		return this;
	}

	public TileDefinitionBuilder blockColor(IBlockColor blockColor) {
		this.blockColor = blockColor;
		return this;
	}

	public TileDefinitionBuilder itemColor(IItemColor itemColor) {
		this.itemColor = itemColor;
		return this;
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

		if( Platform.isClient() )
		{
			registry.addBootstrapComponent( new ItemVariantsComponent( item ) );
			registry.addBootstrapComponent( new ItemModelRegistrationComponent( block, item ) );

			TileEntitySpecialRenderer tesr = block.getTESR();
			if( tesr != null )
			{
				registry.addBootstrapComponent( new TesrComponent<>( tileEntityClass, tesr ) );
			}

			if( modelCustomizer != null )
			{
				registry.modelOverrideComponent.addOverride( registryName, modelCustomizer );
			}
			else
			{
				registry.modelOverrideComponent.addOverride( registryName, ( l, m ) -> new CachingRotatingBakedModel( m ) );
			}

			if( blockColor != null )
			{
				registry.addBootstrapComponent( new BlockColorRegistration( block, blockColor ) );
			}

			if( itemColor != null )
			{
				registry.addBootstrapComponent( new ItemColorRegistration( item, itemColor ) );
			}
		}

		return definition;
	}
}
