/*
 * Copyright 2015 Devmil Solutions
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
package de.devmil.paperlaunch.utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IntentSerializer {
	
	private static final String KEY_ACTION = "Action";
	private static final String KEY_CATEGORIES = "Categories";
	private static final String KEY_EXTRAS = "Extras";
	private static final String KEY_DATA = "Data";
	private static final String KEY_FLAGS = "Flags";
	private static final String KEY_PACKAGE = "Package";
	private static final String KEY_CLASS = "Class";
	
	
	public static String serialize(Intent intent) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		try {
			ObjectOutputStream objectOutStream = new ObjectOutputStream(
					outStream);
			HashMap<String, ContentDescriptor> contentStorage = new HashMap<String, ContentDescriptor>();
			addContent(contentStorage, "", intent);
			
			objectOutStream.writeObject(new SerializableHashMap<String, ContentDescriptor>(contentStorage));
			objectOutStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(IntentSerializer.class.getSimpleName(), "Error serializing Intent", e);
		}
		return Base64.encodeBytes(outStream.toByteArray());
	}
	
	public static Intent deserialize(String input) {
		if (input == null)
			return null;
		try {
			ByteArrayInputStream inStream = new ByteArrayInputStream(Base64
					.decode(input));
			ObjectInputStream objectInStream = new ObjectInputStream(inStream);
			if(inStream.available() > 0) {
				Object obj = objectInStream.readObject();
				Map<String, ContentDescriptor> contentStorage;
				if(SerializableHashMap.class.isAssignableFrom(obj.getClass())) {
					contentStorage = ((SerializableHashMap<String, ContentDescriptor>)obj).getHashMap();
				} else {
					contentStorage = (Map<String, ContentDescriptor>)obj;
				}
				return (Intent)getContent(contentStorage, "");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(IntentSerializer.class.getSimpleName(), "Error deserializing Intent", e);
		}
		return null;
	}
	
	private static void addContent(HashMap<String, ContentDescriptor> content, String key, Object object) {
		if(object == null)
			return;
		if(Bundle.class.isAssignableFrom(object.getClass())) {
			HashMap<String, ContentDescriptor> bundleData = new HashMap<String, ContentDescriptor>();
			Bundle castedObj = (Bundle)object;
			for(String k : castedObj.keySet()) {
				addContent(bundleData, k, castedObj.get(k));
			}
			content.put(key, new ContentDescriptor(Bundle.class, new SerializableHashMap<String, ContentDescriptor>(bundleData)));
		} else if(Intent.class.isAssignableFrom(object.getClass())) {
			HashMap<String, ContentDescriptor> intentData = new HashMap<String, ContentDescriptor>();
			Intent castedObj = (Intent)object;
			intentData.put(KEY_ACTION, new ContentDescriptor(String.class, castedObj.getAction()));

            String[] categories = null;

            if(castedObj.getCategories() != null) {
                categories = new String[castedObj.getCategories().size()];
                int idx = 0;
                for (String c : castedObj.getCategories())
                    categories[idx++] = c;
            }
			intentData.put(KEY_CATEGORIES, new ContentDescriptor(String[].class, categories));
			addContent(intentData, KEY_EXTRAS, castedObj.getExtras());
			if(castedObj.getDataString() != null)
				intentData.put(KEY_DATA, new ContentDescriptor(String.class, castedObj.getDataString()));
			intentData.put(KEY_FLAGS, new ContentDescriptor(int.class, castedObj.getFlags()));
			if(castedObj.getComponent() != null) {
				intentData.put(KEY_PACKAGE, new ContentDescriptor(String.class, castedObj.getComponent().getPackageName()));
				intentData.put(KEY_CLASS, new ContentDescriptor(String.class, castedObj.getComponent().getClassName()));
			}
			content.put(key, new ContentDescriptor(Intent.class, new SerializableHashMap<String, ContentDescriptor>(intentData)));
		} else if(Serializable.class.isAssignableFrom(object.getClass())) {
			content.put(key, new ContentDescriptor(object.getClass(), object));
		}
		else {
			//Add special handler?
		}
	}
	
	private static Object getContent(Map<String, ContentDescriptor> content, String key) {
		if(!content.containsKey(key))
			return null;
		ContentDescriptor desc = content.get(key);
		if(Bundle.class.isAssignableFrom(desc.getClazz())) {
			Map<String, ContentDescriptor> bundleData;
			if(SerializableHashMap.class.isAssignableFrom(desc.getContent().getClass())) {
				bundleData = ((SerializableHashMap<String, ContentDescriptor>)desc.getContent()).getHashMap();
			}else {
				bundleData = (Map<String, ContentDescriptor>)desc.getContent();
			}
			Bundle result = new Bundle();
			for(String k : bundleData.keySet()) {
				Object obj = getContent(bundleData, k);
				setBundleValue(result, k, obj);
			}
			return result;
		} else if(Intent.class.isAssignableFrom(desc.getClazz())) {
			Intent result = new Intent();
			Map<String, ContentDescriptor> intentData;
			if(SerializableHashMap.class.isAssignableFrom(desc.getContent().getClass())) {
				intentData = ((SerializableHashMap<String, ContentDescriptor>)desc.getContent()).getHashMap();
			}else {
				intentData = (Map<String, ContentDescriptor>)desc.getContent();
			}
			if(intentData.containsKey(KEY_ACTION)) 
				result.setAction((String)intentData.get(KEY_ACTION).getContent());
			if(intentData.containsKey(KEY_CATEGORIES)) {
				if(intentData.get(KEY_CATEGORIES).getContent() != null) {
					for(String c : (String[])intentData.get(KEY_CATEGORIES).getContent())
						result.addCategory(c);					
				}
			}
			if(intentData.containsKey(KEY_EXTRAS))
				result.putExtras((Bundle)getContent(intentData, KEY_EXTRAS));
			if(intentData.containsKey(KEY_DATA))
				result.setData(Uri.parse((String)getContent(intentData, KEY_DATA)));
			if(intentData.containsKey(KEY_FLAGS))
				result.setFlags((Integer)getContent(intentData, KEY_FLAGS));
			if(intentData.containsKey(KEY_PACKAGE))
				result.setPackage((String)getContent(intentData, KEY_PACKAGE));
			if(result.getPackage() != null && intentData.containsKey(KEY_CLASS))
				result.setClassName(result.getPackage(), (String)getContent(intentData, KEY_CLASS));
			return result;
		} else {
			return content.get(key).getContent();
		}
	}

	private static void setBundleValue(Bundle bundle, String key, Object value) {
//		if(Boolean.class.isAssignableFrom(value.getClass()))
//			bundle.putBoolean(key, (Boolean)value);
//		if(boolean[].class.isAssignableFrom(value.getClass()))
//			bundle.putBooleanArray(key, (boolean[])value);
		if(Bundle.class.isAssignableFrom(value.getClass()))
			bundle.putBundle(key, (Bundle)value);
//		if(byte.class.isAssignableFrom(value.getClass()))
//			bundle.putByte(key, (Byte)value);
//		if(byte[].class.isAssignableFrom(value.getClass()))
//			bundle.putByteArray(key, (byte[])value);
//		if(Character.class.isAssignableFrom(value.getClass()))
//			bundle.putChar(key, (Character)value);
//		if(char[].class.isAssignableFrom(value.getClass()))
//			bundle.putCharArray(key, (char[])value);
//		if(double.class.isAssignableFrom(value.getClass()))
//			bundle.putDouble(key, (Double)value);
//		if(double[].class.isAssignableFrom(value.getClass()))
//			bundle.putDoubleArray(key, (double[])value);
//		if(float.class.isAssignableFrom(value.getClass()))
//			bundle.putFloat(key, (Float)value);
//		if(float[].class.isAssignableFrom(value.getClass()))
//			bundle.putFloatArray(key, (float[])value);
//		if(int.class.isAssignableFrom(value.getClass()))
//			bundle.putInt(key, (Integer)value);
//		if(int[].class.isAssignableFrom(value.getClass()))
//			bundle.putIntArray(key, (int[])value);
//		if(ArrayList.class.isAssignableFrom(value.getClass()) && Integer.class.isAssignableFrom((Class<?>)value.getClass().getGenericInterfaces()[0]))
//			bundle.putIntegerArrayList(key, (ArrayList<Integer>)value);
//		if(long.class.isAssignableFrom(value.getClass()))
//			bundle.putLong(key, (Long)value);
//		if(long[].class.isAssignableFrom(value.getClass()))
//			bundle.putLongArray(key, (long[])value);
//		if(short.class.isAssignableFrom(value.getClass()))
//			bundle.putShort(key, (Short)value);
//		if(short[].class.isAssignableFrom(value.getClass()))
//			bundle.putShortArray(key, (short[])value);
//		if(SparseArray.class.isAssignableFrom(value.getClass()) && Parcelable.class.isAssignableFrom((Class<?>)value.getClass().getGenericInterfaces()[0]))
//			bundle.putSparseParcelableArray(key, (SparseArray<Parcelable>)value);
//		if(String.class.isAssignableFrom(value.getClass()))
//			bundle.putString(key, (String)value);
//		if(String[].class.isAssignableFrom(value.getClass()))
//			bundle.putStringArray(key, (String[])value);
//		if(ArrayList.class.isAssignableFrom(value.getClass()) && String.class.isAssignableFrom((Class<?>)value.getClass().getGenericInterfaces()[0]))
//			bundle.putStringArrayList(key, (ArrayList<String>)value);
//		if(CharSequence.class.isAssignableFrom(value.getClass()))
//			bundle.putCharSequence(key, (CharSequence)value);
		else if(Serializable.class.isAssignableFrom(value.getClass()))
			bundle.putSerializable(key, (Serializable)value);
		else if(Parcelable.class.isAssignableFrom(value.getClass()))
			bundle.putParcelable(key, (Parcelable)value);
		else if(Parcelable[].class.isAssignableFrom(value.getClass()))
			bundle.putParcelableArray(key, (Parcelable[])value);
		else if(ArrayList.class.isAssignableFrom(value.getClass()) && Parcelable.class.isAssignableFrom((Class<?>)value.getClass().getGenericInterfaces()[0]))
			bundle.putParcelableArrayList(key, (ArrayList<Parcelable>)value);
		
	}
}
