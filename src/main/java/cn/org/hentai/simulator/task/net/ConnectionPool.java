package cn.org.hentai.simulator.task.net;

import cn.org.hentai.simulator.task.AbstractDriveTask;
import cn.org.hentai.simulator.task.SimpleDriveTask;
import cn.org.hentai.simulator.task.event.EventDispatcher;
import io.github.yezhihao.netmc.codec.MessageDecoder;
import io.github.yezhihao.netmc.codec.MessageEncoder;
import io.github.yezhihao.netmc.core.model.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yzh.protocol.basics.JTMessage;
import org.yzh.protocol.codec.JTMessageAdapter;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by matrixy when 2020/4/30.
 */
public class ConnectionPool
{
    static Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

    public static final JTMessageAdapter messageAdapter = new JTMessageAdapter("org.yzh.protocol.t808");

    EventLoopGroup group = null;
    Bootstrap bootstrap = null;
    ConcurrentHashMap<String, Connection> connections = null;

    private ConnectionPool()
    {
        connections = new ConcurrentHashMap<>(1024);
        start();
    }

    private void start()
    {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>()
            {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception
                {
                    ch.pipeline()
                            .addLast("frameDecoder", new DelimiterBasedFrameDecoder(2 + 21 + 1023 * 2 + 1 + 2,
                                    Unpooled.wrappedBuffer(new byte[] { 0x7e }),
                                    Unpooled.wrappedBuffer(new byte[] { 0x7e }, new byte[] { 0x7e })))
                            .addLast("decoder", new ByteToMessageDecoder() {
                                @Override
                                protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) {
//                                    logger.info("<<<<<{}", ByteBufUtil.hexDump(buf));
                                    Object msg = ((MessageDecoder)messageAdapter).decode(buf, null);
//                                    logger.info("<<<<<<<<<<{}", msg);
                                    if (msg != null)
                                        out.add(msg);
                                    buf.skipBytes(buf.readableBytes());
                                }
                            })
                            .addLast("encoder", new MessageToByteEncoder<Message>() {
                                @Override
                                protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) {
//                                    logger.info(">>>>>>>>>>{}", msg);
                                    ByteBuf buf = ((MessageEncoder) messageAdapter).encode(msg, null);
//                                    logger.info(">>>>>{}", ByteBufUtil.hexDump(buf));
                                    out.writeBytes(buf);
                                    buf.release();
                                }
                            })
                            .addLast("adapter", new SimpleNettyHandler())
                    ;
                }
            });
    }

    // 连接到目标服务器
    public void connect(String address, int port, SimpleDriveTask watcher)
    {
        ChannelFuture connectFuture = bootstrap.connect(address, port);

        connectFuture.addListener((ChannelFuture future) -> {
            if (future.isSuccess()) {
                Channel chl = future.channel();
                // 在 EventLoop 线程中安全地执行后续操作
                connections.put(chl.id().asLongText(), new Connection(chl, watcher));
                watcher.onConnectSuccess(chl);
            } else {
                // 连接失败，通知 watcher
                watcher.onConnectFailure(future.cause());
            }
        });
    }

    // 关闭连接
    public void close(String channelId)
    {
        Connection conn = connections.remove(channelId);
        if (conn != null) conn.channel.close();
    }

    // 发送消息
    public void send(String channelId, Object data) throws Exception
    {
        Connection conn = connections.get(channelId);
        if (conn != null)
        {
            conn.channel.writeAndFlush(data);
        }
        else throw new SocketException("connection closed");
    }

    // 当连接通道接收到消息时的通知
    protected void notify(String tag, String channelId, String messageId, Object data)
    {
        // logger.info("notify -> channel: {}, tag: {}", channelId, tag);
        Connection conn = connections.get(channelId);
        if (conn != null)
        {
            EventDispatcher.getInstance().dispatch(conn.watcher, tag, messageId, data);
        }
        else
        {
            logger.error("no channel found for: " + channelId);
        }
    }

    // 彻底关闭，用于进程退出时
    public void shutdown() throws Exception
    {
        group.shutdownGracefully().sync();
    }

    static final ConnectionPool instance = new ConnectionPool();
    public static void init()
    {
        // do nothing here..
    }

    public static ConnectionPool getInstance()
    {
        return instance;
    }

    static class Connection
    {
        public Channel channel;
        public AbstractDriveTask watcher;

        public Connection(Channel channel, AbstractDriveTask watcher)
        {
            this.channel = channel;
            this.watcher = watcher;
        }
    }

    static class SimpleNettyHandler extends SimpleChannelInboundHandler<JTMessage>
    {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception
        {
            instance.notify("connected", ctx.channel().id().asLongText(), null, null);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception
        {
            super.channelInactive(ctx);
            // System.out.println("closed..." + ctx.channel().id().asLongText());
            instance.notify("disconnected", ctx.channel().id().asLongText(), null, null);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, JTMessage msg) throws Exception
        {
            String msgId = String.format("%04x", msg.getMessageId() & 0xffff);
            // logger.debug("received: {}", msgId);
            instance.notify("message_received", ctx.channel().id().asLongText(), msgId, msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
        {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
