package io.xhao.server.netty.traffic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class DiscardClientHandler extends SimpleChannelInboundHandler<Object> {

    private ByteBuf content;
    private ChannelHandlerContext ctx;
    PooledByteBufAllocator pooledAllocator;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws InterruptedException {
        this.ctx = ctx;

        pooledAllocator = new PooledByteBufAllocator();
        // 第一种方案会有问题
        // while (true) {
        // content = pooledAllocator.heapBuffer();
        // content.writeBytes("hello".getBytes());
        // ctx.writeAndFlush(content).sync();
        // System.out.println("ok");
        // }
        // 当不可写时，就停止发数据
        // while(ctx.channel().isWritable()){
        // content = pooledAllocator.ioBuffer(data.length);
        // content.writeBytes(data);
        // ctx.writeAndFlush(content);
        // }
        //
        // 这种方案应该是可以避免的
        // content = pooledAllocator.ioBuffer(data.length);
        // content.writeBytes(data);
        // generateTraffic();
        content = pooledAllocator.heapBuffer();
        content.writeBytes("hello".getBytes());
        generateTraffic();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // content.release();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channelRead0");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    long counter;

    private void generateTraffic() {
        ctx.writeAndFlush(content).addListener(trafficGenerator);
        System.out.println("ok");
    }

    private final ChannelFutureListener trafficGenerator = new ChannelFutureListener() {
        public void operationComplete(ChannelFuture future) {
            if (future.isSuccess()) {
                content = pooledAllocator.heapBuffer();
                content.writeBytes("hello".getBytes());
                generateTraffic();
            } else {
                future.cause().printStackTrace();
                future.channel().close();
            }
        }
    };
}
