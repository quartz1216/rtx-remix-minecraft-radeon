/*
 * SPDX-FileCopyrightText: Copyright (c) 2024-2026 NVIDIA CORPORATION & AFFILIATES. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.nvidia.rtxremix.minecraft.profiling;

/**
 * Lets the mod close {@code WorldRendererMixin}'s open profiler phase from
 * outside the mixin, so a world-render stage event that takes over the rest of
 * the frame is not billed to whichever vanilla phase happened to be open.
 * Render thread only.
 */
public interface WorldRendererProfilingAccess {
    void rtxremix$closeProfilerPhaseForStageEvent();
}
