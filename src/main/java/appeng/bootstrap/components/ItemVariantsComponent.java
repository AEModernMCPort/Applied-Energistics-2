package appeng.bootstrap.components;


import com.google.common.base.Preconditions;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

import appeng.bootstrap.IBootstrapComponent;
import appeng.items.AEBaseItem;


public class ItemVariantsComponent implements IBootstrapComponent
{

	private final Item item;

	private final ResourceLocation[] resourceLocations;

	public ItemVariantsComponent( Item item )
	{
		this.item = item;

		if( item instanceof AEBaseItem )
		{
			AEBaseItem baseItem = (AEBaseItem) item;

			// Handle registration of item variants
			this.resourceLocations = baseItem.getItemVariants().toArray( new ResourceLocation[0] );
		}
		else
		{
			this.resourceLocations = new ResourceLocation[] { item.getRegistryName() };
		}
	}

	@Override
	public void preInitialize( Side side )
	{
		Preconditions.checkState( side == Side.CLIENT );
		ModelBakery.registerItemVariants( item, resourceLocations );
	}
}
