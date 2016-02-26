/*
 * Copyright 2014 Devmil Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.devmil.common.licensing;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by devmil on 18.04.14.
 */
public class LicenseManager {
    private int _LicenseInfoFileId;
    private Context _Context;
    private LicenseInfo _LicenseInfo;

    private static final String TAG = LicenseManager.class.getSimpleName();

    public LicenseManager(Context context, int licenseInfoFileId)
    {
        _Context = context;
        _LicenseInfoFileId = licenseInfoFileId;
        _LicenseInfo = loadLicenseInfo();
    }

    public LicenseInfo getLicenseInfo()
    {
        return _LicenseInfo;
    }

    private LicenseInfo loadLicenseInfo() {
        InputStream stream = _Context.getResources().openRawResource(_LicenseInfoFileId);

        InputStreamReader sr = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(sr);

        String line;
        StringBuilder licenseInfo = new StringBuilder();
        try {
            while ((line = br.readLine()) != null)
            {
                licenseInfo.append(line);
                licenseInfo.append("\n");
            }
        } catch (IOException e) {
            return null;
        }

        JSONObject obj = null;
        try {
            obj = new JSONObject(licenseInfo.toString());
        } catch (JSONException e) {
            Log.w(TAG, "Error reading LicenseInfo", e);
            return null;
        }
        return LicenseInfo.readFromJSON(obj);
    }
}
