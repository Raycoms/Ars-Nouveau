package com.hollingsworth.arsnouveau.client.renderer.tile;

import com.hollingsworth.arsnouveau.client.particle.GlowParticleData;
import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import com.hollingsworth.arsnouveau.client.particle.ParticleUtil;
import com.hollingsworth.arsnouveau.client.renderer.item.GenericItemRenderer;
import com.hollingsworth.arsnouveau.common.block.tile.MycelialSourcelinkTile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

import java.util.Random;

public class MycelialRenderer extends GeoBlockRenderer<MycelialSourcelinkTile> {
    public static SourcelinkModel model =  new SourcelinkModel("mycelial");

    public MycelialRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn, model);
    }


    @Override
    public void renderLate(MycelialSourcelinkTile animatable, MatrixStack stackIn, float ticks, IRenderTypeBuffer renderTypeBuffer, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float partialTicks) {
        World world = animatable.getLevel();
        BlockPos pos = animatable.getBlockPos();
        Random rand = world.random;
        if(Minecraft.getInstance().isPaused())
            return;
        for(int i = 0; i < 1; i++){
            world.addParticle(
                    GlowParticleData.createData(new ParticleColor(
                            rand.nextInt(255),
                            rand.nextInt(255),
                            rand.nextInt(255)
                    )),
                    pos.getX() +0.5  , pos.getY() +0.3  + ParticleUtil.inRange(-0.1, 0.35) , pos.getZ() +0.5 ,
                    0,0,0);;
        }
    }
    public static GenericItemRenderer getISTER(){
        return new GenericItemRenderer(model);
    }
}