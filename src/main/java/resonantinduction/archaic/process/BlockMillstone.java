package resonantinduction.archaic.process;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.block.BlockRI;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.inventory.InventoryUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMillstone extends BlockRI
{
	Icon top;

	public BlockMillstone()
	{
		super("millstone", Material.wood);
		setTextureName(Reference.PREFIX + "material_wood_surface");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconReg)
	{
		this.top = iconReg.registerIcon(Reference.PREFIX + "material_wood_top");
		super.registerIcons(iconReg);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
	{
		return getIcon(side, 0);
	}

	/** Returns the block texture based on the side being looked at. Args: side */
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		if (side == 1)
		{
			return top;
		}

		return blockIcon;
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
	{
		if (!world.isRemote)
		{
			TileEntity te = world.getBlockTileEntity(x, y, z);

			if (te instanceof TileMillstone)
			{
				TileMillstone tile = (TileMillstone) te;

				ItemStack output = tile.getStackInSlot(0);

				if (output != null)
				{
					InventoryUtility.dropItemStack(world, new Vector3(player), output, 0);
					tile.setInventorySlotContents(0, null);
				}

				tile.onInventoryChanged();
			}

		}
	}

	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer player, int hitSide, float hitX, float hitY, float hitZ)
	{
		TileEntity te = world.getBlockTileEntity(x, y, z);

		if (te instanceof TileMillstone)
		{
			TileMillstone tile = (TileMillstone) te;
			ItemStack current = player.inventory.getCurrentItem();

			ItemStack output = tile.getStackInSlot(0);

			if (output != null)
			{
				InventoryUtility.dropItemStack(world, new Vector3(player), output, 0);
				tile.setInventorySlotContents(0, null);
			}
			else if (current != null && tile.isItemValidForSlot(0, current))
			{
				tile.setInventorySlotContents(0, current);
				player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
			}

			world.markBlockForUpdate(x, y, z);
		}

		return false;
	}

	@Override
	public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity te = world.getBlockTileEntity(x, y, z);

		if (te instanceof TileMillstone)
		{
			TileMillstone tile = (TileMillstone) te;
			ItemStack output = tile.getStackInSlot(0);

			if (output != null)
			{
				tile.doGrind(new Vector3(entityPlayer));
				entityPlayer.addExhaustion(0.3f);
				return true;
			}
		}

		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileMillstone();
	}
}