package fr.zeamateis.nuwa.contentpack.common.json.data.events.processes.base;


import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public interface IEntityProcess extends IProcess {

    void process(World worldIn, Entity entityIn);
}