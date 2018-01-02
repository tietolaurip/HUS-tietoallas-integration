package fi.tietoallas.monitoring.commonmonitoring;

public interface MetricService {

        /**
         * Report the number of send bytes for the specified component (e.g. integration).
         *
         * @param component the component
         * @param value the number of send bytes
         */
        void reportSendBytes(final String component, long value);

        /**
         * Report the number of send bytes for the specified part of a component (e.g. a specific table of an integration).
         *
         * @param component the component
         * @param part the part
         * @param value the number of send bytes
         */
        void reportSendBytes(final String component, final String part, long value);

        /**
         * Report successful completion of a task (e.g. streaming integration processed an event).
         *
         * @param component the component
         */
        void reportSuccess(final String component);

        /**
         * Report successfult completion of a part of a task.
         *
         * @param component the component
         * @param part the part
         */
        void reportSuccess(final String component, final String part);
}
