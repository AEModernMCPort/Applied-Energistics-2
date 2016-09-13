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

import appeng.core.lib.module.Toposorter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.resources.I18n;
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
import org.lwjgl.Sys;


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
	private ImmutableMap<String, Module> modules;
	private ImmutableMap<Class, Module> classModule;
	private ImmutableList<String> moduleOrder;
	/*
	 * TODO 1.10.2-MODUSEP - Do we even want some modules be @Mod at the same time? Weird.
	 */
	private ImmutableMap<Module, Boolean> internal;
	private File configDirectory;
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

	public <M extends Module> M getModule( String name )
	{
		return (M) modules.get( name );
	}

	public <M extends Module> M getModule( Class<M> clas )
	{
		return (M) classModule.get( clas );
	}

	public File getConfigDirectory()
	{
		return configDirectory;
	}

	/**
	 * Checks whether all required dependencies are here
     */
	private boolean isValid(String name, Map<String, Pair<Class<Module>, String>> modules, Side currentSide, List<String> validModules, List<String> checkedModules){
		if(checkedModules.contains(name))
			return validModules.contains(name);
		checkedModules.add(name);
		if(!modules.containsKey(name))
			return false;
		for(String dep : modules.get(name).getRight().split(";")){
			String[] temp = dep.split(":");
			String[] modifiers = dep.split("\\-");
			String depName = temp.length>0?temp[1]:null;
			Side requiredSide = ArrayUtils.contains(modifiers, "client" ) ? Side.CLIENT : ArrayUtils.contains(modifiers, "server" ) ? Side.SERVER : currentSide;
			boolean hard = ArrayUtils.contains(modifiers, "hard");
			boolean soft = ArrayUtils.contains(modifiers, "soft");
			boolean crash = hard&&ArrayUtils.contains(modifiers, "crash");
			if(soft && hard)
				return false;
			if(soft) //Allows "client-soft" as side specifier, but noone would write it
				continue;
			if(name==null){
				if(requiredSide==currentSide)
					continue;
				else if(crash)
					throw new CustomModLoadingErrorDisplayException(String.format("Module %s is %s side only!", name, requiredSide.toString()), null) { //Will open a gui.
						@Override public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {}//Used to add buttons, we have none

						@Override
						public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
							//from net.minecraftforge.fml.client.GuiModsMissing
							errorScreen.drawDefaultBackground();
							String text = "Module " + TextFormatting.BOLD + name + TextFormatting.RESET+" can only be used on "+TextFormatting.BOLD+requiredSide.toString()+TextFormatting.RESET+"!";
							errorScreen.drawCenteredString(fontRenderer, text, errorScreen.width / 2, 75, 0xFFFFFF);
						}
					};
				return false;
			}else if(depName!=null&&hard){
				String what = depName.substring( 0, depName.indexOf( '-' ) );
				String which = depName.substring( depName.indexOf( '-' ) + 1, depName.length() );
				boolean depFound = false;
				if(requiredSide==currentSide) {
					if (which.equals("mod")){
						depFound = Loader.isModLoaded(what);
					}else if(which.equals("module")){
						depFound = isValid(what, modules, currentSide, validModules, checkedModules);
					}
				}
				if(!depFound)
					if(crash)
						throw new CustomModLoadingErrorDisplayException(String.format("Missing hard required dependency for module %s - %s", name, depName), null) { //Will open a gui.
							@Override
							public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {}//Used to add buttons, we have none

							@Override
							public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
								//from net.minecraftforge.fml.client.GuiModsMissing
								errorScreen.drawDefaultBackground();
								String modMissingDependenciesText = "Module " + TextFormatting.BOLD + name + TextFormatting.RESET+" is missing required hard dependency "+TextFormatting.BOLD+depName+TextFormatting.RESET+".";
								errorScreen.drawCenteredString(fontRenderer, modMissingDependenciesText, errorScreen.width / 2, 75, 0xFFFFFF);
							}
						};
					return false;
			}else return false; //Syntax error
		}
		validModules.add(name);
		return true;
	}

	private void addAsNode(String name, Map<String, Pair<Class<Module>, String>> foundModules, Toposorter.Graph<String> graph, Side currentSide){
		if(graph.hasNode(name))
			return;
		Toposorter.Graph<String>.Node node = graph.addNewNode(name, name);
		for(String dep : foundModules.get(name).getRight().split(";")){
			String[] temp = dep.split(":");
			String[] modifiers = dep.split("\\-");
			String depName = temp.length>0?temp[1]:null;
			Side requiredSide = ArrayUtils.contains(modifiers, "client" ) ? Side.CLIENT : ArrayUtils.contains(modifiers, "server" ) ? Side.SERVER : currentSide;
			boolean before = ArrayUtils.contains(modifiers, "before");
			boolean after = ArrayUtils.contains(modifiers, "after");
			if(depName!=null){
				String what = depName.substring( 0, depName.indexOf( '-' ) );
				String which = depName.substring( depName.indexOf( '-' ) + 1, depName.length() );
				if(which.equals("module") && requiredSide==currentSide){
					addAsNode(what, foundModules, graph, currentSide);
					if(after)
						node.dependOn(graph.getNode(what));
					else if(before)
						node.dependencyOf(graph.getNode(what));
					//"mod" cannot be handled here because AE2 cannot control mod loading
					//else there is no vertex added to this graph
				}
			}
		}
	}

	@EventHandler
	private void preInit( final FMLPreInitializationEvent event )
	{
		if( !Loader.isModLoaded( "appliedenergistics2-core" ) )
		{
			// TODO 1.10.2-MODUSEP - I dunno what to do with proxies. Srsly. I think we will have to use a proxy per module. If so, do we also need generic proxy
			// TODO 1.10.2-MODUSEP Answer: Internal modules have to have a proxy provided by AE2 or at least a proxy created by AE2 - external mods are @Mod so they have their own
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



		List<String> checked = Lists.newArrayList();
		List<String> valid = Lists.newArrayList();
		Map<String, Class<Module>> modules = Maps.newHashMap();
		for(Map.Entry<String, Pair<Class<Module>, String>> entry:foundModules.entrySet()){
			if(isValid(entry.getKey(), foundModules, event.getSide(), valid, checked)){
				modules.put(entry.getKey(), entry.getValue().getLeft());
			}
		}
		Toposorter.Graph<String> graph = new Toposorter.Graph<String>();
		for(String name : modules.keySet()){
			addAsNode(name, foundModules, graph, event.getSide());
		}

		List<String> ls = null;
		try {
			ls = Toposorter.toposort(graph);
		} catch (Toposorter.SortingException e) {
			boolean moduleFound = false;
			event.getModLog().error("Module "+e.getNode()+" has circular dependencies:");
			for(String s : e.getVisitedNodes()){
				if(s.equals(e.getNode())){
					moduleFound = true;
					event.getModLog().error("\""+s+"\"");
					continue;
				}
				if(moduleFound){
					event.getModLog().error("depending on: \""+s+"\"");
				}
			}
			event.getModLog().error("again depending on \""+e.getNode()+"\"");

			throw new CustomModLoadingErrorDisplayException(String.format("Circular dependency at module %s", e.getNode()), null) { //Will open a gui.
				@Override
				public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {}

				@Override
				public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
					//from net.minecraftforge.fml.client.GuiModsMissing
					errorScreen.drawDefaultBackground();
					String modMissingDependenciesText = "The module " + TextFormatting.BOLD + e.getNode() + TextFormatting.RESET+" has circular dependencies! See the log for a list!";
					errorScreen.drawCenteredString(fontRenderer, modMissingDependenciesText, errorScreen.width / 2, 75, 0xFFFFFF);
				}
			};
		}
		moduleOrder = new ImmutableList.Builder<String>().addAll(ls).build();
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
