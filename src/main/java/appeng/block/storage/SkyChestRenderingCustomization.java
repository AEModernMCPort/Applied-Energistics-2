package appeng.block.storage;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.bootstrap.IRenderingCustomizer;
import appeng.bootstrap.RenderingCustomization;
import appeng.client.render.tesr.SkyChestTESR;


public class SkyChestRenderingCustomization extends RenderingCustomization
{

	@SideOnly( Side.CLIENT )
	@Override
	public void customize( IRenderingCustomizer rendering )
	{
		rendering.tesr( new SkyChestTESR() );
	}
}
