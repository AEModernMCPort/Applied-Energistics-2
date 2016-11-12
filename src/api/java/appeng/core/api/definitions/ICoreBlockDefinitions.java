
package appeng.core.api.definitions;


import appeng.api.definitions.IBlockDefinition;
import net.minecraft.block.Block;

import appeng.api.definitions.IDefinition;
import appeng.api.definitions.IDefinitions;


public interface ICoreBlockDefinitions extends IDefinitions<IBlockDefinition<Block>>
{

	default IBlockDefinition<? extends Block> blockCrank()
	{
		return get( "crank" );
	}

	default IBlockDefinition<? extends Block> blockCharger()
	{
		return get( "charger" );
	}

	default IBlockDefinition<? extends Block> blockGrinder()
	{
		return get( "grinder" );
	}

	default IBlockDefinition<? extends Block> blockInscriber()
	{
		return get( "inscriber" );
	}

	default IBlockDefinition<? extends Block> blockVibrationChamber()
	{
		return get( "vibration_chamber" );
	}


}
