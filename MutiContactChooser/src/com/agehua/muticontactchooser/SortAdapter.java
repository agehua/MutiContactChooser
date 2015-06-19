package com.agehua.muticontactchooser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

/**
 * SectionIndexer实现按字母快速滑动
 * @author Agehua
 *
 */
public class SortAdapter extends BaseAdapter implements SectionIndexer{
	public ArrayList<ContactData> list = null;
	ArrayList<ContactData> selectedList = null;
	private Context mContext;

	private Map<Integer, Boolean> checkedMap;//记录checkbox是否选中

	public SortAdapter(Context mContext, ArrayList<ContactData> list,ArrayList<ContactData> sList) {
		this.mContext = mContext;
		this.list = list;
		this.selectedList =sList;
		checkedMap = new HashMap<Integer, Boolean>();

		//效率低
		for (int i =0; i<list.size();i++){
			checkedMap.put(i, false);
			if (selectedList!= null){
				for (int j =0;j<selectedList.size();j++){
					if (selectedList.get(j).id.equals(list.get(i).id)){
						checkedMap.put(i, true);
						ContactData data = list.get(i);
						data.checked = true;
						ContactData.contactsSelected++;
						
						selectedList.remove(j);
					}
				}
			}
		}
	}

	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView
	 * @param list
	 */
	public void updateListView(ArrayList<ContactData> list){
		this.list = list;
		notifyDataSetChanged();
	}

	public int getCount() {
		return this.list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	/*
	 * 首先我们根据ListView的position调用getSectionForPosition(int position)来获取
	 * 该位置上面name的首字母的ascii值,然后根据这个ascii值调用getPositionForSection(int section)来获取第一次出现
	 * 该首字母的position，如果ListView的position 等于 根据这个ascii值调用getPositionForSection(int section)来
	 * 获取第一次出现该首字母的position，则显示分类字母 否则隐藏
	 */
	public View getView(final int position, View view, ViewGroup arg2) {
		final ViewHolder viewHolder ;

		final ContactData mContent = list.get(position);
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(R.layout.item_contacts, null);
			viewHolder.tvLetter = (TextView) view.findViewById(R.id.catalog);
			viewHolder.tvName = (TextView) view.findViewById(R.id.tvName);
			viewHolder.tvData = (TextView) view.findViewById(R.id.tvData);
			viewHolder.cbSelect = (CheckBox) view.findViewById(R.id.cbSelect);
			viewHolder.wrapper = (LinearLayout)view.findViewById(R.id.llWrapper);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		viewHolder.cbSelect.setChecked(checkedMap.get(position));

//		if (selectedList!= null){
//			for (int i =0;i<selectedList.size();i++){
//				if (selectedList.get(i).id.equals(mContent.id)){
//					viewHolder.cbSelect.setChecked(true);
//					mContent.checked = true;
//					checkedMap.put(position, true);
//					ContactData.contactsSelected++;
//				}
//			}
//		}

		//根据position获取分类的首字母的Char ascii值
		int section = getSectionForPosition(position);

		//如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
		if(position == getPositionForSection(section)){
			viewHolder.tvLetter.setVisibility(View.VISIBLE);
			viewHolder.tvLetter.setText(mContent.sortLetter);
		}else{
			viewHolder.tvLetter.setVisibility(View.GONE);
		}

		viewHolder.wrapper.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!mContent.checked){ //当前状态为false
					if (ContactData.contactsSelected >=5){
						//已经有5个联系人，不让继续选择了
						Toast.makeText(mContext, "最多选择5个联系人", Toast.LENGTH_SHORT);
					}else {
						mContent.checked = !mContent.checked;
						viewHolder.cbSelect.setChecked(mContent.checked);
						checkedMap.put(position, mContent.checked);
						ContactData.contactsSelected ++;
					}
				}else{
					if(ContactData.contactsSelected > 0) {
						mContent.checked = !mContent.checked;
						ContactData.contactsSelected --;
						viewHolder.cbSelect.setChecked(mContent.checked);
						checkedMap.put(position, mContent.checked);
					}
				}

			}
		});

		viewHolder.tvName.setText(mContent.displayName);
		viewHolder.tvData.setText(mContent.phoneNmb + " " + mContent.email);
		viewHolder.cbSelect.setClickable(false);
		

		//		viewHolder.cbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		//
		//			@Override
		//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		//				mContent.checked = isChecked;
		//
		//				// Update number of selected contacts
		//				if(mContent.checked) {
		//					ContactData.contactsSelected++;
		//					checkedMap.put(position, true);
		//				} else {
		//					if(ContactData.contactsSelected > 0) {
		//						ContactData.contactsSelected--;
		//						checkedMap.put(position, false);
		//					}
		//				}
		//			}
		//		});



		return view;

	}



	final static class ViewHolder {
		TextView tvLetter;
		TextView tvName, tvData;
		CheckBox cbSelect;
		View divider;
		LinearLayout  wrapper ;
	}


	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	public int getSectionForPosition(int position) {
		return list.get(position).sortLetter.charAt(0);
	}

	/**
	 * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
	 * 取得该位置后可以设置其为选中，可以实现将其展示在页面最上方
	 */
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).sortLetter;
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}

		return -1;
	}

	public void addAll(Collection<? extends ContactData> collection) {
		list.addAll(collection);
	}

	/**
	 * 提取英文的首字母，非英文字母用#代替。
	 * 
	 * @param str
	 * @return
	 */
	private String getAlpha(String str) {
		String  sortStr = str.trim().substring(0, 1).toUpperCase();
		// 正则表达式，判断首字母是否是英文字母
		if (sortStr.matches("[A-Z]")) {
			return sortStr;
		} else {
			return "#";
		}
	}

	@Override
	public Object[] getSections() {
		return null;
	}
}