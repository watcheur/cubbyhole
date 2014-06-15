package com.supinfo.cubbyhole.mobileapp.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;

import org.apache.commons.io.FileUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.LangUtils;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.supinfo.cubbyhole.mobileapp.R;
import com.supinfo.cubbyhole.mobileapp.adapters.DrawerListAdapter;
import com.supinfo.cubbyhole.mobileapp.adapters.GenericListAdapter;
import com.supinfo.cubbyhole.mobileapp.adapters.MoveListAdapter;
import com.supinfo.cubbyhole.mobileapp.models.Back;
import com.supinfo.cubbyhole.mobileapp.models.Empty;
import com.supinfo.cubbyhole.mobileapp.models.File;
import com.supinfo.cubbyhole.mobileapp.models.Folder;
import com.supinfo.cubbyhole.mobileapp.utils.Data;
import com.supinfo.cubbyhole.mobileapp.utils.Utils;

/**
 * Created by anthonyvialleton on 04/04/14.
 */

public class Home extends ActionBarActivity implements OnRefreshListener {

	private static	PullToRefreshLayout mPullToRefreshLayout;
	private DrawerLayout			mDrawerLayout;
	private LinearLayout			mDrawerLinearLayout;
	private TextView				mDrawerTitleTv;
	private ListView				mDrawerList;
	private ActionBarDrawerToggle	mDrawerToggle;
	private ProgressBar 			pb;
	private ListView 				list;
	private GenericListAdapter 		listAdapter;

	private CharSequence			mDrawerTitle;
	private CharSequence			mTitle;
	private ArrayList<String>		mDrawerItems;
	public static	Object 			itemSelected = null;

	//private String 					m_chosenDir = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_drawer);

		Typeface typefaceFontTitleDrawer = Typeface.createFromAsset(getAssets(),"Roboto-MediumItalic.ttf"); 

		mDrawerItems = new ArrayList<String>();
		Collections.addAll(mDrawerItems, getResources().getStringArray(R.array.draweritems_array)); 
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLinearLayout = (LinearLayout) findViewById(R.id.drawer_ll);
		mDrawerTitleTv = (TextView) findViewById(R.id.drawer_title);
		mDrawerList = (ListView) findViewById(R.id.drawer_list);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		mDrawerList.setAdapter(new DrawerListAdapter(this, R.layout.drawer_list_item, mDrawerItems));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		mDrawerTitleTv.setText(Utils.getUserFromSharedPreferences(this).getEmail());
		mDrawerTitleTv.setTypeface(typefaceFontTitleDrawer);

