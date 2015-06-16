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

package appeng.client.render.effects;


import net.minecraft.client.particle.EntityBreakingFX;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import appeng.api.util.AEPartLocation;
import appeng.client.texture.ExtraBlockTextures;


@SideOnly( Side.CLIENT )
public class CraftingFx extends EntityBreakingFX
{

	private final TextureAtlasSprite particleTextureIndex;

	private final int startBlkX;
	private final int startBlkY;
	private final int startBlkZ;

	public CraftingFx( World par1World, double par2, double par4, double par6, Item par8Item )
	{
		super( par1World, par2, par4, par6, par8Item );
		this.particleGravity = 0;
		this.particleBlue = 1;
		this.particleGreen = 0.9f;
		this.particleRed = 1;
		this.particleAlpha = 1.3f;
		this.particleScale = 1.5f;
		this.particleTextureIndex = ExtraBlockTextures.BlockEnergyParticle.getIcon().getAtlas();
		this.particleMaxAge /= 1.2;

		this.startBlkX = MathHelper.floor_double( this.posX );
		this.startBlkY = MathHelper.floor_double( this.posY );
		this.startBlkZ = MathHelper.floor_double( this.posZ );

		this.noClip = true;
	}

	@Override
	public int getFXLayer()
	{
		return 1;
	}

	@Override
    public void func_180434_a(WorldRenderer par1Tessellator, Entity p_180434_2_, float partialTick, float x, float y, float z, float rx, float rz)
    {
		if( partialTick < 0 || partialTick > 1 )
		{
			return;
		}

		float f6 = this.particleTextureIndex.getMinU();
		float f7 = this.particleTextureIndex.getMaxU();
		float f8 = this.particleTextureIndex.getMinV();
		float f9 = this.particleTextureIndex.getMaxV();
		float scale = 0.1F * this.particleScale;

		float offX = (float) ( this.prevPosX + ( this.posX - this.prevPosX ) * partialTick );
		float offY = (float) ( this.prevPosY + ( this.posY - this.prevPosY ) * partialTick );
		float offZ = (float) ( this.prevPosZ + ( this.posZ - this.prevPosZ ) * partialTick );
		float f14 = 1.0F;

		int blkX = MathHelper.floor_double( offX );
		int blkY = MathHelper.floor_double( offY );
		int blkZ = MathHelper.floor_double( offZ );
		if( blkX == this.startBlkX && blkY == this.startBlkY && blkZ == this.startBlkZ )
		{
			offX -= interpPosX;
			offY -= interpPosY;
			offZ -= interpPosZ;

			// AELog.info( "" + partialTick );
			par1Tessellator.setColorRGBA_F( this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha );
			par1Tessellator.addVertexWithUV( offX - x * scale - rx * scale, offY - y * scale, offZ - z * scale - rz * scale, f7, f9 );
			par1Tessellator.addVertexWithUV( offX - x * scale + rx * scale, offY + y * scale, offZ - z * scale + rz * scale, f7, f8 );
			par1Tessellator.addVertexWithUV( offX + x * scale + rx * scale, offY + y * scale, offZ + z * scale + rz * scale, f6, f8 );
			par1Tessellator.addVertexWithUV( offX + x * scale - rx * scale, offY - y * scale, offZ + z * scale - rz * scale, f6, f9 );
		}
	}

	public void fromItem( AEPartLocation d )
	{
		this.posX += 0.2 * d.xOffset;
		this.posY += 0.2 * d.yOffset;
		this.posZ += 0.2 * d.zOffset;
		this.particleScale *= 0.8f;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		this.particleScale *= 0.51f;
		this.particleAlpha *= 0.51f;
	}
}
