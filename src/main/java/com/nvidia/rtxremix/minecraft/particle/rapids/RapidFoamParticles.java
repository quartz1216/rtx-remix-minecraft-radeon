/*
 * SPDX-FileCopyrightText: Copyright (c) 2024-2026 NVIDIA CORPORATION & AFFILIATES. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.nvidia.rtxremix.minecraft.particle.rapids;

import com.nvidia.rtxremix.minecraft.RtxRemixCommonMod;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class RapidFoamParticles {
    public static final SimpleParticleType RAPID_FOAM = new SimpleParticleType(false);
    public static final SimpleParticleType RAPID_FOAM_DOT = new SimpleParticleType(false);
    public static final SimpleParticleType RAPID_MIST = new SimpleParticleType(false);

    private RapidFoamParticles() {}

    /** Registers the types on both sides; NeoForge syncs this registry. */
    public static void registerTypes(RegisterEvent event) {
        event.register(Registries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(RtxRemixCommonMod.MOD_ID, "rapid_foam"), () -> RAPID_FOAM);
        event.register(Registries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(RtxRemixCommonMod.MOD_ID, "rapid_foam_dot"), () -> RAPID_FOAM_DOT);
        event.register(Registries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(RtxRemixCommonMod.MOD_ID, "rapid_mist"), () -> RAPID_MIST);
    }

}
