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

package appeng.core.lib;


import appeng.core.api.exceptions.FailedConnection;
import appeng.core.api.util.AEPartLocation;
import appeng.core.lib.api.ApiPart;
import appeng.core.lib.api.ApiStorage;
import appeng.core.lib.features.registries.MovableTileRegistry;
import appeng.core.lib.features.registries.RegistryContainer;
import appeng.core.lib.util.Platform;
import appeng.core.me.api.networking.IGridBlock;
import appeng.core.me.api.networking.IGridConnection;
import appeng.core.me.api.networking.IGridNode;
import appeng.core.me.api.storage.IStorageHelper;
import appeng.core.me.grid.GridConnection;
import appeng.core.me.grid.GridNode;


@Deprecated
public final class AppEngApi
{
	public static final AppEngApi INSTANCE = new AppEngApi();

	private final ApiPart partHelper;

	private MovableTileRegistry MovableRegistry = new MovableTileRegistry();
	private final RegistryContainer registryContainer;
	private final IStorageHelper storageHelper;
	private final ApiDefinitions definitions;

	private AppEngApi()
	{
		this.storageHelper = new ApiStorage();
		this.registryContainer = new RegistryContainer();
		this.partHelper = new ApiPart();
		this.definitions = new ApiDefinitions( this.partHelper );
	}

	public RegistryContainer registries()
	{
		return this.registryContainer;
	}

	public IStorageHelper storage()
	{
		return this.storageHelper;
	}

	public ApiPart partHelper()
	{
		return this.partHelper;
	}

	public ApiDefinitions definitions()
	{
		return this.definitions;
	}

	public IGridNode createGridNode( final IGridBlock blk )
	{
		if( Platform.isClient() )
		{
			throw new IllegalStateException( "Grid features for " + blk + " are server side only." );
		}

		return new GridNode( blk );
	}

	public IGridConnection createGridConnection( final IGridNode a, final IGridNode b ) throws FailedConnection
	{
		return new GridConnection( a, b, AEPartLocation.INTERNAL );
	}

	public static AppEngApi internalApi()
	{
		return INSTANCE;
	}
}
