package com.particlesdevs.photoncamera.api;

import android.util.Size;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class OplusFullResolutionRawTest {
    @Test
    public void parsesOplusRawSizePairs() {
        List<Size> sizes = OplusFullResolutionRaw.parseRawSizes(new int[] {
                8192, 6144, 8192, 4608, 6144, 6144, 8192, 3760
        });

        assertEquals(4, sizes.size());
        assertEquals(new Size(8192, 6144), sizes.get(0));
        assertEquals(new Size(8192, 4608), sizes.get(1));
        assertEquals(new Size(6144, 6144), sizes.get(2));
        assertEquals(new Size(8192, 3760), sizes.get(3));
    }

    @Test
    public void ignoresMalformedPairsAndDuplicates() {
        List<Size> sizes = OplusFullResolutionRaw.parseRawSizes(new int[] {
                8192, 6144, 0, 6144, 8192, 6144, -1
        });

        assertEquals(1, sizes.size());
        assertEquals(new Size(8192, 6144), sizes.get(0));
    }
}
