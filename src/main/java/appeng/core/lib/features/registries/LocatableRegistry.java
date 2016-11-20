/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.core.lib.features.registries;


import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import appeng.core.api.events.LocatableEventAnnounce;
import appeng.core.api.events.LocatableEventAnnounce.LocatableEvent;
import appeng.core.api.features.ILocatable;
import appeng.core.lib.util.Platform;


public final class LocatableRegistry
{
	private final Map<Long, ILocatable> set;

	public LocatableRegistry()
	{
		this.set = new HashMap<Long, ILocatable>();
		MinecraftForge.EVENT_BUS.register( this );
	}

	@SubscribeEvent
	public void updateLocatable( final LocatableEventAnnounce e )
	{
		if( Platform.isClient() )
		{
			return; // IGNORE!
		}

		if( e.change == LocatableEvent.Register )
		{
			this.set.put( e.target.getLocatableSerial(), e.target );
		}
		else if( e.change == LocatableEvent.Unregister )
		{
			this.set.remove( e.target.getLocatableSerial() );
		}
	}

	/**
	 * Find a locate-able object by its serial.
	 */
	@Deprecated
	public Object findLocatableBySerial( final long ser )
	{
		return this.set.get( ser );
	}

	public ILocatable getLocatableBy( final long serial )
	{
		return this.set.get( serial );
	}
}
