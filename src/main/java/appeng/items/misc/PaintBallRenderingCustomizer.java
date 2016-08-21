package appeng.items.misc;


import net.minecraft.item.ItemStack;

import appeng.api.util.AEColor;
import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;


public class PaintBallRenderingCustomizer extends ItemRenderingCustomizer
{
	@Override
	public void customize( IItemRendering rendering )
	{
		rendering.color( PaintBallRenderingCustomizer::getColorFromItemstack );
	}

	private static int getColorFromItemstack( ItemStack stack, int tintIndex )
	{
		final AEColor col = ( (ItemPaintBall) stack.getItem() ).getColor( stack );

		final int colorValue = stack.getItemDamage() >= 20 ? col.mediumVariant : col.mediumVariant;
		final int r = ( colorValue >> 16 ) & 0xff;
		final int g = ( colorValue >> 8 ) & 0xff;
		final int b = ( colorValue ) & 0xff;

		if( stack.getItemDamage() >= 20 )
		{
			final float fail = 0.7f;
			final int full = (int) ( 255 * 0.3 );
			return (int) ( full + r * fail ) << 16 | (int) ( full + g * fail ) << 8 | (int) ( full + b * fail ) | 0xff << 24;
		}
		else
		{
			return r << 16 | g << 8 | b | 0xff << 24;
		}
	}
}
