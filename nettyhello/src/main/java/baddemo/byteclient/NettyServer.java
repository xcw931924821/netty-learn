package baddemo.byteclient;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 *  创建NIO sever 的步骤
 * 1、创建ServerBootstrap实例，netty 的引导容器 工作引导器；
 * 2 2、设置并绑定Reactor线程池
 *3 3、设置并绑定服务器端Channel
 *
 *
 */
public class NettyServer {

    private void bind(int port ) throws InterruptedException
    {
        //1.定义server启动类
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        //2.定义工作组:boss分发请求给各个worker:boss负责监听端口请求，worker负责处理请求（读写）
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        serverBootstrap.group(boss,worker);

        //3.设置通道channel
        serverBootstrap.channel(NioServerSocketChannel.class);//A
        //serverBootstrap.channelFactory(new ReflectiveChannelFactory(NioServerSocketChannel.class));//旧版本的写法，但是此过程在A中有同样过程

        //4.添加handler，管道中的处理器，通过ChannelInitializer来构造
        //有事件进来会做，每个客户端连接上都会调用这个channel的事件
        serverBootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                //此方法每次客户端连接都会调用，是为通道初始化的方法
                //获得通道channel中的管道链（执行链、handler链）
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast("serverHandler1",new HelloHandler());
                System.out.println("success to initHandler!");
            }
        });

        //5.设置参数
        //设置参数，TCP参数         BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 2048);         //连接缓冲池的大小
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);//维持链接的活跃，清除死链接
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);//关闭延迟发送

        //6.绑定ip和port
        try {
            ChannelFuture channelFuture = serverBootstrap.bind("0.0.0.0", port).sync();//Future模式的channel对象
            //监听关闭,监听端口的关闭
            channelFuture.channel().closeFuture().sync();  //等待服务关闭，关闭后应该释放资源
            System.out.println("等待服务关闭！！！00");
        } catch (InterruptedException e) {
            System.out.println("server start got exception!");
            e.printStackTrace();
        }finally {
            //7.优雅的关闭资源
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws  Exception {
        NettyServer nettyServer = new NettyServer();
        nettyServer.bind(10000);
    }

    /**
     * 消息接受处理类
     * @author -琴兽-
     *
     */
    static class HelloHandler extends ChannelInboundHandlerAdapter {

        //接受client发送的消息
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
             System.out.println("11111channelRead服务器端接收的消息0："+msg);
             decode1NoDecoder(msg);
            // 向客户端发送消息
             writeNoEncode(ctx);
        }
        //通知处理器最后的channelRead()是当前批处理中的最后一条消息时调用
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            System.out.println("服务端接收数据完毕..");
            ctx.flush();
        }

        //读操作时捕获到异常时调用
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            System.out.println("exceptionCaught..");
            cause.printStackTrace();
            ctx.close();
        }

        //客户端去和服务端连接成功时触发
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("客户端连上了...");
        }
        private void decode1NoDecoder(Object msg){
            System.out.println("服务器端接收的消息0："+msg);
            ByteBuf buf = (ByteBuf) msg;
            System.out.println("接收的消息的长度:"+buf.readableBytes());
            //定义长度
            byte[] req = new byte[buf.readableBytes()];
            buf.readBytes(req);//缓冲区写入数据到字节数组中
            System.out.println("decode1NoDecoder 服务器端接收的消息："+new String(req));
        }
        private void writeNoEncode(ChannelHandlerContext ctx){
            String response = "I am ok!";
            // 在当前场景下，发送的数据必须转换成ByteBuf数组,加了编码也能这样写入
            ByteBuf encoded = ctx.alloc().buffer(4 * response.length());
            encoded.writeBytes(response.getBytes());
            ctx.writeAndFlush(encoded);
        }


    }
}
