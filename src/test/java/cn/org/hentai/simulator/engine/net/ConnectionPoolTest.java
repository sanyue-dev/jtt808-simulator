package cn.org.hentai.simulator.engine.net;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(OutputCaptureExtension.class)
class ConnectionPoolTest
{
    @Test
    void intentionalCloseDoesNotReportMissingChannelWhenInactiveEventArrives(CapturedOutput output)
    {
        ConnectionPool pool = ConnectionPool.getInstance();
        EmbeddedChannel channel = new EmbeddedChannel();
        String channelId = channel.id().asLongText();

        pool.connections.put(channelId, new ConnectionPool.Connection(channel, null));

        try
        {
            pool.close(channelId);
            pool.notify("disconnected", channelId, null, null);

            assertFalse(output.getOut().contains("no channel found for: " + channelId));
            assertFalse(output.getErr().contains("no channel found for: " + channelId));
        }
        finally
        {
            pool.connections.remove(channelId);
            channel.close();
        }
    }

    @Test
    void unknownChannelStillReportsMissingChannel(CapturedOutput output)
    {
        String channelId = "unknown-channel-id";

        ConnectionPool.getInstance().notify("disconnected", channelId, null, null);

        assertTrue(output.getOut().contains("no channel found for: " + channelId)
                || output.getErr().contains("no channel found for: " + channelId));
    }

    @Test
    void closeDoesNotRememberAlreadyInactiveChannel()
    {
        ConnectionPool pool = ConnectionPool.getInstance();
        EmbeddedChannel channel = new EmbeddedChannel();
        String channelId = channel.id().asLongText();

        pool.connections.put(channelId, new ConnectionPool.Connection(channel, null));

        try
        {
            channel.close();
            pool.close(channelId);

            assertFalse(pool.intentionallyClosedChannels.contains(channelId));
        }
        finally
        {
            pool.connections.remove(channelId);
            pool.intentionallyClosedChannels.remove(channelId);
            channel.close();
        }
    }
}
