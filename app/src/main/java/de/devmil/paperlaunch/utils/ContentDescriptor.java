package de.devmil.paperlaunch.utils;

import java.io.Serializable;

public class ContentDescriptor implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Object content;
	private Class<?> clazz;
	
	public ContentDescriptor(Class<?> clazz, Object content) {
		this.content = content;
		this.clazz = clazz;
	}
	
	public void setContent(Object content) {
		this.content = content;
	}
	public Object getContent() {
		return content;
	}
	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
	public Class<?> getClazz() {
		return clazz;
	}
}
