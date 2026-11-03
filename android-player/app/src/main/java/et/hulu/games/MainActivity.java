package et.hulu.games;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

// The Hulu Games lobby in a full-screen WebView. The page sees "HuluApp" in the user agent and shows its own
// sign-in (phone number, then a code from the Telegram bot).
public class MainActivity extends Activity {
    private static final String URL = "https://kibertemesgen19-lgtm.github.io/Hulu-lobby/";
    private static final String[] HOSTS = {"kibertemesgen19-lgtm.github.io", "hulu-games-web-production.up.railway.app"};
    private static final int PICK = 1;
    private WebView web;
    private ValueCallback<Uri[]> picked;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setUserAgentString(s.getUserAgentString() + " HuluApp/1.0");
        CookieManager.getInstance().setAcceptCookie(true);
        // Games run in an iframe from the game provider and need their own cookies.
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true);
        web.setWebViewClient(new WebViewClient() {
            // The lobby and the Hulu server stay in the app; other links (Telegram, support) open outside it.
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                if (!req.isForMainFrame()) return false;
                Uri u = req.getUrl();
                for (String h : HOSTS) if (h.equals(u.getHost())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception e) { }
                return true;
            }
        });
        // Lets the deposit form attach a receipt picture from the gallery.
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (picked != null) picked.onReceiveValue(null);
                picked = callback;
                try {
                    startActivityForResult(params.createIntent(), PICK);
                } catch (Exception e) {
                    picked = null;
                    return false;
                }
                return true;
            }
        });
        setContentView(web);
        if (state != null) web.restoreState(state); else web.loadUrl(URL);
    }

    @Override
    protected void onActivityResult(int code, int result, Intent data) {
        if (code == PICK && picked != null) {
            picked.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result, data));
            picked = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
