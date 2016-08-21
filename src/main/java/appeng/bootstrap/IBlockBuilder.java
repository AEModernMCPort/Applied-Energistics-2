package appeng.bootstrap;


import java.util.function.BiConsumer;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import appeng.api.definitions.IBlockDefinition;
import appeng.core.features.AEFeature;


public interface IBlockBuilder
{

	IBlockBuilder preInit( BiConsumer<Block, Item> callback );

	IBlockBuilder init( BiConsumer<Block, Item> callback );

	IBlockBuilder postInit( BiConsumer<Block, Item> callback );

	IBlockBuilder features( AEFeature... features );

	IBlockBuilder addFeatures( AEFeature... features );

	IBlockBuilder rendering( BlockRenderingCustomizer callback );

	<T extends IBlockDefinition> T build();

}
