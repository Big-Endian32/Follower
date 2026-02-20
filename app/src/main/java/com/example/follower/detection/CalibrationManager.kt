package com.example.follower.detection

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.sqrt

/**
 * Collects sighting patterns during "benign" scenarios (commuting, home, work)
 * and uses basic statistical anomaly detection to auto-tune suspicion thresholds.
 *
 * When calibration mode is active, every scored device's feature vector
 * (exposure time, streak length, location count, RSSI) is recorded as a
 * "normal" sample.  After enough samples are collected the manager
 * computes mean + 2σ for each feature and writes adjusted thresholds
 * back into [SuspicionSettings].
 *
 * A TensorFlow Lite model can replace the statistical approach by loading
 * a trained anomaly-detection .tflite file and running inference on the
 * same feature vectors.  See [inferWithTfLite] for the integration point.
 */
class CalibrationManager(context: Context, private val settings: SuspicionSettings) {

    companion object {
        private const val TAG = "CalibrationManager"
        private const val PREFS_NAME = "calibration_data"
        private const val KEY_SAMPLES = "samples"
        private const val MIN_SAMPLES_FOR_TUNING = 50
        private const val MAX_SAMPLES = 500
        private const val BATCH_FLUSH_INTERVAL = 10
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    data class FeatureVector(
        val totalExposureMinutes: Float,
        val longestStreakMinutes: Float,
        val distinctLocations: Int,
        val avgRssi: Float,
        val score: Int
    )

    /** In-memory buffer for batching writes (avoids writing JSON to disk on every sample). */
    private val pendingBuffer = mutableListOf<FeatureVector>()

    /**
     * Record a scored device observation as a "benign" calibration sample.
     * Only records when calibration mode is enabled.
     *
     * Samples are buffered in memory and flushed to disk every
     * [BATCH_FLUSH_INTERVAL] samples to reduce SharedPreferences I/O.
     */
    fun recordSample(result: SuspicionResult, avgRssi: Float) {
        if (!settings.calibrationModeEnabled) return

        pendingBuffer.add(
            FeatureVector(
                totalExposureMinutes = result.totalExposureMinutes,
                longestStreakMinutes = result.longestStreakMinutes,
                distinctLocations = result.distinctLocationCount,
                avgRssi = avgRssi,
                score = result.totalScore
            )
        )

        // Only flush to disk every N samples
        if (pendingBuffer.size >= BATCH_FLUSH_INTERVAL) {
            flushPendingSamples()
        }
    }

    /**
     * Flush any pending in-memory samples to disk and optionally auto-tune.
     */
    fun flushPendingSamples() {
        if (pendingBuffer.isEmpty()) return

        val samples = loadSamples().toMutableList()
        samples.addAll(pendingBuffer)
        pendingBuffer.clear()

        // Cap stored samples
        val trimmed = if (samples.size > MAX_SAMPLES) {
            samples.takeLast(MAX_SAMPLES)
        } else {
            samples
        }

        saveSamples(trimmed)
        Log.d(TAG, "Calibration samples flushed (${trimmed.size} total)")

        if (trimmed.size >= MIN_SAMPLES_FOR_TUNING) {
            autoTuneThresholds(trimmed)
        }
    }

    /**
     * Compute mean + 2σ of the observed scores and adjust the
     * low/high thresholds so that "normal" encounters don't trigger alerts.
     */
    private fun autoTuneThresholds(samples: List<FeatureVector>) {
        val scores = samples.map { it.score.toFloat() }
        val mean = scores.average().toFloat()
        val stdDev = scores.standardDeviation()

        val newLow = (mean + 1.5f * stdDev).toInt().coerceIn(15, 50)
        val newHigh = (mean + 2.5f * stdDev).toInt().coerceIn(newLow + 10, 90)

        settings.lowThreshold = newLow
        settings.highThreshold = newHigh

        Log.i(
            TAG,
            "Auto-tuned thresholds: low=$newLow, high=$newHigh " +
                "(mean=${mean.toInt()}, σ=${stdDev.toInt()}, n=${samples.size})"
        )
    }

    /**
     * Placeholder for TensorFlow Lite inference.
     *
     * To integrate:
     * 1. Place a trained anomaly-detection `.tflite` model in `assets/`.
     * 2. Load it via `Interpreter(loadModelFile(context, "model.tflite"))`.
     * 3. Convert [FeatureVector] to a float array input buffer.
     * 4. Run `interpreter.run(input, output)`.
     * 5. Interpret the output (e.g. anomaly score > threshold → suspicious).
     */
    @Suppress("unused")
    private fun inferWithTfLite(features: FeatureVector): Float {
        // TODO: Load TFLite model and run inference
        // val interpreter = Interpreter(modelBuffer)
        // val input = floatArrayOf(
        //     features.totalExposureMinutes,
        //     features.longestStreakMinutes,
        //     features.distinctLocations.toFloat(),
        //     features.avgRssi
        // )
        // val output = Array(1) { FloatArray(1) }
        // interpreter.run(arrayOf(input), output)
        // return output[0][0]
        return 0f
    }

    fun getSampleCount(): Int = loadSamples().size + pendingBuffer.size

    fun clearSamples() {
        pendingBuffer.clear()
        prefs.edit().remove(KEY_SAMPLES).apply()
        Log.i(TAG, "Calibration samples cleared")
    }

    // ---- Persistence ----------------------------------------------------------------

    private fun loadSamples(): List<FeatureVector> {
        val json = prefs.getString(KEY_SAMPLES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                FeatureVector(
                    totalExposureMinutes = obj.getDouble("exp").toFloat(),
                    longestStreakMinutes = obj.getDouble("streak").toFloat(),
                    distinctLocations = obj.getInt("locs"),
                    avgRssi = obj.getDouble("rssi").toFloat(),
                    score = obj.getInt("score")
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load calibration samples", e)
            emptyList()
        }
    }

    private fun saveSamples(samples: List<FeatureVector>) {
        val arr = JSONArray()
        for (s in samples) {
            arr.put(
                JSONObject().apply {
                    put("exp", s.totalExposureMinutes.toDouble())
                    put("streak", s.longestStreakMinutes.toDouble())
                    put("locs", s.distinctLocations)
                    put("rssi", s.avgRssi.toDouble())
                    put("score", s.score)
                }
            )
        }
        prefs.edit().putString(KEY_SAMPLES, arr.toString()).apply()
    }

    private fun List<Float>.standardDeviation(): Float {
        if (size < 2) return 0f
        val avg = average().toFloat()
        val variance = map { (it - avg) * (it - avg) }.average().toFloat()
        return sqrt(variance)
    }
}
