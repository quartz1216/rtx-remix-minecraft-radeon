/*
 * SPDX-FileCopyrightText: Copyright (c) 2024-2026 NVIDIA CORPORATION & AFFILIATES. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.nvidia.rtxremix.minecraft.render;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The points inside {@code LevelRenderer.renderLevel} the mod needs to act at.
 *
 * <p>NeoForge's {@code RenderLevelStageEvent} has no stage at the start or end
 * of the world render, after terrain setup, before entities, or at the block
 * outline itself, so {@code LevelRenderStagesMixin} fires these at the points
 * Fabric's world-render events used. One listener, the mod, receives them.
 * Render thread only.
 */
public final class WorldRenderStages {

    /** Receives each stage of a world render, in the order listed. */
    public interface Listener {
        void start(WorldRenderContext context);

        void afterSetup(WorldRenderContext context);

        void beforeEntities(WorldRenderContext context);

        void afterEntities(WorldRenderContext context);

        void beforeBlockOutline(WorldRenderContext context);

        /** Fired only when vanilla draws a block outline this frame. */
        void blockOutline(WorldRenderContext context, WorldRenderContext.BlockOutlineContext outline);

        void beforeDebugRender(WorldRenderContext context);

        void afterTranslucent(WorldRenderContext context);

        void last(WorldRenderContext context);

        void end(WorldRenderContext context);
    }

    private static final WorldRenderContext CONTEXT = new WorldRenderContext();
    private static final WorldRenderContext.BlockOutlineContext OUTLINE =
            new WorldRenderContext.BlockOutlineContext();
    private static Listener listener;
    /** Whether a world render is between {@link #start} and {@link #end}. */
    private static boolean active;

    private WorldRenderStages() {
    }

    public static void setListener(Listener newListener) {
        listener = newListener;
    }

    public static void start(LevelRenderer worldRenderer, DeltaTracker tickCounter, Camera camera,
                             GameRenderer gameRenderer, ClientLevel world) {
        CONTEXT.set(worldRenderer, tickCounter, camera, gameRenderer, world);
        active = true;
        if (listener != null) listener.start(CONTEXT);
    }

    public static void afterSetup() {
        if (active && listener != null) listener.afterSetup(CONTEXT);
    }

    public static void beforeEntities() {
        if (active && listener != null) listener.beforeEntities(CONTEXT);
    }

    public static void afterEntities() {
        if (active && listener != null) listener.afterEntities(CONTEXT);
    }

    public static void beforeBlockOutline() {
        if (active && listener != null) listener.beforeBlockOutline(CONTEXT);
    }

    public static void blockOutline(Entity entity, double cameraX, double cameraY, double cameraZ,
                                    BlockPos blockPos, BlockState blockState) {
        if (!active || listener == null) return;
        OUTLINE.set(entity, cameraX, cameraY, cameraZ, blockPos, blockState);
        try {
            listener.blockOutline(CONTEXT, OUTLINE);
        } finally {
            OUTLINE.set(null, 0.0, 0.0, 0.0, null, null);
        }
    }

    public static void beforeDebugRender() {
        if (active && listener != null) listener.beforeDebugRender(CONTEXT);
    }

    public static void afterTranslucent() {
        if (active && listener != null) listener.afterTranslucent(CONTEXT);
    }

    public static void last() {
        if (active && listener != null) listener.last(CONTEXT);
    }

    public static void end() {
        if (!active) return;
        try {
            if (listener != null) listener.end(CONTEXT);
        } finally {
            active = false;
            CONTEXT.clear();
        }
    }
}
