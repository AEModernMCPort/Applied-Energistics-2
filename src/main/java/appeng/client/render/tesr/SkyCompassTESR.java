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

package appeng.client.render.tesr;


import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.block.misc.BlockSkyCompass;
import appeng.hooks.CompassManager;
import appeng.hooks.CompassResult;
import appeng.tile.misc.TileSkyCompass;


@SideOnly( Side.CLIENT )
public class SkyCompassTESR extends FastTESR<TileSkyCompass>
{

	protected static BlockRendererDispatcher blockRenderer;

	@Override
	public void renderTileEntityFast( TileSkyCompass te, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer )
	{

		if( !te.hasWorldObj() )
		{
			return;
		}

		if( blockRenderer == null )
		{
			blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		}

		BlockPos pos = te.getPos();
		IBlockAccess world = MinecraftForgeClient.getRegionRenderCache( te.getWorld(), pos );
		IBlockState state = world.getBlockState( pos );
		if( state.getPropertyNames().contains( Properties.StaticProperty ) )
		{
			state = state.withProperty( Properties.StaticProperty, false );
		}

		if( state instanceof IExtendedBlockState )
		{
			IExtendedBlockState exState = (IExtendedBlockState) state;

			float time = Animation.getWorldTime( getWorld(), partialTicks );

			IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState( exState.getClean() );
			exState = exState.withProperty( BlockSkyCompass.ROTATION, getRotation( te ) );

			buffer.setTranslation( x - pos.getX(), y - pos.getY(), z - pos.getZ() );

			blockRenderer.getBlockModelRenderer().renderModel( world, model, exState, pos, buffer, false );
		}
	}

	private static float getRotation( TileSkyCompass skyCompass )
	{

		CompassResult cr;

		if( skyCompass.getUp() == EnumFacing.UP || skyCompass.getUp() == EnumFacing.DOWN )
		{
			BlockPos pos = skyCompass.getPos();
			cr = CompassManager.INSTANCE.getCompassDirection( 0, pos.getX(), pos.getY(), pos.getZ() );
		}
		else
		{
			cr = new CompassResult( false, true, 0 );
		}

		if( cr.isValidResult() )
		{
			if( cr.isSpin() )
			{
				long timeMillis = System.currentTimeMillis();
				// 3 seconds per full rotation
				timeMillis %= 3000;
				return timeMillis / 3000.f * (float) Math.PI * 2;
			}
			else
			{
				return (float) ( skyCompass.getForward() == EnumFacing.DOWN ? flipidiy( cr.getRad() ) : cr.getRad() );
			}
		}
		else
		{
			long timeMillis = System.currentTimeMillis();
			// 3 seconds per full rotation
			timeMillis %= 3000;
			return timeMillis / 3000.f * (float) Math.PI * 2;
		}
	}

	private static double flipidiy( final double rad )
	{
		final double x = Math.cos( rad );
		final double y = Math.sin( rad );
		return Math.atan2( -y, x );
	}
}
