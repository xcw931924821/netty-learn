package baddemo.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Date;

/**
 * 用于对网络事件进行读写操作
 * 模拟粘包/拆包故障场景
 * @author Joanna.Yan
 * @date 2017年11月8日下午6:54:35
 */
public class TimeServerHandler1 extends ChannelInboundHandlerAdapter{

    /**
     * 读取消息的次数
     */
    private int counter;

    /**
     * 测试结果发现counter 只被读取了两次消息。理论上要是100个counter
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        System.out.println("channelRewaddddd");
        ByteBuf buf =(ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8").substring(0, req.length-System.getProperty("line.separator").length());
        System.out.println("The time server receive order : "+body +" ;\r\nthe counter is :"+ (++counter));
        String currentTime="QUERY TIME ORDER".equalsIgnoreCase(body) ? new
                Date(System.currentTimeMillis()).toString() : "BAD ORDER";
        currentTime  = currentTime + System.getProperty("line.separator");
        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.write(resp);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        System.out.println("exceptionCaught..");
        cause.printStackTrace();
        //ctx.close();
    }
}