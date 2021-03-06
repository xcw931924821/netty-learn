package LineBasedFrameDecoderDemo.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.util.logging.Logger;

/**
 * 1问题1 发出去的字节数0**-+-超过了LineBasedFrameDecoder的长度怎么办
 */
public class TimeClientGoodByteBufMultiData {
    public static void main(String[] args) throws Exception {
        int port = 9091;
        if(args!=null&&args.length>0){
            try {
                port=Integer.valueOf(args[0]);
            } catch (Exception e) {
                // 采用默认值
            }
        }
        new TimeClientGoodByteBufMultiData().connect(port, "127.0.0.1");

    }

    public void connect(int port,String host) throws Exception{
        //配置客户端NIO线程组
        EventLoopGroup group=new NioEventLoopGroup();

        try {
            Bootstrap b=new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
//                    ch.pipeline().addLast(new TimeClientHandler());
                            //直接在TimeClientHandler之前新增LineBasedFrameDecoder和StringDecoder解码器
                            ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                            ch.pipeline().addLast(new StringDecoder());
                            //模拟粘包/拆包故障场景
                            ch.pipeline().addLast(new LocalTimeClientHandler1());
                        }
                    });
            //发起异步连接操作
            ChannelFuture f = b.connect(host, port).sync();
            //等待客户端链路关闭
            f.channel().closeFuture().sync();
        }finally{
            //优雅退出，释放NIO线程组
            group.shutdownGracefully();
        }
    }

    /**
     * 模拟粘包/拆包故障场景
     */
    static class LocalTimeClientHandler1 extends ChannelInboundHandlerAdapter {

        private static final Logger logger = Logger.getLogger("TimeClientHandler1");
        private int counter;
        private byte[] req;

        //1 /
        public LocalTimeClientHandler1() {
            //有换行符号是变成了2个请求吗，不是的还是1个请求，只是
           // req = ("QUERY TIME ORDER" + "\n3626").getBytes();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelActive start111");

            for(int j = 0;j<20;j++){
                System.out.println("startdataa");
                byte[] req1 = ("hello1" + System.getProperty("line.separator")).getBytes();
                byte[] req2 = ("hello2" + System.getProperty("line.separator")).getBytes();
                byte[] req3_1 = ("hello3").getBytes();
                byte[] req3_2 = (System.getProperty("line.separator")).getBytes();
                ByteBuf buffer = Unpooled.buffer();
                buffer.writeBytes(req1);
                buffer.writeBytes(req2);
                buffer.writeBytes(req3_1);//一个分隔符号一个请求，这里会有2个请求过去
                ctx.writeAndFlush(buffer);
                //第3个的第2部分数据搞过去，拆包问题
//                try {
//                    TimeUnit.SECONDS.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }

//            buffer = Unpooled.buffer();
//            buffer.writeBytes(req3_2);
//            ctx.writeAndFlush(buffer);
        }
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg)
                throws Exception {
//        System.out.println("channelRead start");
//        ByteBuf buf=(ByteBuf) msg;
//        byte[] req = new byte[buf.readableBytes()];
//        buf.readBytes(req);
//        String body=new String(req, "UTF-8");
//        System.out.println("Now is :"+body+" ;the counter is :" + ++counter);
            //拿到的msg已经是解码成字符串之后的应答消息了，相比于之前的代码简洁明了很多。
            String body = (String) msg;
            System.out.println("Receiver server Now is :" + body + " ;the counter is :" + ++counter);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                throws Exception {
            //释放资源
            logger.warning("Unexpected exception from downstream : " + cause.getMessage());
            ctx.close();
        }
    }
}