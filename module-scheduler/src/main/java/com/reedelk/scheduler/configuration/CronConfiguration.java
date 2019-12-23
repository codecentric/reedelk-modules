package com.reedelk.scheduler.configuration;

import com.reedelk.runtime.api.annotation.Combo;
import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.Hint;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = CronConfiguration.class, scope = PROTOTYPE)
public class CronConfiguration implements Implementor {

    @Property("Expression")
    @Default("1000")
    @Hint("* * * ? * *")
    private String expression;

    @Combo(editable = true, comboValues = {
        "", ""
    })
    private String timeZone;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
