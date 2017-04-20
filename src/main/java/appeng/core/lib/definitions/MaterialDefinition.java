
package appeng.core.lib.definitions;


import java.util.Optional;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterialDefinition;
import appeng.api.definitions.sub.IItemSubDefinition;
import appeng.api.item.IStateItem;
import appeng.api.item.IStateItem.State;
import appeng.core.AppEngCore;
import appeng.core.api.items.IItemMaterial;
import appeng.core.api.material.Material;
import appeng.core.definitions.CoreItemDefinitions;
import appeng.core.item.ItemMaterial;


public class MaterialDefinition<M extends Material> extends Definition<M> implements IMaterialDefinition<M>
{

	private static IItemDefinition<ItemMaterial> itemMaterialDefinition = AppEngCore.INSTANCE.<Item, CoreItemDefinitions>definitions( Item.class ).getUncheked( "material" );

	public MaterialDefinition( ResourceLocation identifier, M material )
	{
		super( identifier, material );
	}

	@Override
	public boolean isSameAs( Object other )
	{
		// TODO 1.11.2-CD:A - Add checks
		return super.isSameAs( other );
	}

	@Override
	public <S extends IStateItem.State<I>, I extends Item & IItemMaterial<I>, D extends IItemSubDefinition<S, I>> Optional<D> maybeAsSubDefinition()
	{
		return (Optional<D>) maybe().map( material -> itemMaterialDefinition.<State<ItemMaterial>, ItemMaterial, IItemSubDefinition<State<ItemMaterial>, ItemMaterial>>maybeSubDefinition().get().withProperty( "material", null ) );
	}

}
