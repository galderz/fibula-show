package org.sample.vertx;

import io.netty.handler.codec.http.HttpHeaders;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HeadersUtils
{
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyyy HH:mm:ss z");
    public static final CharSequence VERTX_HEADER = io.vertx.core.http.HttpHeaders.createOptimized("vert.x");
    public static final CharSequence TEXT_PLAIN_HEADER = io.vertx.core.http.HttpHeaders.createOptimized("text/plain");
    public static final CharSequence CONTENT_LENGTH_HEADER = io.vertx.core.http.HttpHeaders.createOptimized("20");
    public static final CharSequence DATE_HEADER = io.vertx.core.http.HttpHeaders.createOptimized(DATE_FORMAT.format(new Date()));

    public static void setBaseHeaders(HttpHeaders headers, boolean asciiNames, boolean asciiValues) {
        headers.add(toString(io.vertx.core.http.HttpHeaders.CONTENT_TYPE, !asciiNames),
            toString(TEXT_PLAIN_HEADER, !asciiValues));
        headers.add(toString(io.vertx.core.http.HttpHeaders.CONTENT_LENGTH, !asciiNames),
            toString(CONTENT_LENGTH_HEADER, !asciiValues));
        headers.add(toString(io.vertx.core.http.HttpHeaders.SERVER, !asciiNames),
            toString(VERTX_HEADER, !asciiValues));
        headers.add(toString(io.vertx.core.http.HttpHeaders.DATE, !asciiNames),
            toString(DATE_HEADER, !asciiValues));
    }

    private static CharSequence toString(CharSequence chars, boolean toString) {
        if (!toString) {
            return chars;
        }
        return chars.toString();
    }
}
