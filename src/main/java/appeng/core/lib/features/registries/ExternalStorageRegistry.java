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


import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import appeng.core.lib.features.registries.entries.ExternalIInv;
import appeng.core.me.api.networking.security.BaseActionSource;
import appeng.core.me.api.storage.IExternalStorageHandler;
import appeng.core.me.api.storage.StorageChannel;


public class ExternalStorageRegistry
{

	private final List<IExternalStorageHandler> Handlers;
	private final ExternalIInv lastHandler = new ExternalIInv();

	public ExternalStorageRegistry()
	{
		this.Handlers = new ArrayList<IExternalStorageHandler>();
	}

	public void addExternalStorageInterface( final IExternalStorageHandler ei )
	{
		this.Handlers.add( ei );
	}

	public IExternalStorageHandler getHandler( final TileEntity te, final EnumFacing d, final StorageChannel chan, final BaseActionSource mySrc )
	{
		for( final IExternalStorageHandler x : this.Handlers )
		{
			if( x.canHandle( te, d, chan, mySrc ) )
			{
				return x;
			}
		}

		if( this.lastHandler.canHandle( te, d, chan, mySrc ) )
		{
			return this.lastHandler;
		}

		return null;
	}
}
