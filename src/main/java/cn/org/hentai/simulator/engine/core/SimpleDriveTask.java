package cn.org.hentai.simulator.engine.core;

import cn.org.hentai.simulator.domain.enums.TaskStatus;
import cn.org.hentai.simulator.domain.enums.TaskState;
import cn.org.hentai.simulator.domain.model.Point;
import cn.org.hentai.simulator.domain.model.TaskLifecycleObserver;
import cn.org.hentai.simulator.engine.event.EventDispatcher;
import cn.org.hentai.simulator.engine.event.EventEnum;
import cn.org.hentai.simulator.engine.event.Listen;
import cn.org.hentai.simulator.domain.enums.LogType;
import cn.org.hentai.simulator.engine.net.ConnectionPool;
import cn.org.hentai.simulator.engine.runner.Executable;

import cn.org.hentai.simulator.infrastructure.util.LBSUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yzh.protocol.basics.JTMessage;
import org.yzh.protocol.commons.JT808;
import org.yzh.protocol.t808.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by matrixy when 2020/5/9.
 */
public class SimpleDriveTask extends AbstractDriveTask
{
    static Logger logger = LoggerFactory.getLogger(SimpleDriveTask.class);

    // 部标808协议连接id
    volatile String connectionId;

    // 状态
    volatile TaskStatus status;

    volatile String token;

    // 1078多媒体传输协议接连id
    String multimediaConnectionId;

    // 消息包流水号
    int sequence = 1;

    // 最后发送的消息ID
    int lastSentMessageId = 0;

    // 以米为单位的总行程里程数
    int mileages = 0;
    Point lastPosition = null;

    // 连接池
    ConnectionPool pool = ConnectionPool.getInstance();

    public SimpleDriveTask(long id, long routeId)
    {
        super(id, routeId);
    }

    @Override
    public void startup() {
        EventDispatcher.register(this);
        pool.connect(getParameter("server.address"), Integer.parseInt(getParameter("server.port")), this);

        // 总行驶里程初始化
        Object km = getParameter("mileages");
        if (km != null)
        {
            int meters = Integer.parseInt(String.valueOf(km)) * 1000;
            mileages = meters;
        }
    }

    @Override
    public void terminate()
    {
        super.terminate();
        pool.close(connectionId);
    }

    // 新增：处理连接成功的回调
    public void onConnectSuccess(Channel channel) {
        // 当连接成功后，这个方法会被 ConnectionPool 调用
        this.connectionId = channel.id().asLongText();
    }

    // 新增：处理连接失败的回调
    public void onConnectFailure(Throwable cause) {
        logger.error("onConnectFailure", cause);
        TaskLifecycleObserver observer = getLifecycleObserver();
        if (observer != null) observer.onConnectionFailed(getInfo(), cause);
        this.terminate(); // 例如，失败了就直接终止任务
    }

    public void onProtocolException(Throwable cause) {
        TaskLifecycleObserver observer = getLifecycleObserver();
        if (observer != null) observer.onProtocolException(getInfo(), cause);
    }

    // 通用下行消息回调，先执行这个方法，后再按消息ID进行路由，所以最好不要在这里做应答
    @Listen(when = EventEnum.message_received)
    public void onServerMessage(JTMessage msg)
    {
        log(LogType.MESSAGE_IN, msg.toString());
    }

    @Listen(when = EventEnum.connected)
    public void onConnected()
    {
        log(LogType.INFO, "connected");
        TaskLifecycleObserver observer = getLifecycleObserver();
        if (observer != null) observer.onConnected(getInfo());
        // 连接成功时，发送注册消息
        String sn = getParameter("device.sn");

        T0100 msg = new T0100();
        msg.setMessageId(JT808.终端注册);
        msg.setProvinceId(1);
        msg.setCityId(1);
        msg.setMakerId("CHINA");
        msg.setDeviceModel("HENTAI-SIMULATOR");
        msg.setDeviceId(sn);
        msg.setPlateColor(1);
        msg.setPlateNo(getParameter("vehicle.number"));

        send(msg);
    }

    @Listen(when = EventEnum.message_received, attachment = "8001")
    public void onGenericResponse(T0001 msg) {
        if (status == TaskStatus.AUTHENTICATING) {
            TaskLifecycleObserver observer = getLifecycleObserver();
            if (msg.isSuccess()) {
                status = TaskStatus.AUTHENTICATION_SUCCESSFUL;
                if (observer != null) observer.onAuthenticationSucceeded(getInfo());
            } else {
                status = TaskStatus.AUTHENTICATION_FAILED;
                if (observer != null) observer.onAuthenticationFailed(getInfo(), "resultCode=" + msg.getResultCode());
            }
        }
        next();
    }

    // 注册应答时
    @Listen(when = EventEnum.message_received, attachment = "8100")
    public void onRegisterResponsed(T8100 msg) {
        int result = msg.getResultCode();
        if (result == 0) {
            status = TaskStatus.REGISTRATION_SUCCESSFUL;
            token = msg.getToken();
            log(LogType.INFO, "registered");
            TaskLifecycleObserver observer = getLifecycleObserver();
            if (observer != null) observer.onRegistrationSucceeded(getInfo());
        } else {
            status = TaskStatus.REGISTRATION_FAILED;
            log(LogType.EXCEPTION, "register failed");
            TaskLifecycleObserver observer = getLifecycleObserver();
            if (observer != null) observer.onRegistrationFailed(getInfo(), "resultCode=" + result);
        }
        next();
    }

