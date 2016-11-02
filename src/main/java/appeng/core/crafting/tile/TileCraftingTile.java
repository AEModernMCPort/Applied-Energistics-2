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

package appeng.core.crafting.tile;


import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import appeng.api.definitions.IItemDefinition;
import appeng.core.api.config.Actionable;
import appeng.core.api.implementations.IPowerChannelState;
import appeng.core.api.util.AEPartLocation;
import appeng.core.api.util.WorldCoord;
import appeng.core.crafting.block.BlockCraftingUnit;
import appeng.core.crafting.block.BlockCraftingUnit.CraftingUnitType;
import appeng.core.lib.AppEngApi;
import appeng.core.lib.tile.TileEvent;
import appeng.core.lib.tile.events.TileEventType;
import appeng.core.lib.util.Platform;
import appeng.core.me.api.networking.GridFlags;
import appeng.core.me.api.networking.IGridHost;
import appeng.core.me.api.networking.events.MENetworkChannelsChanged;
import appeng.core.me.api.networking.events.MENetworkEventSubscribe;
import appeng.core.me.api.networking.events.MENetworkPowerStatusChange;
import appeng.core.me.api.storage.IMEInventory;
import appeng.core.me.api.storage.data.IAEItemStack;
import appeng.core.me.grid.cluster.IAECluster;
import appeng.core.me.grid.cluster.IAEMultiBlock;
import appeng.core.me.grid.cluster.implementations.CraftingCPUCalculator;
import appeng.core.me.grid.cluster.implementations.CraftingCPUCluster;
import appeng.core.me.grid.helpers.AENetworkProxy;
import appeng.core.me.grid.helpers.AENetworkProxyMultiblock;
import appeng.core.me.tile.AENetworkTile;


public class TileCraftingTile extends AENetworkTile implements IAEMultiBlock, IPowerChannelState
{

	private final CraftingCPUCalculator calc = new CraftingCPUCalculator( this );
	private NBTTagCompound previousState = null;
	private boolean isCoreBlock = false;
	private CraftingCPUCluster cluster;

	public TileCraftingTile()
	{
		this.getProxy().setFlags( GridFlags.MULTIBLOCK, GridFlags.REQUIRE_CHANNEL );
		this.getProxy().setValidSides( EnumSet.noneOf( EnumFacing.class ) );
	}

	@Override
	protected AENetworkProxy createProxy()
	{
		return new AENetworkProxyMultiblock( this, "proxy", this.getItemFromTile( this ), true );
	}

	@Override
	protected ItemStack getItemFromTile( final Object obj )
	{
		Optional<ItemStack> is = Optional.empty();

		if( ( (TileCraftingTile) obj ).isAccelerator() )
		{
			is = ( (IItemDefinition) AppEngApi.internalApi().definitions().blocks().craftingAccelerator().block().maybeItem().get() ).maybeStack( 1 );
		}

		return is.orElseGet( () -> super.getItemFromTile( obj ) );
	}

	@Override
	public boolean canBeRotated()
	{
		return true;// return BlockCraftingUnit.checkType( worldObj.getBlockMetadata( xCoord, yCoord, zCoord ),
		// BlockCraftingUnit.BASE_MONITOR );
	}

	@Override
	public void setName( final String name )
	{
		super.setName( name );
		if( this.cluster != null )
		{
			this.cluster.updateName();
		}
	}

	public boolean isAccelerator()
	{
		if( this.worldObj == null )
		{
			return false;
		}

		final BlockCraftingUnit unit = (BlockCraftingUnit) this.worldObj.getBlockState( this.pos ).getBlock();
		return unit.type == CraftingUnitType.ACCELERATOR;
	}

	@Override
	public void onReady()
	{
		super.onReady();
		this.getProxy().setVisualRepresentation( this.getItemFromTile( this ) );
		this.updateMultiBlock();
	}

	public void updateMultiBlock()
	{
		this.calc.calculateMultiblock( this.worldObj, this.getLocation() );
	}

	public void updateStatus( final CraftingCPUCluster c )
	{
		if( this.cluster != null && this.cluster != c )
		{
			this.cluster.breakCluster();
		}

		this.cluster = c;
		this.updateMeta( true );
	}

