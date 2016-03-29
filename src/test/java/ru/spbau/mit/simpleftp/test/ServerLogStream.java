package ru.spbau.mit.simpleftp.test;

import java.io.OutputStream;
import java.io.PrintStream;

public class ServerLogStream extends PrintStream {

    public ServerLogStream(OutputStream out) {
        super(out);
        // TODO Auto-generated constructor stub
    }

    @Override
    public synchronized void println(String x) {
        // TODO Auto-generated method stub
        super.println(x);
    }
}
