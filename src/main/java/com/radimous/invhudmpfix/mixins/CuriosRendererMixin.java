package com.radimous.invhudmpfix.mixins;

import dlovin.inventoryhud.InventoryHUD;
import dlovin.inventoryhud.gui.renderers.CuriosRenderer;
import dlovin.inventoryhud.utils.CuriosIconUtils;
import dlovin.inventoryhud.utils.CuriosSaveUtils;
import dlovin.inventoryhud.utils.CuriosSlot;
import dlovin.inventoryhud.utils.WidgetAligns;
import dlovin.inventoryhud.utils.WidgetAligns.HAlign;
import dlovin.inventoryhud.utils.WidgetAligns.VAlign;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;

@Mixin(CuriosRenderer.class)
public abstract class CuriosRendererMixin {
    private long last = 0;
    private int tries = 0;

    @Shadow
    private HashMap<String, CuriosSlot> slots;

    @Shadow public abstract HashMap<String, CuriosSlot> slots();

    /**
     * @author radimous
     * @reason replaces slotHelper usage with curiosHelper, because slotHelper is not intended to be used on client and is null in MP
     */
    @Overwrite
    public void setupTrinkets() {
        this.tries++;
        this.slots = new HashMap<>();
        int[] i = {0};
        CuriosApi.getCuriosHelper()
            .getCuriosHandler(Minecraft.getInstance().player).resolve()
            .ifPresent(curiosItemHandler ->
                curiosItemHandler.getCurios().values()
                    .forEach(slot -> {
                        for (int j = 0; j < slot.getSlots(); ++j) {
                            String slotId = slot.getIdentifier();
                            if (j > 0) {
                                slotId = slotId + "_" + (j + 1);
                            }

                            this.slots.put(
                                slotId,
                                new CuriosSlot(
                                    0,
                                    i[0] += 20,
                                    new WidgetAligns(HAlign.LEFT, VAlign.TOP),
                                    false,
                                    CuriosIconUtils.getRealResourceLocation(slot.getIdentifier(), CuriosApi.getIconHelper().getIcon(slot.getIdentifier()))
                                )
                            );
                        }
                    })
            );
        if (this.slots.size() == 0) {
            InventoryHUD.log("NO CURIOS LOADED");
        } else {
            CuriosSaveUtils.sync();
            InventoryHUD.log(String.format("Curios has been initialized with %d slot(s) after %d tries", this.slots.size(), this.tries));
        }
    }
    // tries to get curios again
    // ICuriosItemHandler#getCurios returns empty map when server fails to send curio slots (bad connection) or when server has 0 curios
    // the former is why it's needed, the latter is the reason why it's capped to 100 tries
    @Inject(method = "isEmpty", at = @At("HEAD"))
    public void isEmpty(CallbackInfoReturnable<Boolean> cir) {
        if (slots().isEmpty() && tries < 100 && System.currentTimeMillis() - last > 3000) {
            setupTrinkets();
            last = System.currentTimeMillis();
        }
    }

    @Inject(method = "disable", at = @At("HEAD"))
    public void resetTries(CallbackInfo ci) {
        tries = 0;
    }

}
