package org.vg.markusbro.core.service.plugins;

import org.springframework.stereotype.Component;

@Component
public class PluginStart extends AbstractPlugin {

    @Override
    public String getId() {
        return "start";
    }

    @Override
    public double getScore(Context context) {
        return context.getUser().isAccessGeneral()
                ? SCORE_NEVER
                : SCORE_ALWAYS;
    }

    @Override
    public void handle(Context context) {
        context.reply(getResource("response.hello", context.getUserName()));
    }
}
