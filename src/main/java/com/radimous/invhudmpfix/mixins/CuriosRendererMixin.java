package com.radimous.invhudmpfix.mixins;

import dlovin.inventoryhud.gui.renderers.CuriosRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.slottype.SlotType;
import top.theillusivec4.curios.server.SlotHelper;

import java.util.Map;
import java.util.Optional;

@Mixin(CuriosRenderer.class)
public class CuriosRendererMixin {
    @Inject(method = "setupTrinkets", at = @At("HEAD"))
    public void initCurios(CallbackInfo ci) {
        if (CuriosApi.getSlotHelper() == null) {
            CuriosApi.setSlotHelper(new SlotHelper());
        }
        LocalPlayer pl = Minecraft.getInstance().player;
        if (pl != null) {
            Optional<ICuriosItemHandler> cap = pl.getCapability(CuriosCapability.INVENTORY).resolve();
            if (cap.isPresent()) {
                Map<String, ICurioStacksHandler> curiomap = cap.get().getCurios();
                for (ICurioStacksHandler slot : curiomap.values()) {
                    for (int i = 0; i < slot.getStacks().getSlots(); i++) {
                        String slotId = slot.getIdentifier();
                        if (i > 0) {
                            slotId = slotId + "_" + (i + 1);
                        }
                        CuriosApi.getSlotHelper().addSlotType(new SlotType.Builder(slotId).build());
                    }
                }
            }
        }

    }

}
