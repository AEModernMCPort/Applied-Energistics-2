package appeng.bootstrap;


import net.minecraft.client.renderer.color.IItemColor;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/**
 * Allows the rendering of an item to be customized.
 */
public interface IItemRendering
{

	@SideOnly( Side.CLIENT )
	IItemRendering itemColor( IItemColor itemColor );

}
