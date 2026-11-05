package com.exemplo.favoritos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.view.ViewGroup;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String HOME = "https://appassets.androidplatform.net/assets/index.html";
    private static final int OPEN_FILE = 105;
    private static final int CREATE_FILE = 106;
    private WebView browser;
    private TextView estado;
    private ValueCallback<Uri[]> seletorArquivo;
    private byte[] aguardandoSalvar;
    private boolean bridgeAtiva = false;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(15,17,21));
        getWindow().setNavigationBarColor(Color.rgb(15,17,21));
        LinearLayout coluna = new LinearLayout(this);
        coluna.setOrientation(LinearLayout.VERTICAL);
        coluna.setBackgroundColor(Color.rgb(15,17,21));
        LinearLayout barra = new LinearLayout(this);
        barra.setPadding(8, 7, 8, 7);
        barra.setBackgroundColor(Color.rgb(32,28,68));
        Button voltar = criarBotao("← Voltar");
        Button inicio = criarBotao("⌂ Favoritos");
        barra.addView(voltar);
        barra.addView(inicio);
        estado = new TextView(this);
        estado.setText("   Local • sem internet");
        estado.setTextColor(Color.WHITE);
        estado.setTextSize(12);
        estado.setPadding(8,7,8,7);
        barra.addView(estado, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        coluna.addView(barra);
        browser = new WebView(this);
        browser.setBackgroundColor(Color.rgb(15,17,21));
        coluna.addView(browser,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1f));
        setContentView(coluna);
        WebSettings s = browser.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true); // IndexedDB e localStorage isolados por app
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(true); // arquivos escolhidos pelo usuário no seletor Android
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setSupportMultipleWindows(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        if (Build.VERSION.SDK_INT >= 26) s.setSafeBrowsingEnabled(true);
        browser.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest req) {
                Uri uri = req.getUrl();
                if (!"appassets.androidplatform.net".equalsIgnoreCase(uri.getHost())) return null;
                if (!"https".equals(uri.getScheme())) return null;
                String path = uri.getPath();
                if (!"/assets/index.html".equals(path)) {
                    return new WebResourceResponse("text/plain", "UTF-8", 404, "Not found", null,
                            new java.io.ByteArrayInputStream(new byte[0]));
                }
                try {
                    InputStream stream = getAssets().open("index.html");
                    return new WebResourceResponse("text/html", "UTF-8", stream);
                } catch (Exception e) {
                    return new WebResourceResponse("text/plain", "UTF-8", new java.io.ByteArrayInputStream(new byte[0]));
                }
            }
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest req) {
                Uri u = req.getUrl();
                String scheme = u.getScheme();
                if (scheme == null) return true;
                if (scheme.equals("https") || scheme.equals("http") || scheme.equals("blob") || scheme.equals("data") || scheme.equals("about")) {
                    // Páginas externas não recebem a ponte de salvar arquivos.
                    if (!HOME.equals(u.toString())) retirarBridge();
                    return false;
                }
                return true;
            }
            @Override public void onPageFinished(WebView v, String url) {
                boolean isHome = HOME.equals(url);
                estado.setText(isHome ? "   Offline / locais" : "   Web / link");
            }
        });
        browser.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView web, ValueCallback<Uri[]> callback,
                    FileChooserParams params) {
                if (seletorArquivo != null) seletorArquivo.onReceiveValue(null);
                seletorArquivo = callback;
                try {
                    Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("*/*");
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(i, OPEN_FILE);
                } catch(Exception e) {
                    seletorArquivo = null;
                    callback.onReceiveValue(null);
                    Toast.makeText(MainActivity.this,"Não foi possível abrir arquivos", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
        voltar.setOnClickListener(v -> voltar());
        inicio.setOnClickListener(v -> abrirInicio());
        abrirInicio();
    }

    private Button criarBotao(String texto) {
        Button b = new Button(this);
        b.setText(texto); b.setTextAllCaps(false);
        b.setTextSize(12);
        return b;
    }
    @SuppressLint("AddJavascriptInterface")
    private void abrirInicio() {
        if (!bridgeAtiva) {
            browser.addJavascriptInterface(new PonteDeExportacao(), "AndroidBridge");
            bridgeAtiva = true;
        }
        browser.loadUrl(HOME);
    }
    private void retirarBridge() {
        if (bridgeAtiva) {
            browser.removeJavascriptInterface("AndroidBridge");
            bridgeAtiva = false;
        }
    }
    @SuppressLint("AddJavascriptInterface")
    private void voltar() {
        if (browser.canGoBack()) {
            // A interface reaparece só quando a tela anterior terminar de carregar.
            // O bridge continua restrito ao endereço HOME pelo check em saveDocument().
            if (!bridgeAtiva) {
                browser.addJavascriptInterface(new PonteDeExportacao(), "AndroidBridge");
                bridgeAtiva = true;
            }
            browser.goBack();
        } else abrirInicio();
    }
    @Override public void onBackPressed() { voltar(); }

    private final class PonteDeExportacao {
        @JavascriptInterface public void saveDocument(String nome, String mime, String base64) {
            runOnUiThread(() -> {
                // Não fornecer a ponte de exportação para sites externos.
                if (!HOME.equals(browser.getUrl())) return;
                if (base64 == null || base64.length() > 11_000_000) {
                    Toast.makeText(MainActivity.this,"Arquivo grande demais para exportar",Toast.LENGTH_LONG).show(); return;
                }
                try {
                    aguardandoSalvar = Base64.getDecoder().decode(base64);
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType(mime == null || mime.isEmpty() ? "application/octet-stream" : mime.split(";")[0]);
                    intent.putExtra(Intent.EXTRA_TITLE, nome == null ? "arquivo" : nome.replace("/", "_"));
                    startActivityForResult(intent, CREATE_FILE);
                } catch (Exception e) {
                    aguardandoSalvar = null;
                    Toast.makeText(MainActivity.this, "Não consegui preparar arquivo: " + e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == OPEN_FILE) {
            if (seletorArquivo != null) {
                Uri[] uris = (result == RESULT_OK && data != null && data.getData() != null)
                        ? new Uri[]{data.getData()} : null;
                seletorArquivo.onReceiveValue(uris);
                seletorArquivo = null;
            }
        } else if (request == CREATE_FILE) {
            if (result == RESULT_OK && data != null && data.getData() != null && aguardandoSalvar != null) {
                try (OutputStream out = getContentResolver().openOutputStream(data.getData())) {
                    if (out != null) {
                        out.write(aguardandoSalvar);
                        Toast.makeText(this, "Arquivo salvo", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this,"Erro ao salvar: " + e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
            aguardandoSalvar = null;
        }
    }
    @Override public void onDestroy() {
        if (browser != null) browser.destroy();
        super.onDestroy();
    }
}
