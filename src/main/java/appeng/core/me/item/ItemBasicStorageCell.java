/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.core.me.item;


import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.core.api.AEApi;
import appeng.core.api.config.FuzzyMode;
import appeng.core.api.config.IncludeExclude;
import appeng.core.api.exceptions.MissingDefinition;
import appeng.core.api.implementations.items.IItemGroup;
import appeng.core.api.implementations.items.IStorageCell;
import appeng.core.api.implementations.items.IUpgradeModule;
import appeng.core.item.MaterialType;
import appeng.core.lib.AEConfig;
import appeng.core.lib.features.AEFeature;
import appeng.core.lib.item.AEBaseItem;
import appeng.core.lib.localization.GuiText;
import appeng.core.lib.util.InventoryAdaptor;
import appeng.core.lib.util.Platform;
import appeng.core.me.api.storage.ICellInventory;
import appeng.core.me.api.storage.ICellInventoryHandler;
import appeng.core.me.api.storage.IMEInventoryHandler;
import appeng.core.me.api.storage.StorageChannel;
import appeng.core.me.api.storage.data.IAEItemStack;
import appeng.core.me.api.storage.data.IItemList;


public final class ItemBasicStorageCell extends AEBaseItem implements IStorageCell, IItemGroup
{
	private final MaterialType component;
	private final int totalBytes;
	private final int perType;
	private final double idleDrain;

	public ItemBasicStorageCell( final MaterialType whichCell, final int kilobytes )
	{
		this.setMaxStackSize( 1 );
		this.totalBytes = kilobytes * 1024;
		this.component = whichCell;

		switch( this.component )
		{
			case Cell1kPart:
				this.idleDrain = 0.5;
				this.perType = 8;
				break;
			case Cell4kPart:
				this.idleDrain = 1.0;
				this.perType = 32;
				break;
			case Cell16kPart:
				this.idleDrain = 1.5;
				this.perType = 128;
				break;
			case Cell64kPart:
				this.idleDrain = 2.0;
				this.perType = 512;
				break;
			default:
				this.idleDrain = 0.0;
				this.perType = 8;
		}
	}

	@Override
	public void addCheckedInformation( final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo )
	{
		final IMEInventoryHandler<?> inventory = AEApi.instance().registries().cell().getCellInventory( stack, null, StorageChannel.ITEMS );

		if( inventory instanceof ICellInventoryHandler )
		{
			final ICellInventoryHandler handler = (ICellInventoryHandler) inventory;
			final ICellInventory cellInventory = handler.getCellInv();

			if( cellInventory != null )
			{
				lines.add( cellInventory.getUsedBytes() + " " + GuiText.Of.getLocal() + ' ' + cellInventory.getTotalBytes() + ' ' + GuiText.BytesUsed.getLocal() );

				lines.add( cellInventory.getStoredItemTypes() + " " + GuiText.Of.getLocal() + ' ' + cellInventory.getTotalItemTypes() + ' ' + GuiText.Types.getLocal() );

				if( handler.isPreformatted() )
				{
					final String list = ( handler.getIncludeExcludeMode() == IncludeExclude.WHITELIST ? GuiText.Included : GuiText.Excluded ).getLocal();

					if( handler.isFuzzy() )
					{
						lines.add( GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Fuzzy.getLocal() );
					}
					else
					{
						lines.add( GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Precise.getLocal() );
					}
				}
			}
		}
	}

	@Override
	public int getBytes( final ItemStack cellItem )
	{
		return this.totalBytes;
	}

	@Override
	public int getBytesPerType( final ItemStack cellItem )
	{
		return this.perType;
	}

	@Override
	public int getTotalTypes( final ItemStack cellItem )
	{
		return 63;
	}

	@Override
	public boolean isBlackListed( final ItemStack cellItem, final IAEItemStack requestedAddition )
	{
		return false;
	}

	@Override
	public boolean storableInStorageCell()
	{
		return false;
	}

	@Override
	public boolean isStorageCell( final ItemStack i )
	{
		return true;
	}

