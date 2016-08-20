package appeng.bootstrap;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/**
 * @author Sebastian
 * @version rv3 - 20.08.2016
 * @since rv3 20.08.2016
 */
public abstract class RenderingCustomization
{

	@SideOnly( Side.CLIENT )
	public abstract void customize(IRenderingCustomizer rendering);

}
