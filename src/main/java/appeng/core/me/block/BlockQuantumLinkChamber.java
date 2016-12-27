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

package appeng.core.me.block;


import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.core.api.util.AEPartLocation;
import appeng.core.lib.CommonHelper;
import appeng.core.lib.client.EffectType;
import appeng.core.lib.helpers.AEGlassMaterial;
import appeng.core.lib.sync.GuiBridge;
import appeng.core.lib.util.Platform;
import appeng.core.me.tile.TileQuantumBridge;


public class BlockQuantumLinkChamber extends BlockQuantumBase
{

	public BlockQuantumLinkChamber()
	{
		super( AEGlassMaterial.INSTANCE );
	}

	@Override
	public void randomDisplayTick( final IBlockState state, final World w, final BlockPos pos, final Random rand )
	{
		final TileQuantumBridge bridge = this.getTileEntity( w, pos );
		if( bridge != null )
		{
			if( bridge.hasQES() )
			{
				if( CommonHelper.proxy.shouldAddParticles( rand ) )
				{
					CommonHelper.proxy.spawnEffect( EffectType.Energy, w, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, null );
				}
			}
		}
	}

	@Override
	public boolean onActivated( final World w, final BlockPos pos, final EntityPlayer p, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ )
	{
		if( p.isSneaking() )
		{
			return false;
		}

		final TileQuantumBridge tg = this.getTileEntity( w, pos );
		if( tg != null )
		{
			if( Platform.isServer() )
			{
				Platform.openGUI( p, tg, AEPartLocation.fromFacing( side ), GuiBridge.GUI_QNB );
			}
			return true;
		}
		return false;
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( final World w, final BlockPos pos, final Entity player, final boolean b )
	{
		final double onePixel = 2.0 / 16.0;
		return Collections.singletonList( new AxisAlignedBB( onePixel, onePixel, onePixel, 1.0 - onePixel, 1.0 - onePixel, 1.0 - onePixel ) );
	}

	@Override
	public void addCollidingBlockToList( final World w, final BlockPos pos, final AxisAlignedBB bb, final List<AxisAlignedBB> out, final Entity e )
	{
		final double onePixel = 2.0 / 16.0;
		out.add( new AxisAlignedBB( onePixel, onePixel, onePixel, 1.0 - onePixel, 1.0 - onePixel, 1.0 - onePixel ) );
	}
}
