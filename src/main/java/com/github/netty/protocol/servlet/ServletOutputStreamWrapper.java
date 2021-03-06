package com.github.netty.protocol.servlet;

import com.github.netty.core.util.Recyclable;
import com.github.netty.core.util.Wrapper;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.FileRegion;
import io.netty.handler.stream.ChunkedInput;
import io.netty.util.concurrent.Future;

import javax.servlet.WriteListener;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.function.Consumer;

/**
 * Servlets output streams (wrapper classes) that control access to the flow
 * @author wangzihao
 */
public class ServletOutputStreamWrapper extends javax.servlet.ServletOutputStream
        implements Wrapper<ServletOutputStream>, Recyclable,NettyOutputStream {
    /**
     * The source data
     */
    private ServletOutputStream source;
    /**
     * Pause symbol
     */
    private boolean suspendFlag = false;

    private ChannelFutureListener closeListener;

    public ServletOutputStreamWrapper(ChannelFutureListener closeListener) {
        this.closeListener = closeListener;
    }

    /**
     * Set (on/off) to pause the output operation
     * @param suspendFlag True = pause, false= resume
     */
    public void setSuspendFlag(boolean suspendFlag) {
        this.suspendFlag = suspendFlag;
    }

    /**
     * Whether to pause the operation output stream
     * @return boolean suspendFlag
     */
    public boolean isSuspendFlag() {
        return suspendFlag;
    }

    @Override
    public ChannelProgressivePromise write(ChunkedInput input) throws IOException {
        return source.write(input);
    }

    @Override
    public ChannelProgressivePromise write(FileChannel fileChannel, long position, long count) throws IOException {
        return source.write(fileChannel,position,count);
    }

    @Override
    public ChannelProgressivePromise write(File file, long position, long count) throws IOException {
        return source.write(file,position,count);
    }

    @Override
    public boolean isReady() {
        return source.isReady();
    }

    @Override
    public void setWriteListener(WriteListener listener) {
        source.setWriteListener(listener);
    }

    @Override
    public void write(int b) throws IOException {
        if(isSuspendFlag()){
            return;
        }
        source.write(b);
    }

    @Override
    public void close() throws IOException {
        if(isSuspendFlag()){
            return;
        }
        source.close();
    }

    @Override
    public void flush() throws IOException {
        if(isSuspendFlag()){
            return;
        }
        source.flush();
    }

    public void resetBuffer(){
        if(isSuspendFlag()){
            return;
        }
        source.resetBuffer();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if(isSuspendFlag()){
            return;
        }
        source.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        if(isSuspendFlag()){
            return;
        }
        source.write(b);
    }

    @Override
    public void wrap(ServletOutputStream source) {
        if(closeListener != null) {
            source.setCloseListener(closeListener);
        }
        this.source = source;
    }

    @Override
    public ServletOutputStream unwrap() {
        return source;
    }

    @Override
    public <T> void recycle(Consumer<T> consumer) {
        ServletOutputStream out = source;
        if(out != null){
            source = null;
            out.recycle(consumer);
        }else {
            consumer.accept(null);
        }
        suspendFlag = false;
    }
}
