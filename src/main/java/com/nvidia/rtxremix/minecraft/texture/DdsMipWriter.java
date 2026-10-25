/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 rtx-remix-minecraft-radeon-26.3 contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.nvidia.rtxremix.minecraft.texture;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * Writes an uncompressed 8-bit texture and its full mip chain, box-filtered on
 * the CPU, as a DX10-extended DDS file.
 *
 * <p>The runtime builds the mips of a texture created from memory on the GPU,
 * and on AMD that pass leaves every level below the first unwritten: distant
 * surfaces then sample zeros, or stale memory after a resource reload. A
 * texture created from a DDS file takes its mips from the file instead.
 */
final class DdsMipWriter {

    static final int DXGI_FORMAT_R8G8B8A8_UNORM = 28;
    static final int DXGI_FORMAT_R8_UNORM = 61;

    private static final int DDSD_CAPS = 0x1;
    private static final int DDSD_HEIGHT = 0x2;
    private static final int DDSD_WIDTH = 0x4;
    private static final int DDSD_PITCH = 0x8;
    private static final int DDSD_PIXELFORMAT = 0x1000;
    private static final int DDSD_MIPMAPCOUNT = 0x20000;
    private static final int DDPF_FOURCC = 0x4;
    private static final int DDSCAPS_COMPLEX = 0x8;
    private static final int DDSCAPS_TEXTURE = 0x1000;
    private static final int DDSCAPS_MIPMAP = 0x400000;
    private static final int D3D10_RESOURCE_DIMENSION_TEXTURE2D = 3;
    private static final int HEADER_BYTES = 4 + 124 + 20;

    private DdsMipWriter() {}

    static int mipCount(int width, int height) {
        return 32 - Integer.numberOfLeadingZeros(Math.max(width, height));
    }

    /**
     * Writes {@code base} (tightly packed rows of {@code channels} bytes per
     * pixel) and its mip chain to {@code path}, replacing any previous file
     * atomically so a reader never sees a partial one.
     */
    static void write(Path path, byte[] base, int width, int height, int channels, int dxgiFormat)
            throws IOException {
        int mips = mipCount(width, height);
        long total = HEADER_BYTES;
        for (int level = 0, w = width, h = height; level < mips; level++) {
            total += (long) w * h * channels;
            w = Math.max(1, w >> 1);
            h = Math.max(1, h >> 1);
        }
        if (total > Integer.MAX_VALUE) {
            throw new IOException("DDS too large: " + total + " bytes");
        }

        ByteBuffer out = ByteBuffer.allocate((int) total).order(ByteOrder.LITTLE_ENDIAN);
        putHeader(out, width, height, channels, mips, dxgiFormat);

        byte[] level = base;
        int w = width;
        int h = height;
        out.put(level, 0, w * h * channels);
        for (int i = 1; i < mips; i++) {
            int nw = Math.max(1, w >> 1);
            int nh = Math.max(1, h >> 1);
            level = downsample(level, w, h, nw, nh, channels);
            out.put(level, 0, nw * nh * channels);
            w = nw;
            h = nh;
        }
        out.flip();

        Files.createDirectories(path.getParent());
        Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
        try (FileChannel ch = FileChannel.open(tmp, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            while (out.hasRemaining()) {
                ch.write(out);
            }
        }
        Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private static void putHeader(ByteBuffer out, int width, int height, int channels, int mips, int dxgiFormat) {
        out.putInt(0x20534444); // "DDS "
        out.putInt(124);
        out.putInt(DDSD_CAPS | DDSD_HEIGHT | DDSD_WIDTH | DDSD_PITCH | DDSD_PIXELFORMAT | DDSD_MIPMAPCOUNT);
        out.putInt(height);
        out.putInt(width);
        out.putInt(width * channels); // pitch of the top level
        out.putInt(1); // depth
        out.putInt(mips);
        for (int i = 0; i < 11; i++) {
            out.putInt(0); // reserved
        }
        out.putInt(32); // pixel format size
        out.putInt(DDPF_FOURCC);
        out.putInt(0x30315844); // "DX10"
        for (int i = 0; i < 5; i++) {
            out.putInt(0); // RGB bit count and masks
        }
        out.putInt(DDSCAPS_COMPLEX | DDSCAPS_TEXTURE | DDSCAPS_MIPMAP);
        out.putInt(0);
        out.putInt(0);
        out.putInt(0);
        out.putInt(0); // reserved
        out.putInt(dxgiFormat);
        out.putInt(D3D10_RESOURCE_DIMENSION_TEXTURE2D);
        out.putInt(0); // misc flags
        out.putInt(1); // array size
        out.putInt(0); // misc flags 2
    }

    /** Averages each 2x2 footprint, folding the odd edge of a non-power-of-two level into the last texel. */
    private static byte[] downsample(byte[] src, int w, int h, int nw, int nh, int channels) {
        byte[] dst = new byte[nw * nh * channels];
        for (int y = 0; y < nh; y++) {
            int y0 = Math.min(y * 2, h - 1);
            int y1 = Math.min(y * 2 + 1, h - 1);
            for (int x = 0; x < nw; x++) {
                int x0 = Math.min(x * 2, w - 1);
                int x1 = Math.min(x * 2 + 1, w - 1);
                int a = (y0 * w + x0) * channels;
                int b = (y0 * w + x1) * channels;
                int c = (y1 * w + x0) * channels;
                int d = (y1 * w + x1) * channels;
                int o = (y * nw + x) * channels;
                for (int k = 0; k < channels; k++) {
                    int sum = (src[a + k] & 0xFF) + (src[b + k] & 0xFF)
                            + (src[c + k] & 0xFF) + (src[d + k] & 0xFF);
                    dst[o + k] = (byte) ((sum + 2) >> 2);
                }
            }
        }
        return dst;
    }
}
