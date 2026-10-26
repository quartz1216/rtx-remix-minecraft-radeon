/*
 * SPDX-FileCopyrightText: Copyright (c) 2024-2026 NVIDIA CORPORATION & AFFILIATES. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.nvidia.rtxremix.minecraft;

import com.nvidia.rtxremix.minecraft.block.CopperBeaconUpdatePayload;
import com.nvidia.rtxremix.minecraft.block.SpotLightBlock;
import com.nvidia.rtxremix.minecraft.block.CopperBeaconScreenHandler;
import com.nvidia.rtxremix.minecraft.particle.rapids.RapidFoamParticles;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Common (both-sides) entrypoint.
 *
 * <p>The renderer itself is client-only and lives in {@link RtxRemixMod}. This
 * class exists solely for content that must exist on the logical server too:
 * currently the beacon blocks.
 *
 * <p><b>Why this is separate:</b> blocks live in a <i>synced</i> registry. A
 * block registered only on the client desynchronises from a server that has
 * never heard of it, so it cannot be placed or persisted in multiplayer.
 * NeoForge syncs the particle type registry as well, so the rapid-water
 * particle types ({@code RapidFoamParticles}) register here too; only their
 * client-side providers stay in the client entrypoint.
 *
 * <p>Everything reachable from here must avoid client-only classes: this runs
 * on dedicated servers, where {@code net.minecraft.client.*} does not exist.
 * NeoForge freezes its registries outside {@link RegisterEvent}, so every
 * registration waits for that event rather than running in the constructor.
 */
@Mod(RtxRemixCommonMod.LOADER_MOD_ID)
public class RtxRemixCommonMod {

    /**
     * The mod id, and the namespace of every identifier this mod registers.
     *
     * <p>Owned by this class rather than by {@link RtxRemixMod}: block
     * registration runs on dedicated servers, where the client entrypoint
     * cannot be loaded, so a constant that path depends on must not live on a
     * client-only class.
     */
    public static final String MOD_ID = "rtx-remix-renderer";

    /**
     * The id NeoForge knows the mod by. Mod ids may not contain hyphens, so it
     * differs from {@link #MOD_ID}, which stays the namespace of registered
     * content and assets so worlds and resource packs keep resolving.
     */
    public static final String LOADER_MOD_ID = "rtx_remix_renderer";

    public RtxRemixCommonMod(IEventBus modEventBus) {
        modEventBus.addListener(RegisterEvent.class, event -> {
            SpotLightBlock.registerAll(event);
            CopperBeaconScreenHandler.register(event);
            RapidFoamParticles.registerTypes(event);
        });
        modEventBus.addListener(SpotLightBlock::addToCreativeTabs);
        modEventBus.addListener(CopperBeaconUpdatePayload::register);
    }
}
