package com.example.notebook;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView.BufferType;

public class EditNoteActivity extends Activity {
	private final static String TAG = "EditNoteActivity";
	public final static String EXTRA_NOTE_ID = "com.example.notebook.painting_note_id";
	private final static int BITMAP_INITIAL_WIDTH = 1024;
	private final static int BITMAP_INITIAL_HEIGHT = 1920;
	private Paint mPaint;
	private PaintView pv;
	private int bitmapWidth;
	private int bitmapHeight;
	private boolean canPaint = false;
	private EditText dialogEditText;
	private int noteId = 0;
	private boolean isNew = true;
	private Note note = new Note();
	
	private static EditText contentET;
	
	private static int RESULT_LOAD_IMAGE = 1;
	private static int RESULT_TAKE_PHOTO = 3;
	private static final int PICK_FROM_GALLERY = 2;
	Bitmap galleryImg = null;
	static String tempContent = "";
	static SpannableStringBuilder ssb;
	static boolean isInsertingImg = false;
	static boolean isLoadingExistingNote = false;
//	static boolean isPainting = false;

	static int a=0,b=0;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		// receive the clicked noteId from note list
		noteId = getIntent().getIntExtra(ListNotesActivity.EXTRA_NOTE_ID, 0);
//		if(isPainting == true){
//			noteId = getIntent().getIntExtra(PaintPadActivity.EXTRA_NOTE_ID, 0);
//			isPainting = false;
//		}
		Log.i(TAG, "intent-noteId"+noteId);
		setContentView(R.layout.activity_edit_note);
		// Show the Up button in the action bar.
		setupActionBar();
		// disable the title to save more space for action bar
		getActionBar().setDisplayShowTitleEnabled(false);
		
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.edit_note_layout);
		pv = new PaintView(this);
		contentET = new EditText(this);
		
		//set parameters for contentET
		contentET.setLayoutParams(new RelativeLayout.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) contentET.getLayoutParams();
		contentET.setBackgroundColor(Color.TRANSPARENT);
		params.addRule(RelativeLayout.BELOW, R.id.note_title);
		
		//set textListener
		contentET.addTextChangedListener(new TextWatcher(){
			@Override
	        public void afterTextChanged(Editable s) {
	            // TODO Auto-generated method stub

	        }

	        @Override
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	            // TODO Auto-generated method stub	        	 

	        }

	        @Override
	        public void onTextChanged(CharSequence s, int start, int before, int count) {
	        	final int st = start;
	        	final int bf= before;
	        	final int ct = count;
	        	
				Log.i(TAG, "img#:" + note.getImgList().size());

				if(isInsertingImg==false&&isLoadingExistingNote==false){
					Log.i(TAG, "change:");
					//text increases
					if(bf<ct){
						for(int i=0;i<note.getImgList().size();i++){
							//when increase happens at or before a image, then increase the position of this image
							if(note.getImgList().get(i).getPosition()>=st){
								note.getImgList().get(i).setPosition(note.getImgList().get(i).getPosition()+(ct-bf));
								Log.i(TAG, "change:+1");
							}
						}
					}
					//text shrinks
					else if(bf>ct){
						boolean removeHappens=false;
						int removeIndex=0;
						for(int i=0;i<note.getImgList().size();i++){
							//when shrink happens after a image, then eliminate the position of this image
							if(note.getImgList().get(i).getPosition()>st)
								note.getImgList().get(i).setPosition(note.getImgList().get(i).getPosition()-(bf-ct));
							//when shrink happens at a image, then it means this image is deleted
							else if(note.getImgList().get(i).getPosition()==st){
								removeHappens=true;
								removeIndex=i;
//								note.getImgList().remove(i);
							}
						}
						if(removeHappens==true)
							note.getImgList().remove(removeIndex);
					}					
				}
				else if(isInsertingImg == true){
					isInsertingImg=false;
				}
				else if(isLoadingExistingNote==true){
					isLoadingExistingNote=false;
				}
				

	        	contentET.setOnKeyListener(new OnKeyListener() {
	        		

					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						

//						if (keyCode == KeyEvent.KEYCODE_DEL)
//							keyDel = 1;
						a++;
						Log.i(TAG, "a:" + a);
//						if(listenerValid == true){
//							Log.i(TAG, "start:" + st);
//							Log.i(TAG, "before:" + bf);
//							Log.i(TAG, "count:" + ct);
							Log.i(TAG, "keycode:" + keyCode);
							b++;
							Log.i(TAG, "b:" + b);

						return false;
					}
				});
	        	
	        } 
		});
		
				
		
