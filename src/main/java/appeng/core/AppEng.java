/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.core;


import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import appeng.core.lib.module.ModuleLoaderHelper;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.relauncher.Side;

import appeng.core.lib.AEConfig;
import appeng.core.lib.AELog;
import appeng.core.lib.CommonHelper;
import appeng.core.lib.crash.CrashInfo;
import appeng.core.lib.crash.ModCrashEnhancement;
import appeng.core.lib.module.AEModule;
import appeng.core.lib.module.Module;


@Mod( modid = AppEng.MOD_ID, name = AppEng.MOD_NAME, version = AEConfig.VERSION, dependencies = AppEng.MOD_DEPENDENCIES, acceptedMinecraftVersions = ForgeVersion.mcVersion, guiFactory = "appeng.client.gui.config.AEConfigGuiFactory" )
public final class AppEng
{
	public static final String MOD_ID = "appliedenergistics2";
	public static final String MOD_NAME = "Applied Energistics 2";

	public static final String ASSETS = "appliedenergistics2:";

	public static final String MOD_DEPENDENCIES =
			// a few mods, AE should load after, probably.
			// required-after:AppliedEnergistics2API|all;
			// "after:gregtech_addon;after:Mekanism;after:IC2;after:ThermalExpansion;after:BuildCraft|Core;" +

			// depend on version of forge used for build.
			"after:appliedenergistics2-core;" + "required-after:Forge@[" // require forge.
					+ net.minecraftforge.common.ForgeVersion.majorVersion + '.' // majorVersion
					+ net.minecraftforge.common.ForgeVersion.minorVersion + '.' // minorVersion
					+ net.minecraftforge.common.ForgeVersion.revisionVersion + '.' // revisionVersion
					+ net.minecraftforge.common.ForgeVersion.buildVersion + ",)"; // buildVersion

	@Nonnull
	private static final AppEng INSTANCE = new AppEng();

	private AppEng()
	{
		FMLCommonHandler.instance().registerCrashCallable( new ModCrashEnhancement( CrashInfo.MOD_VERSION ) );
	}

	@Nonnull
	@Mod.InstanceFactory
	public static AppEng instance()
	{
		return INSTANCE;
	}

	private ImmutableMap<String, Module> modules;
	private ImmutableMap<Class, Module> classModule;
	private ImmutableList<String> moduleOrder;
	/*
	 * TODO 1.10.2-MODUSEP - Do we even want some modules be @Mod at the same time? Weird.
	 */
	private ImmutableMap<Module, Boolean> internal;

	public <M extends Module> M getModule( String name )
	{
		return (M) modules.get( name );
	}

	public <M extends Module> M getModule( Class<M> clas )
	{
		return (M) classModule.get( clas );
	}

	private File configDirectory;

	public File getConfigDirectory()
	{
		return configDirectory;
	}

