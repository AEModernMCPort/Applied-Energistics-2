package appeng.bootstrap;


import java.util.Collections;
import java.util.EnumSet;

import net.minecraft.util.ResourceLocation;

import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;


public abstract class DefinitionBuilder<T extends DefinitionBuilder>
{

	protected final FeatureFactory registry;

	protected final String registryName;

	protected final ResourceLocation resourceLocation;

	protected final EnumSet<AEFeature> features = EnumSet.noneOf( AEFeature.class );

	protected DefinitionBuilder( FeatureFactory registry, String registryName )
	{
		this.registry = registry;
		this.registryName = registryName;
		this.resourceLocation = new ResourceLocation( AppEng.MOD_ID, registryName );
	}

	public T features( AEFeature... features )
	{
		this.features.clear();
		return addFeatures( features );
	}

	public T addFeatures( AEFeature... features )
	{
		Collections.addAll( this.features, features );
		return getSelf();
	}

	@SuppressWarnings( "unchecked" )
	protected T getSelf()
	{
		return (T) this;
	}

	protected boolean isActive()
	{
		return features.stream()
				.map( AEConfig.instance::isFeatureEnabled )
				.allMatch( Boolean::booleanValue );
	}
}