    @Listen(when = EventEnum.disconnected)
    public void onDisconnected()
    {
        log(LogType.EXCEPTION, "disconnected");
        TaskLifecycleObserver observer = getLifecycleObserver();
        if (observer != null) observer.onDisconnected(getInfo());
        terminate();
    }

    // 接收到文本信息
    @Listen(when = EventEnum.message_received, attachment = "8300")
    public void onTTSMessage(T8300 msg)
    {
        int flag = msg.getSign();
        String text;
        text = msg.getContent();
        boolean emergency = (flag & (1 << 0)) > 0;
        boolean display = (flag & (1 << 2)) > 0;
        boolean tts = (flag & (1 << 3)) > 0;
        boolean adScreen = (flag & (1 << 4)) > 0;
        boolean CANCode = (flag & (1 << 5)) > 0;
        String log = "标志：";
        if (emergency) log += "紧急，";
        if (display) log += "终端显示器显示，";
        if (tts) log += "终端TTS播读，";
        if (adScreen) log += "广告屏显示，";
        log += CANCode ? "CAN故障码，" : "中心导航信息，";
        log(LogType.INFO, log + "文本：" + text);

        // 回应一下
        T0001 responMsg = new T0001();
        responMsg.setSerialNo(msg.getSerialNo());
        responMsg.setResultCode(0);
        send(responMsg);
    }

    // 根据状态进行下一步
    protected void next() {
        if (TaskStatus.REGISTRATION_SUCCESSFUL == status) {
            authenticate();
        } else if (TaskStatus.REGISTRATION_FAILED == status) {
            terminate();
        } else if (TaskStatus.AUTHENTICATION_SUCCESSFUL == status) {
            reportLocation();
        } else if (TaskStatus.AUTHENTICATION_FAILED == status) {
            terminate();
        }
        // 暂时先屏蔽掉，没发送心跳消息就暂时先不执行了
        /*
        executeConstantly(new Executable()
        {
            @Override
            public void execute(AbstractDriveTask driveTask)
            {
                ((SimpleDriveTask)driveTask).heartbeat();
            }
        }, 30000);
        */
//        reportLocation();
    }

    private void authenticate() {
        executeConstantly(driveTask -> {
            if (TaskStatus.REGISTRATION_SUCCESSFUL == status) {
                status = TaskStatus.AUTHENTICATING;
                T0102 message = new T0102();
                message.setMessageId(JT808.终端鉴权);
                message.setToken(token);
                send(message);
            }
        }, 3000);
    }

    public void reportLocation() {
        if (getState() == TaskState.terminated) return;
        lastPosition = getCurrentPosition();
        final Point point = getNextPoint();
        if (point == null) {
            // 10分钟后再关闭
            executeAfter(new Executable() {
                @Override
                public void execute(AbstractDriveTask driveTask) {
                    terminate();
                }
            }, 1000 * 60 * 10);
            return;
        }

        executeAfter(new Executable() {
            @Override
            public void execute(AbstractDriveTask driveTask) {
                if (getState() == TaskState.terminated) return;
                int direction = lastPosition == null ? 0 : LBSUtils.caculateAngle(lastPosition.getLongitude(), lastPosition.getLatitude(), point.getLongitude(), point.getLatitude());

                point.setDirection(direction);

                double longitude = point.getLongitude();
                double latitude = point.getLatitude();

                T0200 msg = new T0200();
                msg.setMessageId(JT808.位置信息汇报);
                msg.setWarnBit(point.getWarnFlags() | getWarningFlags());
                msg.setStatusBit(point.getStatus() | getStateFlags());
                msg.setLatitude((int) (latitude * 100_0000));
                msg.setLongitude((int) (longitude * 100_0000));
                msg.setAltitude(0);
                msg.setSpeed((int) (point.getSpeed() * 10));
                msg.setDirection(direction);
                msg.setDeviceTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(point.getReportTime()), ZoneId.systemDefault()));

                // 里程数
                int km = (lastPosition == null ? 0 : LBSUtils.directDistance(point.getLongitude(), point.getLatitude(), lastPosition.getLongitude(), lastPosition.getLatitude()));
                mileages += km;
                km = mileages;
                // 里程数单位为1/10公里

                km = km / 100;

                Map<Integer, Object> attributes = new HashMap<>();
                attributes.put(0x01, km);
                msg.setAttributes(attributes);
                send(msg);

                setCurrentPosition(point);
                reportLocation();
            }
        }, (int) Math.max(point.getReportTime() - System.currentTimeMillis(), 0));
    }

    public void heartbeat() {
        // TODO: 需要完成心跳消息
        logger.debug("{}: heartbeat...", getParameter("device.sn"));
    }

    @Override
    public void send(JTMessage msg) {
        if (getState() == TaskState.terminated) return;
        try {
            msg.setClientId(getParameter("device.sim"));
            msg.setSerialNo((sequence++) & 0xffff);
            ChannelFuture future = pool.send(connectionId, msg);

            lastSentMessageId = msg.getSerialNo();

//            logger.info("send: {} -> {} : {}", msg.getClientId(), msg.getSerialNo(), String.format("%04x", msg.getMessageId()));

            log(LogType.MESSAGE_OUT, msg.toString());
            future.addListener(channelFuture -> {
                TaskLifecycleObserver observer = getLifecycleObserver();
                if (observer == null) return;
                if (channelFuture.isSuccess()) observer.onLocationReported(getInfo(), msg);
                else observer.onSendFailed(getInfo(), msg, channelFuture.cause());
            });
        } catch (Exception e) {
            TaskLifecycleObserver observer = getLifecycleObserver();
            if (observer != null) observer.onSendFailed(getInfo(), msg, e);
            throw new RuntimeException(e);
        }
    }
}
