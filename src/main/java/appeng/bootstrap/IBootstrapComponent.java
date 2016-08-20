package appeng.bootstrap;


import net.minecraftforge.fml.relauncher.Side;


public interface IBootstrapComponent
{

	default void preInitialize( Side side )
	{

	}

	default void initialize( Side side )
	{

	}

	default void postInitialize( Side side )
	{

	}
}
