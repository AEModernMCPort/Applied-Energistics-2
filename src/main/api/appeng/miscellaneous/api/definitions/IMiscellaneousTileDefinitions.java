
package appeng.miscellaneous.api.definitions;


import net.minecraft.tileentity.TileEntity;

import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.ITileDefinition;


public interface IMiscellaneousTileDefinitions extends IDefinitions<Class<TileEntity>, ITileDefinition<TileEntity>>
{

    default ITileDefinition<TileEntity> lightDetectingFixture()
    {
        return get( "light_detecting_fixture" );
    }
}
