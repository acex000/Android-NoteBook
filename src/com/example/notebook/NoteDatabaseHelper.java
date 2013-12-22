package com.example.notebook;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NoteDatabaseHelper extends SQLiteOpenHelper {
	private static final String TAG = "dbHelper";
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "note_book";

	private static final String TABLE_NOTE = "note";
	private static final String COLUMN_NOTE_ID = "_id"; // convention
	private static final String COLUMN_NOTE_TITLE = "title";
	private static final String COLUMN_NOTE_CONTENT = "content";// //
	private static final String COLUMN_NOTE_BITMAP = "bitmap";
	private static final String COLUMN_NOTE_MODIFIED = "modified";
	
	private static final String TABLE_IMAGE = "image";
	private static final String COLUMN_IMAGE_ID = "_mid";
	private static final String COLUMN_IMAGE_NOTE_ID = "nid";
	private static final String COLUMN_IMAGE_BITMAP = "bitmap";
	private static final String COLUMN_IMAGE_POSITION = "position";
	
	

	public NoteDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("create table " + TABLE_NOTE + "(" 
				+ COLUMN_NOTE_ID + " integer primary key autoincrement, " 
				+ COLUMN_NOTE_TITLE + " text, " 
				+ COLUMN_NOTE_CONTENT + " text, "
				+ COLUMN_NOTE_BITMAP + " BLOB, "
				+ COLUMN_NOTE_MODIFIED + " text)");// //
		
		db.execSQL("create table " + TABLE_IMAGE + "(" 
				+ COLUMN_IMAGE_ID + " integer primary key autoincrement, " 
				+ COLUMN_IMAGE_NOTE_ID + " integer, " 
				+ COLUMN_IMAGE_BITMAP + " BLOB, "
				+ COLUMN_IMAGE_POSITION + " integer)");// //

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		// Drop older table if exists
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE);// //
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGE);
		// create tables again
		onCreate(db);

	}

	// delete all tables and rebuild database
	public void rebuildTable() {
		SQLiteDatabase db = this.getReadableDatabase();
		this.onUpgrade(db, DATABASE_VERSION, DATABASE_VERSION);
	}

	public long insertNote(Note note) {

		ContentValues cv = new ContentValues();
		cv.put(COLUMN_NOTE_TITLE, note.getTitle());
		cv.put(COLUMN_NOTE_CONTENT, note.getContent());
		cv.put(COLUMN_NOTE_BITMAP, note.getImg());// //
		cv.put(COLUMN_NOTE_MODIFIED, note.getModified().toString());
		// return id of new note
		return getWritableDatabase().insert(TABLE_NOTE, null, cv);

	}
	
	//insert all images
	public void insertAllImages(Note note){

		for(int i=0;i<note.getImgList().size();i++){
			insertImage(note, i);
		}
	}
	//insert single image
	public long insertImage(Note note, int index){
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_IMAGE_NOTE_ID, note.getId());
		cv.put(COLUMN_IMAGE_BITMAP, note.getImgList().get(index).getImg());
		cv.put(COLUMN_IMAGE_POSITION, note.getImgList().get(index).getPosition());// //
		return getWritableDatabase().insert(TABLE_IMAGE, null, cv);
	}

	public List<Note> getAllNotes() throws ParseException {
		List<Note> noteList = new ArrayList<Note>();

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from " + TABLE_NOTE, null);

		// loop through all query results
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			Note note = new Note();

			// we only need id and title to display in the list
			note.setId(cursor.getInt(0));
			note.setTitle(cursor.getString(1));
			//get date
			SimpleDateFormat simFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
			Date date = simFormat.parse(cursor.getString(4));
			note.setModified(date);
			noteList.add(note);

		}
		return noteList;
	}

	public byte[] getImg(int id) {
		byte[] imgByte;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("select " + COLUMN_NOTE_BITMAP
				+ " from " + TABLE_NOTE + " where _id=" + id, null);
		// loop through all query results
		cursor.moveToFirst();
		imgByte = cursor.getBlob(0);

		return imgByte;
	}

	public String getContent(int id) {
		String content;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("select " + COLUMN_NOTE_CONTENT + " from "
				+ TABLE_NOTE + " where _id=" + id, null);
		// loop through all query results
		cursor.moveToFirst();
		content = cursor.getString(0);

		return content;
	}

	public Note getNote(int id) throws ParseException {
		Note note = new Note();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from " + TABLE_NOTE
				+ " where _id=" + id, null);
		cursor.moveToFirst();
		note.setId(cursor.getInt(0));
		note.setTitle(cursor.getString(1));
		note.setContent(cursor.getString(2));
		note.setImg(cursor.getBlob(3));
		//get date
		SimpleDateFormat simFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
		Date date = simFormat.parse(cursor.getString(4));
		note.setModified(date);
		note.setImgList(getInsertedImages(note));

		return note;
	}
	
	public int getNewestNoteId(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from " + TABLE_NOTE + " where _id = (select max(_id) from note)", null);
		cursor.moveToFirst();
		return cursor.getInt(0);
	}
	
	public ArrayList<InsertedImage> getInsertedImages(Note note){
		ArrayList<InsertedImage> list = new ArrayList<InsertedImage>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from " + TABLE_IMAGE
				+ " where nid=" + note.getId(), null);
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			InsertedImage insertedImg = new InsertedImage();
			insertedImg.setMid(cursor.getInt(0));
			insertedImg.setNid(cursor.getInt(1));
			insertedImg.setImgByte(cursor.getBlob(2));
			insertedImg.setPosition(cursor.getInt(3));
			list.add(insertedImg);
			
		}
		return list;
	}

	public void update(Note note) {
		ContentValues cv = new ContentValues();

		cv.put(COLUMN_NOTE_TITLE, note.getTitle());
		cv.put(COLUMN_NOTE_CONTENT, note.getContent());
		cv.put(COLUMN_NOTE_BITMAP, note.getImg());// //
		cv.put(COLUMN_NOTE_MODIFIED, note.getModified().toString());
		getWritableDatabase().update(TABLE_NOTE, cv, COLUMN_NOTE_ID + " = "+note.getId(), null);
		
		updateImages(note);
		
	}
	
	private void updateImages(Note note){
		Note oldNote = new Note();
		oldNote.setId(note.getId());
		oldNote.setImgList(note.getImgList());
		oldNote.setImgList(getInsertedImages(note));
		
		//insert new images and update the original images
		for(int i=0;i<note.getImgList().size();i++){
			boolean findSame = false;
			for(int j=0;j<oldNote.getImgList().size();j++){

				if(note.getImgList().get(i).getMid()==oldNote.getImgList().get(j).getMid()){
					findSame = true;
					ContentValues cvImg = new ContentValues();
					
					cvImg.put(COLUMN_IMAGE_NOTE_ID,note.getImgList().get(i).getNid());
					cvImg.put(COLUMN_IMAGE_BITMAP,note.getImgList().get(i).getImg());
					cvImg.put(COLUMN_IMAGE_POSITION,note.getImgList().get(i).getPosition());
					getWritableDatabase().update(TABLE_IMAGE, cvImg, COLUMN_IMAGE_ID + " = "+note.getImgList().get(i).getMid(), null);
				}
				else if((j==oldNote.getImgList().size()-1)&&findSame==false){
					insertImage(note, i);
				}
			}
			if(oldNote.getImgList().size()==0){
				insertImage(note, i);
			}
			
		}

		//delete not existing images
		for(int i=0;i<oldNote.getImgList().size();i++){
			boolean findSame = false;
			for(int j=0;j<note.getImgList().size();j++){

				if(oldNote.getImgList().get(i).getMid()==note.getImgList().get(j).getMid()){
					findSame = true;
					
				}
				else if((j==note.getImgList().size()-1)&&findSame==false){					
					getWritableDatabase().delete(TABLE_IMAGE, COLUMN_IMAGE_ID + " = "+oldNote.getImgList().get(i).getMid(), null);			
				}
			}
			
		}
	}
	
	public void delete(int id){	
		getWritableDatabase().delete(TABLE_NOTE, COLUMN_NOTE_ID + " = "+id, null);
		getWritableDatabase().delete(TABLE_IMAGE, COLUMN_IMAGE_NOTE_ID + " = "+id, null);
	}
	

}
