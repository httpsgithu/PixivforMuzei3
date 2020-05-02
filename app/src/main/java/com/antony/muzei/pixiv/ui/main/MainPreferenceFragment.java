/*
 *     This file is part of PixivforMuzei3.
 *
 *     PixivforMuzei3 is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program  is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.antony.muzei.pixiv.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.work.WorkManager;

import com.antony.muzei.pixiv.PixivArtWorker;
import com.antony.muzei.pixiv.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainPreferenceFragment extends PreferenceFragmentCompat
{
	private SharedPreferences.OnSharedPreferenceChangeListener prefChangeListener;
	private String newCreds, oldCreds;
	private String oldUpdateMode, newUpdateMode;
	private String oldTag, newTag;
	private String oldArtist, newArtist;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.main_preference_layout);

		// If Muzei is not installed, this will redirect the user to Muzei's Play Store listing
		if (!isMuzeiInstalled())
		{
			final String appPackageName = "net.nurik.roman.muzei"; // getPackageName() from Context or Activity object
			try
			{
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
			} catch (android.content.ActivityNotFoundException ex)
			{
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
			}
		}

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

		// All this is needed for the arbitrary selection NSFW filtering
		// Resets to default SFW filtering is all options are unchecked
		// Prints a summary string based on selection
		// Updates authFilterSelectPref summary as user updates it
		MultiSelectListPreference authFilterSelectPref = findPreference("pref_authFilterSelect");
		authFilterSelectPref.setOnPreferenceChangeListener((preference, newValue) ->
		{
			// Resets to SFW on empty selection
			// for some reason a length of 2 is an empty selection
			if (newValue.toString().length() == 2)
			{
				Set<String> defaultSet = new HashSet<>();
				defaultSet.add("2");
				authFilterSelectPref.setValues(defaultSet);

				SharedPreferences.Editor editor = sharedPrefs.edit();
				editor.putStringSet("pref_authFilterSelect", defaultSet);
				editor.commit();
				authFilterSelectPref.setSummary("SFW");
				return false;
			}

			// Prints a comma delimited string of user selections. There is no trailing comma
			// TODO there's gotta be a better wau of doing this
			String str = newValue.toString();
			ArrayList<Integer> arrayList = new ArrayList<>();
			for (int i = 0; i < str.length(); i++)
			{
				if (Character.isDigit(str.charAt(i)))
				{
					arrayList.add(Character.getNumericValue(str.charAt(i)));
				}
			}
			String[] authFilterEntriesPossible = getResources().getStringArray(R.array.pref_authFilterLevel_entries);
			StringBuilder stringBuilderAuth = new StringBuilder();
			for (int i = 0; i < arrayList.size(); i++)
			{
				stringBuilderAuth.append(authFilterEntriesPossible[(arrayList.get(i) - 2) / 2]);
				if (i != arrayList.size() - 1)
				{
					stringBuilderAuth.append(", ");
				}
			}
			String summaryAuth = stringBuilderAuth.toString();

			authFilterSelectPref.setSummary(summaryAuth);

			return true;
		});

		// updates ranking nsfw select summary on preference change
		MultiSelectListPreference rankingFilterSelectPref = findPreference("pref_rankingFilterSelect");
		rankingFilterSelectPref.setOnPreferenceChangeListener((preference, newValue) ->
		{
			// for some reason 2 is an empty selection
			if (newValue.toString().length() == 2)
			{
				Log.v("MANUAL", "pref change empty set");
				Set<String> defaultSet = new HashSet<>();
				defaultSet.add("0");
				rankingFilterSelectPref.setValues(defaultSet);

				SharedPreferences.Editor editor = sharedPrefs.edit();
				editor.putStringSet("pref_rankingFilterSelect", defaultSet);
				editor.commit();
				rankingFilterSelectPref.setSummary("SFW");
				return false;
			}

			String str = newValue.toString();
			ArrayList<Integer> arrayList = new ArrayList<>();
			for (int i = 0; i < str.length(); i++)
			{
				if (Character.isDigit(str.charAt(i)))
				{
					arrayList.add(Character.getNumericValue(str.charAt(i)));
				}
			}
			String[] rankingEntriesAvailable = getResources().getStringArray(R.array.pref_rankingFilterLevel_entries);
			StringBuilder stringBuilderRanking = new StringBuilder();
			for (int i = 0; i < arrayList.size(); i++)
			{
				stringBuilderRanking.append(rankingEntriesAvailable[arrayList.get(i)]);
				if (i != arrayList.size() - 1)
				{
					stringBuilderRanking.append(", ");
				}
			}
			String summaryRanking = stringBuilderRanking.toString();

			rankingFilterSelectPref.setSummary(summaryRanking);

			return true;
		});

		// Generates the ranking NSFW filter summary during activity startup
		Set<String> chosenLevelsSetRanking = sharedPrefs.getStringSet("pref_rankingFilterSelect", null);
		String[] chosenLevelsRanking = chosenLevelsSetRanking.toArray(new String[0]);
		String[] entriesAvailableRanking = getResources().getStringArray(R.array.pref_rankingFilterLevel_entries);
		StringBuilder stringBuilderRanking = new StringBuilder();
		for (int i = 0; i < chosenLevelsRanking.length; i++)
		{
			stringBuilderRanking.append(entriesAvailableRanking[Integer.parseInt(chosenLevelsRanking[i])]);
			if (i != chosenLevelsRanking.length - 1)
			{
				stringBuilderRanking.append(", ");
			}
		}
		String summaryRanking = stringBuilderRanking.toString();
		rankingFilterSelectPref.setSummary(summaryRanking);

		// Generates the authFilterSelectPref summary during activity startup
		// TODO combine this with the above summary setting code section
		Set<String> chosenLevelsSet = sharedPrefs.getStringSet("pref_authFilterSelect", null);
		String[] chosenLevels = chosenLevelsSet.toArray(new String[0]);
		String[] entriesAvailableAuth = getResources().getStringArray(R.array.pref_authFilterLevel_entries);
		StringBuilder stringBuilderAuth = new StringBuilder();
		for (int i = 0; i < chosenLevels.length; i++)
		{
			stringBuilderAuth.append(entriesAvailableAuth[(Integer.parseInt(chosenLevels[i]) - 2) / 2]);
			if (i != chosenLevels.length - 1)
			{
				stringBuilderAuth.append(", ");
			}
		}
		String summaryAuth = stringBuilderAuth.toString();
		authFilterSelectPref.setSummary(summaryAuth);

		// Reveal the tag_search or artist_id EditTextPreference and write the summary if update mode matches
		String updateMode = sharedPrefs.getString("pref_updateMode", "daily");
		if (Arrays.asList("follow", "bookmark", "tag_search", "artist", "recommended")
				.contains(updateMode))
		{
			findPreference("pref_authFilterSelect").setVisible(true);
			findPreference("prefCat_loginSettings").setVisible(true);
			if (updateMode.equals("tag_search"))
			{
				Preference tagSearch = findPreference("pref_tagSearch");
				tagSearch.setVisible(true);
				tagSearch.setSummary(sharedPrefs.getString("pref_tagSearch", ""));
			} else if (updateMode.equals("artist"))
			{
				Preference artistId = findPreference("pref_artistId");
				artistId.setVisible(true);
				artistId.setSummary(sharedPrefs.getString("pref_artistId", ""));
			}
		} else
		{
			findPreference("pref_rankingFilterSelect").setVisible(true);
		}

		// Reveals UI elements as needed depending on Update Mode selection
		findPreference("pref_updateMode").setOnPreferenceChangeListener((preference, newValue) ->
		{
			// If any of the auth feed modes, reveal login Preference Category, reveal the auth NSFW filtering,
			// and hide the ranking NSFW filtering
			if (Arrays.asList("follow", "bookmark", "tag_search", "artist", "recommended")
					.contains(newValue))
			{
				findPreference("prefCat_loginSettings").setVisible(true);
				findPreference("pref_authFilterSelect").setVisible(true);
				findPreference("pref_rankingFilterSelect").setVisible(false);
			} else
			{
				findPreference("pref_rankingFilterSelect").setVisible(true);
				findPreference("prefCat_loginSettings").setVisible(false);
				findPreference("pref_authFilterSelect").setVisible(false);
			}

			if (newValue.equals("tag_search"))
			{
				findPreference("pref_tagSearch").setVisible(true);
			} else
			{
				findPreference("pref_tagSearch").setVisible(false);
			}

			if (newValue.equals("artist"))
			{
				findPreference("pref_artistId").setVisible(true);
			} else
			{
				findPreference("pref_artistId").setVisible(false);
			}

			return true;
		});

		// Stores user toggleable variables into a temporary store for later comparison in onStop()
		// If the value of the preference on Activity creation is different to Activity stop, then take certain action
		oldCreds = sharedPrefs.getString("pref_loginPassword", "");
		newCreds = oldCreds;

		oldUpdateMode = sharedPrefs.getString("pref_updateMode", "");
		newUpdateMode = oldUpdateMode;

		oldTag = sharedPrefs.getString("pref_tagSearch", "");
		newTag = oldTag;

		oldArtist = sharedPrefs.getString("pref_artistId", "");
		newArtist = oldArtist;

		prefChangeListener = (sharedPreferences, key) ->
		{
			switch (key)
			{
				case "pref_loginPassword":
					newCreds = sharedPrefs.getString("pref_loginPassword", "");
					break;
				case "pref_updateMode":
					newUpdateMode = sharedPrefs.getString("pref_updateMode", "");
					break;
				case "pref_tagSearch":
					newTag = sharedPrefs.getString("pref_tagSearch", "");
					break;
				case "pref_artistId":
					newArtist = sharedPrefs.getString("pref_artistId", "");
					break;
			}
		};

		findPreference(getString(R.string.button_clearCache)).setOnPreferenceClickListener(preference ->
		{
			WorkManager.getInstance().cancelUniqueWork("ANTONY");
			File dir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

			String[] children = dir.list();
			for (String child : children)
			{
				new File(dir, child).delete();
			}
			PixivArtWorker.enqueueLoad(true);
			Toast.makeText(getContext(), getString(R.string.toast_clearingCache), Toast.LENGTH_SHORT).show();
			return true;
		});

		// Show authentication status as summary string below login button
		if (sharedPrefs.getString("accessToken", "").isEmpty())
		{
			findPreference("pref_loginId").setSummary(getString(R.string.prefSummary_authFail));
			//loginId.setSummary(Long.toString(System.currentTimeMillis()));
		} else
		{
			String summaryString = getString(R.string.prefSummary_authSuccess) + " " + sharedPrefs.getString("pref_loginId", "");
			findPreference("pref_loginId").setSummary(summaryString);
//                Uri profileImageUri = Uri.parse(sharedPrefs.getString("profileImageUri", ""));
//                loginId.setIcon();
		}
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
	{
	}

	private boolean isMuzeiInstalled()
	{
		boolean found = true;
		try
		{
			getContext().getPackageManager().getPackageInfo("net.nurik.roman.muzei", 0);
		} catch (PackageManager.NameNotFoundException ex)
		{
			found = false;
		}
		return found;
	}
}