	@Override
	public double getIdleDrain()
	{
		return this.idleDrain;
	}

	@Override
	public String getUnlocalizedGroupName( final Set<ItemStack> others, final ItemStack is )
	{
		return GuiText.StorageCells.getUnlocalized();
	}

	@Override
	public boolean isEditable( final ItemStack is )
	{
		return true;
	}

	@Override
	public IInventory getUpgradesInventory( final ItemStack is )
	{
		return new CellUpgrades( is, 2 );
	}

	@Override
	public IInventory getConfigInventory( final ItemStack is )
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode( final ItemStack is )
	{
		final String fz = Platform.openNbtData( is ).getString( "FuzzyMode" );
		try
		{
			return FuzzyMode.valueOf( fz );
		}
		catch( final Throwable t )
		{
			return FuzzyMode.IGNORE_ALL;
		}
	}

	@Override
	public void setFuzzyMode( final ItemStack is, final FuzzyMode fzMode )
	{
		Platform.openNbtData( is ).setString( "FuzzyMode", fzMode.name() );
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick( final ItemStack stack, final World world, final EntityPlayer player, final EnumHand hand )
	{
		this.disassembleDrive( stack, world, player );
		return new ActionResult<ItemStack>( EnumActionResult.SUCCESS, stack );
	}

	private boolean disassembleDrive( final ItemStack stack, final World world, final EntityPlayer player )
	{
		if( player.isSneaking() )
		{
			if( Platform.isClient() )
			{
				return false;
			}

			final InventoryPlayer playerInventory = player.inventory;
			final IMEInventoryHandler inv = AEApi.instance().registries().cell().getCellInventory( stack, null, StorageChannel.ITEMS );
			if( inv != null && playerInventory.getCurrentItem() == stack )
			{
				final InventoryAdaptor ia = InventoryAdaptor.getAdaptor( player, EnumFacing.UP );
				final IItemList<IAEItemStack> list = inv.getAvailableItems( StorageChannel.ITEMS.createList() );
				if( list.isEmpty() && ia != null )
				{
					playerInventory.setInventorySlotContents( playerInventory.currentItem, null );

					// drop core
					final ItemStack extraB = ia.addItems( this.component.stack( 1 ) );
					if( extraB != null )
					{
						player.dropItem( extraB, false );
					}

					// drop upgrades
					final IInventory upgradesInventory = this.getUpgradesInventory( stack );
					for( int upgradeIndex = 0; upgradeIndex < upgradesInventory.getSizeInventory(); upgradeIndex++ )
					{
						final ItemStack upgradeStack = upgradesInventory.getStackInSlot( upgradeIndex );
						final ItemStack leftStack = ia.addItems( upgradeStack );
						if( leftStack != null && upgradeStack.getItem() instanceof IUpgradeModule )
						{
							player.dropItem( upgradeStack, false );
						}
					}

					// drop empty storage cell case
					AEApi.instance().definitions().materials().emptyStorageCell().maybeStack( 1 ).ifPresent( is -> {
						final ItemStack extraA = ia.addItems( is );
						if( extraA != null )
						{
							player.dropItem( extraA, false );
						}
					} );

					if( player.inventoryContainer != null )
					{
						player.inventoryContainer.detectAndSendChanges();
					}

					return true;
				}
			}
		}
		return false;
	}

	@Override
	public EnumActionResult onItemUseFirst( final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand )
	{
		return this.disassembleDrive( stack, world, player ) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
	}

	@Override
	public ItemStack getContainerItem( final ItemStack itemStack )
	{
		return AEApi.instance().definitions().materials().emptyStorageCell().maybeStack( 1 ).orElseThrow( () -> new MissingDefinition( "Tried to use empty storage cells while basic storage cells are defined." ) );
	}

	@Override
	public boolean hasContainerItem( final ItemStack stack )
	{
		return AEConfig.instance.isFeatureEnabled( AEFeature.EnableDisassemblyCrafting );
	}
}