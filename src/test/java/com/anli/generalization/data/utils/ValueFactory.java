package com.anli.generalization.data.utils;

import java.math.BigInteger;
import org.joda.time.DateTime;

public class ValueFactory {

    public static BigInteger bi(long value) {
        return BigInteger.valueOf(value);
    }

    public static DateTime dt(int year, int month, int day, int hours, int minutes, int seconds,
            int milliseconds) {
        return new DateTime(year, month, day, hours, minutes, seconds, milliseconds);
    }
}
