
package appeng.core.me.api.definitions;


import net.minecraft.tileentity.TileEntity;

import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.ITileDefinition;


public interface IMETileDefinitions extends IDefinitions<Class<TileEntity>, ITileDefinition<TileEntity>>
{

	default ITileDefinition<TileEntity> multiPart()
	{
		return get( "multi_part" );
	}

	default ITileDefinition<TileEntity> wirelessAccessPoint()
	{
		return get( "wireless_access_point" );
	}

	default ITileDefinition<TileEntity> securityStation()
	{
		return get( "security_station" );
	}

	// TODO 1.11-CN:A - Subdefine?
	default ITileDefinition<TileEntity> quantumRing()
	{
		return get( "quantum_ring" );
	}

	default ITileDefinition<TileEntity> quantumLink()
	{
		return get( "quantum_link" );
	}

	default ITileDefinition<TileEntity> inscriber()
	{
		return get( "inscriber" );
	}

	default ITileDefinition<TileEntity> controller()
	{
		return get( "controller" );
	}

	default ITileDefinition<TileEntity> drive()
	{
		return get( "drive" );
	}

	default ITileDefinition<TileEntity> chest()
	{
		return get( "chest" );
	}

	default ITileDefinition<TileEntity> iface()
	{
		return get( "iface" );
	}

	default ITileDefinition<TileEntity> cellWorkbench()
	{
		return get( "cell_workbench" );
	}

	default ITileDefinition<TileEntity> iOPort()
	{
		return get( "ioport" );
	}

	default ITileDefinition<TileEntity> condenser()
	{
		return get( "condenser" );
	}

}
