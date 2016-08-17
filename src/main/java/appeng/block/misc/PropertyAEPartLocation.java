package appeng.block.misc;


import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.properties.PropertyHelper;

import appeng.api.util.AEPartLocation;


/**
 * Can't use PropertyEnum&lt;AEPartLocation&gt;, because it doesn't implement IStringSerializer.
 */
public class PropertyAEPartLocation extends PropertyHelper<AEPartLocation>
{

	public PropertyAEPartLocation( String name )
	{
		super( name, AEPartLocation.class );
	}

	private static final EnumSet<AEPartLocation> ALLOWED_VALUES = EnumSet.allOf( AEPartLocation.class );

	// A map from lowercase name to enum literal
	private static final ImmutableMap<String, AEPartLocation> VALUE_MAP = ImmutableMap.copyOf(
			Arrays.stream( AEPartLocation.values() )
					.collect( Collectors.toMap( e -> e.name().toLowerCase(), e -> e ) )
	);

	private static final ImmutableMap<AEPartLocation, String> NAME_MAP = ImmutableMap.copyOf(
			Arrays.stream( AEPartLocation.values() )
					.collect( Collectors.toMap( e -> e, e -> e.name().toLowerCase() ) )
	);

	@Override
	public Collection<AEPartLocation> getAllowedValues()
	{
		return ALLOWED_VALUES;
	}

	@Override
	public Optional<AEPartLocation> parseValue( String value )
	{
		return Optional.fromNullable( VALUE_MAP.get( value ) );
	}

	@Override
	public String getName( AEPartLocation value )
	{
		return NAME_MAP.get( value );
	}
}
