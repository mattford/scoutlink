package uk.org.mattford.scoutlink.model;

import android.widget.TextView;

public class Message {

	private String sender;
	private String text;
	private String channel;
	private int type;
	
	public final static int TYPE_CHANNEL = 0;
	public final static int TYPE_PRIVATE = 1;
	public final static int TYPE_SERVER = 2;
	public final static int TYPE_JOIN = 3;
	public final static int TYPE_MODE = 4;
	public final static int TYPE_TOPIC = 5;
	
	public Message(String sender, String text) {
		this.sender = sender;
		this.text = text;
	}
	
	public Message(int type) {
		this.type = type;
	}
	
	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public TextView renderTextView() {
		switch (this.type){
		
		}
		return null;
	}
	
}
