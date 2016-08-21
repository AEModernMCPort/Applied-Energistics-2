package appeng.block.storage;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IItemRendering;
import appeng.client.render.tesr.SkyChestTESR;


public class SkyChestRenderingCustomizer extends BlockRenderingCustomizer
{

	@SideOnly( Side.CLIENT )
	@Override
	public void customize( IBlockRendering rendering, IItemRendering itemRendering )
	{
		rendering.tesr( new SkyChestTESR() );
	}

}
