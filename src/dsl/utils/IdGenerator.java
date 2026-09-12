package dsl.utils;

import java.util.concurrent.atomic.AtomicLong;

public final class IdGenerator {

    private static final AtomicLong MESSAGES     = new AtomicLong();
    private static final AtomicLong TRANSACTIONS = new AtomicLong();

    private IdGenerator() { }

    public static String message()     { return "msg-" + MESSAGES.incrementAndGet(); }
    public static String transaction() { return "tx-"  + TRANSACTIONS.incrementAndGet(); }
}