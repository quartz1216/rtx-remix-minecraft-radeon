/*
 * SPDX-FileCopyrightText: Copyright (c) 2024-2026 NVIDIA CORPORATION & AFFILIATES. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.nvidia.rtxremix.minecraft.mixin;

import com.mojang.blaze3d.platform.Window;
import com.nvidia.rtxremix.minecraft.RtxRemixMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.glfwGetWindowAttrib;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;

/**
 * Asks for the OpenGL context the Remix GL interop needs, and checks the one
 * the window got.
 *
 * <p>NeoForge usually hands {@code Window} the early loading window instead
 * of creating one, and that window already takes the highest core context the
 * driver offers, so the hints below only apply when the early window is off
 * and NeoForge creates the window here.
 */
@Mixin(Window.class)
public abstract class WindowMixin {

    // Core-profile 4.4: the GL interop imports Remix's images and semaphores
    // through EXT_memory_object and EXT_semaphore.
    private static final int RTXREMIX$GL_MAJOR = 4;
    private static final int RTXREMIX$GL_MINOR = 4;

    @Shadow
    private long window;

    @Inject(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/neoforged/fml/loading/ImmediateWindowHandler;setupMinecraftWindow(Ljava/util/function/IntSupplier;Ljava/util/function/IntSupplier;Ljava/util/function/Supplier;Ljava/util/function/LongSupplier;)J"
        )
    )
    private void rtxremix$requireOpenGl44(CallbackInfo ci) {
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, RTXREMIX$GL_MAJOR);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, RTXREMIX$GL_MINOR);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    }

    /** Logs the GLFW window handle and its context version once the window exists. */
    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void rtxremix$onWindowCreated(CallbackInfo ci) {
        int major = glfwGetWindowAttrib(window, GLFW_CONTEXT_VERSION_MAJOR);
        int minor = glfwGetWindowAttrib(window, GLFW_CONTEXT_VERSION_MINOR);
        RtxRemixMod.LOGGER.info("Window created with GLFW handle: {} (OpenGL {}.{})", window, major, minor);
        if (major < RTXREMIX$GL_MAJOR || (major == RTXREMIX$GL_MAJOR && minor < RTXREMIX$GL_MINOR)) {
            RtxRemixMod.LOGGER.error("The window's OpenGL {}.{} context is older than the {}.{} the Remix"
                            + " GL interop needs; Remix frames will fail to composite.",
                    major, minor, RTXREMIX$GL_MAJOR, RTXREMIX$GL_MINOR);
        }
    }
}
