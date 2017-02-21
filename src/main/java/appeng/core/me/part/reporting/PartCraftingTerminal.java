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

package appeng.core.me.part.reporting;


import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.core.lib.helpers.Reflected;
import appeng.core.lib.sync.GuiBridge;
import appeng.core.lib.tile.inventory.AppEngInternalInventory;


public final class PartCraftingTerminal extends AbstractPartTerminal<PartCraftingTerminal>
{
	private final AppEngInternalInventory craftingGrid = new AppEngInternalInventory( this, 9 );

	@Reflected
	public PartCraftingTerminal( final ItemStack is )
	{
		super( is );
	}

	@Override
	public void getDrops( final List<ItemStack> drops, final boolean wrenched )
	{
		super.getDrops( drops, wrenched );

		for( final ItemStack is : this.craftingGrid )
		{
			if( is != null )
			{
				drops.add( is );
			}
		}
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.craftingGrid.readFromNBT( data, "craftingGrid" );
	}

	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );
		this.craftingGrid.writeToNBT( data, "craftingGrid" );
	}

	@Override
	public GuiBridge getGui( final EntityPlayer p )
	{
		int x = (int) p.posX;
		int y = (int) p.posY;
		int z = (int) p.posZ;
		if( this.getHost().getTile() != null )
		{
			x = this.getTile().getPos().getX();
			y = this.getTile().getPos().getY();
			z = this.getTile().getPos().getZ();
		}

		if( GuiBridge.GUI_CRAFTING_TERMINAL.hasPermissions( this.getHost().getTile(), x, y, z, this.getSide(), p ) )
		{
			return GuiBridge.GUI_CRAFTING_TERMINAL;
		}
		return GuiBridge.GUI_ME;
	}

	@Override
	public IInventory getInventoryByName( final String name )
	{
		if( name.equals( "crafting" ) )
		{
			return this.craftingGrid;
		}
		return super.getInventoryByName( name );
	}
}
