package com.example.notebook;

import java.util.ArrayList;
import java.util.Date;

public class Note {
	private String title;
	private String content;
	private byte[] imgByte;
	private int id;
	private Date modified;
	private ArrayList<InsertedImage> imgList = new ArrayList<InsertedImage>();
	
	public Note() {
		// TODO Auto-generated constructor stub
	}
	
	public void setId(int id){
		this.id = id;
		for(int i=0;i<this.imgList.size();i++)
			this.imgList.get(i).setNid(id);
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public void setContent(String content){
		this.content = content;
	}
	
	public void setImg(byte[] imgByte){
		this.imgByte = imgByte;
	}
	
	public void setModified(Date modified) {
		this.modified = modified;
	}
	
	public void addImg(InsertedImage img){
		this.imgList.add(img);
	}
	
	public void setImgList(ArrayList<InsertedImage> imgList){
		this.imgList = imgList;
	}
	
	public int getId(){
		return this.id;
	}
	
	public String getTitle(){
		return this.title;
	}
	
	public String getContent(){
		return this.content;
	}
	
	public byte[] getImg(){
		return this.imgByte;
	}
	
	public Date getModified(){
		return this.modified;
	}
	
	public ArrayList<InsertedImage> getImgList(){
		return this.imgList;
	}

}
