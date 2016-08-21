package appeng.bootstrap;


import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.bootstrap.components.ItemColorRegistration;
import appeng.bootstrap.components.ItemMeshDefinitionComponent;
import appeng.bootstrap.components.ItemVariantsComponent;


class ItemRendering implements IItemRendering
{

	@SideOnly( Side.CLIENT )
	private IItemColor itemColor;

	@SideOnly( Side.CLIENT )
	@Override
	public IItemRendering itemColor( IItemColor itemColor )
	{
		this.itemColor = itemColor;
		return this;
	}

	void apply( FeatureFactory factory, Item item )
	{
		factory.addBootstrapComponent( new ItemMeshDefinitionComponent( item ) );
		factory.addBootstrapComponent( new ItemVariantsComponent( item ) );

		if( itemColor != null )
		{
			factory.addBootstrapComponent( new ItemColorRegistration( item, itemColor ) );
		}
	}

}
