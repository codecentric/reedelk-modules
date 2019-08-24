package com.reedelk.rest.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

class PathEncoder {

    /**
     * Encode a path as required by the URL specification (<a href="http://www.ietf.org/rfc/rfc1738.txt">
     * RFC 1738</a>). This differs from <code>java.net.URLEncoder.encode()</code> which encodes according
     * to the <code>x-www-form-urlencoded</code> MIME format.
     *
     * @param path the path to encode
     * @return the encoded path
     */
    static String encodePath(String path) {
        /**
         * Note: Here, ' ' should be encoded as "%20"
         * and '/' shouldn't be encoded.
         */

        int maxBytesPerChar = 10;
        StringBuilder rewrittenPath = new StringBuilder(path.length());
        ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);
        OutputStreamWriter writer;
        try {
            writer = new OutputStreamWriter(buf, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            writer = new OutputStreamWriter(buf);
        }

        for (int i = 0; i < path.length(); i++) {
            int c = path.charAt(i);
            if (safeChars.get(c)) {
                rewrittenPath.append((char)c);
            } else {
                // convert to external encoding before hex conversion
                try {
                    writer.write(c);
                    writer.flush();
                } catch(IOException e) {
                    buf.reset();
                    continue;
                }
                byte[] ba = buf.toByteArray();
                for (byte toEncode : ba) {
                    // Converting each byte in the buffer
                    rewrittenPath.append('%');
                    int low = (toEncode & 0x0f);
                    int high = ((toEncode & 0xf0) >> 4);
                    rewrittenPath.append(HEX[high]);
                    rewrittenPath.append(HEX[low]);
                }
                buf.reset();
            }
        }
        return rewrittenPath.toString();
    }

    /**
     * Array containing the safe characters set as defined by RFC 1738
     */
    private static BitSet safeChars;

    private static final char[] HEX =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    'A', 'B', 'C', 'D', 'E', 'F'};

    static {
        safeChars = new BitSet(256);
        int i;
        // 'lowalpha' rule
        for (i = 'a'; i <= 'z'; i++) {
            safeChars.set(i);
        }
        // 'hialpha' rule
        for (i = 'A'; i <= 'Z'; i++) {
            safeChars.set(i);
        }
        // 'digit' rule
        for (i = '0'; i <= '9'; i++) {
            safeChars.set(i);
        }

        // 'safe' rule
        safeChars.set('$');
        safeChars.set('-');
        safeChars.set('_');
        safeChars.set('.');
        safeChars.set('+');

        // 'extra' rule
        safeChars.set('!');
        safeChars.set('*');
        safeChars.set('\'');
        safeChars.set('(');
        safeChars.set(')');
        safeChars.set(',');

        // special characters common to http: file: and ftp: URLs ('fsegment' and 'hsegment' rules)
        safeChars.set('/');
        safeChars.set(':');
        safeChars.set('@');
        safeChars.set('&');
        safeChars.set('=');
    }
}
