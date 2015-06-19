package com.agehua.muticontactchooser.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


import com.agehua.muticontactchooser.ASyncUtils;
import com.agehua.muticontactchooser.CharacterParser;
import com.agehua.muticontactchooser.ClearEditText;
import com.agehua.muticontactchooser.ContactData;
import com.agehua.muticontactchooser.PinyinComparator;
import com.agehua.muticontactchooser.R;
import com.agehua.muticontactchooser.SideBar;
import com.agehua.muticontactchooser.SortAdapter;
import com.agehua.muticontactchooser.R.id;
import com.agehua.muticontactchooser.R.layout;
import com.agehua.muticontactchooser.SideBar.OnTouchingLetterChangedListener;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseContactsActivity extends FragmentActivity {
	private ListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private SortAdapter adapter;
	private ClearEditText mClearEditText;

	final int BUFFER_INTERVAL = 10;

	final int REQUEST_CODE = 100;

	boolean finishTask = false;
	/**
	 * 汉字转换成拼音的类
	 */
	private CharacterParser characterParser;
	private ArrayList<ContactData> SourceDateList;
	private ArrayList<ContactData> selectedContact = null;

	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	private PinyinComparator pinyinComparator;

	private RelativeLayout relaWait;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_contact_activity);

		if (getIntent().hasExtra("SelectedContact")){
			selectedContact = getIntent().getParcelableArrayListExtra("SelectedContact");
		}
		initViews();
		showContacts();
	}

	private void initViews() {
		//实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();

		pinyinComparator = new PinyinComparator();
		sideBar = (SideBar) findViewById(R.id.sidrbar);
		dialog = (TextView) findViewById(R.id.dialog);
		sideBar.setTextView(dialog);

		//设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				//该字母首次出现的位置
				if (adapter!= null){
					int position = adapter.getPositionForSection(s.charAt(0));
					if(position != -1){
						sortListView.setSelection(position);
					}else { //listview中没有该字母则什么也没做

					}
				}

			}
		});

		sortListView = (ListView) findViewById(R.id.country_lvcountry);
		sortListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//这里要利用adapter.getItem(position)来获取当前position所对应的对象
				//				Log.d("!AAAAAAAAAAAAAAA",ContactData.contactsSelected+ "个联系人");
			}
		});

		mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);

		//根据输入框输入值的改变来过滤搜索
		mClearEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				//当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
				if (null == SourceDateList){

				}else 
					filterData(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		relaWait = (RelativeLayout)findViewById(R.id.rela_wait);
		relaWait.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onDestroy() {
		finishTask = true;
		super.onDestroy();
	}


	public void showContacts() {
		AsyncTask<Object, Integer, Object> showContacts = new AsyncTask<Object, Integer, Object>() {

			@Override
			protected Object doInBackground(Object... params) {
				// Run query on all contacts id
				Uri uri = ContactsContract.Contacts.CONTENT_URI;
				String[] projection = new String[] { ContactsContract.Contacts._ID,
						ContactsContract.Contacts.DISPLAY_NAME};
				String selection = null;//ContactsContract.Contacts.HAS_PHONE_NUMBER + " = '" + ("1") + "'";
				String[] selectionArgs = null;
				String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
						+ " COLLATE LOCALIZED ASC";

				ContentResolver contectResolver = getContentResolver();
				Cursor cursor = contectResolver.query(uri, projection, selection, selectionArgs,
						sortOrder);

				//Create buffer
				final ArrayList<ContactData> bufferContacts = new ArrayList<ContactData>();

				//Load contacts one by one
				if(cursor.moveToFirst()) {
					while(!cursor.isAfterLast()) {

						if(finishTask) {
							return null;
						}

						String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

						String[] emailProj = new String[]{Email.DATA};
						Cursor cursorEmail = contectResolver.query(Email.CONTENT_URI, emailProj,Email.CONTACT_ID + " = ? ", new String[] { id }, null);

						String[] phoneProj = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
						Cursor cursorPhone = contectResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, phoneProj,
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);

						String firstName = "";
						String lastName = "";
						String email = "";
						String displayname = "";
						String phoneNmb = "";

						if(cursorPhone.moveToFirst()) {
							phoneNmb = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						}
						cursorPhone.close();
						if(cursorEmail.moveToFirst()) {
							email = cursorEmail.getString(cursorEmail.getColumnIndex(Email.DATA));
						}
						cursorEmail.close();

						displayname = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

						//Divide display name to first and last
						String[] names = new String[]{"---", "---"};

						if(displayname != null) {
							names = displayname.split("\\s+");
							firstName = displayname;
						}
						if(names.length >= 1) {
							firstName = names[0];
						}
						if(names.length >= 2) {
							lastName = names[1];
						}

						final ContactData contactData = new ContactData(id, firstName, lastName, 
								displayname, phoneNmb, email, false, 
								characterParser.getSelling(firstName).substring(0, 1).toUpperCase());

						bufferContacts.add(contactData);
						//Set list view initialy
						cursor.moveToNext();
					}
				}
				cursor.close();

				runOnUiThread(new Runnable() {
					public void run() {
						relaWait.setVisibility(View.GONE);
						SourceDateList = filledData(bufferContacts); 
						Collections.sort(SourceDateList, pinyinComparator);
						adapter = new SortAdapter(ChooseContactsActivity.this, SourceDateList ,selectedContact);
						sortListView.setAdapter(adapter);
						adapter.notifyDataSetChanged();
					}
				});

				return null;
			}
		};
		ASyncUtils.startMyTask(showContacts, null);
	}

	//	/**
	//	 * 每十个数据，更新一下视图
	//	 * @param buffer
	//	 */
	//	public void addBuffer(ArrayList<ContactData> buffer) {
	//
	//		// Add new contacts to count
	//		SourceDateList = filledData(buffer);
	//		Collections.sort(SourceDateList, pinyinComparator);
	//		adapter.addAll(SourceDateList);
	//		adapter.notifyDataSetChanged();
	//		buffer.clear();
	//	}

	/**
	 * 返回数据给启动它的activity
	 */
	public void returnData() {

		Intent result = new Intent();         

		if (adapter!= null){
			ArrayList<ContactData> resultList = adapter.list;
			Iterator<ContactData> iterResultList = resultList.iterator();

			ArrayList<ContactData> results = new ArrayList<ContactData>();
			//pass only checked contacts
			while(iterResultList.hasNext()) {
				ContactData contactData = iterResultList.next();
				if(contactData.checked) {
					results.add(contactData);
				}
			}
			result.putParcelableArrayListExtra(ContactData.CONTACTS_DATA, results);
		}
		if (getParent() == null) {
			setResult(Activity.RESULT_OK, result);
		}
		else {
			getParent().setResult(Activity.RESULT_OK, result);
		}
		ContactData.contactsSelected =0;
		finish();

	}

	/**
	 * 利用中文拼音，将首字母按照A-Z与 #排序，为ListView填充数据
	 * @param date
	 * @return
	 */
	private ArrayList<ContactData> filledData(ArrayList<ContactData> contacts){
		ArrayList<ContactData> mSortList = new ArrayList<ContactData>();

		for(int i=0; i<contacts.size(); i++){
			ContactData sortModel =contacts.get(i);
			//汉字转换成拼音
			String pinyin = characterParser.getSelling(sortModel.firstname);
			String sortString = pinyin.substring(0, 1).toUpperCase();//取得拼音的首字母并转换成大写

			// 正则表达式，判断首字母是否是英文字母
			if(sortString.matches("[A-Z]")){
				sortModel.sortLetter= sortString.toUpperCase();
			}else{
				sortModel.sortLetter = "#";
			}

			mSortList.add(sortModel);
		}
		return mSortList;

	}

	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * @param filterStr
	 */
	private void filterData(String filterStr){
		ArrayList<ContactData> filterDateList = new ArrayList<ContactData>();

		if(TextUtils.isEmpty(filterStr)){
			filterDateList = SourceDateList;
		}else{
			filterDateList.clear();
			for(ContactData sortModel : SourceDateList){
				String name = sortModel.displayName;
				//下面的判断是包含该字母，或者以该字母开头
				if(name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).substring(0, 1).equalsIgnoreCase(filterStr.toString())){
					filterDateList.add(sortModel);
				}
			}
		}

		// 根据a-z进行排序
		Collections.sort(filterDateList, pinyinComparator);
		adapter.updateListView(filterDateList);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			onBackPressed();
			return true;
		}else {
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * 堆栈后进先出，堆栈顶的就是用户能看到的对象
	 */
	@Override
	public void onBackPressed() {
		ContactData.contactsSelected =0;
//		FinishActivity();
	}

}
