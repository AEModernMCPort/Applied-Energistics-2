
package appeng.client.render.model;


import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;

import org.apache.commons.io.IOUtils;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;


public class AEIgnoringStateMapper extends StateMapperBase implements IResourceManagerReloadListener
{

	private final ResourceLocation ignoredRL;
	private final Set<String> ignored = new HashSet<>();

	public AEIgnoringStateMapper()
	{
		this.ignoredRL = null;
	}

	public AEIgnoringStateMapper( ResourceLocation ignoredRL )
	{
		this.ignoredRL = new ResourceLocation( ignoredRL.getResourceDomain(), "blockstates/" + ignoredRL.getResourcePath() + ".ignore.json" );
	}

	@Override
	public void onResourceManagerReload( IResourceManager resourceManager )
	{
		ignored.clear();
		ignored.add( "forward" );
		ignored.add( "up" );
		if( ignoredRL != null )
		{
			try( IResource resource = resourceManager.getResource( ignoredRL ) )
			{
				ignored.addAll( IOUtils.readLines( resource.getInputStream() ) );
			}
			catch( IOException e )
			{
				// There's no ignore file, so everything is ok.
			}
		}
	}

	@Override
	protected ModelResourceLocation getModelResourceLocation( IBlockState state )
	{
		Map<IProperty<?>, Comparable<?>> map = Maps.<IProperty<?>, Comparable<?>>newLinkedHashMap( state.getProperties() );
		Iterator<Entry<IProperty<?>, Comparable<?>>> it = map.entrySet().iterator();
		while( it.hasNext() )
		{
			if( ignored.contains( it.next().getKey().getName() ) )
			{
				it.remove();
			}
		}
		return new ModelResourceLocation( Block.REGISTRY.getNameForObject( state.getBlock() ), this.getPropertyString( map ) );
	}
}
