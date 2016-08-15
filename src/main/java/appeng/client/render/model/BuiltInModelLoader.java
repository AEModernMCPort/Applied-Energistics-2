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

package appeng.client.render.model;


import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

import appeng.core.AppEng;


/**
 * This model loader manages built-in singleton models.
 */
public class BuiltInModelLoader implements ICustomModelLoader
{

	private final ImmutableMap<String, IModel> models;

	public BuiltInModelLoader( Map<String, IModel> models )
	{
		this.models = ImmutableMap.copyOf( models );
	}

	@Override
	public boolean accepts( ResourceLocation modelLocation )
	{
		return AppEng.MOD_ID.equals( modelLocation.getResourceDomain() ) && models.containsKey( modelLocation.getResourcePath() );
	}

	@Override
	public IModel loadModel( ResourceLocation modelLocation ) throws Exception
	{
		return models.get( modelLocation.getResourcePath() );
	}

	@Override
	public void onResourceManagerReload( IResourceManager resourceManager )
	{

	}
}
