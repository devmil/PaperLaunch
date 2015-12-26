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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by devmil on 18.04.14.
 */
public class PackageInfo {

    private static String NAME_IDENTIFIER = "name";
    private static String VENDOR_IDENTIFIER = "vendor";
    private static String LICENSE_IDENTIFIER = "license";
    private static String URL_IDENTIFIER = "url";
    private static String COPYRIGHT_IDENTIFIER = "copyright";
    private static String ICON_IDENTIFIER = "icon";

    private final String _Name;
    private final String _Vendor;
    private final String _LicenseId;
    private final String _Url;
    private final String _Copyright;
    private final String _IconName;
    private final LicenseDefinition _License;

    private PackageInfo(String name, String vendor, String licenseId, String url, String copyright, String iconName, ILicenseAccess licenseAccess)
    {
        _Name = name;
        _Vendor = vendor;
        _LicenseId = licenseId;
        _Url = url;
        _Copyright = copyright;
        _IconName = iconName;
        _License = licenseAccess.getLicense(licenseId);
    }

    public static PackageInfo readFromJSON(JSONObject obj, ILicenseAccess licenseAccess)
    {
        try {
            String name = obj.getString(NAME_IDENTIFIER);
            String vendor = obj.getString(VENDOR_IDENTIFIER);
            String licenseId = obj.getString(LICENSE_IDENTIFIER);
            String url = obj.getString(URL_IDENTIFIER);
            String copyright = obj.getString(COPYRIGHT_IDENTIFIER);
            String iconName = obj.getString(ICON_IDENTIFIER);
            PackageInfo result = new PackageInfo(name, vendor, licenseId, url, copyright, iconName, licenseAccess);

            return result;
        }
        catch(JSONException e)
        {
            Log.w(PackageInfo.class.getSimpleName(), "Error reading LicenseDefinition", e);
            return null;
        }
    }

    public String getName() {
        return _Name;
    }

    public String getVendor() {
        return _Vendor;
    }

    public String getLicenseId() {
        return _LicenseId;
    }

    public String getUrl() {
        return _Url;
    }

    public String getCopyright() {
        return _Copyright;
    }

    public LicenseDefinition getLicense() {
        return _License;
    }

    public String getIconName() {
        return _IconName;
    }
}
