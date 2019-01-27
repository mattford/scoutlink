package uk.org.mattford.scoutlink.tasks;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.database.entities.LogMessage;

public class ExportLogFileTask implements Runnable {

    private List<LogMessage> messages;
    private String conversationName;
    private Context context;

    public ExportLogFileTask(List<LogMessage> messages, String conversationName, Context context) {
        this.messages = messages;
        this.conversationName = conversationName;
        this.context = context;
    }

    @Override
    public void run() {
        File exportFile = getFile();

        try (
            FileOutputStream os = new FileOutputStream(exportFile);
            OutputStreamWriter writer = new OutputStreamWriter(os)
        ) {
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

            (new Handler(Looper.getMainLooper())).post(
                () -> Toast.makeText(
                        this.context,
                        this.context.getString(R.string.log_exported, exportFile.getAbsolutePath()),
                        Toast.LENGTH_LONG
                ).show());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getFile() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String fileName = "logs-" + conversationName + "-" + dateFormat.format(new Date()) + ".txt";
        String filePath = Environment.getExternalStorageDirectory() + "/Documents/ScoutLinkLogs";
        File file = new File(filePath, fileName);
        try {
            if (!file.getParentFile().mkdirs() || !file.createNewFile()) {
                Log.e("SL", "Failed to create file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}
