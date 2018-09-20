package uk.org.mattford.scoutlink.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import uk.org.mattford.scoutlink.R;

public class RulesActivity extends AppCompatActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);
        WebView webview = (WebView)findViewById(R.id.webView);
        webview.setWebViewClient(new WebViewClient());
        webview.loadUrl("http://www.scoutlink.net/get-connected/rules");
    }
}
