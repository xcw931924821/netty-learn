package gooddemo1.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Logger;

/**
 * 模拟粘包/拆包故障场景
 * @author Joanna.Yan
 * @date 2017年11月10日下午2:18:51
 */
public class TimeClientHandler1 extends ChannelInboundHandlerAdapter{

    private static final Logger logger=Logger.getLogger(TimeClientHandler1.class.getName());
    private int counter;
    private byte[] req;

    public TimeClientHandler1(){
        req = ("QUER TIME ORDER"+ "\n").getBytes();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("channelActive start");
//        ByteBuf message=null;
//        for (int i = 0; i < 100; i++) {
//            message = Unpooled.buffer(req.length);
//            message.writeBytes(req);
//            ctx.writeAndFlush(message);
//        }
        System.out.println("channelActive start");
        ByteBuf messageByteBuf = null;
        for (int i = 0; i < 100; i++) {
            messageByteBuf = Unpooled.buffer(req.length);
            messageByteBuf.writeBytes(req);
            ctx.writeAndFlush(messageByteBuf);
        }
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
        System.out.println("Receiver server Now is :"+body+" ;the counter is :" + ++counter);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        //释放资源
        logger.warning("Unexpected exception from downstream : "+cause.getMessage());
        ctx.close();
    }
}