package fi.tietoallas.monitoring.commonmonitoring;

/**
 * A tag used in logging.
 *
 * Tags make it easier to interpret and process an aggregated log stream.
 */
public enum Tag {

    /** A default tag to use when none of the others are appropriate. */
    DL_DEFAULT,

    /** Statistics */
    DL_STAT_INFO,

    /** An internal connection failure (e.g. HTTP call to pseudo-proxy failed) */
    DL_INTERNAL_CONNECTION_FAILURE,

    /** An external connection failure (e.g. source system database not responding) */
    DL_EXTERNAL_CONNECTION_FAILURE,

    /** Out of memory */
    DL_OUT_OF_MEMORY
}

