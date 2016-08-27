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


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

import appeng.core.lib.sync.GuiBridge;
import appeng.core.lib.util.Platform;


public class PartInterfaceTerminal extends AbstractPartDisplay
{
	public PartInterfaceTerminal( final ItemStack is )
	{
		super( is );
	}

	@Override
	public boolean onPartActivate( final EntityPlayer player, final EnumHand hand, final Vec3d pos )
	{
		if( !super.onPartActivate( player, hand, pos ) )
		{
			if( !player.isSneaking() )
			{
				if( Platform.isClient() )
				{
					return true;
				}

				Platform.openGUI( player, this.getHost().getTile(), this.getSide(), GuiBridge.GUI_INTERFACE_TERMINAL );

				return true;
			}
		}

		return false;
	}
}
