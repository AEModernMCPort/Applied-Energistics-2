package appeng.bootstrap.components;


import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;

import appeng.block.AEBaseBlock;
import appeng.block.IHasSpecialItemModel;


public class ItemModelRegistrationComponent implements InitComponent
{

	private final Block block;

	private final Item item;

	public ItemModelRegistrationComponent( Block block, Item item )
	{
		this.block = block;
		this.item = item;
	}

	@Override
	public void initialize( Side side )
	{
		ItemModelMesher itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

		// Retrieve a custom item mesh definition, if the block defines one
		ItemMeshDefinition itemMeshDefinition = null;
		if( block instanceof AEBaseBlock )
		{
			itemMeshDefinition = ( (AEBaseBlock) block ).getItemMeshDefinition();
		}

		if( itemMeshDefinition != null )
		{
			// This block has a custom item mesh definition, so register it instead of the resource location
			itemModelMesher.register( item, itemMeshDefinition );
		}
		else if( !block.getBlockState().getProperties().isEmpty() || block instanceof IHasSpecialItemModel )
		{
			itemModelMesher.register( item, 0, new ModelResourceLocation( block.getRegistryName(), "inventory" ) );
		}
		else
		{
			itemModelMesher.register( item, 0, new ModelResourceLocation( block.getRegistryName(), "normal" ) );
		}
	}
}
