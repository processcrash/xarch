package com.xarch.mcp.runtime.stdio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stderr-only logging — never write to stdout, that channel is the MCP
 * wire. This class is a thin convenience over SLF4J so callers don't have
 * to keep creating loggers everywhere.
 */
public final class Logging {

    private static final Logger LOG = LoggerFactory.getLogger("xarch.mcp");

    private Logging() {}

    public static void info(String fmt, Object... args)  { LOG.info(fmt, args); }
    public static void warn(String fmt, Object... args)  { LOG.warn(fmt, args); }
    public static void error(String fmt, Object... args) { LOG.error(fmt, args); }
    public static void debug(String fmt, Object... args) { LOG.debug(fmt, args); }
}
