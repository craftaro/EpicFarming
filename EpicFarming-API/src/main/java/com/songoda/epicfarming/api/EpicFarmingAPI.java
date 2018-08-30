package com.songoda.epicfarming.api;
/**
 * The access point of the EpicFarmingAPI, a class acting as a bridge between API
 * and plugin implementation. It is from here where developers should access the
 * important and core methods in the API. All static methods in this class will
 * call directly upon the implementation at hand (in most cases this will be the
 * EpicFarming plugin itself), therefore a call to {@link #getImplementation()} is
 * not required and redundant in most situations. Method calls from this class are
 * preferred the majority of time, though an instance of {@link EpicFarming} may
 * be passed if absolutely necessary.
 *
 * @see EpicFarming
 * @since 3.0.0
 */
public class EpicFarmingAPI {
    
        private static EpicFarming implementation;

        /**
         * Set the EpicFarming implementation. Once called for the first time, this
         * method will throw an exception on any subsequent invocations. The implementation
         * may only be set a single time, presumably by the EpicFarming plugin
         *
         * @param implementation the implementation to set
         */
        public static void setImplementation(EpicFarming implementation) {
            if (EpicFarmingAPI.implementation != null) {
                throw new IllegalArgumentException("Cannot set API implementation twice");
            }

            EpicFarmingAPI.implementation = implementation;
        }

        /**
         * Get the EpicFarming implementation. This method may be redundant in most
         * situations as all methods present in {@link EpicFarming} will be mirrored
         * with static modifiers in the {@link EpicFarmingAPI} class
         *
         * @return the EpicFarming implementation
         */
        public static EpicFarming getImplementation() {
            return implementation;
        }
    }


