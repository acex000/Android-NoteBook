package com.example.notebook;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListNotesActivity extends Activity {
	public final static String TAG = "ListNotesActivity";
	public final static String EXTRA_NOTE_ID = "com.example.notebook.listview_click_id";
	private ListView listView;
	private ArrayList<String> arrString = new ArrayList<String>();
	private List<Note> orderedList = new ArrayList<Note>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_notes);
//		// Show the Up button in the action bar.
//		setupActionBar();
		listView = (ListView) findViewById(R.id.notes_listview);
		try {
			showList();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				//call EditNoteActivity to edit the note clicked
				Intent intent = new Intent(ListNotesActivity.this, EditNoteActivity.class);
				intent.putExtra(EXTRA_NOTE_ID, orderedList.get(position).getId());//position(0~...) on the listView
				startActivity(intent);
			}
		});
		registerForContextMenu(listView);
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
		getMenuInflater().inflate(R.menu.list_notes, menu);
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
			return true;
		case R.id.action_new_note:
			createNewNote();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}
	
	//call EditNoteActivity to create new note
	private void createNewNote() {
		Intent i = new Intent(this, EditNoteActivity.class);
		i.putExtra(EXTRA_NOTE_ID, 0);
		startActivity(i);
	}

	public void showList() throws ParseException{

		NoteDatabaseHelper dbh = new NoteDatabaseHelper(this);
//		dbh.rebuildTable();
		List<Note> noteList = dbh.getAllNotes();
		//call sorting method
		orderedList = orderByModified(noteList); 
		
		for(int i=1;i<=orderedList.size();i++){
			arrString.add(orderedList.get(i-1).getTitle());
			
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrString);
		listView.setAdapter(adapter);
	}
	
	
	//sorting list by modified time
	public List<Note> orderByModified(List<Note> list){	
		//the later the note is modified , the upper it appears
		for(int j=1;j<list.size();j++){
			Note key = list.get(j);
			int i = j-1;
			while(i>=0 && list.get(i).getModified().getTime()<key.getModified().getTime()){
				list.set(i+1, list.get(i));
				i=i-1;
			}
			list.set(i+1,key);
		}
		return list;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.notes_listview) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle(orderedList.get(info.position).getTitle());
			String[] menuItems = getResources().getStringArray(R.array.list_menu);
			for (int i = 0; i < menuItems.length; i++) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	  int menuItemIndex = item.getItemId();
	  String[] menuItems = getResources().getStringArray(R.array.list_menu);
	  String menuItemName = menuItems[menuItemIndex];
	  String listItemName = orderedList.get(info.position).getTitle();

	  if(menuItemName.equals("Delete")){
		  deleteNote(orderedList.get(info.position).getId());
		  orderedList.remove(info.position);
		  ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView.getAdapter();
		  adapter.remove(adapter.getItem(info.position));
		  adapter.notifyDataSetChanged();
		
	  }
	  return true;
	}
	
	public void deleteNote(int id){
		NoteDatabaseHelper dbHelper = new NoteDatabaseHelper(this);
		dbHelper.delete(id);
	}
	
}
