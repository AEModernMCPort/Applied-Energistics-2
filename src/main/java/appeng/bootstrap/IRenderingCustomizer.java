package appeng.bootstrap;


import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public interface IRenderingCustomizer
{

	@SideOnly( Side.CLIENT )
	IRenderingCustomizer modelCustomizer( BiFunction<ModelResourceLocation, IBakedModel, IBakedModel> customizer );

	@SideOnly( Side.CLIENT )
	IRenderingCustomizer blockColor( Supplier<IBlockColor> blockColor );

	@SideOnly( Side.CLIENT )
	IRenderingCustomizer itemColor( Supplier<IItemColor> itemColor );

	@SideOnly( Side.CLIENT )
	IRenderingCustomizer tesr( Supplier<TileEntitySpecialRenderer<?>> tesr );

	@SideOnly( Side.CLIENT )
	IRenderingCustomizer tesr( TileEntitySpecialRenderer<?> tesr );

}
