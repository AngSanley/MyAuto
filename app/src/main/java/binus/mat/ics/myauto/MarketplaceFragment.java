package binus.mat.ics.myauto;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class MarketplaceFragment extends Fragment {

    private WebView myWebView;
    private SwipeRefreshLayout mySwipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments

        return inflater.inflate(R.layout.fragment_marketplace, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mySwipeRefreshLayout = view.findViewById(R.id.swipeContainer);
        myWebView = view.findViewById(R.id.webviewMarketplace);

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mySwipeRefreshLayout.setRefreshing(true);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mySwipeRefreshLayout.setRefreshing(false);
                super.onPageFinished(view, url);
            }
        });
        myWebView.loadUrl("http://www.google.com");

        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        myWebView.reload();
                    }
                }
        );

    }

    public void onBackKeyPressed(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (myWebView.canGoBack()) myWebView.goBack();
            else getActivity().finish();
        }
    }
}