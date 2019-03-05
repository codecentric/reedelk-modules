package com.esb.lifecycle;

import org.osgi.framework.Bundle;

public interface Step<I, O> {

    O run(I input);

    Bundle bundle();

    void bundle(Bundle bundle);

}
