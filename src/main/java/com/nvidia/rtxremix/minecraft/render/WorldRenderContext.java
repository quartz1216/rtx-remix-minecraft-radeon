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
 * The arguments of the {@code LevelRenderer.renderLevel} call in progress, as
 * {@link WorldRenderStages} hands them to each stage. One instance is reused
 * for every frame, so a stage must not keep it past its own call. Render thread
 * only.
 */
public final class WorldRenderContext {
    private LevelRenderer worldRenderer;
    private DeltaTracker tickCounter;
    private Camera camera;
    private GameRenderer gameRenderer;
    private ClientLevel world;

    void set(LevelRenderer worldRenderer, DeltaTracker tickCounter, Camera camera,
             GameRenderer gameRenderer, ClientLevel world) {
        this.worldRenderer = worldRenderer;
        this.tickCounter = tickCounter;
        this.camera = camera;
        this.gameRenderer = gameRenderer;
        this.world = world;
    }

    void clear() {
        set(null, null, null, null, null);
    }

    public LevelRenderer worldRenderer() {
        return worldRenderer;
    }

    public DeltaTracker tickCounter() {
        return tickCounter;
    }

    public Camera camera() {
        return camera;
    }

    public GameRenderer gameRenderer() {
        return gameRenderer;
    }

    public ClientLevel world() {
        return world;
    }

    /**
     * The targeted block vanilla is about to outline, with the camera position
     * it draws the outline relative to. Reused like its enclosing context.
     */
    public static final class BlockOutlineContext {
        private Entity entity;
        private double cameraX;
        private double cameraY;
        private double cameraZ;
        private BlockPos blockPos;
        private BlockState blockState;

        void set(Entity entity, double cameraX, double cameraY, double cameraZ,
                 BlockPos blockPos, BlockState blockState) {
            this.entity = entity;
            this.cameraX = cameraX;
            this.cameraY = cameraY;
            this.cameraZ = cameraZ;
            this.blockPos = blockPos;
            this.blockState = blockState;
        }

        public Entity entity() {
            return entity;
        }

        public double cameraX() {
            return cameraX;
        }

        public double cameraY() {
            return cameraY;
        }

        public double cameraZ() {
            return cameraZ;
        }

        public BlockPos blockPos() {
            return blockPos;
        }

        public BlockState blockState() {
            return blockState;
        }
    }
}
