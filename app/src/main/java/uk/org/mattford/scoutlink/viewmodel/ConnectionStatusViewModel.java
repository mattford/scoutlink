package uk.org.mattford.scoutlink.viewmodel;

import android.app.Application;
import android.content.res.Resources;

import org.pircbotx.PircBotX;

import java.util.ResourceBundle;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.model.Server;

public class ConnectionStatusViewModel extends AndroidViewModel implements Server.OnConnectionStatusChangedListener {
    private Server server;
    private MutableLiveData<String> connectionStatus;

    public ConnectionStatusViewModel(Application app) {
        super(app);
        server = Server.getInstance();
        server.addOnConnectionStatusChangedListener(this);
        connectionStatus = new MutableLiveData<>();
        onConnectionStatusChanged();
    }

    private String getConnectionStatusString() {
        Resources resources = getApplication().getResources();
        PircBotX connection = server.getConnection();
        String connectionStatus;
        if (connection != null && connection.isConnected() && server.getStatus() == Server.STATUS_CONNECTED) {
            connectionStatus = connection.getUserBot().getNick();
        } else if (server.getStatus() == Server.STATUS_CONNECTING) {
            connectionStatus = resources.getString(R.string.connect_message);
        } else {
            connectionStatus = resources.getString(R.string.not_connected);
        }
        return connectionStatus;
    }

    public LiveData<String> getConnectionStatus() {
        return connectionStatus;
    }

    public void onConnectionStatusChanged() {
        String newConnectionStatus = getConnectionStatusString();
        connectionStatus.postValue(newConnectionStatus);
    }
}
