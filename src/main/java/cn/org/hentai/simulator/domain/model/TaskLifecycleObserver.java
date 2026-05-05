package cn.org.hentai.simulator.domain.model;

import org.yzh.protocol.basics.JTMessage;

public interface TaskLifecycleObserver
{
    default void onConnected(TaskInfo taskInfo) {}

    default void onConnectionFailed(TaskInfo taskInfo, Throwable cause) {}

    default void onRegistrationSucceeded(TaskInfo taskInfo) {}

    default void onRegistrationFailed(TaskInfo taskInfo, String reason) {}

    default void onAuthenticationSucceeded(TaskInfo taskInfo) {}

    default void onAuthenticationFailed(TaskInfo taskInfo, String reason) {}

    default void onLocationReported(TaskInfo taskInfo, JTMessage message) {}

    default void onDisconnected(TaskInfo taskInfo) {}

    default void onTerminated(TaskInfo taskInfo) {}

    default void onSendFailed(TaskInfo taskInfo, JTMessage message, Throwable cause) {}

    default void onProtocolException(TaskInfo taskInfo, Throwable cause) {}
}
