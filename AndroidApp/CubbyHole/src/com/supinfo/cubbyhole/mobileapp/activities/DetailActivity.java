package com.supinfo.cubbyhole.mobileapp.activities;

import org.json.JSONObject;

import com.supinfo.cubbyhole.mobileapp.R;
import com.supinfo.cubbyhole.mobileapp.models.File;
import com.supinfo.cubbyhole.mobileapp.models.Folder;
import com.supinfo.cubbyhole.mobileapp.utils.BitmapLruCache;
import com.supinfo.cubbyhole.mobileapp.utils.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

public class DetailActivity extends ActionBarActivity {

	private Folder currentFolder = null;
	private File currentFile = null;

	private TextView detail_itemCreationDate_tv;
	private TextView detail_itemLastUpdateDate_tv;
	private TextView detail_itemSize_tv;
	private Switch detail_isPublic_switch;
	private Button detail_manage_btn;
	private NetworkImageView networkImageView;
	private Button detail_share_btn;
	private ImageView line;
	
	private RequestQueue mRequestQueue;
	private ImageLoader imageLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_detail);

		// Recuperation de l'item selectionne
		if (Home.itemSelected instanceof File){
			currentFile = (File) Home.itemSelected;
		}else if (Home.itemSelected instanceof Folder){
			currentFolder = (Folder) Home.itemSelected;
		}else{
			Intent mIntent = new Intent(DetailActivity.this, Home.class);
			setResult(Utils.INTENT_DETAIL, mIntent);
			finish();
		}

		// ActionBar
		if (currentFolder != null){
			getSupportActionBar().setTitle(currentFolder.getName());
		}else{
			getSupportActionBar().setTitle(currentFile.getName());
		}
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		SetupComponents();
		
		if (currentFolder != null){
			SetupFolder();
		}else if (currentFile != null){
			SetupFile();
		}else{
			finish();
		}

		if (currentFile != null){
			SetupFile();
		}else if (currentFolder != null){
			SetupFolder();
		}else{
			finish();
		}

	}

	private void SetupComponents(){
		networkImageView = (NetworkImageView) findViewById(R.id.detail_networkImageView);
		detail_itemCreationDate_tv = (TextView) findViewById(R.id.detail_itemCreationDate_tv);
		detail_itemLastUpdateDate_tv = (TextView) findViewById(R.id.detail_itemLastUpdateDate_tv);
		detail_itemSize_tv = (TextView) findViewById(R.id.detail_itemSize_tv);
		detail_manage_btn = (Button) findViewById(R.id.detail_manage_btn);
		detail_isPublic_switch = (Switch) findViewById(R.id.detail_isPublic_switch);
		detail_share_btn = (Button) findViewById(R.id.detail_share_btn);
		line = (ImageView) findViewById(R.id.detail_line);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent mIntent = new Intent(DetailActivity.this, Home.class);
			setResult(Utils.INTENT_DETAIL, mIntent);
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}



	/*
	 *  Folder
	 */

	private void SetupFolder(){
		
		networkImageView.setVisibility(View.GONE);
		detail_isPublic_switch.setVisibility(View.GONE);
		detail_share_btn.setVisibility(View.GONE);
		detail_itemSize_tv.setVisibility(View.GONE);
		line.setVisibility(View.GONE);
		
		// Date de creation
		detail_itemCreationDate_tv.setText(Utils.DateToString(currentFolder.getCreationDate()));

		// Date de maj
		detail_itemLastUpdateDate_tv.setText(Utils.DateToString(currentFolder.getLastUpdateDate()));

		// Manage
		detail_manage_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent intent_to_managepermissions = new Intent(DetailActivity.this, ManagePersmissionsActivity.class);
				startActivityForResult(intent_to_managepermissions, Utils.INTENT_MANAGEPERMISSIONS);

			}
		});
				
	}


	/*
	 *  File
	 */

	
	private void SetupFile(){

		// Preview image
		if (currentFile.getName().contains(".jpg") || currentFile.getName().contains(".jpeg") || 
				currentFile.getName().contains(".png") || currentFile.getName().contains(".jfif")){
 
			mRequestQueue = Volley.newRequestQueue(this);
			imageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache(
					BitmapLruCache.getDefaultLruCacheSize()));
			//String url = Utils.FILE+"synchronize"+"/"+currentFile.getId()+"?hash="+Utils.HASH_DL;
			String url = "http://img.photobucket.com/albums/v420/BlackChaos65/Heilosoverlayupdated.png";
			networkImageView.setImageUrl(url, imageLoader);
			
		}else{
			networkImageView.setVisibility(View.GONE);
		}

		// Date de creation
		detail_itemCreationDate_tv.setText(Utils.DateToString(currentFile.getCreationDate()));
		
		// Date de maj
		detail_itemLastUpdateDate_tv.setText(Utils.DateToString(currentFile.getLastUpdateDate()));
		
		// Taille
		detail_itemSize_tv.setText(currentFile.getSize().toString());
		
		// Publique
		detail_isPublic_switch.setChecked(currentFile.getIsPublic());
		if (!currentFile.getIsPublic()){
			detail_share_btn.setVisibility(View.GONE);
		}
		
		// Manage
		detail_manage_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				Intent intent_to_managepermissions = new Intent(DetailActivity.this, ManagePersmissionsActivity.class);
				startActivityForResult(intent_to_managepermissions, Utils.INTENT_MANAGEPERMISSIONS);
				
			}
		});
		
	}

}