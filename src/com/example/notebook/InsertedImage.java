package com.example.notebook;

public class InsertedImage {
	
	private int nid;
	private int mid;
	private byte[] imgByte;
	private int position;
	
	public InsertedImage() {
		// TODO Auto-generated constructor stub

	}

	public InsertedImage(int nid, byte[] imgByte) {
		// TODO Auto-generated constructor stub
		this.nid = nid;
		this.imgByte = imgByte;
	}
	
	public InsertedImage(int nid, byte[] imgByte, int position) {
		// TODO Auto-generated constructor stub
		this.nid = nid;
		this.imgByte = imgByte;
		this.position = position;
	}
	
	public InsertedImage( byte[] imgByte, int position) {
		// TODO Auto-generated constructor stub
		this.imgByte = imgByte;
		this.position = position;
	}
	
	public void setNid(int nid){
		this.nid = nid;
	}
	
	public void setMid(int mid){
		this.mid = mid;
	}
	
	public void setImgByte(byte[] imgByte){
		this.imgByte = imgByte;
	}
	
	public void setPosition(int position){
		this.position = position;
	}
	
	public int getNid(){
		return this.nid;
	}
	
	public int getMid(){
		return this.mid;
	}
	
	public int getPosition(){
		return this.position;
	}
	
	public byte[] getImg(){
		return this.imgByte;
	}

}
