﻿// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

using Mozilla.Glean.FFI;
using System;

namespace Mozilla.Glean.Private
{
    /// <summary>
    /// This implements the developer facing API for recording string metrics.
    /// 
    /// Instances of this class type are automatically generated by the parsers at build time,
    /// allowing developers to record values that were previously registered in the metrics.yaml file.
    /// 
    /// The internal constructor is only used by `LabeledMetricType` directly.
    /// </summary>
    public sealed class StringMetricType
    {
        private readonly bool disabled;
        private readonly string[] sendInPings;
        private readonly LibGleanFFI.StringMetricTypeHandle handle;

        public StringMetricType(
            bool disabled,
            string category,
            Lifetime lifetime,
            string name,
            string[] sendInPings
            ) : this(new LibGleanFFI.StringMetricTypeHandle(), disabled, sendInPings)
        {
            handle = LibGleanFFI.glean_new_string_metric(
                    category: category,
                    name: name,
                    send_in_pings: sendInPings,
                    send_in_pings_len: sendInPings.Length,
                    lifetime: (int)lifetime,
                    disabled: disabled);
        }

        internal StringMetricType(
            LibGleanFFI.StringMetricTypeHandle handle,
            bool disabled,
            string[] sendInPings
            )
        {
            this.disabled = disabled;
            this.sendInPings = sendInPings;
            this.handle = handle;
        }

        /// <summary>
        /// Set a string value.
        /// </summary>
        /// <param name="value">This is a user defined string value. If the length of the string exceeds
        /// the maximum length, it will be truncated</param>
        public void Set(string value)
        {
            if (disabled)
            {
                return;
            }

            Dispatchers.LaunchAPI(() => {
                SetSync(value);
            });
        }

        /// <summary>
        /// Internal only, synchronous API for setting a string value.
        /// </summary>
        /// <param name="value">This is a user defined string value. If the length of the string exceeds
        /// the maximum length, it will be truncated</param>
        internal void SetSync(string value)
        {
            LibGleanFFI.glean_string_set(this.handle, value);
        }


        /// <summary>
        /// Tests whether a value is stored for the metric for testing purposes only. This function will
        /// attempt to await the last task (if any) writing to the the metric's storage engine before
        /// returning a value.
        /// </summary>
        /// <param name="pingName">represents the name of the ping to retrieve the metric for Defaults
        /// to the first value in `sendInPings`</param>
        /// <returns>true if metric value exists, otherwise false</returns>
        public bool TestHasValue(string pingName = null)
        {
            Dispatchers.AssertInTestingMode();

            string ping = pingName ?? sendInPings[0];
            return LibGleanFFI.glean_string_test_has_value(this.handle, ping) != 0;
        }

        /// <summary>
        /// Returns the stored value for testing purposes only. This function will attempt to await the
        /// last task (if any) writing to the the metric's storage engine before returning a value.
        /// @throws [NullPointerException] if no value is stored
        /// </summary>
        /// <param name="pingName">represents the name of the ping to retrieve the metric for.
        /// Defaults to the first value in `sendInPings`</param>
        /// <returns>value of the stored metric</returns>
        /// <exception cref="System.NullReferenceException">Thrown when the metric contains no value</exception>
        public string TestGetValue(string pingName = null)
        {
            Dispatchers.AssertInTestingMode();

            if (!TestHasValue(pingName)) {
                throw new NullReferenceException();
            }

            string ping = pingName ?? sendInPings[0];
            return LibGleanFFI.glean_string_test_get_value(this.handle, ping).AsString();
        }

        /**
         * Returns the number of errors recorded for the given metric.
         *
         * @param errorType The type of the error recorded.
         * @param pingName represents the name of the ping to retrieve the metric for.
         *                 Defaults to the first value in `sendInPings`.
         * @return the number of errors recorded for the metric.
         */
        public Int32 TestGetNumRecordedErrors(Testing.ErrorType errorType, string pingName = null)
        {
            Dispatchers.AssertInTestingMode();

            string ping = pingName ?? sendInPings[0];
            return LibGleanFFI.glean_string_test_get_num_recorded_errors(
                this.handle, (int)errorType, ping
            );
        }
    }
}