	@EventHandler
	private void preInit( final FMLPreInitializationEvent event )
	{
		if( !Loader.isModLoaded( "appliedenergistics2-core" ) )
		{
			// TODO 1.10.2-MODUSEP - I dunno what to do with proxies. Srsly. I think we will have to use a proxy per module. If so, do we also need generic proxy :D ?
			CommonHelper.proxy.missingCoreMod();
		}

		Map<String, Pair<Class<Module>, String>> foundModules = new HashMap<>();
		ASMDataTable annotations = event.getAsmData();
		for( ASMData data : annotations.getAll( AEModule.class.getCanonicalName() ) )
		{
			try
			{
				Class clas = Class.forName( data.getClassName() );
				Class<Module> claz = clas.asSubclass( Module.class );
				Module module = claz.newInstance();
				foundModules.put( (String) data.getAnnotationInfo().get( "value" ), new ImmutablePair<Class<Module>, String>( claz, (String) data.getAnnotationInfo().get( "dependencies" ) ) );
			}
			catch( Exception e )
			{
				// :(
			}
		}


		Map<String, Class<Module>> modules = Maps.newHashMap();
		List<String> order = ModuleLoaderHelper.checkAndOrderModules(foundModules, event.getSide(), modules);
		ImmutableList.Builder<String> orderBuilder = ImmutableList.builder();
		order.addAll(order);
		this.moduleOrder = orderBuilder.build();


		ImmutableMap.Builder<String, Module> modulesBuilder = ImmutableMap.builder();
		ImmutableMap.Builder<Class, Module> classModuleBuilder = ImmutableMap.builder();
		ImmutableMap.Builder<Module, Boolean> internalBuilder = ImmutableMap.builder();
		for(String name : moduleOrder)
			{
			try
			{
				Class<Module> moduleClass = modules.get(name);
				Module module = moduleClass.newInstance();
				modulesBuilder.put( name, module );
				classModuleBuilder.put( moduleClass, module );
				internalBuilder.put( module, !moduleClass.isAnnotationPresent( Mod.class ) );
			}
			catch( Exception exc )
			{
				// :(
			}
		}
		this.modules = modulesBuilder.build();
		this.classModule = classModuleBuilder.build();
		this.internal = internalBuilder.build();

		AELog.info( "Succesfully loaded %s modules", modules.size() );

		final Stopwatch watch = Stopwatch.createStarted();
		AELog.info( "Pre Initialization ( started )" );

		this.configDirectory = new File( event.getModConfigurationDirectory().getPath(), "AppliedEnergistics2" );
		AEConfig.instance = new AEConfig( new File( AppEng.instance().getConfigDirectory(), "AppliedEnergistics2.cfg" ) );

		for( String name : moduleOrder)
		{
			Module module = getModule(name);
			if( this.internal.get( module ) )
			{
				module.preInit( event );
			}
		}

		AELog.info( "Pre Initialization ( ended after " + watch.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );
	}

	@EventHandler
	private void init( final FMLInitializationEvent event )
	{
		final Stopwatch start = Stopwatch.createStarted();
		AELog.info( "Initialization ( started )" );

		for( String name : moduleOrder)
		{
			Module module = getModule(name);
			if( this.internal.get( module ) )
			{
				module.init( event );
			}
		}

		AELog.info( "Initialization ( ended after " + start.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );
	}

	@EventHandler
	private void postInit( final FMLPostInitializationEvent event )
	{
		final Stopwatch start = Stopwatch.createStarted();
		AELog.info( "Post Initialization ( started )" );

		for( String name : moduleOrder)
		{
			Module module = getModule(name);
			if( this.internal.get( module ) )
			{
				module.postInit( event );
			}
		}

		AEConfig.instance.save();

		AELog.info( "Post Initialization ( ended after " + start.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );
	}

	@EventHandler
	private void handleIMCEvent( final FMLInterModComms.IMCEvent event )
	{
		for( Module module : modules.values() )
		{
			// TODO 1.10.2-MODUSEP - Do modules still need to receive IMC sent to AE even if they're separate mods?
			// if( internal.get( module ) )
			{
				module.handleIMCEvent( event );
			}
		}
	}

	@EventHandler
	private void serverAboutToStart( final FMLServerAboutToStartEvent event )
	{
		for( String name : moduleOrder)
		{
			Module module = getModule(name);
			if( this.internal.get( module ) )
			{
				module.serverAboutToStart( event );
			}
		}
	}

	@EventHandler
	private void serverStarting( final FMLServerStartingEvent event )
	{
		for( String name : moduleOrder)
		{
			Module module = getModule(name);
			if( this.internal.get( module ) )
			{
				module.serverStarting( event );
			}
		}
	}

	@EventHandler
	private void serverStopping( final FMLServerStoppingEvent event )
	{
		for( String name : moduleOrder)
		{
			Module module = getModule(name);
			if( this.internal.get( module ) )
			{
				module.serverStopping( event );
			}
		}
	}

	@EventHandler
	private void serverStopped( final FMLServerStoppedEvent event )
	{
		for( String name : moduleOrder)
		{
			Module module = getModule(name);
			if( this.internal.get( module ) )
			{
				module.serverStopped( event );
			}
		}
	}
}
