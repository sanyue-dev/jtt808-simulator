package cn.org.hentai.simulator.engine.core;

import cn.org.hentai.simulator.domain.enums.LogType;
import cn.org.hentai.simulator.domain.enums.TaskStatus;
import cn.org.hentai.simulator.domain.model.TaskLifecycleObserver;
import org.yzh.protocol.commons.JT808;
import org.yzh.protocol.t808.T0100;
import org.yzh.protocol.t808.T0001;
import org.yzh.protocol.t808.T8100;

final class Jtt808ProtocolStage
{
    void onConnected(SimpleDriveTask task)
    {
        if (task.isTerminated()) return;

        task.log(LogType.INFO, "connected");
        TaskLifecycleObserver observer = task.getLifecycleObserver();
        if (observer != null) observer.onConnected(task.getInfo());

        T0100 msg = new T0100();
        msg.setMessageId(JT808.终端注册);
        msg.setProvinceId(1);
        msg.setCityId(1);
        msg.setMakerId("CHINA");
        msg.setDeviceModel("HENTAI-SIMULATOR");
        msg.setDeviceId(task.getParameter("device.sn"));
        msg.setPlateColor(1);
        msg.setPlateNo(task.getParameter("vehicle.number"));

        task.send(msg);
    }

    void onRegisterResponse(SimpleDriveTask task, T8100 msg)
    {
        if (task.isTerminated()) return;

        int result = msg.getResultCode();
        TaskLifecycleObserver observer = task.getLifecycleObserver();
        if (result == 0)
        {
            task.status = TaskStatus.REGISTRATION_SUCCESSFUL;
            task.token = msg.getToken();
            task.log(LogType.INFO, "registered");
            if (observer != null) observer.onRegistrationSucceeded(task.getInfo());
            task.authenticate();
        }
        else
        {
            task.status = TaskStatus.REGISTRATION_FAILED;
            task.log(LogType.EXCEPTION, "register failed");
            if (observer != null) observer.onRegistrationFailed(task.getInfo(), "resultCode=" + result);
            task.terminate();
        }
    }

    void onGenericResponse(SimpleDriveTask task, T0001 msg)
    {
        if (task.isTerminated()) return;
        if (task.status != TaskStatus.AUTHENTICATING) return;

        TaskLifecycleObserver observer = task.getLifecycleObserver();
        if (msg.isSuccess())
        {
            task.status = TaskStatus.AUTHENTICATION_SUCCESSFUL;
            if (observer != null) observer.onAuthenticationSucceeded(task.getInfo());
            task.reportLocation();
        }
        else
        {
            task.status = TaskStatus.AUTHENTICATION_FAILED;
            if (observer != null) observer.onAuthenticationFailed(task.getInfo(), "resultCode=" + msg.getResultCode());
            task.terminate();
        }
    }
}
