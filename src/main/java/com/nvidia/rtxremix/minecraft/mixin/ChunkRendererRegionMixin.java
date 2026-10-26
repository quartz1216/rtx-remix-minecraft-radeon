/*
 * SPDX-FileCopyrightText: Copyright (c) 2024-2026 NVIDIA CORPORATION & AFFILIATES. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.nvidia.rtxremix.minecraft.mixin;

import com.nvidia.rtxremix.minecraft.terrain.capture.RegionSnapshotRelease;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.world.level.Level;

/**
 * A render region holds one reference on each of its nine chunk snapshots from construction
 * until {@link #rtxremix$release}, which the rebuild task that consumed it calls exactly once;
 * see {@link RebuildTaskMixin}.
 */
@Mixin(RenderChunkRegion.class)
public abstract class ChunkRendererRegionMixin implements RegionSnapshotRelease {

    @Shadow
    @Final
    protected RenderChunk[] chunks;

    private final AtomicBoolean rtxremix$released = new AtomicBoolean();

    // NeoForge's four-argument constructor delegates here, so this one sees every region once.
    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;II[Lnet/minecraft/client/renderer/chunk/RenderChunk;Lit/unimi/dsi/fastutil/longs/Long2ObjectFunction;)V",
            at = @At("RETURN"))
    private void rtxremix$retainSnapshots(Level world, int chunkX, int chunkZ, RenderChunk[] renderedChunks,
                                          Long2ObjectFunction<ModelData> modelDataSnapshot, CallbackInfo ci) {
        for (RenderChunk snapshot : chunks) {
            ((RegionSnapshotRelease) snapshot).rtxremix$retain();
        }
    }

    @Override
    public void rtxremix$retain() {
        throw new UnsupportedOperationException("regions are held by exactly one rebuild task");
    }

    @Override
    public void rtxremix$release() {
        if (!rtxremix$released.compareAndSet(false, true)) {
            return;
        }
        for (RenderChunk snapshot : chunks) {
            ((RegionSnapshotRelease) snapshot).rtxremix$release();
        }
    }
}
