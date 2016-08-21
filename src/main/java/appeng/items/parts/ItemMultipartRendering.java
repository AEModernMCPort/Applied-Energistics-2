package appeng.items.parts;


import net.minecraft.client.renderer.block.model.ModelResourceLocation;

import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;


public class ItemMultipartRendering extends ItemRenderingCustomizer
{

	private final ItemMultiPart item;

	public ItemMultipartRendering( ItemMultiPart item )
	{
		this.item = item;
	}

	@Override
	public void customize( IItemRendering rendering )
	{
		rendering.meshDefinition( is -> new ModelResourceLocation( item.getTypeByStack( is ).getModel(), null ) );
	}
}
