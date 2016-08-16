package appeng.util.item;


import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.fml.relauncher.ReflectionHelper;


/**
 * Small helper class that allows one to get the pickup delay of an entity, if it is an item.
 */
public final class PickupDelayHelper
{

	private static final MethodHandle pickupDelayGetter = generateGetter();

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
			throw new RuntimeException( "Unable to retrieve delayBeforeCanPickup", throwable );
		}
	}

	private static MethodHandle generateGetter()
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
