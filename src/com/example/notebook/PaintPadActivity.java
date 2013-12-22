package com.example.notebook;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class PaintPadActivity extends Activity {
	public final static String EXTRA_NOTE_ID = "com.example.notebook.painting_note_id";
	private final static int BITMAP_INITIAL_WIDTH = 1024;
	private final static int BITMAP_INITIAL_HEIGHT = 1920;
	private Paint mPaint;
	private PaintView pv;
	private int bitmapWidth;
	private int bitmapHeight;
	int nid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		nid = this.getIntent().getIntExtra(EditNoteActivity.EXTRA_NOTE_ID, 0);
		setContentView(R.layout.activity_paint_pad);
		// Show the Up button in the action bar.
		setupActionBar();
		// disable the title to save more space for action bar
		getActionBar().setDisplayShowTitleEnabled(false);
		
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.paint_pad_layout);
		pv = new PaintView(this);
		rl.addView(pv);
		// set pen attribute
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(0xffffff00);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(5);

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
		getMenuInflater().inflate(R.menu.paint_pad, menu);
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
//			NavUtils.navigateUpFromSameTask(this);
			Intent intent = new Intent(this, ListNotesActivity.class);
//			intent.putExtra(EXTRA_NOTE_ID, nid);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case R.id.action_save_paint:
			saveToGallery();
			return true;
		case R.id.action_erase_paint:
			changeToEraser();
			return true;	
		case R.id.action_continue_paint:
			continuePaint();
			return true;	
		default:
			return super.onOptionsItemSelected(item);
		}
		
//		return super.onOptionsItemSelected(item);
	}
	
	private void continuePaint() {
		// TODO Auto-generated method stub
		mPaint.setStrokeWidth(5);
		mPaint.setXfermode(null);
	}

	private void changeToEraser() {
		// TODO Auto-generated method stub
		mPaint.setColor(0xffffff00);
		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		mPaint.setStrokeWidth(20);
	}

	private void saveToGallery() {
		// TODO Auto-generated method stub
		Point size = getScreenSize();
		Bitmap scaled = Bitmap.createScaledBitmap(pv.mBitmap, size.x/2, size.y/2, true);
		MediaStore.Images.Media.insertImage(getContentResolver(), scaled, "paint" , "paint for notebook");
		Intent intent = new Intent(this, ListNotesActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
	//get the size of the screen
	public Point getScreenSize() {
		// get the size of the device
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size;
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
//			mBitmapPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//			mBitmap.eraseColor(Color.argb(000,000,255,000));
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawColor(0x00ffffff);

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
//			if (canPaint == true) {

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
//			}
			return true;
		}
	}

}
