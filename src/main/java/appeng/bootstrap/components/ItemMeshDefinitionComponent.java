package appeng.bootstrap.components;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;

import appeng.items.AEBaseItem;


public class ItemMeshDefinitionComponent implements InitComponent
{

	private final Item item;

	public ItemMeshDefinitionComponent( Item item )
	{
		this.item = item;
	}

	@Override
	public void initialize( Side side )
	{
		ItemMeshDefinition meshDefinition = null;

		// Register a custom item model handler if the item wants one
		if( item instanceof AEBaseItem )
		{
			AEBaseItem baseItem = (AEBaseItem) item;
			meshDefinition = baseItem.getItemMeshDefinition();
		}

		if( meshDefinition != null )
		{
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register( item, meshDefinition );
		}
		else
		{
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register( item, 0, new ModelResourceLocation( item.getRegistryName(), "inventory" ) );
		}
	}
}
