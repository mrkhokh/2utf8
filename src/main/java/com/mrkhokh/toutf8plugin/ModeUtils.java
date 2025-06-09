package com.mrkhokh.toutf8plugin;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.intellij.openapi.vfs.VirtualFile;
import org.mozilla.universalchardet.UniversalDetector;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ModeUtils {

    public static String getLineEncoding(byte[] bytes) {
        CharsetDetector detector = new CharsetDetector();
        detector.setText(bytes);
        CharsetMatch[] matches = detector.detectAll();

        return matches.length > 0 ? matches[0].getName() : null;
    }

    public static List<byte[]> splitBytesByLines(byte[] bytes) {
        List<byte[]> lines = new ArrayList<>();
        int start = 0;

        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == '\r' || bytes[i] =='\n') {
                lines.add(copyOfRange(bytes, start, i));

                if (bytes[i] == '\r' && i + 1 < bytes.length && bytes[i + 1] == '\n') {
                    i++;
                }

                start = i + 1;
            }
        }
        if (start < bytes.length) {
            lines.add(copyOfRange(bytes, start, bytes.length));
        }
        return lines;
    }

    public static byte[] copyOfRange(byte[] array, int start, int end) {
        int length = end - start;
        if (length < 0) {
            return new byte[0];
        }
        byte[] result = new byte[length];
        System.arraycopy(array, start, result, 0, length);
        return result;
    }

    public static String detectEncoding(byte[] bytes) {
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(bytes, 0, bytes.length);
        detector.dataEnd();

        String encoding = detector.getDetectedCharset();
        detector.reset();
        return encoding != null ? encoding : StandardCharsets.UTF_8.name();
    }

    public static boolean isJavaFile(VirtualFile file) {
        return file != null && !file.isDirectory() && "java".equals(file.getExtension());
    }
}
