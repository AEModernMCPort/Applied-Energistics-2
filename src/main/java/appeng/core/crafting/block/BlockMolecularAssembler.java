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

package appeng.core.crafting.block;


import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.core.api.util.AEPartLocation;
import appeng.core.crafting.tile.TileMolecularAssembler;
import appeng.core.lib.block.AEBaseTileBlock;
import appeng.core.lib.sync.GuiBridge;
import appeng.core.lib.util.Platform;


public class BlockMolecularAssembler extends AEBaseTileBlock
{

	public BlockMolecularAssembler()
	{
		super( Material.IRON );

		this.setTileEntity( TileMolecularAssembler.class );
		this.setOpaque( false );
		this.lightOpacity = 1;
	}

	@Override
	public boolean canRenderInLayer( final IBlockState state, final BlockRenderLayer layer )
	{
		return layer == BlockRenderLayer.CUTOUT_MIPPED;
	}

	@Override
	public boolean onBlockActivated( final World w, final BlockPos pos, final IBlockState state, final EntityPlayer p, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ )
	{
		final TileMolecularAssembler tg = this.getTileEntity( w, pos );
		if( tg != null && !p.isSneaking() )
		{
			Platform.openGUI( p, tg, AEPartLocation.fromFacing( side ), GuiBridge.GUI_MAC );
			return true;
		}
		return false;
	}
}