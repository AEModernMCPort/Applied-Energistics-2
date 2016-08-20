package appeng.bootstrap;


import java.util.function.BiFunction;

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
	private IBlockColor blockColor;

	@SideOnly( Side.CLIENT )
	private IItemColor itemColor;

	@SideOnly( Side.CLIENT )
	private TileEntitySpecialRenderer<?> tesr;

	@SideOnly( Side.CLIENT )
	public IRenderingCustomizer modelCustomizer( BiFunction<ModelResourceLocation, IBakedModel, IBakedModel> customizer )
	{
		modelCustomizer = customizer;
		return this;
	}

	@SideOnly( Side.CLIENT )
	@Override
	public IRenderingCustomizer blockColor( IBlockColor blockColor )
	{
		this.blockColor = blockColor;
		return this;
	}

	@SideOnly( Side.CLIENT )
	@Override
	public IRenderingCustomizer itemColor( IItemColor itemColor )
	{
		this.itemColor = itemColor;
		return this;
	}

	@SideOnly( Side.CLIENT )
	@Override
	public IRenderingCustomizer tesr( TileEntitySpecialRenderer<?> tesr )
	{
		this.tesr = tesr;
		return this;
	}

	void apply( FeatureFactory registry, Block block, Item item, Class<?> tileEntityClass )
	{
		registry.addBootstrapComponent( new ItemVariantsComponent( item ) );
		registry.addBootstrapComponent( new ItemModelRegistrationComponent( block, item ) );

		if( tesr != null )
		{
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

		if( blockColor != null )
		{
			registry.addBootstrapComponent( new BlockColorRegistration( block, blockColor ) );
		}

		if( itemColor != null )
		{
			registry.addBootstrapComponent( new ItemColorRegistration( item, itemColor ) );
		}
	}
}