	public void updateMeta( final boolean updateFormed )
	{
		if( this.worldObj == null || this.notLoaded() )
		{
			return;
		}

		final boolean formed = this.isFormed();
		boolean power = false;

		if( this.getProxy().isReady() )
		{
			power = this.getProxy().isActive();
		}

		final IBlockState current = this.worldObj.getBlockState( this.pos );
		final IBlockState newState = current.withProperty( BlockCraftingUnit.POWERED, power ).withProperty( BlockCraftingUnit.FORMED, formed );

		if( current != newState )
		{
			this.worldObj.setBlockState( this.pos, newState );
		}

		if( updateFormed )
		{
			if( formed )
			{
				this.getProxy().setValidSides( EnumSet.allOf( EnumFacing.class ) );
			}
			else
			{
				this.getProxy().setValidSides( EnumSet.noneOf( EnumFacing.class ) );
			}
		}
	}

	public boolean isFormed()
	{
		if( Platform.isClient() )
		{
			return (boolean) this.worldObj.getBlockState( this.pos ).getValue( BlockCraftingUnit.FORMED );
		}
		return this.cluster != null;
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileCraftingTile( final NBTTagCompound data )
	{
		data.setBoolean( "core", this.isCoreBlock() );
		if( this.isCoreBlock() && this.cluster != null )
		{
			this.cluster.writeToNBT( data );
		}
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileCraftingTile( final NBTTagCompound data )
	{
		this.setCoreBlock( data.getBoolean( "core" ) );
		if( this.isCoreBlock() )
		{
			if( this.cluster != null )
			{
				this.cluster.readFromNBT( data );
			}
			else
			{
				this.setPreviousState( (NBTTagCompound) data.copy() );
			}
		}
	}

	@Override
	public void disconnect( final boolean update )
	{
		if( this.cluster != null )
		{
			this.cluster.destroy();
			if( update )
			{
				this.updateMeta( true );
			}
		}
	}

	@Override
	public IAECluster getCluster()
	{
		return this.cluster;
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@MENetworkEventSubscribe
	public void onPowerStateChange( final MENetworkChannelsChanged ev )
	{
		this.updateMeta( false );
	}

	@MENetworkEventSubscribe
	public void onPowerStateChange( final MENetworkPowerStatusChange ev )
	{
		this.updateMeta( false );
	}

	public boolean isStatus()
	{
		return false;
	}

	public boolean isStorage()
	{
		return false;
	}

	public int getStorageBytes()
	{
		return 0;
	}

	public void breakCluster()
	{
		if( this.cluster != null )
		{
			this.cluster.cancel();
			final IMEInventory<IAEItemStack> inv = this.cluster.getInventory();

			final LinkedList<WorldCoord> places = new LinkedList<WorldCoord>();

			final Iterator<IGridHost> i = this.cluster.getTiles();
			while( i.hasNext() )
			{
				final IGridHost h = i.next();
				if( h == this )
				{
					places.add( new WorldCoord( this ) );
				}
				else
				{
					final TileEntity te = (TileEntity) h;

					for( final AEPartLocation d : AEPartLocation.SIDE_LOCATIONS )
					{
						final WorldCoord wc = new WorldCoord( te );
						wc.add( d, 1 );
						if( this.worldObj.isAirBlock( wc.getPos() ) )
						{
							places.add( wc );
						}
					}
				}
			}

			Collections.shuffle( places );

			if( places.isEmpty() )
			{
				throw new IllegalStateException( this.cluster + " does not contain any kind of blocks, which were destroyed." );
			}

			for( IAEItemStack ais : inv.getAvailableItems( AppEngApi.internalApi().storage().createItemList() ) )
			{
				ais = ais.copy();
				ais.setStackSize( ais.getItemStack().getMaxStackSize() );
				while( true )
				{
					final IAEItemStack g = inv.extractItems( ais.copy(), Actionable.MODULATE, this.cluster.getActionSource() );
					if( g == null )
					{
						break;
					}

					final WorldCoord wc = places.poll();
					places.add( wc );

					Platform.spawnDrops( this.worldObj, wc.getPos(), Collections.singletonList( g.getItemStack() ) );
				}
			}

			this.cluster.destroy();
		}
	}

	@Override
	public boolean isPowered()
	{
		if( Platform.isClient() )
		{
			return (boolean) this.worldObj.getBlockState( this.pos ).getValue( BlockCraftingUnit.POWERED );
		}
		return this.getProxy().isActive();
	}

	@Override
	public boolean isActive()
	{
		if( Platform.isServer() )
		{
			return this.getProxy().isActive();
		}
		return this.isPowered() && this.isFormed();
	}

	public boolean isCoreBlock()
	{
		return this.isCoreBlock;
	}

	public void setCoreBlock( final boolean isCoreBlock )
	{
		this.isCoreBlock = isCoreBlock;
	}

	public NBTTagCompound getPreviousState()
	{
		return this.previousState;
	}

	public void setPreviousState( final NBTTagCompound previousState )
	{
		this.previousState = previousState;
	}
}
