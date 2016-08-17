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

package appeng.block.misc;


import java.util.EnumSet;
import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import appeng.api.util.AEPartLocation;
import appeng.api.util.IOrientable;
import appeng.block.AEBaseTileBlock;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.misc.TileInterface;
import appeng.util.Platform;


public class BlockInterface extends AEBaseTileBlock
{

	public static final PropertyAEPartLocation POINT_AT = new PropertyAEPartLocation( "point_at" );

	public BlockInterface()
	{
		super( Material.IRON );

		this.setTileEntity( TileInterface.class );
		this.setFeature( EnumSet.of( AEFeature.Core ) );
		this.setDefaultState( getDefaultState().withProperty( POINT_AT, AEPartLocation.INTERNAL ) );
	}

	@Override
	protected IProperty[] getAEStates()
	{
		// Remove mapping of forward / up
		return new IProperty[] {
				POINT_AT
		};
	}

	@Override
	public IBlockState getActualState( IBlockState state, IBlockAccess world, BlockPos pos )
	{
		// Remove mapping of forward / up
		return state;
	}

	@Override
	public boolean onActivated( final World w, final BlockPos pos, final EntityPlayer p, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ )
	{
		if( p.isSneaking() )
		{
			return false;
		}

		final TileInterface tg = this.getTileEntity( w, pos );
		if( tg != null )
		{
			if( Platform.isServer() )
			{
				Platform.openGUI( p, tg, AEPartLocation.fromFacing( side ), GuiBridge.GUI_INTERFACE );
			}
			return true;
		}
		return false;
	}

	@Override
	protected boolean hasCustomRotation()
	{
		return true;
	}

	@Override
	protected void customRotateBlock( final IOrientable rotatable, final EnumFacing axis )
	{
		if( rotatable instanceof TileInterface )
		{
			( (TileInterface) rotatable ).setSide( AEPartLocation.fromFacing( axis ) );
		}
	}

	@Override
	public IBlockState getStateFromMeta( int meta )
	{
		AEPartLocation pointAt = AEPartLocation.INTERNAL;
		if( meta >= 0 && meta < AEPartLocation.values().length )
		{
			pointAt = AEPartLocation.values()[meta];
		}

		return getDefaultState().withProperty( POINT_AT, pointAt );
	}

	@Override
	public int getMetaFromState( IBlockState state )
	{
		AEPartLocation pointAt = state.getValue( POINT_AT );
		return pointAt.ordinal();
	}
}
