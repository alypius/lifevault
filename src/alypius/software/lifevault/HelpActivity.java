package alypius.software.lifevault;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Shows a html document for help
 */
public class HelpActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_layout);

        // Get webview and set html content
        WebView wv = (WebView) findViewById(R.id.webView);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.loadUrl("file:///android_asset/helpfile.html");

    }
}
