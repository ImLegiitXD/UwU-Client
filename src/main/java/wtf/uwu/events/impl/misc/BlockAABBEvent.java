package wtf.uwu.events.impl.misc;

import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import wtf.uwu.events.impl.CancellableEvent;

@Getter
public class BlockAABBEvent extends CancellableEvent {

    private final World world;
    private final Block block;
    private final BlockPos blockPos;
    private final AxisAlignedBB maskBoundingBox;
    public AxisAlignedBB boundingBox;

    public BlockAABBEvent(World world, Block block, BlockPos blockPos, AxisAlignedBB boundingBox, AxisAlignedBB maskBoundingBox) {
        this.world = world;
        this.block = block;
        this.blockPos = blockPos;
        this.boundingBox = boundingBox;
        this.maskBoundingBox = maskBoundingBox;
    }

    public Block getBlock() {
        return block;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(AxisAlignedBB boundingBox) {
        this.boundingBox = boundingBox;
    }
}