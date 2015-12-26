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

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by devmil on 18.04.14.
 */
public class LicenseInfo implements ILicenseAccess {

    private static String TAG = LicenseInfo.class.getSimpleName();

    private static String LICENSE_ARRAY_IDENTIFIER = "license";
    private static String PACKAGE_ARRAY_IDENTIFIER = "package";

    private Dictionary<String, LicenseDefinition> _Licenses;
    private List<PackageInfo> _Packages;

    private LicenseInfo()
    {
        _Licenses = new Hashtable<String, LicenseDefinition>();
        _Packages = new ArrayList<PackageInfo>();
    }

    public List<PackageInfo> getPackages()
    {
        return _Packages;
    }

    public static LicenseInfo readFromJSON(JSONObject obj) {
        LicenseInfo result = new LicenseInfo();
        JSONArray licenses = null;
        try {
            licenses = obj.getJSONArray(LICENSE_ARRAY_IDENTIFIER);
        } catch (JSONException e) {
            Log.w(TAG, "Error reading LicenseInfo", e);
            return null;
        }
        for(int i=0; i<licenses.length(); i++)
        {
            try {
                JSONObject licenseObj = licenses.getJSONObject(i);
                LicenseDefinition ld = LicenseDefinition.readFromJSON(licenseObj);
                if(ld != null)
                    result._Licenses.put(ld.getId(), ld);
            } catch (JSONException e) {
                Log.w(TAG, "Error reading LicenseInfo", e);
            }
        }
        JSONArray packages = null;
        try {
            packages = obj.getJSONArray(PACKAGE_ARRAY_IDENTIFIER);
        } catch (JSONException e) {
            Log.w(TAG, "Error reading LicenseInfo", e);
            return null;
        }
        for(int i=0; i<packages.length(); i++)
        {
            try
            {
                JSONObject packageObj = packages.getJSONObject(i);
                PackageInfo pi = PackageInfo.readFromJSON(packageObj, result);
                if(pi != null)
                    result._Packages.add(pi);
            }
            catch (JSONException e)
            {
                Log.w(TAG, "Error reading LicenseInfo", e);
            }
        }
        return result;
    }



    @Override
    public LicenseDefinition getLicense(String identifier) {
        return _Licenses.get(identifier);
    }
}
