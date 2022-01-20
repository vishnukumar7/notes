package com.app.notepad.model;

import com.google.gson.annotations.SerializedName;

public class ContentItem{

	@SerializedName("notes")
	private String notes;

	@SerializedName("mobileno")
	private String mobileno;

	@SerializedName("createdtime")
	private String createdtime;

	@SerializedName("key")
	private String key;

	public void setNotes(String notes){
		this.notes = notes;
	}

	public String getNotes(){
		return notes;
	}

	public void setMobileno(String mobileno){
		this.mobileno = mobileno;
	}

	public String getMobileno(){
		return mobileno;
	}

	public void setCreatedtime(String createdtime){
		this.createdtime = createdtime;
	}

	public String getCreatedtime(){
		return createdtime;
	}

	public void setKey(String key){
		this.key = key;
	}

	public String getKey(){
		return key;
	}
}