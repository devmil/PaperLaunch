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
public class LicenseDefinition {

    private static String ID_IDENTIFIER = "id";
    private static String URL_IDENTIFIER = "url";
    private static String NAME_IDENTIFIER = "name";
    private static String CONTENT_IDENTIFIER = "content";

    private final String _Id;
    private final String _Url;
    private final String _Content;
    private final String _Name;

    private LicenseDefinition(String id, String name, String url, String content)
    {
        _Id = id;
        _Name = name;
        _Url = url;
        _Content = content;
    }

    public String getId()
    {
        return _Id;
    }

    public String getUrl()
    {
        return _Url;
    }

    public String getContent()
    {
        return _Content;
    }

    public String getName() {
        return _Name;
    }

    public static LicenseDefinition readFromJSON(JSONObject obj)
    {
        try {
            String id = obj.getString(ID_IDENTIFIER);
            String name = obj.getString(NAME_IDENTIFIER);
            String url = obj.getString(URL_IDENTIFIER);
            String content = obj.getString(CONTENT_IDENTIFIER);
            LicenseDefinition result = new LicenseDefinition(id, name, url, content);

            return result;
        }
        catch(JSONException e)
        {
            Log.w(LicenseDefinition.class.getSimpleName(), "Error reading LicenseDefinition", e);
            return null;
        }
    }
}
