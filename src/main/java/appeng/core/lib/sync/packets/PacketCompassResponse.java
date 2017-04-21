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

package appeng.core.lib.sync.packets;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;

import appeng.core.lib.sync.AppEngPacket;
import appeng.core.lib.sync.network.INetworkInfo;
import appeng.tools.hooks.CompassManager;
import appeng.tools.hooks.CompassResult;


public class PacketCompassResponse extends AppEngPacket
{

	private final long attunement;
	private final int cx;
	private final int cz;
	private final int cdy;

	private CompassResult cr;

	// automatic.
	public PacketCompassResponse( final ByteBuf stream )
	{
		this.attunement = stream.readLong();
		this.cx = stream.readInt();
		this.cz = stream.readInt();
		this.cdy = stream.readInt();

		this.cr = new CompassResult( stream.readBoolean(), stream.readBoolean(), stream.readDouble() );
	}

	// api
	public PacketCompassResponse( final PacketCompassRequest req, final boolean hasResult, final boolean spin, final double radians )
	{

		final ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeLong( this.attunement = req.attunement );
		data.writeInt( this.cx = req.cx );
		data.writeInt( this.cz = req.cz );
		data.writeInt( this.cdy = req.cdy );

		data.writeBoolean( hasResult );
		data.writeBoolean( spin );
		data.writeDouble( radians );

		this.configureWrite( data );
	}

	@Override
	public void clientPacketData( final INetworkInfo network, final AppEngPacket packet, final EntityPlayer player )
	{
		CompassManager.INSTANCE.postResult( this.attunement, this.cx << 4, this.cdy << 5, this.cz << 4, this.cr );
	}
}