package resonantinduction.atomic.fusion;

import java.util.ArrayList;

import javax.security.auth.login.Configuration;
import javax.security.auth.login.Configuration.Parameters;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import resonant.api.event.PlasmaEvent.SpawnPlasmaEvent;
import resonant.lib.config.Config;
import resonant.lib.content.module.TileBase;
import resonant.lib.prefab.vector.Cuboid;
import resonant.lib.thermal.ThermalGrid;
import resonantinduction.core.Settings;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;

public class TilePlasma extends TileBase
{
    @Config
    public static int plasmaMaxTemperature = 10000000; // initial changed to 10M K
    private float temperature = plasmaMaxTemperature;

    public TilePlasma()	// создание блока плазмы
    {
        super(Material.lava);
        textureName = "plasma";
        isOpaqueCube = false;
    }

    @Override
    public int getLightValue(IBlockAccess access)
    {
        return 7;
    }

    @Override
    public boolean isSolid(IBlockAccess access, int side)
    {
        return false;
    }

    @Override
    public Iterable<Cuboid> getCollisionBoxes()
    {
        return new ArrayList();
    }

    @Override
    public ArrayList<ItemStack> getDrops(int metadata, int fortune)
    {
        return new ArrayList<ItemStack>();
    }

    @Override
    public int getRenderBlockPass()
    {
        return 1;
    }

    @Override
    public void collide(Entity entity)
    {
        entity.attackEntityFrom(DamageSource.inFire, 100); // прикосновение к плазме)
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        int bonus = 0;
        int childbonus = 0;
        // матрица температур для кипячения воды
        ThermalGrid.addTemperature(new VectorWorld(this), (temperature - ThermalGrid.getTemperature(new VectorWorld(this))) * 0.1f);
        // раз в секунду
        if (ticks % 20 == 0)
        {
        	if(!worldObj.isRemote){
        		// для клиентской стороны вообще ничего не выводим
        		System.out.printf("plasm@[%d,%d,%d]:%d °K\n",xCoord,yCoord,zCoord,(int)this.temperature);
            	System.out.printf("TG:%f\n", ThermalGrid.getTemperature(new VectorWorld(this)));
        		this.setTemperature(this.getTemperature() * Settings.plasmaAutoCooling / 100);
        		System.out.printf("autocool to:%d °K\n",(int)this.temperature);
        	}
            
            if (temperature <= plasmaMaxTemperature / 100) // low limit is 100k °K
            {
            	if(!worldObj.isRemote){
            		System.out.printf("destroy entity\n");
            		worldObj.removeBlockTileEntity(xCoord, yCoord, zCoord); // верное решение! плазма пропадает насовсем
            		/* тут тоже нужна условная замена! если ПОД целевым блоком воздух, пламя или плазма - огонь не создавать!*/
            		if(worldObj.isAirBlock(xCoord, yCoord-1, zCoord)) {
            			worldObj.setBlockToAir(xCoord, yCoord, zCoord);
            			return;
            		}
                	Vector3 sub = new Vector3(this);
                	sub.translate(ForgeDirection.DOWN);
                	TileEntity teSub = sub.getTileEntity(worldObj);
                	if(teSub instanceof TilePlasma){
                		worldObj.setBlockToAir(xCoord, yCoord, zCoord);
                		return;
                	}
                	worldObj.setBlock(xCoord, yCoord, zCoord, Block.fire.blockID, 0, 3);
                    return;
            	}
            }

            for (int i = 0; i < 6; i++)	// check all 6 directions
            {
            	bonus = 0;
            	childbonus=0;
                // Randomize spread direction.
                if (worldObj.rand.nextFloat() > 0.4) // with probability of 60% 
                {
                    continue;
                }

                Vector3 diDian = new Vector3(this);
                diDian.translate(ForgeDirection.getOrientation(i));
                int bid = diDian.getBlockID(worldObj);
                TileEntity tileEntity = diDian.getTileEntity(worldObj);

                
                /* все-таки тут нужно обрабатывать запретные блоки, чтобы не половинить температуру родительской плазмы!
                 *
                 */
                int newtemp;
                if(tileEntity instanceof TilePlasma){
                	// newtemp рассчитываем исходя из температур обоих блоков плазмы
                	// также рассчитаем бонусы bonus и childbonus
                	int theytemp = ((TilePlasma)tileEntity).getTemperature();
                	if(this.temperature - theytemp > 1000){ // температура соседа ниже, произвести передачу!
                    	System.out.printf("Plasma energy exchange: left:%dK, right:%dK\n",(int)this.temperature,theytemp);                		
                		childbonus = ((int)this.temperature - theytemp) / 2;
                		bonus = -1 * childbonus;
                	}
                	else continue;             	// а если температура соседа выше - ничего ему не передаем!
                	this.setTemperature((int)(this.temperature + bonus));
                	newtemp = ((TilePlasma)tileEntity).getTemperature(); // должна остаться та же! 
                }
                else if((!isUnbreakable(bid))&&(!(tileEntity instanceof TileElectromagnet))){
                	if(this.getTemperature() > TilePlasma.plasmaMaxTemperature / 50){
                		newtemp = this.getTemperature() / 2; // only hot plasma can be didided
                		// бонус положительный при замещении пламени
                		// и отрицательный - при испарении материи
                		this.setTemperature(newtemp + bonus);
                		if(!worldObj.isRemote){
                			System.out.printf("Spawn plasma at [%d,%d,%d] with temp%d\n", diDian.intX(), diDian.intY(), diDian.intZ(), newtemp + childbonus);
                			// spawn plasma on any block, except unbreakable and magnets
                			MinecraftForge.EVENT_BUS.post(new SpawnPlasmaEvent(worldObj, diDian.intX(), diDian.intY(), diDian.intZ(),  newtemp + childbonus));
                		}
                	}
                }
            }
        }
    }

    public void setTemperature(int newTemperature)
    {
        temperature = newTemperature;
    }
    public int getTemperature(){
    	return (int)this.temperature;
    }

    public boolean isUnbreakable(int blockId){
    	if((blockId == Block.bedrock.blockID) ||
    			(blockId == Block.commandBlock.blockID) ||
    			(blockId == Block.endPortalFrame.blockID))
    		return true;
    	return false;
    }
}
