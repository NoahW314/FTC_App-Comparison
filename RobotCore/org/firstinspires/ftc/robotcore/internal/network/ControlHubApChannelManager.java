package org.firstinspires.ftc.robotcore.internal.network;

import android.content.Context;
import android.content.Intent;

import com.qualcomm.robotcore.util.Intents;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public final class ControlHubApChannelManager implements ApChannelManager
{
    private final static String TAG = "ControlHubApChannelManager";
    private final static int FACTORY_DEFAULT_AP_CHANNEL = 0;
    private Context context;

    public ControlHubApChannelManager()
    {
        context = AppUtil.getInstance().getApplication();
    }

    private int validateChannel(String channel) {
        try {
            final int toRtn = Integer.valueOf(channel);
            return (toRtn >= 0 && toRtn <= 11) ? toRtn : -1;
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    @Override
    public boolean setChannel(String channel)
    {
        final int ch = validateChannel(channel);
        if (ch < 0) {
            return false;
        }
        return setChannel(ch);
    }

    private boolean setChannel(int ch)
    {
        RobotLog.vv(TAG, "Sending ap channel change intent");
        Intent intent = new Intent(Intents.ACTION_FTC_AP_CHANNEL_CHANGE);
        intent.putExtra(Intents.EXTRA_AP_PREF, ch);
        context.sendBroadcast(intent);
        return true;
    }

    @Override
    public boolean resetChannel()
    {
        return setChannel(FACTORY_DEFAULT_AP_CHANNEL);
    }
}
