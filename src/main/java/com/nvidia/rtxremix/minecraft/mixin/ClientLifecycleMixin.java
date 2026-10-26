/*
 * SPDX-FileCopyrightText: Copyright (c) 2024-2026 NVIDIA CORPORATION & AFFILIATES. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.nvidia.rtxremix.minecraft.mixin;

import com.nvidia.rtxremix.minecraft.RtxRemixMod;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client start and stop, at the points Fabric's client lifecycle events used.
 * NeoForge has no event for the moment the render loop is about to begin: the
 * window and GL context exist and the first resource load is underway.
 */
@Mixin(Minecraft.class)
public abstract class ClientLifecycleMixin {

    @Inject(method = "run",
            at = @At(value = "FIELD",
                     target = "Lnet/minecraft/client/Minecraft;gameThread:Ljava/lang/Thread;",
                     shift = At.Shift.AFTER,
                     ordinal = 0))
    private void rtxremix$onClientStarted(CallbackInfo ci) {
        RtxRemixMod.onClientStarted((Minecraft) (Object) this);
    }

    @Inject(method = "destroy", at = @At("HEAD"))
    private void rtxremix$onClientStopping(CallbackInfo ci) {
        RtxRemixMod.shutdownForClientStop();
    }
}
