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

package appeng.integration.modules;


import net.mcft.copy.betterstorage.api.crate.ICrateStorage;
import appeng.api.AEApi;
import appeng.api.util.ForgeDirection;
import appeng.integration.IIntegrationModule;
import appeng.integration.abstraction.IBetterStorage;
import appeng.integration.modules.helpers.BSCrateHandler;
import appeng.integration.modules.helpers.BSCrateStorageAdaptor;
import appeng.util.InventoryAdaptor;


public class BetterStorage implements IIntegrationModule, IBetterStorage
{

	public static BetterStorage instance;

	@Override
	public boolean isStorageCrate( Object te )
	{
		return te instanceof ICrateStorage;
	}

	@Override
	public InventoryAdaptor getAdaptor( Object te, ForgeDirection d )
	{
		if( te instanceof ICrateStorage )
		{
			return new BSCrateStorageAdaptor( te, d );
		}
		return null;
	}

	@Override
	public void init()
	{

	}

	@Override
	public void postInit()
	{
		AEApi.instance().registries().externalStorage().addExternalStorageInterface( new BSCrateHandler() );
	}
}
