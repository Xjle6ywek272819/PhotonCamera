package com.particlesdevs.photoncamera.api;

import android.hardware.camera2.CameraCharacteristics;
import android.util.Size;

import com.particlesdevs.photoncamera.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Helpers for the full-resolution RAW metadata exposed by OPlus camera HALs.
 *
 * <p>Some OPlus devices advertise only the binned RAW size through the standard
 * {@link android.hardware.camera2.params.StreamConfigurationMap}, while the OEM
 * metadata contains the native QCFA/remosaic RAW dimensions.  Keep all handling
 * in one small, defensive class so unsupported devices retain stock behaviour.</p>
 */
public final class OplusFullResolutionRaw {
    private static final String TAG = "OplusFullResRaw";

    public static final String SUPPORTED_RAW_SIZES = "com.oplus.custom.support.raw.size";
    public static final String SENSOR_MODE_TABLE =
            "org.quic.camera2.sensormode.info.SensorModeTable";
    public static final String SENSOR_MODES_IN_CONFIG =
            "org.codeaurora.qcamera3.sessionParameters.SensorModesInConfig";

    private OplusFullResolutionRaw() {}

    /** Returns valid width/height pairs from com.oplus.custom.support.raw.size. */
    public static List<Size> getSupportedRawSizes(CameraCharacteristics characteristics) {
        if (characteristics == null) return Collections.emptyList();
        try {
            CameraCharacteristics.Key<int[]> key =
                    new CameraCharacteristics.Key<>(SUPPORTED_RAW_SIZES, int[].class);
            return parseRawSizes(characteristics.get(key));
        } catch (Exception e) {
            Log.d(TAG, "OPlus full-resolution RAW metadata unavailable: " + e);
            return Collections.emptyList();
        }
    }

    /** Parses width/height pairs, rejecting malformed and implausible entries. */
    static List<Size> parseRawSizes(int[] values) {
        if (values == null || values.length < 2) return Collections.emptyList();
        Set<Size> unique = new LinkedHashSet<>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            int width = values[i];
            int height = values[i + 1];
            if (width <= 0 || height <= 0 || width > 32768 || height > 32768) continue;
            unique.add(new Size(width, height));
        }
        return new ArrayList<>(unique);
    }

    /**
     * Logs the OEM sensor-mode table in index: width x height @ fps form.
     * The first two integers are count and tuple width on current Qualcomm HALs.
     */
    public static void logSensorModes(CameraCharacteristics characteristics) {
        if (characteristics == null) return;
        try {
            CameraCharacteristics.Key<int[]> key =
                    new CameraCharacteristics.Key<>(SENSOR_MODE_TABLE, int[].class);
            int[] table = characteristics.get(key);
            if (table == null || table.length < 5) return;
            int count = table[0];
            int stride = table[1];
            if (count <= 0 || stride < 3 || 2L + (long) count * stride > table.length) {
                Log.w(TAG, "Malformed sensor mode table: count=" + count
                        + " stride=" + stride + " length=" + table.length);
                return;
            }
            for (int mode = 0; mode < count; mode++) {
                int base = 2 + mode * stride;
                int width = table[base];
                int height = table[base + 1];
                int fps = table[base + 2];
                if ((long) width * height > 30_000_000L) {
                    Log.i(TAG, "Full RAW sensor mode " + mode + ": "
                            + width + "x" + height + " @ " + fps);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Sensor mode table unavailable: " + e);
        }
    }
}
