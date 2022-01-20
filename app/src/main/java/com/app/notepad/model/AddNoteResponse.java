package com.app.notepad.model;

import com.google.gson.annotations.SerializedName;

public class AddNoteResponse{

	@SerializedName("message")
	private String message;

	@SerializedName("content")
	private Content content;

	@SerializedName("statusCode")
	private String statusCode;

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setContent(Content content){
		this.content = content;
	}

	public Content getContent(){
		return content;
	}

	public void setStatusCode(String statusCode){
		this.statusCode = statusCode;
	}

	public String getStatusCode(){
		return statusCode;
	}
}