package com.sollyu.android.appenv.activity;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ListHolder;
import com.sollyu.android.appenv.R;
import com.sollyu.android.appenv.helper.LibSuHelper;
import com.sollyu.android.appenv.helper.RandomHelper;
import com.sollyu.android.appenv.helper.XposedSharedPreferencesHelper;
import com.sollyu.android.appenv.module.AppInfo;
import com.sollyu.android.appenv.view.DetailItem;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {

    private ApplicationInfo applicationInfo    = null;
    private Integer         activityResultCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        applicationInfo = getIntent().getParcelableExtra("applicationInfo");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            switch (applicationInfo.packageName) {
                case "all":
                    getSupportActionBar().setTitle("全局拦截");
                    findViewById(R.id.menu_run_app).setEnabled(false);
                    findViewById(R.id.menu_clear_app).setEnabled(false);
                    findViewById(R.id.menu_force_stop).setEnabled(false);
                    break;
                case "user":
                    getSupportActionBar().setTitle("第三方拦截");
                    findViewById(R.id.menu_run_app).setEnabled(false);
                    findViewById(R.id.menu_clear_app).setEnabled(false);
                    findViewById(R.id.menu_force_stop).setEnabled(false);
                    break;
                default:
                    getSupportActionBar().setTitle(applicationInfo.loadLabel(getPackageManager()));
                    break;
            }
        }

        appInfoToUi(XposedSharedPreferencesHelper.getInstance().get(applicationInfo.packageName));
        getOverflowMenu();
    }

    //强制显示菜单中的更多按钮
    private void getOverflowMenu() {
        try {
            ViewConfiguration config       = ViewConfiguration.get(this);
            Field             menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            MobclickAgent.reportError(this, e);
        }
    }

    /**
     * 强制让目录显示图标
     *
     * @param featureId 1
     * @param menu      2
     * @return 1
     */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    MobclickAgent.reportError(this, e);
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    /**
     * 强制让目录显示图标
     *
     * @param view v
     * @param menu m
     * @return b
     */
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    MobclickAgent.reportError(this, e);
                }
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DetailActivity.this.setResult(activityResultCode);
        DetailActivity.this.finish();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void appInfoToUi(AppInfo appInfo) {
        if (appInfo == null)
            return;

        DetailItem manufacturer          = (DetailItem) findViewById(R.id.manufacturer);
        DetailItem model                 = (DetailItem) findViewById(R.id.model);
        DetailItem serial                = (DetailItem) findViewById(R.id.serial);
        DetailItem phone_number          = (DetailItem) findViewById(R.id.phone_number);
        DetailItem phone_network_type    = (DetailItem) findViewById(R.id.phone_network_type);
        DetailItem phone_device_id       = (DetailItem) findViewById(R.id.phone_device_id);
        DetailItem sim_serial_number     = (DetailItem) findViewById(R.id.sim_serial_number);
        DetailItem wifi_info_ssid        = (DetailItem) findViewById(R.id.wifi_info_ssid);
        DetailItem wifi_info_mac_address = (DetailItem) findViewById(R.id.wifi_info_mac_address);

        manufacturer.getEditText().setText(appInfo.buildManufacturer);
        model.getEditText().setText(appInfo.buildModel);
        serial.getEditText().setText(appInfo.buildSerial);
        phone_number.getEditText().setText(appInfo.telephonyGetLine1Number);
        phone_network_type.getEditText().setText(appInfo.telephonyGetNetworkType);
        phone_device_id.getEditText().setText(appInfo.telephonyGetDeviceId);
        sim_serial_number.getEditText().setText(appInfo.telephonyGetSimSerialNumber);
        wifi_info_ssid.getEditText().setText(appInfo.wifiInfoGetSSID);
        wifi_info_mac_address.getEditText().setText(appInfo.wifiInfoGetMacAddress);
    }

    private AppInfo uiToAppInfo() {
        DetailItem manufacturer          = (DetailItem) findViewById(R.id.manufacturer);
        DetailItem model                 = (DetailItem) findViewById(R.id.model);
        DetailItem serial                = (DetailItem) findViewById(R.id.serial);
        DetailItem phone_number          = (DetailItem) findViewById(R.id.phone_number);
        DetailItem phone_network_type    = (DetailItem) findViewById(R.id.phone_network_type);
        DetailItem phone_device_id       = (DetailItem) findViewById(R.id.phone_device_id);
        DetailItem sim_serial_number     = (DetailItem) findViewById(R.id.sim_serial_number);
        DetailItem wifi_info_ssid        = (DetailItem) findViewById(R.id.wifi_info_ssid);
        DetailItem wifi_info_mac_address = (DetailItem) findViewById(R.id.wifi_info_mac_address);

        AppInfo appInfo = new AppInfo();
        appInfo.buildManufacturer = manufacturer.getEditText().getText().toString();
        appInfo.buildModel = model.getEditText().getText().toString();
        appInfo.buildSerial = serial.getEditText().getText().toString();
        appInfo.telephonyGetLine1Number = phone_number.getEditText().getText().toString();
        appInfo.telephonyGetNetworkType = phone_network_type.getEditText().getText().toString();
        appInfo.telephonyGetDeviceId = phone_device_id.getEditText().getText().toString();
        appInfo.telephonyGetSimSerialNumber = sim_serial_number.getEditText().getText().toString();
        appInfo.wifiInfoGetSSID = wifi_info_ssid.getEditText().getText().toString();
        appInfo.wifiInfoGetMacAddress = wifi_info_mac_address.getEditText().getText().toString();

        return appInfo;
    }

    public void onClickRandomAll(View view) {
        appInfoToUi(RandomHelper.getInstance().randomAll());
    }

    public void onClickRunApp(View view) {
        LibSuHelper.getInstance().addCommand("monkey -p " + applicationInfo.packageName + " -c android.intent.category.LAUNCHER 1", 0, (commandCode, exitCode, output) -> {
            if (exitCode != 0) {
                Snackbar.make(view, "start app error: " + exitCode, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public void onClickClearApp(View view) {
        LibSuHelper.getInstance().addCommand("pm clear " + applicationInfo.packageName, 0, (commandCode, exitCode, output) -> {
            if (exitCode != 0)
                Snackbar.make(view, "wipe data error: " + exitCode, Snackbar.LENGTH_LONG).show();
            else
                Snackbar.make(view, "wipe data success.", Snackbar.LENGTH_LONG).show();
        });
    }

    public void onClickForceStopApp(View view) {
        LibSuHelper.getInstance().addCommand("am force-stop " + applicationInfo.packageName, 0, (commandCode, exitCode, output) -> {
            if (exitCode != 0)
                Snackbar.make(view, "force stop app error: " + exitCode, Snackbar.LENGTH_LONG).show();
            else
                Snackbar.make(view, "force stop app success.", Snackbar.LENGTH_LONG).show();
        });
    }

    public void onClickSaveConfig(View view) {
        activityResultCode = 1;
        XposedSharedPreferencesHelper.getInstance().set(applicationInfo.packageName, uiToAppInfo());
    }

    public void onClickManufacturer(View view) {

        ArrayList<String> selectStringArrayList = new ArrayList<>();
        selectStringArrayList.add("小米");
        selectStringArrayList.add("魅族");
        selectStringArrayList.add("360");
        selectStringArrayList.add("乐视");
        selectStringArrayList.add("金立");
        selectStringArrayList.add("酷派");
        selectStringArrayList.add("联想");


        DialogPlus dialogPlus = DialogPlus.newDialog(view.getContext())
                .setHeader(R.layout.dialog_plus_header)
                .setContentHolder(new ListHolder())
                .setAdapter(new ArrayAdapter<>(view.getContext(), android.R.layout.simple_list_item_1, selectStringArrayList))
                .setOnItemClickListener((dialog, item, view1, position) -> {
                    DetailItem detailItem = (DetailItem) view;
                    detailItem.getEditText().setText(selectStringArrayList.get(position));
                    dialog.dismiss();
                })
                .setExpanded(true)
                .create();

        ((TextView) dialogPlus.getHeaderView().findViewById(R.id.text_view1)).setText(R.string.manufacturer);

        dialogPlus.show();
    }

    public void onClickModel(View view) {

    }

    public void onClickSerial(View view) {
        DetailItem detailItem = (DetailItem) view;
        detailItem.getEditText().setText(RandomHelper.getInstance().randomBuildSerial());
    }

    public void onClickLineNumber(View view) {
        DetailItem detailItem = (DetailItem) view;
        detailItem.getEditText().setText(RandomHelper.getInstance().randomTelephonyGetLine1Number());
    }

    public void onClickNetworkType(View view) {
        HashMap<String, Object> hashMap = RandomHelper.getInstance().getTelephonyGetNetworkTypeList();

        ArrayList<String> displayArrayList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
            displayArrayList.add(entry.getKey());
        }

        DialogPlus dialogPlus = DialogPlus.newDialog(view.getContext())
                .setHeader(R.layout.dialog_plus_header)
                .setContentHolder(new ListHolder())
                .setAdapter(new ArrayAdapter<>(view.getContext(), android.R.layout.simple_list_item_1, displayArrayList))
                .setOnItemClickListener((dialog, item, view1, position) -> {
                    DetailItem detailItem = (DetailItem) view;
                    detailItem.getEditText().setText(String.valueOf(hashMap.get(displayArrayList.get(position))));
                    dialog.dismiss();
                })
                .setExpanded(true)
                .create();

        ((TextView) dialogPlus.getHeaderView().findViewById(R.id.text_view1)).setText(R.string.phone_network_type);

        dialogPlus.show();
    }

    public void onClickDeviceId(View view) {
        DetailItem detailItem = (DetailItem) view;
        detailItem.getEditText().setText(RandomHelper.getInstance().randomTelephonyGetDeviceId());
    }

    public void onClickSimSerialNumber(View view) {
        DetailItem detailItem = (DetailItem) view;
        detailItem.getEditText().setText(RandomHelper.getInstance().randomTelephonySimSerialNumber());
    }

    public void onClickWifiInfoSSID(View view) {
        DetailItem detailItem = (DetailItem) view;
        detailItem.getEditText().setText(RandomHelper.getInstance().randomWifiInfoSSID());
    }

    public void onClickWifiInfoMacAddress(View view) {
        DetailItem detailItem = (DetailItem) view;
        detailItem.getEditText().setText(RandomHelper.getInstance().randomWifiInfoMacAddress());
    }

    public void onMenuClearConfig(MenuItem item) {
        XposedSharedPreferencesHelper.getInstance().remove(applicationInfo.packageName);
        activityResultCode = 1;
        DetailActivity.this.setResult(activityResultCode);
        DetailActivity.this.finish();
    }

    public void onMenuRemoteRandom(MenuItem item) {
    }
}