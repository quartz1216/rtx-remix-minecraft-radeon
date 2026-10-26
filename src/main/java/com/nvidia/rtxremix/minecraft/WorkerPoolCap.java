/*
 * SPDX-FileCopyrightText: Copyright (c) 2024-2026 NVIDIA CORPORATION & AFFILIATES. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.nvidia.rtxremix.minecraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bounds Minecraft's shared background worker pool.
 *
 * <p>{@code net.minecraft.Util} sizes its {@code Worker-Main} pool to the
 * logical CPU count minus one, capped by the {@code max.bg.threads} system
 * property. Every section build, chunk load, lighting update and resource reload
 * runs there, so on a 32-thread machine a chunk-boundary crossing puts 31
 * threads to work at once, and with Remix capture inline in every section build
 * that wave contends with the render thread and the dxvk CS thread for every
 * core: the frame it lands in inflates by tens of milliseconds. The same work
 * needs about two cores on average, so a small static pool keeps throughput and
 * removes the contention.
 *
 * <p>The pool is created in a static initialiser, and NeoForge constructs mods
 * after that class has loaded, so the mod cannot set the property itself. The
 * installer's recommended JVM arguments and the development run configuration
 * pass {@code -Dmax.bg.threads} instead, and a launcher value is left alone;
 * {@link #checkApplied()} only reports what the pool got.
 */
public final class WorkerPoolCap {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerPoolCap.class);
    static final String PROPERTY = "max.bg.threads";
    static final int MAX_THREADS = 8;

    private WorkerPoolCap() {
    }

    /** Logs whether the launcher capped the pool, and the value to pass if not. */
    public static void checkApplied() {
        String value = System.getProperty(PROPERTY);
        if (value != null) {
            LOGGER.info("Background worker pool: {}={} set by the launcher", PROPERTY, value);
            return;
        }
        // Vanilla already clamps the pool to the CPU count minus one, so the cap
        // alone gives every machine choose(availableProcessors).
        LOGGER.warn("Background worker pool is uncapped ({} logical CPUs). Add -D{}={} to the"
                        + " JVM arguments to keep chunk-build waves from stalling frames.",
                Runtime.getRuntime().availableProcessors(), PROPERTY, MAX_THREADS);
    }

    /**
     * Mirrors vanilla's own choice ({@code availableProcessors - 1}, at least 1)
     * up to {@link #MAX_THREADS}.
     */
    static int choose(int availableProcessors) {
        return Math.max(1, Math.min(MAX_THREADS, availableProcessors - 1));
    }
}
