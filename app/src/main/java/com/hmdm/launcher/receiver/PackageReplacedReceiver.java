/*
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hmdm.launcher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hmdm.launcher.Const;
import com.hmdm.launcher.helper.SettingsHelper;
import com.hmdm.launcher.ui.MainActivity;
import com.hmdm.launcher.util.RemoteLogger;

/**
 * When the launcher updates its own APK, Android kills the process and does NOT relaunch it,
 * so the lockdown (the launcher running in the foreground as Home and blocking other apps) is lost
 * until something brings it back.
 *
 * MY_PACKAGE_REPLACED is delivered to our own app right after it has been updated, even though the
 * app is in the stopped state. We use it to relaunch the launcher and restore the lockdown without
 * depending on the external EMUI launcher restarter helper app.
 */
public class PackageReplacedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(Const.LOG_TAG, "Got the MY_PACKAGE_REPLACED broadcast, relaunching the launcher");
        RemoteLogger.log(context, Const.LOG_DEBUG, "Launcher updated itself, relaunching to restore lockdown");

        SettingsHelper settingsHelper = SettingsHelper.getInstance(context.getApplicationContext());
        if (!settingsHelper.isBaseUrlSet()) {
            // Not enrolled yet (e.g. update before initial setup) - nothing to restore
            return;
        }

        // Bring our launcher back to the foreground. As the device owner / default Home app we are
        // exempt from background activity start restrictions, so this is allowed after the update.
        try {
            Intent launchIntent = new Intent(context, MainActivity.class);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            context.startActivity(launchIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
