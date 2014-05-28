package com.supinfo.cubbyhole.mobileapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.supinfo.cubbyhole.mobileapp.R;
import com.supinfo.cubbyhole.mobileapp.adapters.GenericListAdapter;
import com.supinfo.cubbyhole.mobileapp.adapters.MoveListAdapter;
import com.supinfo.cubbyhole.mobileapp.models.Back;
import com.supinfo.cubbyhole.mobileapp.models.Empty;
import com.supinfo.cubbyhole.mobileapp.models.File;
import com.supinfo.cubbyhole.mobileapp.models.Folder;
import com.supinfo.cubbyhole.mobileapp.quickactions.ActionItem;
import com.supinfo.cubbyhole.mobileapp.quickactions.AlertDialogIntentChooser;
import com.supinfo.cubbyhole.mobileapp.quickactions.QuickAction;
import com.supinfo.cubbyhole.mobileapp.utils.Data;
import com.supinfo.cubbyhole.mobileapp.utils.Utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by anthonyvialleton on 04/04/14.
 */

public class Home extends ActionBarActivity implements OnRefreshListener{

    private ProgressBar pb;
    private ListView list;
    private GenericListAdapter listAdapter;
    private QuickAction quickAction;
    
    private	PullToRefreshLayout mPullToRefreshLayout;
    public static Object itemSelected = null;
    
    private String selectedImagePath;
    private String filemanagerstring;
	  
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getSupportActionBar().setTitle(Utils.getUserFromSharedPreferences(this).getEmail());

        /*
         *  QuickActions
         */

        // item.setSticky permet de desactiver le dismiss de la dialog apres le clic sur l'item
        ActionItem moveItem 	= new ActionItem(Utils.QUICKACTION_ID_MOVE, "Déplacer", getResources().getDrawable(R.drawable.dark_rename));
        ActionItem manageItem 	= new ActionItem(Utils.QUICKACTION_ID_MANAGE, "Gérer", getResources().getDrawable(R.drawable.dark_rename));
        ActionItem renameItem 	= new ActionItem(Utils.QUICKACTION_ID_RENAME, "Renommer", getResources().getDrawable(R.drawable.dark_rename));
        ActionItem deleteItem 	= new ActionItem(Utils.QUICKACTION_ID_DELETE, "Supprimer", getResources().getDrawable(R.drawable.dark_delete));

        quickAction = new QuickAction(this, QuickAction.HORIZONTAL);

        quickAction.addActionItem(manageItem);
        quickAction.addActionItem(moveItem);
        quickAction.addActionItem(renameItem);
        quickAction.addActionItem(deleteItem);

        /*
         *  List
         */

