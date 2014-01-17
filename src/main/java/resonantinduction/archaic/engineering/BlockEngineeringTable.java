package resonantinduction.archaic.engineering;

import java.util.Random;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import resonantinduction.core.prefab.block.BlockRI;
import universalelectricity.api.vector.Vector2;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.inventory.InventoryUtility;
import codechicken.multipart.ControlKeyModifer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A world-based crafting table.
 * 
 * TODO: Filter support, inventory seek support.
 * 
 * @author Calclavia
 */
public class BlockEngineeringTable extends BlockRI
{
	@SideOnly(Side.CLIENT)
	private Icon iconTop;
	@SideOnly(Side.CLIENT)
	private Icon iconFront;

	public BlockEngineeringTable()
	{
		super("engineeringTable");
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
	{
		if (!world.isRemote)
		{
			dropEntireInventory(world, x, y, z, 0, 0);
		}
	}

	@Override
	public void dropEntireInventory(World world, int x, int y, int z, int par5, int par6)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity != null)
		{
			if (tileEntity instanceof IInventory)
			{
				IInventory inventory = (IInventory) tileEntity;

				// PREVENTS OUTPUT FROM DROPPING!
				for (int var6 = 0; var6 < inventory.getSizeInventory() - 1; ++var6)
				{
					ItemStack var7 = inventory.getStackInSlot(var6);

					if (var7 != null)
					{
						Random random = new Random();
						float var8 = random.nextFloat() * 0.8F + 0.1F;
						float var9 = random.nextFloat() * 0.8F + 0.1F;
						float var10 = random.nextFloat() * 0.8F + 0.1F;

						while (var7.stackSize > 0)
						{
							int var11 = random.nextInt(21) + 10;

							if (var11 > var7.stackSize)
							{
								var11 = var7.stackSize;
							}

							var7.stackSize -= var11;
							EntityItem var12 = new EntityItem(world, (x + var8), (y + var9), (z + var10), new ItemStack(var7.itemID, var11, var7.getItemDamage()));

							if (var7.hasTagCompound())
							{
								var12.getEntityItem().setTagCompound((NBTTagCompound) var7.getTagCompound().copy());
							}

							float var13 = 0.05F;
							var12.motionX = ((float) random.nextGaussian() * var13);
							var12.motionY = ((float) random.nextGaussian() * var13 + 0.2F);
							var12.motionZ = ((float) random.nextGaussian() * var13);
							world.spawnEntityInWorld(var12);
						}
					}
				}

				inventory.onInventoryChanged();
			}
		}
	}

	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer player, int hitSide, float hitX, float hitY, float hitZ)
	{
		TileEntity te = world.getBlockTileEntity(x, y, z);

		if (te instanceof TileEngineeringTable)
		{
			TileEngineeringTable tile = (TileEngineeringTable) te;

			if (hitSide == 1)
			{
				if (!world.isRemote)
				{
					ItemStack current = player.inventory.getCurrentItem();

					Vector2 hitVector = new Vector2(hitX, hitZ);
					double regionLength = 1d / 3d;

					/**
					 * Crafting Matrix
					 */
					matrix:
					for (int j = 0; j < 3; j++)
					{
						for (int k = 0; k < 3; k++)
						{
							Vector2 check = new Vector2(j, k).scale(regionLength);

							if (check.distance(hitVector) < regionLength)
							{
								int slotID = j * 3 + k;
								interactCurrentItem(tile, slotID, player);
								break matrix;
							}
						}
					}

					tile.onInventoryChanged();
				}

				return true;
			}
			else if (hitSide != 0)
			{
				if (!world.isRemote)
				{
					tile.setPlayerInventory(player.inventory);

					ItemStack output = tile.getStackInSlot(9);
					boolean firstLoop = true;

					while (output != null && (firstLoop || ControlKeyModifer.isControlDown(player)))
					{
						InventoryUtility.dropItemStack(world, new Vector3(player), output, 0);

						tile.onPickUpFromSlot(player, 9, output);
						tile.setInventorySlotContents(9, null);
						tile.onInventoryChanged();

						output = tile.getStackInSlot(9);
						firstLoop = false;
					}

					tile.setPlayerInventory(null);
				}
			}
		}

		return false;
	}

	@Override
	public boolean onUseWrench(World worldorld, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = worldorld.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileEngineeringTable)
		{
			TileEngineeringTable tile = (TileEngineeringTable) tileEntity;
			tile.searchInventories = !tile.searchInventories;
			worldorld.markBlockForUpdate(x, y, z);
			return true;
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int par1, int par2)
	{
		return par1 == 1 ? this.iconTop : (par1 == 0 ? this.blockIcon : (par1 != 2 && par1 != 4 ? this.blockIcon : this.iconFront));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon(this.getTextureName() + "_side");
		this.iconTop = par1IconRegister.registerIcon(this.getTextureName() + "_top");
		this.iconFront = par1IconRegister.registerIcon(this.getTextureName() + "_front");
	}

	@Override
	public boolean isControlDown(EntityPlayer player)
	{
		return ControlKeyModifer.isControlDown(player);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEngineeringTable();
	}
}
