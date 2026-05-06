package cn.org.hentai.simulator.service.monitor;

import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.domain.model.TaskLifecycleObserver;
import org.yzh.protocol.basics.JTMessage;

public final class TaskLifecycleObservers
{
    private TaskLifecycleObservers()
    {
    }

    public static TaskLifecycleObserver composite(TaskLifecycleObserver first, TaskLifecycleObserver second)
    {
        if (first == null) return second;
        if (second == null) return first;
        return new TaskLifecycleObserver()
        {
            @Override
            public void onConnected(TaskInfo taskInfo)
            {
                first.onConnected(taskInfo);
                second.onConnected(taskInfo);
            }

            @Override
            public void onConnectionFailed(TaskInfo taskInfo, Throwable cause)
            {
                first.onConnectionFailed(taskInfo, cause);
                second.onConnectionFailed(taskInfo, cause);
            }

            @Override
            public void onRegistrationSucceeded(TaskInfo taskInfo)
            {
                first.onRegistrationSucceeded(taskInfo);
                second.onRegistrationSucceeded(taskInfo);
            }

            @Override
            public void onRegistrationFailed(TaskInfo taskInfo, String reason)
            {
                first.onRegistrationFailed(taskInfo, reason);
                second.onRegistrationFailed(taskInfo, reason);
            }

            @Override
            public void onAuthenticationSucceeded(TaskInfo taskInfo)
            {
                first.onAuthenticationSucceeded(taskInfo);
                second.onAuthenticationSucceeded(taskInfo);
            }

            @Override
            public void onAuthenticationFailed(TaskInfo taskInfo, String reason)
            {
                first.onAuthenticationFailed(taskInfo, reason);
                second.onAuthenticationFailed(taskInfo, reason);
            }

            @Override
            public void onLocationReported(TaskInfo taskInfo, JTMessage message)
            {
                first.onLocationReported(taskInfo, message);
                second.onLocationReported(taskInfo, message);
            }

            @Override
            public void onDisconnected(TaskInfo taskInfo)
            {
                first.onDisconnected(taskInfo);
                second.onDisconnected(taskInfo);
            }

            @Override
            public void onTerminated(TaskInfo taskInfo)
            {
                first.onTerminated(taskInfo);
                second.onTerminated(taskInfo);
            }

            @Override
            public void onSendFailed(TaskInfo taskInfo, JTMessage message, Throwable cause)
            {
                first.onSendFailed(taskInfo, message, cause);
                second.onSendFailed(taskInfo, message, cause);
            }

            @Override
            public void onProtocolException(TaskInfo taskInfo, Throwable cause)
            {
                first.onProtocolException(taskInfo, cause);
                second.onProtocolException(taskInfo, cause);
            }
        };
    }
}
