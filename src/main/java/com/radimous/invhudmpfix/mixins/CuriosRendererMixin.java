package com.radimous.invhudmpfix.mixins;

import com.radimous.invhudmpfix.InvHudMPFix;
import dlovin.inventoryhud.gui.renderers.CuriosRenderer;
import dlovin.inventoryhud.utils.CuriosSlot;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.slottype.SlotType;
import top.theillusivec4.curios.server.SlotHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mixin(CuriosRenderer.class)
public abstract class CuriosRendererMixin {
    private long last = 0;
    private int tries = 0;

    @Shadow
    public abstract HashMap<String, CuriosSlot> slots();

    @Shadow
    public abstract void setupTrinkets();

    @Inject(method = "setupTrinkets", at = @At("HEAD"))
    public void initCurios(CallbackInfo ci) {
        tries++;
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
            if(tries == 100  && CuriosApi.getSlotHelper().getSlotTypes().size() == 0){
                pl.sendMessage(new TextComponent("[InvHudMPFix] Curio slots failed to load 100 times, remove this mod if your server doesn't support curios."), Util.NIL_UUID);
            }
        }
        if (CuriosApi.getSlotHelper().getSlotTypes().size() != 0) {
            InvHudMPFix.logger.info("Loading curios took " + tries + " tries.");
        }
    }

    @Inject(method = "isEmpty", at = @At("HEAD"))
    public void isEmpty(CallbackInfoReturnable<Boolean> cir) {
        if (slots().isEmpty() && tries < 100 && System.currentTimeMillis() - last > 3000) {
            setupTrinkets();
            last = System.currentTimeMillis();
        }
    }
    @Inject(method = "disable", at = @At("HEAD"))
    public void resetTries(CallbackInfo ci){
        tries = 0;
    }

}
