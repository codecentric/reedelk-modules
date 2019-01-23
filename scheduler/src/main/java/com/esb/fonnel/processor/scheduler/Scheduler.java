package com.esb.fonnel.processor.scheduler;


import com.esb.foonnel.domain.AbstractInbound;
import com.esb.foonnel.domain.Message;
import org.osgi.service.component.annotations.Component;

import java.util.Timer;
import java.util.TimerTask;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = Scheduler.class, scope = PROTOTYPE)
public class Scheduler extends AbstractInbound {

    private Timer timer;

    private long delay;
    private long period;

    @Override
    public void onStart() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new SendMessage(), delay, period);
    }

    @Override
    public void onShutdown() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    class SendMessage extends TimerTask {
        @Override
        public void run() {
            Message emptyMessage = new Message();
            onEvent(emptyMessage);
        }
    }
}
