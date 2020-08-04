/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.telemetry.glean.private

import androidx.annotation.VisibleForTesting
import com.sun.jna.StringArray
import mozilla.telemetry.glean.Dispatchers
import mozilla.telemetry.glean.rust.LibGleanFFI
import mozilla.telemetry.glean.rust.getAndConsumeRustString
import mozilla.telemetry.glean.rust.toBoolean
import mozilla.telemetry.glean.rust.toByte
import mozilla.telemetry.glean.testing.ErrorType
import org.json.JSONObject

/**
 * A representation of a JWE value.
 */
data class JweData(
    val header: String,
    val key: String,
    val initVector: String,
    val cipherText: String,
    val authTag: String
)

/**
 * This implements the developer facing API for recording JWE metrics.
 *
 * Instances of this class type are automatically generated by the parsers at build time,
 * allowing developers to record values that were previously registered in the metrics.yaml file.
 *
 * The JWE API exposes the [set] and [setWithCompactRepresentation] methods,
 * which take care of validating the input data.
 */
class JweMetricType internal constructor(
    private var handle: Long,
    private val disabled: Boolean,
    private val sendInPings: List<String>
) {
    /**
     * The public constructor used by automatically generated metrics.
     */
    constructor(
        disabled: Boolean,
        category: String,
        lifetime: Lifetime,
        name: String,
        sendInPings: List<String>
    ) : this(handle = 0, disabled = disabled, sendInPings = sendInPings) {
        val ffiPingsList = StringArray(sendInPings.toTypedArray(), "utf-8")
        this.handle = LibGleanFFI.INSTANCE.glean_new_jwe_metric(
            category = category,
            name = name,
            send_in_pings = ffiPingsList,
            send_in_pings_len = sendInPings.size,
            lifetime = lifetime.ordinal,
            disabled = disabled.toByte()
        )
    }

    /**
     * Set a JWE value.
     *
     * @param value The compact representation of a JWE value.
     */
    fun setWithCompactRepresentation(value: String) {
        if (disabled) {
            return
        }

        @Suppress("EXPERIMENTAL_API_USAGE")
        Dispatchers.API.launch {
            LibGleanFFI.INSTANCE.glean_jwe_set_with_compact_representation(this@JweMetricType.handle, value)
        }
    }

    /**
     * Build a JWE value from its elements and set to it.
     *
     * @param header A variable-size JWE protected header.
     * @param key A variable-size encrypted key.
     *      This can be an empty octet sequence.
     * @param initVector A fixed-size, 96-bit, base64 encoded Jwe initialization vector.
     *      If not required by the encryption algorithm, can be an empty octet sequence.
     * @param cipherText The variable-size base64 encoded cipher text.
     * @param authTag A fixed-size, 132-bit, base64 encoded authentication tag.
     *      Can be an empty octet sequence.
     */
    fun set(header: String, key: String, initVector: String, cipherText: String, authTag: String) {
        if (disabled) {
            return
        }

        @Suppress("EXPERIMENTAL_API_USAGE")
        Dispatchers.API.launch {
            LibGleanFFI.INSTANCE.glean_jwe_set(this@JweMetricType.handle, header, key, initVector, cipherText, authTag)
        }
    }

    /**
     * Tests whether a value is stored for the metric for testing purposes only. This function will
     * attempt to await the last task (if any) writing to the the metric's storage engine before
     * returning a value.
     *
     * @param pingName represents the name of the ping to retrieve the metric for.
     *                 Defaults to the first value in `sendInPings`.
     * @return true if metric value exists, otherwise false
     */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @JvmOverloads
    fun testHasValue(pingName: String = sendInPings.first()): Boolean {
        @Suppress("EXPERIMENTAL_API_USAGE")
        Dispatchers.API.assertInTestingMode()

        return LibGleanFFI.INSTANCE
            .glean_jwe_test_has_value(this.handle, pingName)
            .toBoolean()
    }

    /**
     * Returns the stored value for testing purposes only. This function will attempt to await the
     * last task (if any) writing to the the metric's storage engine before returning a value.
     *
     * @param pingName represents the name of the ping to retrieve the metric for.
     *                 Defaults to the first value in `sendInPings`.
     * @return value of the stored metric
     * @throws [NullPointerException] if no value is stored
     */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @JvmOverloads
    fun testGetValue(pingName: String = sendInPings.first()): JweData {
        @Suppress("EXPERIMENTAL_API_USAGE")
        Dispatchers.API.assertInTestingMode()

        if (!testHasValue(pingName)) {
            throw NullPointerException()
        }

        val ptr = LibGleanFFI.INSTANCE.glean_jwe_test_get_value_as_json_string(this.handle, pingName)!!
        val json = JSONObject(ptr.getAndConsumeRustString())

        return JweData(
            header = json.get("header") as String,
            key = json.get("key") as String,
            initVector = json.get("init_vector") as String,
            cipherText = json.get("cipher_text") as String,
            authTag = json.get("auth_tag") as String
        )
    }

    /**
     * Returns the stored value in the compact representation for testing purposes only.
     * This function will attempt to await the last task (if any)
     * writing to the the metric's storage engine before returning a value.
     *
     * @param pingName represents the name of the ping to retrieve the metric for.
     *                 Defaults to the first value in `sendInPings`.
     * @return value of the stored metric
     * @throws [NullPointerException] if no value is stored
     */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @JvmOverloads
    fun testGetCompactRepresentation(pingName: String = sendInPings.first()): String {
        @Suppress("EXPERIMENTAL_API_USAGE")
        Dispatchers.API.assertInTestingMode()

        if (!testHasValue(pingName)) {
            throw NullPointerException()
        }
        val ptr = LibGleanFFI.INSTANCE.glean_jwe_test_get_value(this.handle, pingName)!!
        return ptr.getAndConsumeRustString()
    }

    /**
     * Returns the number of errors recorded for the given metric.
     *
     * @param errorType The type of the error recorded.
     * @param pingName represents the name of the ping to retrieve the metric for.
     *                 Defaults to the first value in `sendInPings`.
     * @return the number of errors recorded for the metric.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @JvmOverloads
    fun testGetNumRecordedErrors(errorType: ErrorType, pingName: String = sendInPings.first()): Int {
        @Suppress("EXPERIMENTAL_API_USAGE")
        Dispatchers.API.assertInTestingMode()

        return LibGleanFFI.INSTANCE.glean_jwe_test_get_num_recorded_errors(
            this.handle, errorType.ordinal, pingName
        )
    }
}