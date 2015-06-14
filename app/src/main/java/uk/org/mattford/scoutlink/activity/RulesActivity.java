package uk.org.mattford.scoutlink.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.webkit.WebView;

import uk.org.mattford.scoutlink.R;

public class RulesActivity extends ActionBarActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);
        WebView webview = (WebView)findViewById(R.id.webView);
        webview.loadUrl("http://www.scoutlink.net/get-connected/rules");
    }
}
