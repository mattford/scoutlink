package uk.org.mattford.scoutlink.activity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import uk.org.mattford.scoutlink.R;

public class RulesActivity extends EdgeToEdgeActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);
        WebView webview = findViewById(R.id.webView);
        webview.setWebViewClient(new WebViewClient());
        webview.loadUrl("https://www.scoutlink.net/get-connected/rules");
    }
}
