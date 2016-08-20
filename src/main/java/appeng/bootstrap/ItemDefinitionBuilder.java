package appeng.bootstrap;


import java.util.function.Supplier;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

import appeng.bootstrap.components.ItemMeshDefinitionComponent;
import appeng.bootstrap.components.ItemVariantsComponent;
import appeng.core.CreativeTab;
import appeng.core.CreativeTabFacade;
import appeng.core.features.ItemDefinition;
import appeng.items.parts.ItemFacade;
import appeng.util.Platform;


public class ItemDefinitionBuilder extends DefinitionBuilder<ItemDefinitionBuilder>
{

	private final Supplier<Item> itemSupplier;

	ItemDefinitionBuilder( FeatureFactory registry, String registryName, Supplier<Item> itemSupplier )
	{
		super( registry, registryName );
		this.itemSupplier = itemSupplier;
	}

	public ItemDefinition build()
	{
		if( !isActive() )
		{
			return new ItemDefinition( registryName, null );
		}

		Item item = itemSupplier.get();
		item.setRegistryName( resourceLocation );

		ItemDefinition definition = new ItemDefinition( registryName, item );

		item.setUnlocalizedName( "appliedenergistics2." + registryName );

		if( item instanceof ItemFacade )
		{
			item.setCreativeTab( CreativeTabFacade.instance );
		}
		else
		{
			item.setCreativeTab( CreativeTab.instance );
		}

		registry.addPreInit( side -> GameRegistry.register( item ) );

		if( Platform.isClient() )
		{
			registry.addBootstrapComponent( new ItemVariantsComponent( item ) );
			registry.addBootstrapComponent( new ItemMeshDefinitionComponent( item ) );
		}

		return definition;
	}
}
