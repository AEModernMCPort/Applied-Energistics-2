package appeng.bootstrap;


import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.bootstrap.components.BlockColorRegistration;
import appeng.bootstrap.components.ItemColorRegistration;
import appeng.bootstrap.components.ItemModelRegistrationComponent;
import appeng.bootstrap.components.ItemVariantsComponent;
import appeng.bootstrap.components.TesrComponent;
import appeng.client.render.model.CachingRotatingBakedModel;


public class RenderingCustomizer implements IRenderingCustomizer
{

	@SideOnly( Side.CLIENT )
	BiFunction<ModelResourceLocation, IBakedModel, IBakedModel> modelCustomizer;

	@SideOnly( Side.CLIENT )
	private Supplier<IBlockColor> blockColorSupplier;

	@SideOnly( Side.CLIENT )
	private Supplier<IItemColor> itemColorSupplier;

	@SideOnly( Side.CLIENT )
	private Supplier<TileEntitySpecialRenderer<?>> tesrSupplier;

	@SideOnly( Side.CLIENT )
	public IRenderingCustomizer modelCustomizer( BiFunction<ModelResourceLocation, IBakedModel, IBakedModel> customizer )
	{
		modelCustomizer = customizer;
		return this;
	}

	@SideOnly( Side.CLIENT )
	public IRenderingCustomizer blockColor( Supplier<IBlockColor> blockColor )
	{
		blockColorSupplier = blockColor;
		return this;
	}

	@SideOnly( Side.CLIENT )
	public IRenderingCustomizer itemColor( Supplier<IItemColor> itemColor )
	{
		itemColorSupplier = itemColor;
		return this;
	}

	@SideOnly( Side.CLIENT )
	public IRenderingCustomizer tesr( Supplier<TileEntitySpecialRenderer<?>> tesr )
	{
		tesrSupplier = tesr;
		return this;
	}

	@SideOnly( Side.CLIENT )
	public IRenderingCustomizer tesr( TileEntitySpecialRenderer<?> tesr )
	{
		tesrSupplier = () -> tesr;
		return this;
	}

	void apply( FeatureFactory registry, Block block, Item item, Class<?> tileEntityClass )
	{
		registry.addBootstrapComponent( new ItemVariantsComponent( item ) );
		registry.addBootstrapComponent( new ItemModelRegistrationComponent( block, item ) );

		if( tesrSupplier != null )
		{
			TileEntitySpecialRenderer<?> tesr = tesrSupplier.get();
			registry.addBootstrapComponent( new TesrComponent( tileEntityClass, tesr ) );
		}

		if( modelCustomizer != null )
		{
			registry.modelOverrideComponent.addOverride( block.getRegistryName().getResourcePath(), modelCustomizer );
		}
		else
		{
			registry.modelOverrideComponent.addOverride( block.getRegistryName().getResourcePath(), ( l, m ) -> new CachingRotatingBakedModel( m ) );
		}

		if( blockColorSupplier != null )
		{
			registry.addBootstrapComponent( new BlockColorRegistration( block, blockColorSupplier.get() ) );
		}

		if( itemColorSupplier != null )
		{
			registry.addBootstrapComponent( new ItemColorRegistration( item, itemColorSupplier.get() ) );
		}
	}
}
