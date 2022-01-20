package com.app.notepad.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class DataNoteResponse{

	@SerializedName("message")
	private String message;

	@SerializedName("content")
	private List<ContentItem> content;

	@SerializedName("statusCode")
	private String statusCode;

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setContent(List<ContentItem> content){
		this.content = content;
	}

	public List<ContentItem> getContent(){
		return content;
	}

	public void setStatusCode(String statusCode){
		this.statusCode = statusCode;
	}

	public String getStatusCode(){
		return statusCode;
	}
}