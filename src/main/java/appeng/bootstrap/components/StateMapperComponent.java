package appeng.bootstrap.components;


import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;

import appeng.client.render.model.AEIgnoringStateMapper;


public class StateMapperComponent implements InitComponent
{

	private final Block block;

	private final IStateMapper stateMapper;

	public StateMapperComponent( Block block, IStateMapper stateMapper )
	{
		this.block = block;
		this.stateMapper = stateMapper;
	}

	@Override
	public void initialize( Side side )
	{
		AEIgnoringStateMapper mapper = new AEIgnoringStateMapper( block.getRegistryName() );
		ModelLoader.setCustomStateMapper( block, mapper );
		( (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager() ).registerReloadListener( mapper );
	}

}
