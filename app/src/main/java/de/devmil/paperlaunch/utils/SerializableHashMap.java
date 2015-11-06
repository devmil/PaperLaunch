package de.devmil.paperlaunch.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SerializableHashMap<K, V> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Object[] keyArray;
	private Object[] valuesArray;

	public SerializableHashMap() {
		
	}

	public SerializableHashMap(Map<?, ?> map) {
		populateFromhashMap(map);
	}

	public void populateFromhashMap(Map<?, ?> map) {
		int size = map == null ? 0 : map.size();
		keyArray = new Object[size];
		valuesArray = new Object[size];
		if(map != null) {
			int idx = 0;
			for(Object k : map.keySet()) {
				keyArray[idx] = k;
				valuesArray[idx] = map.get(k);
				idx++;
			}
		}
	}
	
	public Map<K, V> getHashMap() {
		if(keyArray == null || valuesArray == null)
			return null;
		HashMap<K, V> result = new HashMap<K, V>(keyArray.length);
		fillHashMap(result);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public void fillHashMap(HashMap<K, V> map) {
		if(keyArray == null || valuesArray == null)
			return;
		for(int i=0; i<keyArray.length; i++)
			map.put((K)keyArray[i], (V)valuesArray[i]);		
	}
}