        pb = (ProgressBar)findViewById(R.id.home_pb);
        list = (ListView)findViewById(R.id.home_list);
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);

        // Instanciation du pulltorefresh
        ActionBarPullToRefresh.from(this)
                .allChildrenArePullable()
                .listener(this)
                .setup(mPullToRefreshLayout);
        
        // Instanciation de la vue avec ROOT
        Utils.DATA_ROOT = Utils.DATA_ROOT_BASE+Utils.getUserFromSharedPreferences(this).getId()+"/"+"root";
        new GetData(Home.this, Utils.DATA_ROOT).execute();

        /*
         *  Handlers
         */
        
        SetHandlers();
    }

    private void SetHandlers(){
    	
    	quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {

                ActionItem actionItem = quickAction.getActionItem(pos);
                
                /*
                 *  Manage
                 */
                
                if (actionId == Utils.QUICKACTION_ID_MANAGE) {
                		Intent intent_to_detail = new Intent(Home.this, DetailActivity.class);
                        startActivityForResult(intent_to_detail, Utils.INTENT_DETAIL);
                }
                
                /*
                 * 	Rename
                 */
                
                if (actionId == Utils.QUICKACTION_ID_RENAME) {
                	
                	if (itemSelected != null && itemSelected instanceof Folder){
                		
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
                	        	pairs.add(new BasicNameValuePair(Utils.JSON_FOLDER_ISPUBLIC, folderSelected.getIsPublic().toString()));
                	        	pairs.add(new BasicNameValuePair(Utils.JSON_FOLDER_LASTUPDATE, folderSelected.getLastUpdateDate().toString()));
                				
                    			new UpdateData(Home.this, folderSelected, pairs).execute();
                			
                			}else{
                				Utils.DisplayToastHome(Home.this, "Le champs nom ne peut pas etre vide..");
                			}
                			
                		}
                		});
                		alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                		  public void onClick(DialogInterface dialog, int whichButton) {
                		  }
                		});
                		alert.show();
                		
                	}else if (itemSelected != null && itemSelected instanceof File){
                		
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
                	        	pairs.add(new BasicNameValuePair(Utils.JSON_FOLDER_ISPUBLIC, fileSelected.getIsPublic().toString()));
                	        	pairs.add(new BasicNameValuePair(Utils.JSON_FOLDER_LASTUPDATE, fileSelected.getLastUpdateDate().toString()));
                	        	pairs.add(new BasicNameValuePair(Utils.JSON_FILE_PUBLICLINKPATH,fileSelected.getPublicLinkPath()));
                	        	pairs.add(new BasicNameValuePair(Utils.JSON_FILE_SIZE, fileSelected.getSize().toString()));
                				
                    			new UpdateData(Home.this, fileSelected, pairs).execute();
                			
                			}else{
                				Utils.DisplayToastHome(Home.this, "Le champs nom ne peut pas etre vide..");
                			}
                			
                		}
                		});
                		alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                		  public void onClick(DialogInterface dialog, int whichButton) {
                		  }
                		});
                		alert.show();
                		
                	}
                	
                }
                /*
                 *  Delete
                 */
                
                else if (actionId == Utils.QUICKACTION_ID_DELETE) {

                	if (itemSelected != null && itemSelected instanceof Folder){

                		new DeleteData(Home.this, itemSelected).execute();
                		
                	}else if (itemSelected != null && itemSelected instanceof File){
                		
                		new DeleteData(Home.this, itemSelected).execute();
                		
                	}
                	
                }
                
                /*
                 *  Move
                 */

                else if (actionId == Utils.QUICKACTION_ID_MOVE){

                	if (itemSelected != null && itemSelected instanceof Folder){
                		
                		final AlertDialog alertDialog = new AlertDialog.Builder(Home.this).create();
                        LayoutInflater inflater = getLayoutInflater();
                        View convertView = (View) inflater.inflate(R.layout.listview_move, null);
                        alertDialog.setTitle("Déplacer le dossier vers");
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
                        
                        MoveListAdapter moveListAdapter = new MoveListAdapter(Home.this, R.layout.listview_move, folderItems);
                        lv.setAdapter(moveListAdapter);
                        
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
                        
                        alertDialog.setView(convertView);
                        alertDialog.show();
                		
                	}else if (itemSelected != null && itemSelected instanceof File){
                		
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
                        
                        MoveListAdapter moveListAdapter = new MoveListAdapter(Home.this, R.layout.listview_move, folderItems);
                        lv.setAdapter(moveListAdapter);
                        alertDialog.setView(convertView);
                        alertDialog.show();
	                	
                	}
                }
            }
        });
    
    	
    	 list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                 if (list.getAdapter().getItem(position) instanceof Folder){

                     Folder folderSelected = (Folder) list.getAdapter().getItem(position);
                     // On entre dans le level inferieur
                     new GetData(Home.this, Utils.DATA_FOLDER+folderSelected.getId()).execute();

                 }else if (list.getAdapter().getItem(position) instanceof File){

                     File fileSelected = (File) list.getAdapter().getItem(position);
                     itemSelected = fileSelected;
                     // Telechargement du fichier depuis le webservice et affichage
                     new DownloadFile(Home.this, fileSelected).execute();

                 }else if (list.getAdapter().getItem(position) instanceof Back){

                     if (Data.currentFolder.getParentID() == -1){
                         // Requete vers le folder root
                     	Data.currentFolder = null;
                         new GetData(Home.this, Utils.DATA_ROOT).execute();
                     }else{
                         // Requete vers le folder parent
                         new GetData(Home.this, Utils.DATA_FOLDER+Data.currentFolder.getParentID()).execute();
                     }

                 }

             }
         });

         list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
             @Override
             public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {

                 if (list.getAdapter().getItem(position) instanceof Folder){

                     Folder folderSelected = (Folder) list.getAdapter().getItem(position);
                     itemSelected = (Object) folderSelected;
                     
                     quickAction.show(view);
                     quickAction.setAnimStyle(QuickAction.ANIM_REFLECT);

                 }else if (list.getAdapter().getItem(position) instanceof File){

                     File fileSelected = (File) list.getAdapter().getItem(position);
                     itemSelected = (Object) fileSelected;
                     
                     quickAction.show(view);
                     quickAction.setAnimStyle(QuickAction.ANIM_REFLECT);

                 }
            	 return true;
             }
         });
    	
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @SuppressLint("InlinedApi")
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
            				Utils.DisplayToastHome(Home.this, "Les caracteres : //!.#&* sont interdits pour la creation d'un fichier..");
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
              				System.out.println("good");
            				pairs.add(new BasicNameValuePair("name", value));
            				pairs.add(new BasicNameValuePair("user_id", String.valueOf(Utils.getUserFromSharedPreferences(Home.this).getId())));
            				if (Data.currentFolder == null){
            					pairs.add(new BasicNameValuePair("parent", "null"));
            				}else{
            					pairs.add(new BasicNameValuePair("folder_id", String.valueOf(Data.currentFolder.getId())));
            				}
            				new AddData(Home.this, new Folder(), pairs).execute();
            			}else{
            				Utils.DisplayToastHome(Home.this, "Les caracteres : //!.#&* sont interdits pour la creation d'un fichier..");
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
            	
            	/*if (Build.VERSION.SDK_INT <19){
            	    Intent intent = new Intent(); 
            	    intent.setType("image/*");
            	    intent.setAction(Intent.ACTION_GET_CONTENT);
            	    startActivityForResult(Intent.createChooser(intent, "Selectionnez une application"),0);
            	} else {
            	    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            	    intent.addCategory(Intent.CATEGORY_OPENABLE);
            	    intent.setType("image/*");
            	    startActivityForResult(intent, 1);
            	}*/
            	Intent intent = new Intent(Intent.ACTION_PICK, 
						Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, Utils.INTENT_UPLOAD);
            	return true;
            	
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	System.out.println("request code : "+requestCode+" result code : "+resultCode);
    	
    	//if (resultCode != Activity.RESULT_OK) return;
    	//if (null == data) return;
        
    	if (requestCode == Utils.INTENT_UPLOAD) {
    		
	    	//Uri return from external activity
			Uri orgUri = data.getData();
			//path converted from Uri
			String convertedPath = Utils.GetRealPathFromURI(this, orgUri);
			//Uri convert back again from path
			Uri uriFromPath = Uri.fromFile(new java.io.File(convertedPath));
			
			java.io.File file = new java.io.File(convertedPath);
			 List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("name", file.getName()));
				pairs.add(new BasicNameValuePair("user_id", String.valueOf(Utils.getUserFromSharedPreferences(Home.this).getId())));
				if (Data.currentFolder == null){
					pairs.add(new BasicNameValuePair("folder_id", "null"));
				}else{
					pairs.add(new BasicNameValuePair("folder_id", String.valueOf(Data.currentFolder.getId())));
				}
			new AddImage(file, this, pairs).execute();
			
    	}else if (requestCode == Utils.INTENT_DETAIL){
    		
    		
    		
    	}else if (requestCode == Utils.INTENT_OPEN){
    		
    	}
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	java.io.File sdCard = Environment.getExternalStorageDirectory();
      	java.io.File directory = new java.io.File (sdCard.getAbsolutePath() + "/Cubbyhole");
      	
    	try {
			FileUtils.cleanDirectory(directory);
		} catch (IOException e) {
			e.printStackTrace();
		} 
    }
    
    private void shareContent(String update) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, update);
        startActivity(Intent.createChooser(intent, "Partager ce fichier"));
    }
    
    /**
     *  Asynctask
     *
     */
    
    public class DownloadFile extends AsyncTask<Void, Integer, java.io.File> {

        private Context ctx;
        private File file;
        private  ProgressDialog ringProgressDialog;
        
        public DownloadFile(Context ctx, File file) {
            this.ctx = ctx;
            this.file = file;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            
            ringProgressDialog = ProgressDialog.show(ctx, "Veuillez patienter...", "Téléchargement du fichier en cours..", true);
            ringProgressDialog.setCancelable(true);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                            } catch (Exception e) {
             
                            }
                        }
                    }).start();


        }
        
        @Override
        protected java.io.File doInBackground(Void... params) {
        	
        	return Utils.UrlToFile(this.ctx, this.file);
        	
        }

        @Override
        protected void onPostExecute(java.io.File file) {
            super.onPostExecute(file);
            
            ringProgressDialog.dismiss();
            
            if (file != null){
            	 Utils.OpenFile(Home.this, file);
            }else{
            	Utils.DisplayToastHome(this.ctx, "La récupération du fichier a échouée, merci de vérifier votre connexion internet.");
            }
            
        }

    }
    
    public class AddData extends AsyncTask<Void, Integer, Integer> {

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
        protected Integer doInBackground(Void... params) {

        	if (item instanceof Folder){
        		Utils.AddFolder(this.ctx, pairs);
        	}else if (item instanceof File){
        		
        		try {
					Utils.AddFile(this.ctx, pairs);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
        		
        	}
        	
        	return 1;
        	
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);

        	pb.setVisibility(View.GONE);
            RefreshView();
        }

    }
    
    public class AddImage extends AsyncTask<Void, Integer, Integer> {

        private Context ctx;
        private List<NameValuePair> pairs;
        private java.io.File file;
        
        public AddImage(java.io.File file, Context ctx,List<NameValuePair> pairs) {
            this.ctx = ctx;
            this.pairs = pairs;
            this.file = file;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
        }
        
        @Override
        protected Integer doInBackground(Void... params) {

        		try {
					Utils.AddImage(this.file ,this.ctx, pairs);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	
        	return 1;
        	
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);

        	pb.setVisibility(View.GONE);
            RefreshView();
        }

    }
    
    public class UpdateData extends AsyncTask<Void, Integer, Integer> {

        private Context ctx;
        private Object item;
        private List<NameValuePair> pairs;
        private int id;
        
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
        protected Integer doInBackground(Void... params) {

        	if (item instanceof Folder){
        		Utils.UpdateFolder(ctx, (Folder)item, pairs);
        	}else if (item instanceof File){
        		Utils.UpdateFile(ctx, (File)item, pairs);
        	}
        	
        	return 1;
        	
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);

            itemSelected = null;
        	pb.setVisibility(View.GONE);
            RefreshView();
        }

    }
    
    public class DeleteData extends AsyncTask<Void, Integer, Integer> {

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

        protected Integer doInBackground(Void... params) {

        	if (item instanceof Folder){
        		Utils.DeleteFolder(ctx, (Folder)item);
        	}else if (item instanceof File){
        		Utils.DeleteFile(ctx, (File)item);
        	}
        	
        	return null;
        	
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
            
        	itemSelected = null;
            pb.setVisibility(View.GONE);
            RefreshView();

        }

    }
    
    private class GetData extends AsyncTask<Void, Integer, List<Object>> {

        String url;
        Context ctx;

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
            mPullToRefreshLayout.setRefreshComplete();
        }

    }

    /**
     *  Refresh Layout
     */
    
    private void RefreshView(){
    	
    	// Le folder courant est le root
		if (Data.currentFolder == null){
			new GetData(Home.this, Utils.DATA_ROOT).execute();
		}else{
			new GetData(Home.this, Utils.DATA_FOLDER+Data.currentFolder.getId()).execute();
		}
		
    }
    
	@Override
	public void onRefreshStarted(View view) {
		
		RefreshView();
		
	}

}
