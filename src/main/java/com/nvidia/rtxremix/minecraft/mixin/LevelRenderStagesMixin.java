/*
 * SPDX-FileCopyrightText: Copyright (c) 2024-2026 NVIDIA CORPORATION & AFFILIATES. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.nvidia.rtxremix.minecraft.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nvidia.rtxremix.minecraft.render.WorldRenderStages;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires {@link WorldRenderStages} from inside {@code renderLevel}, at the
 * points Fabric's world-render events used, so the mod's frame bookkeeping and
 * profiler phases keep their boundaries on NeoForge.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRenderStagesMixin {

    @Shadow
    private ClientLevel level;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void rtxremix$stageStart(DeltaTracker tickCounter, boolean renderBlockOutline, Camera camera,
                                     GameRenderer gameRenderer, LightTexture lightTexture,
                                     Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        WorldRenderStages.start((LevelRenderer) (Object) this, tickCounter, camera, gameRenderer, level);
    }

    @Inject(method = "renderLevel",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/LevelRenderer;setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V",
                     shift = At.Shift.AFTER))
    private void rtxremix$stageAfterSetup(CallbackInfo ci) {
        WorldRenderStages.afterSetup();
    }

    @Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=entities", ordinal = 0))
    private void rtxremix$stageBeforeEntities(CallbackInfo ci) {
        WorldRenderStages.beforeEntities();
    }

    @Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=blockentities", ordinal = 0))
    private void rtxremix$stageAfterEntities(CallbackInfo ci) {
        WorldRenderStages.afterEntities();
    }

    // The second hitResult read is the outline's, after the destroy-progress overlays.
    @Inject(method = "renderLevel",
            at = @At(value = "FIELD",
                     target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;",
                     ordinal = 1,
                     shift = At.Shift.AFTER))
    private void rtxremix$stageBeforeBlockOutline(CallbackInfo ci) {
        WorldRenderStages.beforeBlockOutline();
    }

    @Inject(method = "renderHitOutline", at = @At("HEAD"))
    private void rtxremix$stageBlockOutline(PoseStack matrices, VertexConsumer vertexConsumer, Entity entity,
                                            double cameraX, double cameraY, double cameraZ,
                                            BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        WorldRenderStages.blockOutline(entity, cameraX, cameraY, cameraZ, blockPos, blockState);
    }

    @Inject(method = "renderLevel",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDD)V",
                     ordinal = 0))
    private void rtxremix$stageBeforeDebugRender(CallbackInfo ci) {
        WorldRenderStages.beforeDebugRender();
    }

    // Translucent terrain and particles are done; clouds and weather follow.
    @Inject(method = "renderLevel",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/Options;getCloudsType()Lnet/minecraft/client/CloudStatus;",
                     ordinal = 0))
    private void rtxremix$stageAfterTranslucent(CallbackInfo ci) {
        WorldRenderStages.afterTranslucent();
    }

    @Inject(method = "renderLevel",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/LevelRenderer;renderDebug(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/Camera;)V",
                     ordinal = 0))
    private void rtxremix$stageLast(CallbackInfo ci) {
        WorldRenderStages.last();
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void rtxremix$stageEnd(CallbackInfo ci) {
        WorldRenderStages.end();
    }
}
