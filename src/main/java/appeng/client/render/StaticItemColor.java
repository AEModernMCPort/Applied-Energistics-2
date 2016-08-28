package appeng.client.render;


import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

import appeng.api.util.AEColor;


/**
 * Returns the shades of a single AE color for tint indices 0, 1, and 2.
 */
public class StaticItemColor implements IItemColor
{

	private final AEColor color;

	public StaticItemColor( AEColor color )
	{
		this.color = color;
	}

	@Override
	public int getColorFromItemstack( ItemStack stack, int tintIndex )
	{
		switch( tintIndex )
		{
			default:
			case 0:
				return color.whiteVariant;
			case 1:
				return color.mediumVariant;
			case 2:
				return color.blackVariant;
		}
	}

}
