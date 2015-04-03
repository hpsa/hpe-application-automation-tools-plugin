package com.hp.mqm.client.internal;

import com.hp.mqm.client.InputStreamSource;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InputStreamSourceEntity extends AbstractHttpEntity {

    private int OUTPUT_BUFFER_SIZE = 2048;

    private InputStreamSource inputStreamSource;
    private final long length;

    public InputStreamSourceEntity(InputStreamSource inputStreamSource) {
        this(inputStreamSource, null);
    }

    public InputStreamSourceEntity(InputStreamSource inputStreamSource, long length) {
        this(inputStreamSource, length, null);
    }

    public InputStreamSourceEntity(InputStreamSource inputStreamSource, ContentType contentType) {
        this(inputStreamSource, -1, contentType);
    }

    public InputStreamSourceEntity(InputStreamSource inputStreamSource, long length, ContentType contentType) {
        if (inputStreamSource == null) {
            throw new IllegalArgumentException("InputStreamSource cannot be null.");
        }
        this.length = length;
        if (contentType != null) {
            setContentType(contentType.toString());
        }
        this.inputStreamSource = inputStreamSource;
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public long getContentLength() {
        return length;
    }

    @Override
    public boolean isStreaming() {
        return false;
    }

    @Override
    public InputStream getContent() throws IOException {
        InputStream inputStream = inputStreamSource.getInputStream();
        if (inputStream == null) {
            throw new IllegalStateException("InputStreamSource#getInputSteam() returns null.");
        }
        return inputStream;
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        if (outputStream == null) {
            throw new IllegalArgumentException("Output stream cannot be null.");
        }
        final InputStream inputStream = getContent();
        try {
            final byte[] buffer = new byte[OUTPUT_BUFFER_SIZE];
            int l;
            if (this.length < 0) {
                while ((l = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, l);
                }
            } else {
                long remaining = this.length;
                while (remaining > 0) {
                    l = inputStream.read(buffer, 0, (int)Math.min(OUTPUT_BUFFER_SIZE, remaining));
                    if (l == -1) {
                        break;
                    }
                    outputStream.write(buffer, 0, l);
                    remaining -= l;
                }
            }
        } finally {
            inputStream.close();
        }
    }
}
