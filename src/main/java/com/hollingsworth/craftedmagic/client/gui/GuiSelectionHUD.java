package com.hollingsworth.craftedmagic.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuiSelectionHUD extends AbstractGui implements IGuiEventListener {



//    @SubscribeEvent
//    public static void overlayEvent(RenderGameOverlayEvent.Pre event) {
//        if (Minecraft.getInstance().currentScreen instanceof GuiRadialMenu) {
//            if (event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
//                event.setCanceled(true);
//            }
//        }
//    }
}
