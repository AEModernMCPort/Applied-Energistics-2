package appeng.bootstrap;


import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.google.common.collect.ObjectArrays;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.definitions.IBlockDefinition;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseTileBlock;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.CreativeTab;
import appeng.core.features.AEFeature;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockDefinition;
import appeng.core.features.BlockStackSrc;
import appeng.core.features.TileDefinition;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;


class BlockDefinitionBuilder implements IBlockBuilder
{

	private final FeatureFactory factory;

	private final String registryName;

	private final Supplier<? extends Block> blockSupplier;

	private final List<BiConsumer<Block, Item>> preInitCallbacks = new ArrayList<>();

	private final List<BiConsumer<Block, Item>> initCallbacks = new ArrayList<>();

	private final List<BiConsumer<Block, Item>> postInitCallbacks = new ArrayList<>();

	private final EnumSet<AEFeature> features = EnumSet.noneOf( AEFeature.class );

	private CreativeTabs creativeTab = CreativeTab.instance;

	@SideOnly( Side.CLIENT )
	private BlockRendering renderingCustomizer;

	BlockDefinitionBuilder( FeatureFactory factory, String id, Supplier<? extends Block> blockSupplier )
	{
		this.factory = factory;
		this.registryName = id;
		this.blockSupplier = blockSupplier;
	}

	@Override
	public BlockDefinitionBuilder preInit( BiConsumer<Block, Item> callback )
	{
		preInitCallbacks.add( callback );
		return this;
	}

	@Override
	public BlockDefinitionBuilder init( BiConsumer<Block, Item> callback )
	{
		initCallbacks.add( callback );
		return this;
	}

	@Override
	public BlockDefinitionBuilder postInit( BiConsumer<Block, Item> callback )
	{
		postInitCallbacks.add( callback );
		return this;
	}

	@Override
	public IBlockBuilder features( AEFeature... features )
	{
		this.features.clear();
		addFeatures( features );
		return this;
	}

	@Override
	public IBlockBuilder addFeatures( AEFeature... features )
	{
		Collections.addAll( this.features, features );
		return this;
	}

	public BlockDefinitionBuilder rendering( BlockRenderingCustomizer callback )
	{
		if( Platform.isClient() )
		{
			customizeForClient( callback );
		}

		return this;
	}

	@SideOnly( Side.CLIENT )
	private void customizeForClient( BlockRenderingCustomizer callback )
	{
		if( renderingCustomizer == null )
		{
			renderingCustomizer = new BlockRendering();
		}
		callback.customize( renderingCustomizer );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public <T extends IBlockDefinition> T build()
	{
		if( !AEConfig.instance.areFeaturesEnabled( features ) )
		{
			return (T) new TileDefinition( registryName, null, null );
		}

		// Create block and matching item, and set factory name of both
		Block block = blockSupplier.get();
		block.setRegistryName( AppEng.MOD_ID, registryName );

		ItemBlock item = constructItemFromBlock( block );
		item.setRegistryName( AppEng.MOD_ID, registryName );

		// Register the item and block with the game
		factory.addPreInit( side ->
		{
			GameRegistry.register( block );
			GameRegistry.register( item );
		} );

		block.setCreativeTab( creativeTab );
		block.setUnlocalizedName( "appliedenergistics2." + registryName );

		// Register all extra handlers
		preInitCallbacks.forEach( consumer -> factory.addPreInit( side -> consumer.accept( block, item ) ) );
		initCallbacks.forEach( consumer -> factory.addInit( side -> consumer.accept( block, item ) ) );
		postInitCallbacks.forEach( consumer -> factory.addPostInit( side -> consumer.accept( block, item ) ) );

		if( Platform.isClient() )
		{
			if( renderingCustomizer == null )
			{
				renderingCustomizer = new BlockRendering();
			}

			if( block instanceof AEBaseTileBlock )
			{
				AEBaseTileBlock tileBlock = (AEBaseTileBlock) block;
				renderingCustomizer.apply( factory, block, item, tileBlock.getTileEntityClass() );
			}
			else
			{
				renderingCustomizer.apply( factory, block, item, null );
			}
		}

		if( block instanceof AEBaseTileBlock )
		{
			AEBaseTileBlock tileBlock = (AEBaseTileBlock) block;

			factory.addPreInit( side ->
			{
				Class<? extends AEBaseTile> tileEntityClass = tileBlock.getTileEntityClass();
				AEBaseTile.registerTileItem( tileEntityClass, new BlockStackSrc( block, 0, ActivityState.Enabled ) );

				GameRegistry.registerTileEntity( tileEntityClass, registryName );
			} );

			return (T) new TileDefinition( registryName, (AEBaseTileBlock) block, item );
		}
		else
		{
			return (T) new BlockDefinition( registryName, block, item );
		}
	}

	/**
	 * Create an {@link ItemBlock} from a {@link Block} to register it later as {@link Item}
	 *
	 * @param block source block
	 *
	 * @return item from block
	 */
	private static ItemBlock constructItemFromBlock( Block block )
	{
		if( block == null )
		{
			return null;
		}
		return constructItemBlock( block, getItemBlockConstructor( block ) );
	}

	/**
	 * Returns the constructor to use.
	 *
	 * Either {@link ItemBlock} or in case of an {@link AEBaseBlock} the class returned by
	 * AEBaseBlock.getItemBlockClass().
	 *
	 * @param block the block used to determine the used constructor.
	 *
	 * @return a {@link Class} extending ItemBlock
	 */
	private static Class<? extends ItemBlock> getItemBlockConstructor( final Block block )
	{
		if( block instanceof AEBaseBlock )
		{
			final AEBaseBlock aeBaseBlock = (AEBaseBlock) block;
			return aeBaseBlock.getItemBlockClass();
		}

		return ItemBlock.class;
	}

	/**
	 * Actually construct an instance of {@link Item} with the block and earlier determined constructor.
	 *
	 * Shamelessly stolen from the forge magic.
	 *
	 * TODO: throw an exception instead of returning null? As this could cause issue later on.
	 *
	 * @param block the block to create the {@link ItemBlock} from
	 * @param itemclass the class used to construct it.
	 *
	 * @return an {@link Item} for the block. Actually always a sub type of {@link ItemBlock}
	 */
	private static ItemBlock constructItemBlock( final Block block, final Class<? extends ItemBlock> itemclass )
	{
		try
		{
			final Object[] itemCtorArgs = {};
			final Class<?>[] ctorArgClasses = new Class<?>[itemCtorArgs.length + 1];
			ctorArgClasses[0] = Block.class;
			for( int idx = 1; idx < ctorArgClasses.length; idx++ )
			{
				ctorArgClasses[idx] = itemCtorArgs[idx - 1].getClass();
			}

			final Constructor<? extends ItemBlock> itemCtor = itemclass.getConstructor( ctorArgClasses );
			return itemCtor.newInstance( ObjectArrays.concat( block, itemCtorArgs ) );
		}
		catch( final Throwable t )
		{
			return null;
		}
	}
}
