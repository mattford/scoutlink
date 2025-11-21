package uk.org.mattford.scoutlink.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.List;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.database.entities.LogMessage;

public class ExportLogFileTask implements Runnable {

    private final List<LogMessage> messages;
    private final Context context;
    private final Uri uri;

    public ExportLogFileTask(List<LogMessage> messages, Context context, Uri uri) {
        this.messages = messages;
        this.context = context;
        this.uri = uri;
    }

    @Override
    public void run() {
        if (uri == null) {
            showToast(this.context.getString(R.string.error_exporting_logs));
            return;
        }

        try (
            OutputStream os = context.getContentResolver().openOutputStream(uri);
            OutputStreamWriter writer = new OutputStreamWriter(os)
        ) {
            if (os == null) {
                showToast(this.context.getString(R.string.error_exporting_logs));
                return;
            }

            for (LogMessage msg : messages) {
                DateFormat dateFormat = DateFormat.getDateTimeInstance();
                StringBuilder sb = new StringBuilder();
                sb.append("[").append(dateFormat.format(msg.date)).append("] ");
                if (msg.sender != null) {
                    sb.append("<").append(msg.sender).append("> ");
                }
                sb.append(msg.message).append("\n");
                String logLine = sb.toString();
                writer.append(logLine);
            }

            showToast(this.context.getString(R.string.log_exported));
        } catch (IOException e) {
            Log.e("SL", "Failed to export logs", e);
            showToast(this.context.getString(R.string.error_exporting_logs));
        }
    }

    private void showToast(String message) {
        (new Handler(Looper.getMainLooper())).post(
                () -> Toast.makeText(
                        this.context,
                        message,
                        Toast.LENGTH_LONG
                ).show());
    }
}