//		//----bitmap scrollable test
//		RelativeLayout.LayoutParams lay = new RelativeLayout.LayoutParams(
//			    RelativeLayout.LayoutParams.WRAP_CONTENT, 
//			    RelativeLayout.LayoutParams.WRAP_CONTENT);
////		EditText contentET = (EditText) findViewById(R.id.note_content);
////		lay.addRule(RelativeLayout.ALIGN_TOP, contentET.getId());
//		//----bitmap scrollable test		
//		rl.addView(pv, lay);
		
		rl.addView(pv);
		rl.addView(contentET);
		
		
		// set pen attribute
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(0xFF000000);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(5);

		// check if this is a new note
		checkIfNewNote(noteId);
		
		if (isNew == false) {
			try {
				loadNote(noteId);
//				 listenerValid= true;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		else
//			listenerValid = false;

		// Enable type function initially
		typeText();
	}

	// check whether the note is a new note or not
	private void checkIfNewNote(int id) {
		if (id != 0)
			isNew = false;
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_note, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
//		case R.id.action_load:
//			popupDialog();
//			return true;
		case R.id.action_save:
			saveAll();
			return true;
//		case R.id.action_type_text:
//			typeText();
//			return true;
		case R.id.action_camera:
			openCamera();
			return true;	
		case R.id.action_paint:
			paintLine();
			return true;
//		case R.id.action_erase:
//			eraseLine();
//			return true;
		case R.id.action_insert_img:
			insertImg();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		// return super.onOptionsItemSelected(item);
	}

	private void openCamera() {
		// TODO Auto-generated method stub
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		//takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));

		//Uri fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
		//takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
		startActivityForResult(takePictureIntent, RESULT_TAKE_PHOTO);
	}

	//insert a image from gallery
	private void insertImg() {
		// TODO Auto-generated method stub
		visitGallery();
	}

	// switch to typing mode
	public void typeText() {
		RelativeLayout parent = (RelativeLayout) pv.getParent();
		parent.removeView(pv);
		parent.addView(pv, 0);// set pv to the most back layer
		canPaint = false;
	}

	// switch to paint mode
	private void paintLine() {
		saveAll();
//		isPainting = true;
		Intent intent = new Intent(this, PaintPadActivity.class);
		intent.putExtra(EXTRA_NOTE_ID, note.getId());
		startActivity(intent);
		
		pv.bringToFront();
		canPaint = true;
		mPaint.setStrokeWidth(5);
		mPaint.setXfermode(null);
//		EditText contentET = (EditText) findViewById(R.id.note_content);
		Log.i(TAG, "width" + contentET.getWidth());
		Log.i(TAG, "height" + contentET.getHeight());
		if (contentET.getWidth() >= BITMAP_INITIAL_HEIGHT) {
			resizeBitmap(contentET.getHeight());
		}
	}

	private void resizeBitmap(int height) {
		// If the height of text content if bigger or equal to the height of
		// bitmap
		// then double the height of bitmap
		bitmapHeight = 2 * height;

		Bitmap bm = pv.mBitmap;
		pv.mBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight,
				Bitmap.Config.ARGB_8888);
		pv.mCanvas.setBitmap(pv.mBitmap);
		// clear the previous image on canvas
		pv.mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		// set the loaded image to the canvas
		pv.mCanvas.drawBitmap(bm, 0, 0, null);
	}

	// switch to paint mode
	private void eraseLine() {
		pv.bringToFront();
		canPaint = true;
		mPaint.setXfermode((new PorterDuffXfermode(PorterDuff.Mode.CLEAR)));
		mPaint.setStrokeWidth(20);

	}

	// save note and image into database
	public void saveAll() {
		Bitmap bm = pv.mBitmap;
		byte[] imgByte = getBitmapAsByteArray(bm);

		NoteDatabaseHelper dbHelper = new NoteDatabaseHelper(this);
		EditText titleET = (EditText) findViewById(R.id.note_title);
//		EditText contentET = (EditText) findViewById(R.id.note_content);
		note.setModified(new Date());
		note.setTitle(titleET.getText().toString());
		note.setContent(contentET.getText().toString());
		note.setImg(imgByte);
		if (isNew == true){
			dbHelper.insertNote(note);
			note.setId(dbHelper.getNewestNoteId());
			dbHelper.insertAllImages(note);
			isNew = false;
		}
		else if (isNew == false){
			dbHelper.update(note);
		}
	}

	// convert Bitmap to byte[];
	public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 100, outputStream);
		return outputStream.toByteArray();
	}

	// load the note image from note object
	public void loadImg() {
		byte[] imgByte;
		Bitmap bitmap;
		imgByte = note.getImg();
		bitmap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
		// clear the previous image on canvas
		pv.mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		// set the loaded image to the canvas
		pv.mCanvas.drawBitmap(bitmap, 0, 0, null);
	}

	// load note from database
	public void loadNote(int id) throws ParseException {
		EditText titleET = (EditText) findViewById(R.id.note_title);
//		EditText contentET = (EditText) findViewById(R.id.note_content);
		isLoadingExistingNote=true;
		NoteDatabaseHelper dbHelper = new NoteDatabaseHelper(this);
		note = dbHelper.getNote(id);
		titleET.setText(note.getTitle());
		
		Log.i(TAG, note.getTitle());
		Log.i(TAG, "nid"+note.getId());
		setAllImagesForExistingNote();
		// load image into canvas
		loadImg();
		
		
	}

	// let the user input which note to load by id
	public void popupDialog() {

		dialogEditText = new EditText(this);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Input id of note").setIcon(
				android.R.drawable.ic_dialog_info);
		builder.setView(dialogEditText);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				// loadImg(Integer.valueOf(dialogEditText.getText().toString()));
				// loadContent(Integer
				// .valueOf(dialogEditText.getText().toString()));
				try {
					loadNote(Integer.valueOf(dialogEditText.getText().toString()));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		builder.setNegativeButton("Cancel", null);
		builder.show();
	}

	class PaintView extends View {

		private Bitmap mBitmap;
		private Canvas mCanvas;
		private Path mPath;
		private Paint mBitmapPaint;

		public PaintView(Context c) {
			super(c);
			bitmapWidth = BITMAP_INITIAL_WIDTH;
			bitmapHeight = BITMAP_INITIAL_HEIGHT;
			// create new bitmap based on parameters
			mBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight,
					Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas(mBitmap);
			mPath = new Path();
			mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawColor(0x00000000);

			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

			canvas.drawPath(mPath, mPaint);
		}

		private float mX, mY;
		private static final float TOUCH_TOLERANCE = 4;

		// start paint
		private void touch_start(float x, float y) {
			mPath.reset();
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
		}// move pen

		private void touch_move(float x, float y) {
			float dx = Math.abs(x - mX);
			float dy = Math.abs(y - mY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				mX = x;
				mY = y;
			}
		}

		// draw on screen
		private void touch_up() {
			mPath.lineTo(mX, mY);
			// draw content on screen
			mCanvas.drawPath(mPath, mPaint);
			// set pen to avoid repeating description
			mPath.reset();
		}

		// responding time of touching the screen
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			// use this flag to control paintable or not
			if (canPaint == true) {

				float x = event.getX();
				float y = event.getY();

				switch (event.getAction()) {
				// press event respond
				case MotionEvent.ACTION_DOWN:
					touch_start(x, y);
					invalidate();
					break;
				// move event respond
				case MotionEvent.ACTION_MOVE:
					touch_move(x, y);
					invalidate();
					break;
				// release event respond
				case MotionEvent.ACTION_UP:
					touch_up();
					invalidate();
					break;
				}
			}
			return true;
		}

	}
	
	public void getImg(View view){
		visitGallery();
	}
	
	public void visitGallery() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, RESULT_LOAD_IMAGE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  

	    super.onActivityResult(requestCode, resultCode, data);     

		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
				&& null != data) {

			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);

			galleryImg = (BitmapFactory.decodeFile(picturePath));

			cursor.close();
			//set flag to invalid OnTextChangedListener
			isInsertingImg = true;
		}
		else if(resultCode == RESULT_OK && requestCode == RESULT_TAKE_PHOTO){
			Bundle extras = data.getExtras();
			galleryImg = (Bitmap) extras.get("data");

			//save photo to gallery
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			String imageTitle = "photo" + timeStamp;
			String imageDescription = "photo from DigiNote";
			MediaStore.Images.Media.insertImage(getContentResolver(), galleryImg,imageTitle,imageDescription);
			isInsertingImg = true;
			
		}
		else if (resultCode != RESULT_OK)
			return;
		
	    insertNewImageAndRefreshContent();


	}
	
	public void insertNewImageAndRefreshContent(){
		Log.i(TAG, "et2w:" + contentET.getWidth());
		
		tempContent = contentET.getText().toString();		
		ssb = new SpannableStringBuilder(tempContent);
		

		int cursorPosition = contentET.getSelectionStart();
		Log.i(TAG, "cursor:" + cursorPosition);

		//recalculate the new position of each image
		for(int i=0;i<note.getImgList().size();i++){
			if(cursorPosition<=note.getImgList().get(i).getPosition()){
				note.getImgList().get(i).setPosition(note.getImgList().get(i).getPosition()+1);
			}
		}
		

		
		//use note.imgList to carry all inserted image
		if(isNew == true)
			note.getImgList().add( new InsertedImage(getBitmapAsByteArray(galleryImg), cursorPosition));
		else if(isNew == false)
			note.getImgList().add( new InsertedImage(note.getId(), getBitmapAsByteArray(galleryImg), cursorPosition));
			
		//insert a character to make space for the new inserted image
		ssb.insert(cursorPosition, "s");
		//use a loop to put all inserted images back to their right position
		for(int i=0;i<note.getImgList().size();i++){
			Bitmap imageBitmap = BitmapFactory.decodeByteArray(note.getImgList().get(i).getImg(), 0, note.getImgList().get(i).getImg().length);
			
			//check if the image is bigger than the screen,if yes, resize it
			Point size = getScreenSize();
			if((8*size.x/10)<=imageBitmap.getWidth()){
				int scaledHeight = (imageBitmap.getHeight()*(8 * size.x / 10))/imageBitmap.getWidth();
				Bitmap scaled = Bitmap.createScaledBitmap(imageBitmap, 8*size.x/10, scaledHeight, true);
				imageBitmap = scaled;
			}
							
			ImageSpan ims = new ImageSpan(this, imageBitmap);
			

			ssb.setSpan(ims, note.getImgList().get(i).getPosition(), note.getImgList().get(i).getPosition() + 1,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			Log.i(TAG, note.getImgList().get(i).getMid()+"position:"+note.getImgList().get(i).getPosition());
 

		}
		contentET.setText(ssb, BufferType.SPANNABLE);
	}
	
	//get the size of the screen
	public Point getScreenSize(){
		 //get the size of the device 
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size;
	}
	
	public void setAllImagesForExistingNote(){
		tempContent = note.getContent();
		

		Log.i(TAG, "temp"+tempContent);
		ssb = new SpannableStringBuilder(tempContent);
		
		//use a loop to put all inserted images back to their right position
		for(int i=0;i<note.getImgList().size();i++){
			Bitmap imageBitmap = BitmapFactory.decodeByteArray(note.getImgList().get(i).getImg(), 0, note.getImgList().get(i).getImg().length);
			
			//check if the image is bigger than the screen,if yes, resize it
			Point size = getScreenSize();
			if((8*size.x/10)<=imageBitmap.getWidth()){
				int scaledHeight = (imageBitmap.getHeight()*(8 * size.x / 10))/imageBitmap.getWidth();
				Bitmap scaled = Bitmap.createScaledBitmap(imageBitmap, 8*size.x/10, scaledHeight, true);
				imageBitmap = scaled;
			}
							
			ImageSpan ims = new ImageSpan(this, imageBitmap);
			Log.i(TAG, "stp:"+note.getImgList().get(i).getPosition());
			ssb.setSpan(ims, note.getImgList().get(i).getPosition(), note.getImgList().get(i).getPosition() + 1,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			Log.i(TAG, note.getImgList().get(i).getMid()+"position:"+note.getImgList().get(i).getPosition());
 

		}
		contentET.setText(ssb, BufferType.SPANNABLE);
	}

	

}
