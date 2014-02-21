package resonantinduction.mechanical.turbine;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import calclavia.lib.prefab.turbine.TileTurbine;
import calclavia.lib.render.RenderUtility;
import calclavia.lib.render.item.ISimpleItemRenderer;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderWaterTurbine extends TileEntitySpecialRenderer implements ISimpleItemRenderer
{
	public static final IModelCustom MODEL = AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "waterTurbines.obj");

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		TileTurbine tile = (TileTurbine) t;

		if (tile.getMultiBlock().isPrimary())
		{
			GL11.glPushMatrix();
			GL11.glTranslatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);
			GL11.glPushMatrix();

			RenderUtility.rotateBlockBasedOnDirectionUp(tile.getDirection());
			GL11.glRotatef((float) Math.toDegrees(tile.rotation), 0, 1, 0);

			if (tile.getDirection().offsetY != 0)
				renderWaterTurbine(tile.getMultiBlock().isConstructed());
			else
				renderWaterWheel(tile.getMultiBlock().isConstructed());

			GL11.glPopMatrix();
			GL11.glPopMatrix();
		}
	}

	public void renderWaterWheel(boolean isLarge)
	{
		if (isLarge)
		{
			GL11.glPushMatrix();
			GL11.glScalef(1, 1.6f, 1);
			RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "cobblestone.png");
			MODEL.renderOnly("Beamknot", "wheel_shaft");
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glScalef(1, 1.4f, 1);
			RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "planks_spruce.png");
			MODEL.renderOnly("BaseSupporter");
			RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "planks_oak.png");
			MODEL.renderOnly("Scoops", "SupporterCircle");
			GL11.glPopMatrix();

		}
		else
		{
			RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "cobblestone.png");
			MODEL.renderOnly("turbine_centre");
		}
	}

	public void renderWaterTurbine(boolean isLarge)
	{
		if (isLarge)
		{
			RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "cobblestone.png");
			MODEL.renderOnly("turbine_centre");
			RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "iron_block.png");
			MODEL.renderOnly("turbine_blades");

		}
		else
		{
			RenderUtility.bind(Reference.BLOCK_TEXTURE_DIRECTORY + "cobblestone.png");
			MODEL.renderOnly("turbine_centre");
		}
	}

	@Override
	public void renderInventoryItem(ItemStack itemStack)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float) 0.5f, (float) 0.5f, (float) 0.5f);
		renderWaterWheel(false);
		GL11.glPopMatrix();

	}

}