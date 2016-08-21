package appeng.block.networking;


import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;

import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.block.AEBaseItemBlockChargeable;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.tile.networking.TileEnergyCell;


public class BlockEnergyCellRenderCustomizer extends BlockRenderingCustomizer
{

	@Override
	public void customize( IBlockRendering rendering, IItemRendering itemRendering )
	{
		itemRendering.meshDefinition( BlockEnergyCellRenderCustomizer::getItemModel );
	}

	/**
	 * Determines which version of the energy cell model should be used depending on the fill factor
	 * of the item stack.
	 */
	private static ModelResourceLocation getItemModel( ItemStack is )
	{
		double fillFactor = getFillFactor( is );

		int storageLevel = TileEnergyCell.getStorageLevelFromFillFactor( fillFactor );

		return new ModelResourceLocation( "appliedenergistics2:tile.BlockEnergyCell", "fullness=" + storageLevel );
	}

	/**
	 * Helper method that returns the energy fill factor (between 0 and 1) of a given item stack.
	 * Returns 0 if the item stack has no fill factor.
	 */
	private static double getFillFactor( ItemStack is )
	{
		if( !( is.getItem() instanceof IAEItemPowerStorage ) )
		{
			return 0;
		}

		AEBaseItemBlockChargeable itemChargeable = (AEBaseItemBlockChargeable) is.getItem();
		double curPower = itemChargeable.getAECurrentPower( is );
		double maxPower = itemChargeable.getAEMaxPower( is );

		return curPower / maxPower;
	}
}
