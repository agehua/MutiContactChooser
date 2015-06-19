package com.agehua.muticontactchooser;

import java.util.Comparator;

/**
 * 
 * @author xiaanming
 *
 */
public class PinyinComparator implements Comparator<ContactData> {

	public int compare(ContactData o1, ContactData o2) {
		if (o1.sortLetter.equals("@")
				|| o2.sortLetter.equals("#")) {
			return -1;
		} else if (o1.sortLetter.equals("#")
				|| o2.sortLetter.equals("@")) {
			return 1;
		} else {
			return o1.sortLetter.compareTo(o2.sortLetter);
		}
	}

}
