package appeng.bootstrap;


import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

import appeng.bootstrap.components.ItemModelRegistrationComponent;
import appeng.bootstrap.components.ItemVariantsComponent;
import appeng.core.CreativeTab;
import appeng.core.features.BlockDefinition;
import appeng.util.Platform;


public class BlockDefinitionBuilder extends DefinitionBuilder<BlockDefinitionBuilder>
{

	protected final Supplier<? extends Block> blockSupplier;

	private final List<BiConsumer<Block, Item>> extraPreInitBootstrappers = new ArrayList<>();

	private final List<BiConsumer<Block, Item>> extraInitBootstrappers = new ArrayList<>();

	private final List<BiConsumer<Block, Item>> extraPostInitBootstrappers = new ArrayList<>();

	BlockDefinitionBuilder( FeatureFactory registry, String id, Supplier<? extends Block> blockSupplier )
	{
		super( registry, id );
		this.blockSupplier = blockSupplier;
	}

	public BlockDefinitionBuilder preInit( BiConsumer<Block, Item> callback )
	{
		extraPreInitBootstrappers.add( callback );
		return this;
	}

	public BlockDefinitionBuilder init( BiConsumer<Block, Item> callback )
	{
		extraInitBootstrappers.add( callback );
		return this;
	}

	public BlockDefinitionBuilder postInit( BiConsumer<Block, Item> callback )
	{
		extraPostInitBootstrappers.add( callback );
		return this;
	}

	public BlockDefinition build()
	{
		if( !isActive() )
		{
			return new BlockDefinition( registryName, null );
		}

		Block block = blockSupplier.get();

		BlockDefinition definition = new BlockDefinition( registryName, block );
		ItemBlock item = definition.maybeItemBlock().get();

		block.setCreativeTab( CreativeTab.instance );
		block.setUnlocalizedName( "appliedenergistics2." + registryName );
		block.setRegistryName( registryName );
		item.setRegistryName( registryName );

		registry.addPreInit( side ->
		{
			GameRegistry.register( block );
			GameRegistry.register( item );
		} );

		// Register all extra handlers
		extraPreInitBootstrappers.forEach( consumer -> registry.addPreInit( side -> consumer.accept( block, item ) ) );
		extraInitBootstrappers.forEach( consumer -> registry.addInit( side -> consumer.accept( block, item ) ) );
		extraPostInitBootstrappers.forEach( consumer -> registry.addPostInit( side -> consumer.accept( block, item ) ) );

		if( Platform.isClient() )
		{
			registry.addBootstrapComponent( new ItemVariantsComponent(
					item
			) );

			registry.addBootstrapComponent( new ItemModelRegistrationComponent( block, item ) );
		}

		return definition;
	}
}
