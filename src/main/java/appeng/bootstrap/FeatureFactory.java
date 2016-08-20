package appeng.bootstrap;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import appeng.api.definitions.IItemDefinition;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.block.AEBaseTileBlock;
import appeng.bootstrap.components.InitComponent;
import appeng.bootstrap.components.PostInitComponent;
import appeng.bootstrap.components.PreInitComponent;
import appeng.bootstrap.components.ModelOverrideComponent;
import appeng.core.features.AEFeature;
import appeng.core.features.ActivityState;
import appeng.core.features.ColoredItemDefinition;
import appeng.core.features.ItemStackSrc;


public class FeatureFactory
{

	private final AEFeature[] defaultFeatures;

	private final List<IBootstrapComponent> bootstrapComponents;

	final ModelOverrideComponent modelOverrideComponent;

	public FeatureFactory()
	{
		this.defaultFeatures = new AEFeature[] { AEFeature.Core };
		this.bootstrapComponents = new ArrayList<>();

		modelOverrideComponent = new ModelOverrideComponent();
		this.bootstrapComponents.add( modelOverrideComponent );
	}

	private FeatureFactory( FeatureFactory parent, AEFeature... defaultFeatures )
	{
		this.defaultFeatures = defaultFeatures.clone();
		this.bootstrapComponents = parent.bootstrapComponents;
		this.modelOverrideComponent = parent.modelOverrideComponent;
	}

	public BlockDefinitionBuilder block( String id, Supplier<Block> block )
	{
		return new BlockDefinitionBuilder( this, id, block ).features( defaultFeatures );
	}

	public TileDefinitionBuilder tile( String id, Supplier<AEBaseTileBlock> block )
	{
		return new TileDefinitionBuilder( this, id, block ).features( defaultFeatures );
	}

	public ItemDefinitionBuilder item( String id, Supplier<Item> item )
	{
		return new ItemDefinitionBuilder( this, id, item ).features( defaultFeatures );
	}

	public AEColoredItemDefinition colored( IItemDefinition target, int offset )
	{
		final ColoredItemDefinition definition = new ColoredItemDefinition();

		for( final Item targetItem : target.maybeItem().asSet() )
		{
			for( final AEColor color : AEColor.VALID_COLORS )
			{
				final ActivityState state = ActivityState.from( target.isEnabled() );

				definition.add( color, new ItemStackSrc( targetItem, offset + color.ordinal(), state ) );
			}
		}

		return definition;
	}

	public FeatureFactory features( AEFeature... features )
	{
		return new FeatureFactory( this, features );
	}

	void addBootstrapComponent( IBootstrapComponent component )
	{
		this.bootstrapComponents.add( component );
	}

	void addPreInit( PreInitComponent component )
	{
		this.bootstrapComponents.add( component );
	}

	void addInit( InitComponent component )
	{
		this.bootstrapComponents.add( component );
	}

	void addPostInit( PostInitComponent component )
	{
		this.bootstrapComponents.add( component );
	}

	public List<IBootstrapComponent> getBootstrapComponents()
	{
		return bootstrapComponents;
	}
}
