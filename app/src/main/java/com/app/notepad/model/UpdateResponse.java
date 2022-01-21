package com.app.notepad.model;

import com.google.gson.annotations.SerializedName;

public class UpdateResponse{

	@SerializedName("message")
	private String message;

	@SerializedName("content")
	private String content;

	@SerializedName("statusCode")
	private String statusCode;

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setContent(String content){
		this.content = content;
	}

	public String getContent(){
		return content;
	}

	public void setStatusCode(String statusCode){
		this.statusCode = statusCode;
	}

	public String getStatusCode(){
		return statusCode;
	}
}