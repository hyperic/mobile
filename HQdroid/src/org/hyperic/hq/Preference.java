package org.hyperic.hq;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preference extends PreferenceActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		
	}

}