		mTitle = mDrawerTitle = getTitle();

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,R.drawable.apptheme_ic_navigation_drawer, R.string.draweropen,R.string.drawerclose)
		{
			public void onDrawerClosed(View view)
			{
				getSupportActionBar().setTitle(mTitle);
				invalidateOptionsMenu();  // appelle onPrepareOptionsMenu
			}

			public void onDrawerOpened(View drawerView)
			{
				getSupportActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // appelle onPrepareOptionsMenu
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		Utils.DATA_ROOT = Utils.DATA_ROOT_BASE+Utils.getUserFromSharedPreferences(this).getId()+"/"+"root";
		
		if (savedInstanceState == null){selectItemHome();}
	}

	/**
	 * 	Drawer
	 */

	/* invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{

		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerLinearLayout);

		if (Data.currentDrawerSelected == Utils.DRAWER_SHARES_SELECTED && Data.currentFolder == null){
			menu.findItem(R.id.action_add).setVisible(false);
			menu.findItem(R.id.action_upload).setVisible(false);

			if (drawerOpen){
				menu.findItem(R.id.action_settings).setVisible(false);
			}

		}else{
			menu.findItem(R.id.action_add).setVisible(!drawerOpen);
			menu.findItem(R.id.action_upload).setVisible(!drawerOpen);
			menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	/* Listener de la list du Drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			if (position == Utils.DRAWER_SHARES_SELECTED){
				selectItemShares();
			}else if (position == Utils.DRAWER_HOME_SELECTED){
				selectItemHome();
			}
		}
	}

	private void selectItemShares()
	{
		Data.currentFolder = null;
		Fragment fragment = new ListFragmentShares();

		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

		mDrawerList.setItemChecked(Utils.DRAWER_SHARES_SELECTED, true);
		setTitle(mDrawerItems.get(Utils.DRAWER_SHARES_SELECTED));
		mDrawerLayout.closeDrawer(mDrawerLinearLayout);

		Data.currentDrawerSelected = Utils.DRAWER_SHARES_SELECTED;
	}

	private void selectItemHome()
	{
		Data.currentFolder = null;
		Fragment fragment = new ListFragmentHome();

		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

		mDrawerList.setItemChecked(Utils.DRAWER_HOME_SELECTED, true);
		setTitle(mDrawerItems.get(Utils.DRAWER_HOME_SELECTED));
		mDrawerLayout.closeDrawer(mDrawerLinearLayout);

		Data.currentDrawerSelected = Utils.DRAWER_HOME_SELECTED;
	}

	@Override
	public void setTitle(CharSequence title)
	{
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/**
	 * Fragment Accueil
	 */

	public static class ListFragmentHome extends Fragment
	{
		static Home myActivity;

		public ListFragmentHome(){}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			myActivity = (Home) getActivity();

			View rootView = inflater.inflate(R.layout.activity_home, container, false);
			mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.ptr_layout);
			// Instanciation du pulltorefresh
			ActionBarPullToRefresh.from(myActivity)
			.allChildrenArePullable()
			.listener(myActivity)
			.setup(mPullToRefreshLayout);

			String item = getResources().getStringArray(R.array.draweritems_array)[0];

			/*
			 *  List
			 */

			myActivity.pb = (ProgressBar)rootView.findViewById(R.id.home_pb);
			myActivity.list = (ListView)rootView.findViewById(R.id.home_list);
			registerForContextMenu(myActivity.list);

			// Instanciation de la vue avec ROOT
			myActivity.new GetData(rootView.getContext(), Utils.DATA_ROOT).execute();

			/*
			 *  Handlers
			 */

			SetHandlers();

			getActivity().setTitle(item);
			return rootView;
		}

		private static void SetHandlers(){

			myActivity.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

					if (myActivity.list.getAdapter().getItem(position) instanceof Folder){

						Folder folderSelected = (Folder) myActivity.list.getAdapter().getItem(position);
						// On entre dans le level inferieur
						myActivity.new GetData(myActivity, Utils.DATA_FOLDER+folderSelected.getId()).execute();
					}else if (myActivity.list.getAdapter().getItem(position) instanceof File){

						File fileSelected = (File) myActivity.list.getAdapter().getItem(position);
						Home.itemSelected = fileSelected;
						// Telechargement du fichier depuis le webservice et affichage
						myActivity.new OpenFile(myActivity, fileSelected).execute();

					}else if (myActivity.list.getAdapter().getItem(position) instanceof Back){

						if (Data.currentFolder.getParentID() == -1){
							// Requete vers le folder root
							Data.currentFolder = null;
							myActivity.new GetData(myActivity, Utils.DATA_ROOT).execute();
						}else{
							// Requete vers le folder parent
							myActivity.new GetData(myActivity, Utils.DATA_FOLDER+Data.currentFolder.getParentID()).execute();
						}

					}

				}
			});

		}
	}

	/**
	 * Fragment Mes partages
	 */

	public static class ListFragmentShares extends Fragment
	{
		static Home myActivity;

		public ListFragmentShares(){}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			myActivity = (Home) getActivity();

			View rootView = inflater.inflate(R.layout.activity_home, container, false);
			mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.ptr_layout);
			// Instanciation du pulltorefresh
			ActionBarPullToRefresh.from(myActivity)
			.allChildrenArePullable()
			.listener(myActivity)
			.setup(mPullToRefreshLayout);

			String item = getResources().getStringArray(R.array.draweritems_array)[1];

			/*
			 *  List
			 */

			myActivity.pb = (ProgressBar)rootView.findViewById(R.id.home_pb);
			myActivity.list = (ListView)rootView.findViewById(R.id.home_list);
			registerForContextMenu(myActivity.list);

			// Instanciation de la vue avec ROOT
			myActivity.new GetDataSharesRoot(rootView.getContext(), Utils.DATA_ROOT).execute();

			/*
			 *  Handlers
			 */

			SetHandlers();

			getActivity().setTitle(item);
			return rootView;
		}

		private static void SetHandlers(){

			myActivity.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

					if (myActivity.list.getAdapter().getItem(position) instanceof Folder){

						Folder folderSelected = (Folder) myActivity.list.getAdapter().getItem(position);

						// On entre dans le level inferieur
						myActivity.new GetData(myActivity, Utils.DATA_FOLDER+folderSelected.getId()).execute();

					}else if (myActivity.list.getAdapter().getItem(position) instanceof File){

						File fileSelected = (File) myActivity.list.getAdapter().getItem(position);
						Home.itemSelected = fileSelected;
						// Telechargement du fichier depuis le webservice et affichage
						myActivity.new OpenFile(myActivity, fileSelected).execute();

					}else if (myActivity.list.getAdapter().getItem(position) instanceof Back){

						if (Data.currentFolder.getParentID() == -1){
							// Requete vers le folder root
							Data.currentFolder = null;
							myActivity.invalidateOptionsMenu(); // appelle onPrepareOptionsMenu

							myActivity.new GetDataSharesRoot(myActivity, Utils.DATA_ROOT).execute();

						}else{
							// Requete vers le folder parent
							myActivity.new GetData(myActivity, Utils.DATA_FOLDER+Data.currentFolder.getParentID()).execute();
						}

					}

				}
			});

		}
	}

	/**
	 * 	Menus
	 */

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		MenuInflater inflater = getMenuInflater();

		if (Data.currentDrawerSelected == Utils.DRAWER_SHARES_SELECTED){

			if (list.getAdapter().getItem(info.position) instanceof Folder){
				inflater.inflate(R.menu.contextual_menu_drawer_folder, menu);
			}else if (list.getAdapter().getItem(info.position) instanceof File){
				inflater.inflate(R.menu.contextual_menu_drawer_file, menu);
			}

		}else if (Data.currentDrawerSelected == Utils.DRAWER_HOME_SELECTED){

			if (list.getAdapter().getItem(info.position) instanceof Folder){
				inflater.inflate(R.menu.contextual_menu_folder, menu);
			}else if (list.getAdapter().getItem(info.position) instanceof File){
				inflater.inflate(R.menu.contextual_menu_file, menu);
			}

		}

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		Folder folderSelected = null;
		File fileSelected = null;

		if (list.getAdapter().getItem(info.position) instanceof Folder){

			folderSelected = (Folder) list.getAdapter().getItem(info.position);
			itemSelected = (Object) folderSelected;

		}else if (list.getAdapter().getItem(info.position) instanceof File){

			fileSelected = (File) list.getAdapter().getItem(info.position);
			itemSelected = (Object) fileSelected;
		}

		switch (item.getItemId()) {
		// File
		case R.id.context_file_rename:
			if (itemSelected != null){
				AlertDialog.Builder alert = new AlertDialog.Builder(Home.this);
				alert.setTitle("Edition du fichier");
				alert.setMessage("Merci de spécifier un nouveau nom pour ce fichier :");
				final EditText input = new EditText(Home.this);
				alert.setView(input);
				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						String value = input.getText().toString();

						if (!value.trim().isEmpty()){

							final File fileSelected = (File)itemSelected;
							fileSelected.setName((Utils.GetNewFileNameWithExtension(fileSelected.getName(), input.getText().toString())));

							List<NameValuePair> pairs = new ArrayList<NameValuePair>();
							pairs.add(new BasicNameValuePair(Utils.JSON_FOLDER_NAME, fileSelected.getName()));

							new UpdateData(Home.this, fileSelected, pairs).execute();

						}else{
							Utils.DisplayToast(Home.this, "Le champs nom ne peut pas etre vide..");
						}

					}
				});
				alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});
				alert.show();

			}
			return true;

		case R.id.context_file_move:
			if (itemSelected != null){
				final AlertDialog alertDialog = new AlertDialog.Builder(Home.this).create();
				LayoutInflater inflater = getLayoutInflater();
				View convertView = (View) inflater.inflate(R.layout.listview_move, null);
				alertDialog.setTitle("Déplacer le fichier vers");
				final ListView lv = (ListView) convertView.findViewById(R.id.move_lv);

				List<Folder> folderItems = new ArrayList<Folder>();
				for(Object obj : Data.currentArray){
					if (obj instanceof Back){
						Folder backFolder = new Folder();
						backFolder.setName("Dossier précédent");
						backFolder.setId(Data.currentFolder.getParentID());
						folderItems.add(backFolder);
					}else if (obj instanceof Folder){
						folderItems.add((Folder)obj);
					}
				}

				lv.setOnItemClickListener(new OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> adapter, View v, int position,
							long id) 
					{
						Folder folderSelected = (Folder)lv.getAdapter().getItem(position);

						List<NameValuePair> pairs = new ArrayList<NameValuePair>();
						if (folderSelected.getId() == -1){
							pairs.add(new BasicNameValuePair("folder_id", "null"));
						}else{
							pairs.add(new BasicNameValuePair("folder_id", Integer.toString(folderSelected.getId())));
						}

						new UpdateData(Home.this, itemSelected, pairs).execute();
						alertDialog.dismiss();
					}
				});
				
				final MoveListAdapter moveListAdapter = new MoveListAdapter(Home.this, R.layout.listview_move, folderItems);
				lv.setAdapter(moveListAdapter);
				alertDialog.setView(convertView);
				
				// Shared
				final GetFoldersSharesRoot getFoldersShared = new  GetFoldersSharesRoot(this){
					@Override
					protected void onPostExecute(List<Folder> downloadedArray) {
						super.onPostExecute(downloadedArray);
						
						if (downloadedArray != null){
							moveListAdapter.addAll(downloadedArray);
							moveListAdapter.notifyDataSetChanged();
						}
					}
				};
				
				alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			        @Override
			        public void onShow(DialogInterface dialogInterface) {
			        	getFoldersShared.execute();
			        }
			    });
				alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			        @Override
			        public void onDismiss(DialogInterface dialogInterface) {
			        	getFoldersShared.cancel(true);
			        }
			    });
				
				alertDialog.show();
				
			}
			return true;

		case R.id.context_file_export:
			if (itemSelected != null){

				final Intent chooserIntent = new Intent(
						Home.this,
						DirectoryChooserActivity.class);

				chooserIntent.putExtra(
						DirectoryChooserActivity.EXTRA_NEW_DIR_NAME,
						"Nouveau dossier");

				startActivityForResult(chooserIntent, Utils.INTENT_FOLDERCHOOSER);

				/*DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(Home.this, new DirectoryChooserDialog.ChosenDirectoryListener() 
		                {
		                    @Override
		                    public void onChosenDir(String chosenDir) 
		                    {
		                        new ExportFile(Home.this, chosenDir, (File) itemSelected).execute();
		                    }
		                });
	                directoryChooserDialog.setNewFolderEnabled(true);
	                directoryChooserDialog.chooseDirectory(m_chosenDir);*/
			}
			return true;

		case R.id.context_file_share:
			if (itemSelected != null){
				Intent intent_to_detail = new Intent(Home.this, DetailActivity.class);
				startActivityForResult(intent_to_detail, Utils.INTENT_DETAIL);
			}
			return true;

		case R.id.context_file_delete:
			if (itemSelected != null){

				final File file = (File) itemSelected;

				if (file.getIsFromShared() && file.getIdShare() != -1){
					
					AlertDialog.Builder alert = new AlertDialog.Builder(Home.this);
					alert.setTitle("Suppression du partage");
					alert.setMessage("Êtes-vous sûr de vouloir supprimer le partage de ce fichier définitivement ?");
					alert.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							new DeleteShare(Home.this, file.getIdShare()).execute();
						}
					});
					alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					});
					alert.show();
					
				}else{
					new DeleteData(Home.this, itemSelected).execute();
				}

			}
			return true;

			// Folder
		case R.id.context_folder_rename:
			if (itemSelected != null){

				AlertDialog.Builder alert = new AlertDialog.Builder(Home.this);
				alert.setTitle("Edition du dossier");
				alert.setMessage("Merci de spécifier un nouveau nom pour ce dossier :");
				final EditText input = new EditText(Home.this);
				alert.setView(input);
				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						String value = input.getText().toString();

						if (!value.trim().isEmpty()){

							final Folder folderSelected = (Folder)itemSelected;
							folderSelected.setName(input.getText().toString());

							List<NameValuePair> pairs = new ArrayList<NameValuePair>();
							pairs.add(new BasicNameValuePair(Utils.JSON_FOLDER_NAME, folderSelected.getName()));

							new UpdateData(Home.this, folderSelected, pairs).execute();

						}else{
							Utils.DisplayToast(Home.this, "Le champs nom ne peut pas etre vide..");
						}

					}
				});
				alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});
				alert.show();

			}
			return true;

		case R.id.context_folder_move:
			if (itemSelected != null){
				
				if (Data.currentDrawerSelected == Utils.DRAWER_SHARES_SELECTED){ // Shares
				
					final AlertDialog alertDialog2 = new AlertDialog.Builder(Home.this).create();
					LayoutInflater inflater2 = getLayoutInflater();
					View convertView2 = (View) inflater2.inflate(R.layout.listview_move, null);
					alertDialog2.setTitle("Déplacer le dossier vers");
					final ListView lv2 = (ListView) convertView2.findViewById(R.id.move_lv);

					List<Folder> folderItems2 = new ArrayList<Folder>();
					for(Object obj : Data.currentArray){
						if (obj instanceof Back){
							Folder backFolder = new Folder();
							backFolder.setName("Dossier précédent");
							backFolder.setId(Data.currentFolder.getParentID());
							folderItems2.add(backFolder);
						}else if (obj instanceof Folder){
							if (itemSelected != obj){
								folderItems2.add((Folder)obj);
							}
						}
					}

					final MoveListAdapter moveListAdapter2 = new MoveListAdapter(Home.this, R.layout.listview_move, folderItems2);
					lv2.setAdapter(moveListAdapter2);

					lv2.setOnItemClickListener(new OnItemClickListener()
					{
						@Override
						public void onItemClick(AdapterView<?> adapter, View v, int position,
								long id) 
						{
							Folder folderSelected = (Folder)lv2.getAdapter().getItem(position);

							List<NameValuePair> pairs = new ArrayList<NameValuePair>();
							if (folderSelected.getId() == -1){
								pairs.add(new BasicNameValuePair("folder_id", "null"));
							}else{
								pairs.add(new BasicNameValuePair("folder_id", Integer.toString(folderSelected.getId())));
							}

							new UpdateData(Home.this, itemSelected, pairs).execute();
							alertDialog2.dismiss();
						}
					});

					alertDialog2.setView(convertView2);
					
					// Folders root
					final GetFoldersRoot getFolders = new  GetFoldersRoot(this){
						@Override
						protected void onPostExecute(List<Folder> downloadedArray) {
							super.onPostExecute(downloadedArray);
							
							moveListAdapter2.addAll(downloadedArray);
							moveListAdapter2.notifyDataSetChanged();
						}
					};
					
					alertDialog2.setOnShowListener(new DialogInterface.OnShowListener() {
				        @Override
				        public void onShow(DialogInterface dialogInterface) {
				        	getFolders.execute();
				        }
				    });
					alertDialog2.setOnDismissListener(new DialogInterface.OnDismissListener() {
				        @Override
				        public void onDismiss(DialogInterface dialogInterface) {
				        	getFolders.cancel(true);
				        }
				    });
					
					alertDialog2.show();
				}else{ // Home
					
					final AlertDialog alertDialog2 = new AlertDialog.Builder(Home.this).create();
					LayoutInflater inflater2 = getLayoutInflater();
					View convertView2 = (View) inflater2.inflate(R.layout.listview_move, null);
					alertDialog2.setTitle("Déplacer le dossier vers");
					final ListView lv2 = (ListView) convertView2.findViewById(R.id.move_lv);

					List<Folder> folderItems2 = new ArrayList<Folder>();
					for(Object obj : Data.currentArray){
						if (obj instanceof Back){
							Folder backFolder = new Folder();
							backFolder.setName("Dossier précédent");
							backFolder.setId(Data.currentFolder.getParentID());
							folderItems2.add(backFolder);
						}else if (obj instanceof Folder){
							if (itemSelected != obj){
								folderItems2.add((Folder)obj);
							}
						}
					}

					final MoveListAdapter moveListAdapter2 = new MoveListAdapter(Home.this, R.layout.listview_move, folderItems2);
					lv2.setAdapter(moveListAdapter2);

					lv2.setOnItemClickListener(new OnItemClickListener()
					{
						@Override
						public void onItemClick(AdapterView<?> adapter, View v, int position,
								long id) 
						{
							Folder folderSelected = (Folder)lv2.getAdapter().getItem(position);

							List<NameValuePair> pairs = new ArrayList<NameValuePair>();
							if (folderSelected.getId() == -1){
								pairs.add(new BasicNameValuePair("folder_id", "null"));
							}else{
								pairs.add(new BasicNameValuePair("folder_id", Integer.toString(folderSelected.getId())));
							}

							new UpdateData(Home.this, itemSelected, pairs).execute();
							alertDialog2.dismiss();
						}
					});

					alertDialog2.setView(convertView2);
					
					// Shared
					final GetFoldersSharesRoot getFoldersShared = new  GetFoldersSharesRoot(this){
						@Override
						protected void onPostExecute(List<Folder> downloadedArray) {
							super.onPostExecute(downloadedArray);
							
							moveListAdapter2.addAll(downloadedArray);
							moveListAdapter2.notifyDataSetChanged();
						}
					};
					
					alertDialog2.setOnShowListener(new DialogInterface.OnShowListener() {
				        @Override
				        public void onShow(DialogInterface dialogInterface) {
				        	getFoldersShared.execute();
				        }
				    });
					alertDialog2.setOnDismissListener(new DialogInterface.OnDismissListener() {
				        @Override
				        public void onDismiss(DialogInterface dialogInterface) {
				        	getFoldersShared.cancel(true);
				        }
				    });
					
					alertDialog2.show();
				}
				
				
			}
			return true;

		case R.id.context_folder_share:
			if (itemSelected != null){
				Intent intent_to_detail2 = new Intent(Home.this, DetailActivity.class);
				startActivityForResult(intent_to_detail2, Utils.INTENT_DETAIL);
			}
			return true;

		case R.id.context_folder_delete:

			final Folder folder = (Folder) itemSelected;

			if (folder.getIsFromShared() && folder.getIdShare() != -1){
				
				AlertDialog.Builder alert = new AlertDialog.Builder(Home.this);
				alert.setTitle("Suppression du partage");
				alert.setMessage("Êtes-vous sûr de vouloir supprimer le partage de ce dossier définitivement ?");
				alert.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						new DeleteShare(Home.this, folder.getIdShare()).execute();
					}
				});
				alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});
				alert.show();
				
			}else{
				new DeleteData(Home.this, itemSelected).execute();
			}

			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}


	@SuppressLint("InlinedApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item))
		{
			return true;
		}

		switch (item.getItemId()) {

		case R.id.action_settings:

			Utils.removeUserFromSharedPreferences(this);
			Intent intent_to_login = new Intent(this, LoginActivity.class);
			startActivity(intent_to_login);
			finish();

			return true;

		case R.id.action_add:

			AlertDialog.Builder alert = new AlertDialog.Builder(Home.this);
			alert.setTitle("Ajout d'élément dans le repertoire courant");
			alert.setMessage("Que souhaitez vous faire ?");
			alert.setPositiveButton("Ajouter un fichier texte", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					AlertDialog.Builder alert = new AlertDialog.Builder(Home.this);
					alert.setTitle("Ajout d'un fichier texte");
					alert.setMessage("Merci de spécifier un nom pour ce fichier :");
					final EditText input = new EditText(Home.this);
					alert.setView(input);
					alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

							String value = input.getText().toString();

							List<NameValuePair> pairs = new ArrayList<NameValuePair>();
							if (!value.trim().isEmpty() && !value.contains(".") && !value.contains("!") && !value.contains("//") && !value.contains("#")){
								pairs.add(new BasicNameValuePair("name", value+".txt"));
								pairs.add(new BasicNameValuePair("user_id", String.valueOf(Utils.getUserFromSharedPreferences(Home.this).getId())));
								if (Data.currentFolder == null){
									pairs.add(new BasicNameValuePair("folder_id", "null"));
								}else{
									pairs.add(new BasicNameValuePair("folder_id", String.valueOf(Data.currentFolder.getId())));
								}
								new AddData(Home.this, new File(), pairs).execute();
							}else{
								Utils.DisplayToast(Home.this, "Les caracteres : //!.#&* sont interdits pour la creation d'un fichier..");
							}

						}
					});
					alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					});
					alert.show();

				}
			});
			alert.setNegativeButton("Ajouter un dossier", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					AlertDialog.Builder alert = new AlertDialog.Builder(Home.this);
					alert.setTitle("Ajout d'un dossier");
					alert.setMessage("Merci de spécifier un nom pour ce dossier :");
					final EditText input = new EditText(Home.this);
					alert.setView(input);
					alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

							String value = input.getText().toString();

							List<NameValuePair> pairs = new ArrayList<NameValuePair>();
							if (!value.trim().isEmpty() && !value.contains(".") && !value.contains("!") && !value.contains("//") && !value.contains("#")){
								pairs.add(new BasicNameValuePair("name", value));
								pairs.add(new BasicNameValuePair("user_id", String.valueOf(Utils.getUserFromSharedPreferences(Home.this).getId())));
								if (Data.currentFolder == null){
									pairs.add(new BasicNameValuePair("parent", "null"));
								}else{
									pairs.add(new BasicNameValuePair("folder_id", String.valueOf(Data.currentFolder.getId())));
								}
								new AddData(Home.this, new Folder(), pairs).execute();
							}else{
								Utils.DisplayToast(Home.this, "Les caracteres : //!.#&* sont interdits pour la creation d'un fichier..");
							}

						}
					});
					alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					});
					alert.show();

				}
			});
			alert.show();

			return true;

		case R.id.action_upload:

			Intent target = com.ipaulpro.afilechooser.utils.FileUtils.createGetContentIntent();
			Intent intent = Intent.createChooser(
					target, "Choisissez un fichier");
			try {
				startActivityForResult(intent, Utils.INTENT_AFILECHOOSER);
			} catch (ActivityNotFoundException e) {
			}

			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		System.out.println("request code : "+requestCode+" result code : "+resultCode);

		//if (resultCode != Activity.RESULT_OK) return;
		// if (null == data) return;

		if (requestCode == Utils.INTENT_AFILECHOOSER){
			try {

				Uri uri = data.getData();
				java.io.File file = com.ipaulpro.afilechooser.utils.FileUtils.getFile(this, uri);

				if (file.exists()){
					List<NameValuePair> pairs = new ArrayList<NameValuePair>();

					pairs.add(new BasicNameValuePair("name", file.getName()));
					pairs.add(new BasicNameValuePair("user_id", String.valueOf(Utils.getUserFromSharedPreferences(Home.this).getId())));
					if (Data.currentFolder == null){
						pairs.add(new BasicNameValuePair("folder_id", "null"));
					}else{
						pairs.add(new BasicNameValuePair("folder_id", String.valueOf(Data.currentFolder.getId())));
					}

					new AddImage(file, this, pairs).execute();
				}
			} catch (Exception e) {
				Log.e("Onactivityresult Home Intent Afilechooser", "File select error", e);
			}
		}else if (requestCode == Utils.INTENT_DETAIL){

			RefreshViewHomeBlock();

		}else if (requestCode == Utils.INTENT_OPEN){

			java.io.File sdCard = Environment.getExternalStorageDirectory();
			java.io.File directory = new java.io.File (sdCard.getAbsolutePath() + "/Cubbyhole/Cache");
			File file = (File) itemSelected;

			if (file.getName() != null && file.getName().contains(".txt") || file.getName().contains(".doc") || file.getName().contains(".docx")){

				// Comparaison du file avant ouverture (tmp) et du file en retour d'ouverture
				java.io.File fileTmp = new java.io.File(directory.getAbsolutePath()+"/"+Utils.GetPathTmpFile(file.getName()));
				java.io.File fileReturned = new java.io.File(directory.getAbsolutePath()+"/"+file.getName());
				
				boolean fileContentIsEqual;
				try {

					fileContentIsEqual = org.apache.commons.io.FileUtils.contentEquals(fileTmp, fileReturned);
					System.out.println("File txt returned content is equal : "+fileContentIsEqual);
					if (!fileContentIsEqual){
						new UpdateFileTxt(Home.this, file, fileReturned).execute();
					}else{
						// Clear cache
						try {
							FileUtils.cleanDirectory(directory);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}

			}else{
				// Clear cache
				try {
					FileUtils.cleanDirectory(directory);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} 

		}else if (requestCode ==  Utils.INTENT_FOLDERCHOOSER){

			if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
				new ExportFile(Home.this, data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR), (File) itemSelected).execute();
			} else {
				//Utils.DisplayToast(Home.this, "Le chemin specifie n'est pas disponible, merci de reessayer ulterieurement.");
			}

		}

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		java.io.File sdCard = Environment.getExternalStorageDirectory();
		java.io.File directory = new java.io.File (sdCard.getAbsolutePath() + "/Cubbyhole/Cache");

		try {
			FileUtils.cleanDirectory(directory);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	/**
	 *  Asynctask
	 *
	 */

	public class ExportFile extends AsyncTask<Void, Integer, java.io.File> {

		private Context ctx;
		private File file;
		private String pathToSave;
		private  ProgressDialog ringProgressDialog;

		public ExportFile(Context ctx, String pathToSave, File file) {
			this.ctx = ctx;
			this.file = file;
			this.pathToSave = pathToSave;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			ringProgressDialog = ProgressDialog.show(ctx, "Veuillez patienter...", "Export du fichier en cours..", true);
			ringProgressDialog.setCancelable(false);
		}

		@Override
		protected java.io.File doInBackground(Void... params) {

			return Utils.UrlToFileDownloadForExport(ctx, pathToSave, file);

		}

		@Override
		protected void onPostExecute(java.io.File file) {
			super.onPostExecute(file);

			ringProgressDialog.dismiss();

			if (file != null){
				Utils.DisplayToast(this.ctx, "Le fichier a été exporté avec succès!");
			}else{
				Utils.DisplayToast(ctx, Data.errorMessage);
				Data.errorMessage = getResources().getString(R.string.errorMessage);
			}

		}

	}



	public class OpenFile extends AsyncTask<Void, Integer, java.io.File> {

		private Context ctx;
		private File file;
		private  ProgressDialog ringProgressDialog;

		public OpenFile(Context ctx, File file) {
			this.ctx = ctx;
			this.file = file;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			ringProgressDialog = ProgressDialog.show(ctx, "Veuillez patienter...", "Récupération du fichier en cours..", true);
			ringProgressDialog.setCancelable(false);
		}

		@Override
		protected java.io.File doInBackground(Void... params) {

			return Utils.UrlToFileCache(this.ctx, this.file);
		}

		@Override
		protected void onPostExecute(java.io.File file) {
			super.onPostExecute(file);

			ringProgressDialog.dismiss();

			if (file != null){
				Utils.OpenFile(Home.this, file);
			}else{
				Utils.DisplayToast(ctx, Data.errorMessage);
				Data.errorMessage = getResources().getString(R.string.errorMessage);
			}

		}

	}

	public class AddData extends AsyncTask<Void, Integer, Boolean> {

		private Context ctx;
		private Object item;
		private List<NameValuePair> pairs;

		public AddData(Context ctx, Object item, List<NameValuePair> pairs) {
			this.ctx = ctx;
			this.item = item;
			this.pairs = pairs;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pb.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			if (item instanceof Folder){
				return Utils.AddFolder(this.ctx, pairs);
			}else if (item instanceof File){
				try {
					return Utils.AddFile(this.ctx, pairs);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
			return false;

		}

		@Override
		protected void onPostExecute(Boolean isGood) {
			super.onPostExecute(isGood);

			if (!isGood){
				Utils.DisplayToast(ctx, Data.errorMessage);
				Data.errorMessage = getResources().getString(R.string.errorMessage);
			}

			pb.setVisibility(View.GONE);
			onRefreshStarted(new View(ctx));
		}

	}

	public class AddImage extends AsyncTask<Void, Integer, Boolean> {

		private Context ctx;
		private List<NameValuePair> pairs;
		private java.io.File file;
		ProgressDialog ringProgressDialog;

		public AddImage(java.io.File file, Context ctx,List<NameValuePair> pairs) {
			this.ctx = ctx;
			this.pairs = pairs;
			this.file = file;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			ringProgressDialog = ProgressDialog.show(ctx, "Veuillez patienter...", "Chargement du fichier en cours..", true);
			ringProgressDialog.setCancelable(false);

		}

		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				return Utils.AddImage(this.file ,this.ctx, pairs);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}

		}

		@Override
		protected void onPostExecute(Boolean isGood) {
			super.onPostExecute(isGood);

			if (!isGood){
				Utils.DisplayToast(ctx, Data.errorMessage);
				Data.errorMessage = getResources().getString(R.string.errorMessage);
			}

			ringProgressDialog.dismiss();
			onRefreshStarted(new View(ctx));
		}

	}

	public class UpdateData extends AsyncTask<Void, Integer, Boolean> {

		private Context ctx;
		private Object item;
		private List<NameValuePair> pairs;

		public UpdateData(Context ctx, Object item, List<NameValuePair> pairs) {
			this.ctx = ctx;
			this.item = item;
			this.pairs = pairs;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pb.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			if (item instanceof Folder){
				return Utils.UpdateFolder(ctx, (Folder)item, pairs);
			}else if (item instanceof File){
				return Utils.UpdateFileSimple(ctx, (File)item, pairs);
			}
			return false;

		}

		@Override
		protected void onPostExecute(Boolean isGood) {
			super.onPostExecute(isGood);

			if (!isGood){
				Utils.DisplayToast(ctx, Data.errorMessage);
				Data.errorMessage = getResources().getString(R.string.errorMessage);
			}

			itemSelected = null;
			pb.setVisibility(View.GONE);
			onRefreshStarted(new View(ctx));

		}

	}

	public class UpdateFileTxt extends AsyncTask<Void, Integer, Boolean> {

		private Context ctx;
		private File file;
		private java.io.File fileTxt;
		private  ProgressDialog ringProgressDialog;

		public UpdateFileTxt(Context ctx, File file, java.io.File fileTxt) {
			this.ctx = ctx;
			this.file = file;
			this.fileTxt = fileTxt;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			ringProgressDialog = ProgressDialog.show(ctx, "Veuillez patienter...", "Mise à jour du fichier en cours..", true);
			ringProgressDialog.setCancelable(false);
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				return Utils.UpdateFileTxt(ctx, file, fileTxt);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}

		}

		@Override
		protected void onPostExecute(Boolean isGood) {
			super.onPostExecute(isGood);

			if (!isGood){
				Utils.DisplayToast(ctx, Data.errorMessage);
				Data.errorMessage = getResources().getString(R.string.errorMessage);
			}

			itemSelected = null;
			ringProgressDialog.dismiss();
			onRefreshStarted(new View(ctx));
		}

	}

	public class DeleteShare extends AsyncTask<Void, Integer, Boolean> {

		private Context ctx;
		private int idShare;

		public DeleteShare(Context ctx, int idShare) {
			this.ctx = ctx;
			this.idShare = idShare;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pb.setVisibility(View.VISIBLE);
		}

		protected Boolean doInBackground(Void... params) {

			return Utils.DeleteShare(ctx, idShare);

		}

		@Override
		protected void onPostExecute(Boolean isGood) {
			super.onPostExecute(isGood);

			if (!isGood){
				Utils.DisplayToast(ctx, Data.errorMessage);
				Data.errorMessage = getResources().getString(R.string.errorMessage);
			}

			itemSelected = null;
			pb.setVisibility(View.GONE);
			onRefreshStarted(new View(ctx));

		}

	}

	public class DeleteData extends AsyncTask<Void, Integer, Boolean> {

		private Context ctx;
		private Object item;

		public DeleteData(Context ctx, Object item) {
			this.ctx = ctx;
			this.item = item;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pb.setVisibility(View.VISIBLE);
		}

		protected Boolean doInBackground(Void... params) {

			if (item instanceof Folder){
				return Utils.DeleteFolder(ctx, (Folder)item);
			}else if (item instanceof File){
				return Utils.DeleteFile(ctx, (File)item);
			}
			return false;

		}

		@Override
		protected void onPostExecute(Boolean isGood) {
			super.onPostExecute(isGood);

			if (!isGood){
				Utils.DisplayToast(ctx, Data.errorMessage);
				Data.errorMessage = getResources().getString(R.string.errorMessage);
			}

			itemSelected = null;
			pb.setVisibility(View.GONE);
			onRefreshStarted(new View(ctx));

		}

	}

	private class GetFoldersSharesRoot extends AsyncTask<Void, Integer, List<Folder>> {

		private Context ctx;

		public GetFoldersSharesRoot(Context ctx) {
			this.ctx = ctx;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		protected List<Folder> doInBackground(Void... params) {

			try {

				return Utils.GetFoldersSharesRoot(ctx);

			} catch (Exception e) {
				Log.w(getClass().getSimpleName(), "exception Connect : Json");
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<Folder> downloadedArray) {
			super.onPostExecute(downloadedArray);
		}

	}
	
	private class GetFoldersRoot extends AsyncTask<Void, Integer, List<Folder>> {

		private Context ctx;

		public GetFoldersRoot(Context ctx) {
			this.ctx = ctx;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		protected List<Folder> doInBackground(Void... params) {

			try {

				return Utils.GetFoldersRoot(ctx);

			} catch (Exception e) {
				Log.w(getClass().getSimpleName(), "exception Connect : Json");
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<Folder> downloadedArray) {
			super.onPostExecute(downloadedArray);
		}

	}
	
	private class GetDataSharesRoot extends AsyncTask<Void, Integer, List<Object>> {

		private String url;
		private Context ctx;

		public GetDataSharesRoot(Context ctx, String url) {
			this.url = url;
			this.ctx = ctx;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pb.setVisibility(View.VISIBLE);

		}

		protected List<Object> doInBackground(Void... params) {

			try {

				return Utils.GetDataSharesRoot(ctx, url);

			} catch (Exception e) {
				Log.w(getClass().getSimpleName(), "exception Connect : Json");
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<Object> downloadedArray) {
			super.onPostExecute(downloadedArray);


			if (downloadedArray != null && downloadedArray.size() > 0){

				if (listAdapter != null){
					listAdapter.clear();
					listAdapter.notifyDataSetChanged();
				}

				Data.currentArray = downloadedArray;

				listAdapter = new GenericListAdapter(Home.this, R.layout.listview_item, downloadedArray);
				list.setAdapter(listAdapter);

			}else{

				List<Object> emptyItemArray = new ArrayList<Object>();
				Empty emptyItem = new Empty("Aucun fichier.");
				emptyItemArray.add(emptyItem);

				listAdapter = new GenericListAdapter(Home.this, R.layout.listview_item, emptyItemArray);
				list.setAdapter(listAdapter);

			}

			if (pb!=null){pb.setVisibility(View.GONE);}
			if (mPullToRefreshLayout != null){
				mPullToRefreshLayout.setRefreshComplete();
			}
		}

	}

	private class GetData extends AsyncTask<Void, Integer, List<Object>> {

		private String url;
		private Context ctx;

		public GetData(Context ctx, String url) {
			this.url = url;
			this.ctx = ctx;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pb.setVisibility(View.VISIBLE);

		}

		protected List<Object> doInBackground(Void... params) {

			try {


				return Utils.GetData(ctx, url);

			} catch (Exception e) {
				Log.w(getClass().getSimpleName(), "exception Connect : Json");
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<Object> downloadedArray) {
			super.onPostExecute(downloadedArray);

			invalidateOptionsMenu();

			if (downloadedArray != null && downloadedArray.size() > 0){

				if (listAdapter != null){
					listAdapter.clear();
					listAdapter.notifyDataSetChanged();
				}

				Data.currentArray = downloadedArray;

				listAdapter = new GenericListAdapter(Home.this, R.layout.listview_item, downloadedArray);
				list.setAdapter(listAdapter);

			}else{

				List<Object> emptyItemArray = new ArrayList<Object>();
				if (Data.currentFolder != null){
					Back back = new Back(ctx.getResources().getString(R.string.previous_folder));
					emptyItemArray.add(back);
				}
				Empty emptyItem = new Empty("Aucun fichier.");
				emptyItemArray.add(emptyItem);

				listAdapter = new GenericListAdapter(Home.this, R.layout.listview_item, emptyItemArray);
				list.setAdapter(listAdapter);

			}

			if (pb!=null){pb.setVisibility(View.GONE);}
			if (mPullToRefreshLayout != null){
				mPullToRefreshLayout.setRefreshComplete();
			}
		}

	}

	private class GetDataBlock extends AsyncTask<Void, Integer, List<Object>> {

		private String url;
		private Context ctx;
		private ProgressDialog ringProgressDialog;

		public GetDataBlock(Context ctx, String url) {
			this.url = url;
			this.ctx = ctx;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			ringProgressDialog = ProgressDialog.show(ctx, "Veuillez patienter...", "Mise à jour en cours..", true);
			ringProgressDialog.setCancelable(false);

		}

		protected List<Object> doInBackground(Void... params) {

			try {

				return Utils.GetData(ctx, url);

			} catch (Exception e) {
				Log.w(getClass().getSimpleName(), "exception Connect : Json");
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<Object> downloadedArray) {
			super.onPostExecute(downloadedArray);

			if (downloadedArray != null && downloadedArray.size() > 0){

				if (listAdapter != null){
					listAdapter.clear();
					listAdapter.notifyDataSetChanged();
				}

				Data.currentArray = downloadedArray;

				listAdapter = new GenericListAdapter(Home.this, R.layout.listview_item, downloadedArray);
				list.setAdapter(listAdapter);

			}else{

				List<Object> emptyItemArray = new ArrayList<Object>();
				if (Data.currentFolder != null){
					Back back = new Back(ctx.getResources().getString(R.string.previous_folder));
					emptyItemArray.add(back);
				}
				Empty emptyItem = new Empty("Aucun fichier.");
				emptyItemArray.add(emptyItem);

				listAdapter = new GenericListAdapter(Home.this, R.layout.listview_item, emptyItemArray);
				list.setAdapter(listAdapter);

			}

			ringProgressDialog.dismiss();
			mPullToRefreshLayout.setRefreshComplete();
		}

	}

	/**
	 *  Refresh Layout
	 */

	private void RefreshViewHome(){

		if (Utils.IsNetworkAvailable(this)){
			// Le folder courant est le root
			if (Data.currentFolder == null){
				new GetData(Home.this, Utils.DATA_ROOT).execute();
			}else{
				new GetData(Home.this, Utils.DATA_FOLDER+Data.currentFolder.getId()).execute();
			}
		}else{
			mPullToRefreshLayout.setRefreshComplete();
			Utils.DisplayToast(this, "Actualisation des donnees impossible car il n'y aucun reseau disponible.");
		}

	}

	private void RefreshViewHomeBlock(){

		if (Utils.IsNetworkAvailable(this)){
			// Le folder courant est le root
			if (Data.currentFolder == null){
				new GetDataBlock(Home.this, Utils.DATA_ROOT).execute();
			}else{
				new GetDataBlock(Home.this, Utils.DATA_FOLDER+Data.currentFolder.getId()).execute();
			}
		}else{
			mPullToRefreshLayout.setRefreshComplete();
			Utils.DisplayToast(this, "Actualisation des donnees impossible car il n'y aucun reseau disponible.");
		}

	}

	private void RefreshViewShares(){

		if (Utils.IsNetworkAvailable(this)){
			// Le folder courant est le root
			if (Data.currentFolder == null){
				new GetDataSharesRoot(Home.this, Utils.DATA_ROOT).execute();
			}else{
				new GetData(Home.this, Utils.DATA_FOLDER+Data.currentFolder.getId()).execute();
			}
		}else{
			mPullToRefreshLayout.setRefreshComplete();
			Utils.DisplayToast(this, "Actualisation des donnees impossible car il n'y aucun reseau disponible.");
		}

	}

	@Override
	public void onRefreshStarted(View view) {

		System.out.println("on refresh started");

		if (Data.currentDrawerSelected == Utils.DRAWER_SHARES_SELECTED){
			RefreshViewShares();
		}else {
			RefreshViewHome();
		}
	}
}
