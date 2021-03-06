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

package appeng.core.me.part.networking;


import java.util.EnumSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;

import appeng.core.api.config.Actionable;
import appeng.core.api.util.AECableType;
import appeng.core.api.util.AEPartLocation;
import appeng.core.me.api.networking.GridFlags;
import appeng.core.me.api.networking.IGridNode;
import appeng.core.me.api.networking.energy.IEnergyGrid;
import appeng.core.me.api.networking.energy.IEnergyGridProvider;
import appeng.core.me.api.parts.IPartCollisionHelper;
import appeng.core.me.api.parts.IPartHost;
import appeng.core.me.grid.GridAccessException;
import appeng.core.me.grid.helpers.AENetworkProxy;
import appeng.core.me.part.AEBasePart;


public final class PartQuartzFiber extends AEBasePart<PartQuartzFiber> implements IEnergyGridProvider
{

	private final AENetworkProxy outerProxy = new AENetworkProxy( this, "outer", this.getProxy().getMachineRepresentation(), true );

	public PartQuartzFiber( final ItemStack is )
	{
		super( is );
		this.getProxy().setIdlePowerUsage( 0 );
		this.getProxy().setFlags( GridFlags.CANNOT_CARRY );
		this.outerProxy.setIdlePowerUsage( 0 );
		this.outerProxy.setFlags( GridFlags.CANNOT_CARRY );
	}

	@Override
	public AECableType getCableConnectionType( final AEPartLocation dir )
	{
		return AECableType.GLASS;
	}

	@Override
	public void getBoxes( final IPartCollisionHelper bch )
	{
		bch.addBox( 6, 6, 10, 10, 10, 16 );
	}

	@Override
	public void readFromNBT( final NBTTagCompound extra )
	{
		super.readFromNBT( extra );
		this.outerProxy.readFromNBT( extra );
	}

	@Override
	public void writeToNBT( final NBTTagCompound extra )
	{
		super.writeToNBT( extra );
		this.outerProxy.writeToNBT( extra );
	}

	@Override
	public void removeFromWorld()
	{
		super.removeFromWorld();
		this.outerProxy.invalidate();
	}

	@Override
	public void addToWorld()
	{
		super.addToWorld();
		this.outerProxy.onReady();
	}

	@Override
	public void setPartHostInfo( final AEPartLocation side, final IPartHost host, final TileEntity tile )
	{
		super.setPartHostInfo( side, host, tile );
		this.outerProxy.setValidSides( EnumSet.of( side.getFacing() ) );
	}

	@Override
	public IGridNode getExternalFacingNode()
	{
		return this.outerProxy.getNode();
	}

	@Override
	public float getCableConnectionLength( AECableType cable )
	{
		return 16;
	}

	@Override
	public void onPlacement( final EntityPlayer player, final EnumHand hand, final ItemStack held, final AEPartLocation side )
	{
		super.onPlacement( player, hand, held, side );
		this.outerProxy.setOwner( player );
	}

	@Override
	public double extractAEPower( final double amt, final Actionable mode, final Set<IEnergyGrid> seen )
	{
		double acquiredPower = 0;

		try
		{
			final IEnergyGrid eg = this.getProxy().getEnergy();
			acquiredPower += eg.extractAEPower( amt - acquiredPower, mode, seen );
		}
		catch( final GridAccessException e )
		{
			// :P
		}

		try
		{
			final IEnergyGrid eg = this.outerProxy.getEnergy();
			acquiredPower += eg.extractAEPower( amt - acquiredPower, mode, seen );
		}
		catch( final GridAccessException e )
		{
			// :P
		}

		return acquiredPower;
	}

	@Override
	public double injectAEPower( final double amt, final Actionable mode, final Set<IEnergyGrid> seen )
	{

		try
		{
			final IEnergyGrid eg = this.getProxy().getEnergy();
			if( !seen.contains( eg ) )
			{
				return eg.injectAEPower( amt, mode, seen );
			}
		}
		catch( final GridAccessException e )
		{
			// :P
		}

		try
		{
			final IEnergyGrid eg = this.outerProxy.getEnergy();
			if( !seen.contains( eg ) )
			{
				return eg.injectAEPower( amt, mode, seen );
			}
		}
		catch( final GridAccessException e )
		{
			// :P
		}

		return amt;
	}

	@Override
	public double getEnergyDemand( final double amt, final Set<IEnergyGrid> seen )
	{
		double demand = 0;

		try
		{
			final IEnergyGrid eg = this.getProxy().getEnergy();
			demand += eg.getEnergyDemand( amt - demand, seen );
		}
		catch( final GridAccessException e )
		{
			// :P
		}

		try
		{
			final IEnergyGrid eg = this.outerProxy.getEnergy();
			demand += eg.getEnergyDemand( amt - demand, seen );
		}
		catch( final GridAccessException e )
		{
			// :P
		}

		return demand;
	}
}
