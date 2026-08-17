package com.sj0404.nimbusdeck;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Independent launcher for the official GeForce NOW Android client.
 *
 * NimbusDeck never receives provider credentials. Authentication and game
 * streaming stay in the official client.
 */
public final class MainActivity extends Activity implements NimbusDashboard.Actions {
    static final String SERVICE_PACKAGE = "com.nvidia.geforcenow";
    private static final Uri PLAY_STORE_URI = Uri.parse("market://details?id=" + SERVICE_PACKAGE);
    private static final Uri PLAY_STORE_WEB_URI = Uri.parse(
            "https://play.google.com/store/apps/details?id=" + SERVICE_PACKAGE);
    private static final Uri LEARN_MORE_URI = Uri.parse(
            "https://www.nvidia.com/en-us/geforce-now/how-to-play/");

    private NimbusDashboard dashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();
        dashboard = new NimbusDashboard(this, this);
        setContentView(dashboard);
        installSystemBarInsets(dashboard);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus(false);
    }

    @Override
    public void onPrimaryAction() {
        if (isServiceInstalled()) {
            launchOfficialClient();
        } else {
            openStoreListing();
        }
    }

    @Override
    public void onLearnMore() {
        openUri(LEARN_MORE_URI, R.string.open_failed);
    }

    @Override
    public void onRefresh() {
        refreshStatus(true);
    }

    @Override
    public void onOpenAppSettings() {
        if (!isServiceInstalled()) {
            openStoreListing();
            return;
        }
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + SERVICE_PACKAGE));
        openIntent(intent, R.string.open_failed);
    }

    private void refreshStatus(boolean announce) {
        if (dashboard == null) {
            return;
        }
        dashboard.render(new LauncherState(isOnline(), isServiceInstalled()));
        if (announce) {
            dashboard.announceForAccessibility(getString(R.string.refreshed));
        }
    }

    private boolean isServiceInstalled() {
        try {
            getPackageManager().getPackageInfo(SERVICE_PACKAGE, 0);
            return getPackageManager().getLaunchIntentForPackage(SERVICE_PACKAGE) != null;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    private boolean isOnline() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        Network network = manager.getActiveNetwork();
        if (network == null) {
            return false;
        }
        NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private void launchOfficialClient() {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(SERVICE_PACKAGE);
        if (launchIntent == null) {
            Toast.makeText(this, R.string.service_launch_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        openIntent(launchIntent, R.string.service_launch_failed);
    }

    private void openStoreListing() {
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, PLAY_STORE_URI);
        marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        try {
            startActivity(marketIntent);
        } catch (ActivityNotFoundException ignored) {
            openUri(PLAY_STORE_WEB_URI, R.string.open_failed);
        }
    }

    private void openUri(Uri uri, int errorMessage) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        openIntent(intent, errorMessage);
    }

    private void openIntent(Intent intent, int errorMessage) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException exception) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.rgb(3, 6, 18));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
        } else {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }

    private void installSystemBarInsets(final View content) {
        content.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
                int top;
                int bottom;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    android.graphics.Insets bars = insets.getInsets(
                            WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                    top = bars.top;
                    bottom = bars.bottom;
                } else {
                    top = insets.getSystemWindowInsetTop();
                    bottom = insets.getSystemWindowInsetBottom();
                }
                view.setPadding(0, top, 0, bottom);
                return insets;
            }
        });
        content.requestApplyInsets();
    }
}
