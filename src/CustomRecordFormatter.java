package src;
import java.io.PrintWriter;
import java.util.logging.LogRecord;

import java.io.StringWriter;
import java.util.logging.Formatter;

// This logging formatting code is inspired from SO : https://stackoverflow.com/questions/2950704/java-util-logging-how-to-suppress-date-line
// Just a formatter so not related to algorithms part of the course itself
class CustomRecordFormatter extends Formatter {
    @Override
    public String format(final LogRecord record) {
        
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(formatMessage(record)).append(System.getProperty("line.separator"));

        if (null!=record.getThrown()) {
            stringBuilder.append("Throwable occurred:"); //$NON-NLS-1$
            Throwable throwable = record.getThrown();
            PrintWriter printWriter =null;
            try {
                StringWriter stringWriter = new StringWriter();
                printWriter = new PrintWriter(stringWriter);
                throwable.printStackTrace(printWriter);
                stringBuilder.append(stringWriter.toString());
            } finally {
                if (printWriter != null) {
                    try {
                        printWriter.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
        return stringBuilder.toString();
    }
}