package appeng.util.item;


import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import appeng.core.AELog;


/**
 * Small helper class that allows one to get the pickup delay of an entity, if it is an item.
 */
public final class PickupDelayHelper
{

	private static final MethodHandle pickupDelayGetter = generatePickupDelayGetter();

	private PickupDelayHelper()
	{
	}

	public static int getPickupDelay( EntityItem entity )
	{
		try
		{
			return (Integer) pickupDelayGetter.invoke( entity );
		}
		catch( Throwable throwable )
		{
			AELog.warn( throwable, "Unable to get item delayBeforeCanPickup for entity " + entity );
			return 40; // Default for player-thrown objects
		}
	}

	/**
	 * Copies the pickup delay from the given entity to the given target entity. If the source entity is not
	 * an item, a default pickup delay is used for the target.
	 */
	public static void copyPickupDelay( Entity from, EntityItem to )
	{

		if( from instanceof EntityItem )
		{
			to.setPickupDelay( getPickupDelay( (EntityItem) from ) );
		}
		else
		{
			to.setDefaultPickupDelay();
		}
	}

	private static MethodHandle generatePickupDelayGetter()
	{
		Field field = ReflectionHelper.findField( EntityItem.class, "delayBeforeCanPickup", "field_145804_b" );

		try
		{
			return MethodHandles.lookup().unreflectGetter( field );
		}
		catch( IllegalAccessException e )
		{
			throw new RuntimeException( "Unable to create a getter for delayBeforeCanPickup!", e );
		}
	}
}
