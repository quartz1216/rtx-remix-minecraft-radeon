/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 NVIDIA CORPORATION & AFFILIATES. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.nvidia.rtxremix.minecraft.light;

import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Analytic lights for exposed lava surfaces.
 *
 * <p>Lava's emissive texels alone barely light anything: a pool's surface sits
 * below the top of the blocks around it, so the floor beside it sees the
 * emitter edge-on and the walls catch only grazing light. A sphere light just
 * above the surface lights the surroundings the way vanilla's level-15 lava
 * does.
 *
 * <p>One light per lava block would flood the light list in a lava lake, so
 * section capture gathers exposed surfaces into {@link #CELL_SIZE}-square
 * columns of each section and asks for one light per non-empty cell, centred on
 * the cell's lava. Power grows with the square root of the cell's surface count,
 * so a lake stays bright without a single cell outshining a campfire by more
 * than a few times.
 */
public final class LavaLights {
    /** Horizontal edge of one aggregation cell, in blocks; divides the section size. */
    public static final int CELL_SIZE = 4;

    /** Rule path naming these lights, so their intensity control reads like a block rule's. */
    static final String RULE_PATH = "lava";
    static final Identifier RULE_ID = BlockLightRuleIds.builtIn(RULE_PATH);
    private static final int LAVA_LIGHT_LEVEL = 15;
    private static final LightEntry.ColorOverride LAVA_COLOR = LightEntry.ColorOverride.of(1.0f, 0.38f, 0.08f);
    private static final float RADIUS = 0.5f;
    /** Centre height above the lava surface, so the sphere clears both the surface and a pool's rim. */
    private static final float HEIGHT_ABOVE_SURFACE = 0.6f;
    /**
     * Radiance of a cell holding one surface block: the power of a campfire
     * (radiance 562 over a 0.25 radius) spread over this sphere's larger area.
     */
    private static final float SINGLE_BLOCK_RADIANCE = 140.0f;

    private LavaLights() {}

    /**
     * Light for one cell.
     *
     * @param cellOrigin   any position unique to the cell, used only as the light's identity
     * @param surfaceCount exposed lava surfaces gathered in the cell, at least one
     * @param x            mean world x of the surfaces' block centres
     * @param surfaceY     mean world y of the surfaces
     * @param z            mean world z of the surfaces' block centres
     */
    public static LightEntry cell(BlockPos cellOrigin, int surfaceCount, float x, float surfaceY, float z) {
        float intensity = SINGLE_BLOCK_RADIANCE * (float) Math.sqrt(surfaceCount);
        return LightEntry.sphere(new BlockLightKey(cellOrigin.toImmutable(), RULE_ID, 0),
                LAVA_LIGHT_LEVEL, Blocks.LAVA,
                x, surfaceY + HEIGHT_ABOVE_SURFACE, z,
                RADIUS, LAVA_COLOR, intensity, LightEntry.defaultVolumetricRadianceScale());
    }
}
