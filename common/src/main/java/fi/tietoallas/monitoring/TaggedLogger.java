package fi.tietoallas.monitoring;

/*-
 * #%L
 * common
 * %%
 * Copyright (C) 2017 Helsingin ja Uudenmaan sairaanhoitopiiri, Helsinki, Finland
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper for SLF4J to harmonize logging between integrations and other components.
 */
public class TaggedLogger {

    /** The logger we are delegating to. */
    private Logger logger;

    /** The component */
    private String component;

    /**
     * Creates a new instance.
     *
     * @param clazz the returned logger will be named after clazz
     */
    public TaggedLogger(Class clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    /**
     * Creates a new instance.
     *
     * @param clazz the returned logger will be named after clazz
     * @param component the component
     */
    public TaggedLogger(Class clazz, String component) {
        this.logger = LoggerFactory.getLogger(clazz);
        this.component = component;
    }

    private String format(String msg) {
        return format(Tag.DL_DEFAULT, msg);
    }

    private String format(Tag tag, String msg) {
        StringBuilder builder = new StringBuilder();
        builder.append("[" + tag.name() + "]");
        if (component != null) {
            builder.append(" [" + component + "]");
        }
        builder.append(" " + msg);
        return builder.toString();
    }

    public void debug(String msg) {
        logger.debug(format(msg));
    }

    public void debug(Tag tag, String msg) {
        logger.debug(format(tag, msg));
    }

    public void debug(String msg, Throwable t) {
        logger.debug(format(msg), t);
    }

    public void debug(Tag tag, String msg, Throwable t) {
        logger.debug(format(tag, msg), t);
    }

    public void info(String msg) {
        logger.info(format(msg));
    }

    public void info(Tag tag, String msg) {
        logger.info(format(tag, msg));
    }

    public void info(String msg, Throwable t) {
        logger.info(format(msg), t);
    }

    public void info(Tag tag, String msg, Throwable t) {
        logger.info(format(tag, msg), t);
    }

    public void warn(String msg) {
        logger.warn(format(msg));
    }

    public void warn(Tag tag, String msg) {
        logger.warn(format(tag, msg));
    }

    public void warn(String msg, Throwable t) {
        logger.warn(format(msg), t);
    }

    public void warn(Tag tag, String msg, Throwable t) {
        logger.warn(format(tag, msg), t);
    }

    public void error(String msg) {
        logger.error(format(msg));
    }

    public void error(Tag tag, String msg) {
        logger.error(format(tag, msg));
    }

    public void error(String msg, Throwable t) {
        logger.error(format(msg), t);
    }

    public void error(Tag tag, String msg, Throwable t) {
        logger.error(format(tag, msg), t);
    }

}
