package com.builtbroken.minecraft.tilenetwork.prefab;

import net.minecraft.tileentity.TileEntity;

import com.builtbroken.minecraft.interfaces.IPowerLess;
import com.builtbroken.minecraft.tilenetwork.INetworkEnergyPart;
import com.builtbroken.minecraft.tilenetwork.INetworkPart;

/** Used for tile networks that only need to share power or act like a group battery that doesn't
 * store power on world save
 * 
 * @author DarkGuardsman */
public class NetworkSharedPower extends NetworkTileEntities implements IPowerLess
{
    private long energy, energyMax;
    private boolean runPowerLess;

    public NetworkSharedPower(INetworkPart... parts)
    {
        super(parts);
    }

    @Override
    public boolean isValidMember(INetworkPart part)
    {
        return super.isValidMember(part) && part instanceof INetworkEnergyPart;
    }

    public long addPower(TileEntity entity, long receive, boolean doReceive)
    {
        if (this.networkMembers.contains(entity) && !this.runPowerLess() && receive > 0 && this.runPowerLess)
        {
            long prevEnergyStored = this.getEnergy();
            long newStoredEnergy = Math.min(this.getEnergy() + receive, this.getEnergyCapacity());

            if (doReceive)
            {
                this.setEnergy(newStoredEnergy);
            }

            return Math.max(newStoredEnergy - prevEnergyStored, 0);
        }
        return 0;
    }

    public long removePower(TileEntity entity, long request, boolean doExtract)
    {
        if (this.networkMembers.contains(entity) && request > 0)
        {
            long requestedEnergy = Math.min(request, this.getEnergy());
            if (doExtract)
            {
                this.setEnergy(this.getEnergy() - requestedEnergy);
            }
            return requestedEnergy;
        }
        return 0;
    }

    @Override
    public void cleanUpMembers()
    {
        super.cleanUpMembers();
        boolean set = false;
        this.energyMax = 0;
        for (INetworkPart part : this.networkMembers)
        {
            if (!set && part instanceof IPowerLess && ((IPowerLess) part).runPowerLess())
            {
                this.setPowerLess(((IPowerLess) part).runPowerLess());
                set = true;
            }
            if (part instanceof INetworkEnergyPart)
            {
                this.energyMax += ((INetworkEnergyPart) part).getPartMaxEnergy();
            }
        }

    }

    @Override
    public boolean runPowerLess()
    {
        return this.runPowerLess;
    }

    @Override
    public void setPowerLess(boolean bool)
    {
        this.runPowerLess = bool;
        for (INetworkPart part : this.networkMembers)
        {
            if (part instanceof IPowerLess)
            {
                ((IPowerLess) part).setPowerLess(bool);
            }

        }
    }

    public void setEnergy(long energy)
    {
        this.energy = energy;
        if (this.energy > this.getEnergyCapacity())
        {
            this.energy = this.getEnergyCapacity();
        }
    }

    public long getEnergy()
    {
        if (this.energy < 0)
        {
            this.energy = 0;
        }
        return this.energy;
    }

    public long getEnergyCapacity()
    {
        if (this.energyMax < 0)
        {
            this.energyMax = Math.abs(this.energyMax);
        }
        return this.energyMax;
    }

    /** Space left to store more energy */
    public float getEnergySpace()
    {
        return Math.max(this.getEnergyCapacity() - this.getEnergy(), 0);
    }

    @Override
    public void save()
    {
        this.cleanUpMembers();
        long energyRemaining = this.getEnergy();
        for (INetworkPart part : this.getMembers())
        {
            long watts = energyRemaining / this.getMembers().size();
            if (part instanceof INetworkEnergyPart)
            {
                ((INetworkEnergyPart) part).setPartEnergy(Math.min(watts, ((INetworkEnergyPart) part).getPartMaxEnergy()));
                energyRemaining -= Math.min(((INetworkEnergyPart) part).getPartEnergy(), ((INetworkEnergyPart) part).getPartMaxEnergy());
            }
        }
    }

    @Override
    public void load()
    {
        this.setEnergy(0);
        this.cleanUpMembers();
        for (INetworkPart part : this.getMembers())
        {
            if (part instanceof INetworkEnergyPart)
            {
                this.setEnergy(this.getEnergy() + ((INetworkEnergyPart) part).getPartEnergy());
            }
        }
    }

}
