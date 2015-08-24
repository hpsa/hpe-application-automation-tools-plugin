// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.mqm.client;

import java.io.IOException;
import java.io.OutputStream;

public interface LogOutput {

    OutputStream getOutputStream() throws IOException;

    void setContentType(String contentType);

}
