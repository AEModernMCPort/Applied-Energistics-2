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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.common.ModClassLoader;
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


	private static void insertBeforeDependencies(String key, Map<String, Pair<Class<Module>, String>> foundModules, Side currentSide){
		Pair<Class<Module>, String> entry = foundModules.get(key);if(entry.getRight() != null ) {
			for (String dep : entry.getRight().split(";")) {
				String[] depkv = dep.split(":");
				String[] keys = depkv[0].split("\\-");
				String value = depkv.length > 0 ? depkv[1] : null;

				Side side = ArrayUtils.contains(keys, "client") ? Side.CLIENT : ArrayUtils.contains(keys, "server") ? Side.SERVER : null;
				boolean hardDepend = ArrayUtils.contains(keys, "hard");
				boolean softDepend = ArrayUtils.contains(keys, "soft");
				boolean loadBefore = ArrayUtils.contains(keys, "before");
				if (!loadBefore) continue; //we only care for "before" here
				boolean crash = ArrayUtils.contains(keys, "crash");
				boolean isModuleDependency = hardDepend || softDepend;


				if (isModuleDependency && value != null) //we only can infer this into modules
				{
					String what = value.substring(0, value.indexOf('-'));
					String which = value.substring(value.indexOf('-') + 1, value.length());
					if (side == null || side == currentSide && what.equals("module"))
						if (foundModules.containsKey(which)) {
							Pair<Class<Module>, String> pair = foundModules.get(which);
							String s = pair.getRight();
							if (side != null) {
								s = s + (side == Side.CLIENT ? "client" : "server");
							}
							if (softDepend)
								s += "soft";
							if (hardDepend) {
								s += "hard";
								if(crash)
									s+=crash;
							}
							foundModules.put(which, new ImmutablePair<Class<Module>, String>(pair.getLeft(), s));
						} else
							throw new CustomModLoadingErrorDisplayException(String.format("Missing hard required dependency for module %s - %s", key, dep), null) { //Will open a gui.
								@Override
								public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {
									//Used to add buttons, we have none
								}

								@Override
								public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
									//from net.minecraftforge.fml.client.GuiModsMissing
									errorScreen.drawDefaultBackground();
									int offset = 75;
									String modMissingDependenciesText = "Module " + TextFormatting.BOLD + key + TextFormatting.RESET + " is missing required hard dependency " + TextFormatting.BOLD + dep + TextFormatting.RESET + ".";
									errorScreen.drawCenteredString(fontRenderer, modMissingDependenciesText, errorScreen.width / 2, offset, 0xFFFFFF);

								}
							};
				}
			}
		}
	}

	private static void validateAndPutModule(String key, Map<String, Pair<Class<Module>, String>> foundModules, Side currentSide, Map<String, Class<Module>> result, List<String> order, List<String> checked){
		checked.add(key);
		boolean load = true;
		Pair<Class<Module>, String> entry = foundModules.get(key);
		if(entry.getRight() != null )
		{
			for( String dep : entry.getRight().split( ";" ) )
			{
				String[] depkv = dep.split( ":" );
				String[] keys = depkv[0].split( "\\-" );
				String value = depkv.length > 0 ? depkv[1] : null;

				Side side = ArrayUtils.contains( keys, "client" ) ? Side.CLIENT : ArrayUtils.contains( keys, "server" ) ? Side.SERVER : null;
				boolean hardDepend = ArrayUtils.contains( keys, "hard");
				boolean softDepend = ArrayUtils.contains( keys, "soft");
				boolean crash = ArrayUtils.contains( keys, "crash" ) && hardDepend;
				boolean isModuleDependency = hardDepend||softDepend;

				if( side != null && value == null ) //Side-only generous modules
				{
					load &= side == currentSide;
					if( side == currentSide || !crash )
					{
						continue;
					}
				}
				else if( isModuleDependency && value != null ) //look for mod
				{
					String what = value.substring( 0, value.indexOf( '-' ) );
					String which = value.substring( value.indexOf( '-' ) + 1, value.length() );
					boolean depFound = side == null || side == currentSide;
					if(depFound)
						switch( what )
						{
							case "mod":
								depFound &= Loader.isModLoaded( which );
								break;
							case "module":
								if(order.contains(which)) {
									depFound &= true;
									break;
								}
								if(checked.contains(which)) {//order does not, but checked does
									depFound = false;
									break;
								}
								if(foundModules.containsKey(which)){
									validateAndPutModule(which, foundModules, currentSide, result, order, checked);
									depFound &= order.contains(which);
								}else depFound = false;
								break;
							default:
								depFound = false;
						}
					if(softDepend)
						continue; //do not affect loading
					if(hardDepend){
						load &= depFound;
						if(depFound||!crash){
							continue;
						}
					}
				}
				throw new CustomModLoadingErrorDisplayException(String.format("Missing hard required dependency for module %s - %s", key, dep), null) { //Will open a gui.
					@Override
					public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {
						//Used to add buttons, we have none
					}

					@Override
					public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
						//from net.minecraftforge.fml.client.GuiModsMissing
						errorScreen.drawDefaultBackground();
						int offset = 75;
						String modMissingDependenciesText = "Module " + TextFormatting.BOLD + key + TextFormatting.RESET+" is missing required hard dependency "+TextFormatting.BOLD+dep+TextFormatting.RESET+".";
						errorScreen.drawCenteredString(fontRenderer, modMissingDependenciesText, errorScreen.width / 2, offset, 0xFFFFFF);

					}
				};
			}
		}
		if( load )
		{
			result.put(key, entry.getLeft());
			order.add(key);
		}
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

		Maps.newHashMap(foundModules).forEach((name, data) -> {insertBeforeDependencies(name, foundModules, event.getSide());}); //Avoid ConcurrentModificationException

		Map<String, Class<Module>> modules = Maps.newHashMap();
		List<String> order = Lists.newArrayList();
		List<String> checked = Lists.newArrayList();
		for( String key: foundModules.keySet() )
		{
			if(!checked.contains(key)){
			validateAndPutModule(key, foundModules, event.getSide(), modules, order, checked);
		}
		}
		ImmutableList.Builder<String> orderBuilder = ImmutableList.builder();
		orderBuilder.addAll(order);
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
