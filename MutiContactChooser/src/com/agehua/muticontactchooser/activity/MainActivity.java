package com.agehua.muticontactchooser.activity;

import java.util.ArrayList;
import java.util.Iterator;

import com.agehua.muticontactchooser.ContactData;
import com.agehua.muticontactchooser.R;
import com.agehua.muticontactchooser.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

	private Button shareBtn;
	private TextView txtView;
	ArrayList<ContactData> contactList = new ArrayList<ContactData>();
	final int REQUEST_CODE = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		txtView= (TextView)findViewById(R.id.txt_share);


		shareBtn=(Button)findViewById(R.id.btn_share_conf);
		shareBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ArrayList<String> contactId = new ArrayList<String>(); 
				Intent contactPicker = new Intent(MainActivity.this, ChooseContactsActivity.class);
				contactPicker.putParcelableArrayListExtra("SelectedContact", contactList);
				startActivityForResult(contactPicker, REQUEST_CODE);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(requestCode == REQUEST_CODE) {
			if(resultCode == Activity.RESULT_OK) {
				Bundle extras = data.getExtras();
				if (null!= extras){
					if(data.hasExtra(ContactData.CONTACTS_DATA)) {
						ArrayList<ContactData> contacts = data.getParcelableArrayListExtra(ContactData.CONTACTS_DATA);

						if(contacts != null) {
							contactList.clear();
							Iterator<ContactData> iterContacts = contacts.iterator();
							while(iterContacts.hasNext()) {
								ContactData contact = iterContacts.next();
								if (contactList.size()<5){
									contactList.add(contact);
								}
								if (contactList.size()>= 5){
								}
							}
							String tmp = "";
							for (ContactData contact: contacts){
								tmp =contact.displayName+":"+contact.phoneNmb+"\n";
							}
							txtView.append(tmp);
						}
					}
				}else{
				}

			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}


}
