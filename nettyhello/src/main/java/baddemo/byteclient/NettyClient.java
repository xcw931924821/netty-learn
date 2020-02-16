package baddemo.byteclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

public class NettyClient {
    public static void main(String[] args) {
        startClient();
    }

    public static void startClient(){
        //1.定义服务类
        Bootstrap clientBootstap = new Bootstrap();

        //2.定义执行线程组
        EventLoopGroup worker = new NioEventLoopGroup();

        //3.设置线程池
        clientBootstap.group(worker);

        //4.设置通道
        clientBootstap.channel(NioSocketChannel.class);

        //5.添加Handler
        clientBootstap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                System.out.println("client channel init!");
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast("ClientHandler",new ClientHandler());
            }
        });

        //6.建立连接
        ChannelFuture channelFuture = clientBootstap.connect("0.0.0.0",10000);
        try {
            //7.测试输入
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                System.out.println("请输入：");
                String msg = bufferedReader.readLine();
                //如果没有编码器，只能写ByteBuf
                byte[] msgByte = msg.getBytes();
                ByteBuf messageByteBuf = Unpooled.buffer(msgByte.length);
                messageByteBuf.writeBytes(msgByte);
                channelFuture.channel().writeAndFlush(messageByteBuf);

                //直接写字符串是不行的，必须是ByteBuf或FileRegion类型，可以对字符串封装一下：

                //channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("hello world", CharsetUtil.UTF_8));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //8.关闭连接
            worker.shutdownGracefully();
        }
    }
     static class ClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("byte client receive msg:"+msg.toString());
        }
    }


}
