
package com.supinfo.cubbyhole.mobileapp.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.supinfo.cubbyhole.mobileapp.R;
import com.supinfo.cubbyhole.mobileapp.activities.Home;
import com.supinfo.cubbyhole.mobileapp.models.Back;
import com.supinfo.cubbyhole.mobileapp.models.File;
import com.supinfo.cubbyhole.mobileapp.models.Folder;
import com.supinfo.cubbyhole.mobileapp.models.User;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by anthonyvialleton on 04/04/14.
 */
public class Utils {

    /** API FOLDER/FILES **/
    /** **************** **/
	
	public static final int INTENT_UPLOAD = 0;
	public static final int INTENT_DETAIL = 1;
	public static final int INTENT_OPEN = 2;
	
	public static final String JSON_FOLDER_NAME = "name";
	public static final String JSON_FOLDER_LASTUPDATE = "last_update_date";
	public static final String JSON_FOLDER_ISPUBLIC = "is_public";
	public static final String JSON_FILE_PUBLICLINKPATH = "public_link_path";
	public static final String JSON_FILE_SIZE = "size";
	
    public static final String DATA_ROOT_BASE = "http://cubbyhole.name/api/folder/user/";
    public static final String DATA_FOLDER = "http://cubbyhole.name/api/folder/details/";
    public static final String DELETE_FOLDER = "http://cubbyhole.name/api/folder/remove/";
    public static final String UPDATE_FOLDER = "http://cubbyhole.name/api/folder/update/";
    public static final String DELETE_FILE = "http://cubbyhole.name/api/file/remove/";
    public static final String UPDATE_FILE = "http://cubbyhole.name/api/file/update/";
    public static final String ADD_FILE = "http://cubbyhole.name/api/file/add";
    public static final String ADD_FOLDER = "http://cubbyhole.name/api/folder/add";
    public static String DATA_ROOT = "";
    
    public static final String HASH_DL = "ab14d0415c485464a187d5a9c97cc27c";
    public static final String FILE = "http://cubbyhole.name/api/file/"; 	// Preview : preview/ID?hash=
    																		// DL avec quota : download/ID?hash=
    																		// DL sans quota : synchronize/ID?hash=
    
    /*
     *  Download File
     */
    
	  public static java.io.File GetFileFromStorage(Context ctx, File mfile){
			java.io.File sdCard = Environment.getExternalStorageDirectory();
	      	java.io.File directory = new java.io.File (sdCard.getAbsolutePath() + "/Cubbyhole");
	      	java.io.File file = new java.io.File(directory, mfile.getName());
	      	return file;
	  }
	    
	  public static java.io.File UrlToFile(Context ctx, File mfile){
	
		  HttpClient httpclient = new DefaultHttpClient();
	      HttpGet httpget = new HttpGet(Utils.FILE+"preview"+"/"+mfile.getId()+"?hash="+Utils.HASH_DL);
	      
	      httpget.setHeader(X_API_KEY, getUserFromSharedPreferences(ctx).getToken());
	
	      try {
				
	      	HttpResponse response = httpclient.execute(httpget);
	      	InputStream is = response.getEntity().getContent();
	      	
	      	java.io.File folder = new java.io.File(Environment.getExternalStorageDirectory() + "/Cubbyhole");
	      	boolean success = true;
	      	if (!folder.exists()) {
	      	    success = folder.mkdir();
	      	}
	      	if (success) {
	      		FileOutputStream fos = new FileOutputStream(new java.io.File(folder, mfile.getName()));
	      		
	      		int read = 0;
	          	byte[] buffer = new byte[32768];
	          	while( (read = is.read(buffer)) > 0) {
	          	  fos.write(buffer, 0, read);
	          	  
	          	}
	
	          	fos.close();
	          	is.close();
	          	
	          	return GetFileFromStorage(ctx, mfile);
	          	
	      	} else {
	      		return null;
	      	}
	      	
	
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	      	return null;
	    	
	    }
    
    public static Drawable UrlToDrawable(Context ctx, File mfile) {
    	
    	URL url;
		try {
			url = new URL(Utils.FILE+"preview"+"/"+mfile.getId()+"?hash="+Utils.HASH_DL);
			 try {
		            InputStream is = (InputStream) url.getContent();
		            Drawable d = Drawable.createFromStream(is, mfile.getName());
		            return d;
		        } catch (Exception e) {
		        	e.printStackTrace();
		            return null;
		        }
			 
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return null;
		}
    }
    
    /*
     *  Delete
     */
    
    public static void DeleteFolder(Context ctx, Folder folder){
    	 
    	HttpClient httpclient = new DefaultHttpClient();
        HttpDelete httpdelete = new HttpDelete(DELETE_FOLDER+folder.getId());
        
        httpdelete.setHeader(X_API_KEY, getUserFromSharedPreferences(ctx).getToken());

        try {
        	
        	HttpResponse response = httpclient.execute(httpdelete);
		    
        } catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    }
   
    public static void DeleteFile(Context ctx, File file){
    	
    	HttpClient httpclient = new DefaultHttpClient();
        HttpDelete httpdelete = new HttpDelete(DELETE_FILE+file.getId());
        
        httpdelete.setHeader(X_API_KEY, getUserFromSharedPreferences(ctx).getToken());

        try {
        	
        	HttpResponse response = httpclient.execute(httpdelete);
		
        } catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
    
    /*
     *  Add
     */
    
    public static void AddFolder(Context ctx, List<NameValuePair> pairs){
    	
    	HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpost = new HttpPost(ADD_FOLDER);
        
        httpost.setHeader(X_API_KEY, getUserFromSharedPreferences(ctx).getToken());

        try {
			
        	httpost.setEntity(new UrlEncodedFormEntity(pairs));
        	HttpResponse response = httpclient.execute(httpost);
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
    
    
    @SuppressWarnings("deprecation")
	public static void AddFile(Context ctx, List<NameValuePair> pairs) throws ClientProtocolException, IOException{
    	
    	    java.io.File file = new java.io.File(Environment.getExternalStorageDirectory() + java.io.File.separator +pairs.get(0).getValue());
    	    file.createNewFile();
    	    FileBody fileBody = new FileBody(file);

    	    if(file.exists()){
    	    	  	HttpClient client = new DefaultHttpClient();
    	    	    HttpPost post = new HttpPost(ADD_FILE);
    	    	    post.setHeader("enctype", "multipart/form-data");
    	    	    post.setHeader(X_API_KEY, getUserFromSharedPreferences(ctx).getToken());
    	    	    
    	    	    MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
    	    	    multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
    	    	    multipartEntity.addPart("file",fileBody);
    	    	    for(NameValuePair pair : pairs){
    	    	    	if (pair.getValue() != "null"){
    	    	    		 multipartEntity.addPart(pair.getName(), new StringBody(pair.getValue()));
    	    	    	}
    	    	    }
    	    	    post.setEntity(multipartEntity.build());
    	    	    
    	    	    HttpResponse response = client.execute(post);
    	    	    file.delete();

    	    	    String responseBody = EntityUtils.toString(response.getEntity());
    	    	    Log.v("multiPartPost HTTP Response", responseBody);
    	    }
    	
    }
    
    
    @SuppressWarnings("deprecation")
   	public static void AddImage(java.io.File file, Context ctx, List<NameValuePair> pairs) throws ClientProtocolException, IOException{
       	
       	    FileBody fileBody = new FileBody(file);

       	    if(file.exists()){
       	    	  HttpClient client = new DefaultHttpClient();
       	    	    HttpPost post = new HttpPost(ADD_FILE);
       	    	    post.setHeader("enctype", "multipart/form-data");
       	    	    post.setHeader(X_API_KEY, getUserFromSharedPreferences(ctx).getToken());
       	    	    
       	    	    MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
       	    	    multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
       	    	    multipartEntity.addPart("file",fileBody);
       	    	    for(NameValuePair pair : pairs){
       	    	    	if (pair.getValue() != "null"){
       	    	    		 multipartEntity.addPart(pair.getName(), new StringBody(pair.getValue()));
       	    	    	}
       	    	    }
       	    	    post.setEntity(multipartEntity.build());
       	    	    
       	    	    HttpResponse response = client.execute(post);
       	    	    file.delete();

       	    	    String responseBody = EntityUtils.toString(response.getEntity());
       	    	    Log.v("multiPartPost HTTP Response", responseBody);
       	    }
       	
       }
    
    /*
     *  Update
     */
    
    public static void UpdateFolder(Context ctx, Folder folder, List<NameValuePair> pairs){
    	
    	HttpClient httpclient = new DefaultHttpClient();
        HttpPut httpput = new HttpPut(UPDATE_FOLDER+folder.getId());
        
        httpput.setHeader(X_API_KEY, getUserFromSharedPreferences(ctx).getToken());

        try {
			
        	httpput.setEntity(new UrlEncodedFormEntity(pairs));
        	
        	HttpResponse response = httpclient.execute(httpput);
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
    
    public static void UpdateFile(Context ctx, File file, List<NameValuePair> pairs){
    	
    	HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(UPDATE_FILE+file.getId());
        httppost.setHeader(X_API_KEY, getUserFromSharedPreferences(ctx).getToken());

        try {
			
        	httppost.setEntity(new UrlEncodedFormEntity(pairs));
        	
        	HttpResponse response = httpclient.execute(httppost);
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
    
    /*
     *  Get Data Files/Folder
     */
    
    public static List<Object> GetData(final Context ctx, String url) {

    	String USER_API_KEY = getUserFromSharedPreferences(ctx).getToken();
        List<Folder> arrayFolders = new ArrayList<Folder>();
        List<File> arrayFiles = new ArrayList<File>();
        Back back = null;

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);

        System.out.println("url getdata : "+url+" api key : "+USER_API_KEY);
        
        try {

            try {
                httpget.setHeader(X_API_KEY, USER_API_KEY);

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                InputStream is = httpEntity.getContent();

                try {

                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            is, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    String json = sb.toString();

                    // Parsing du JSON de retour
                    try {

                        JSONObject jObj = new JSONObject(json);
                        JSONObject data = jObj.getJSONObject("data");

                        // Root
                        if (url.contains("root")){

                            // Folders
                            JSONArray JSONArrayFolders = data.getJSONArray("folders");
                            for (int i=0; i < JSONArrayFolders.length(); i++) {

                                JSONObject JSONFolder = JSONArrayFolders.getJSONObject(i);
                                Folder folder = new Folder();

                                folder.setId(JSONFolder.getInt("id"));
                                folder.setName(JSONFolder.getString("name"));
                                folder.setCreationDate(StringToDate(JSONFolder.getJSONObject("creation_date").getString("date")));
                                folder.setLastUpdateDate(StringToDate(JSONFolder.getJSONObject("last_update_date").getString("date")));
                                folder.setIsPublic(JSONFolder.getBoolean("is_public"));
                                folder.setAccessKey(JSONFolder.getString("access_key"));
                                //folder.setShare(JSONFolder.get());
                                folder.setParentID(JSONFolder.optInt("parent", -1));

                                arrayFolders.add(folder);
                            }

                            // Files
                            JSONArray JSONArrayFiles = data.getJSONArray("files");
                            for (int i=0; i < JSONArrayFiles.length(); i++) {

                                JSONObject JSONFile = JSONArrayFiles.getJSONObject(i);
                                File file = new File();

                                file.setId(JSONFile.getInt("id"));
                                file.setName(JSONFile.getString("name"));
                                file.setCreationDate(StringToDate(JSONFile.getJSONObject("creation_date").getString("date")));
                                file.setLastUpdateDate(StringToDate(JSONFile.getJSONObject("last_update_date").getString("date")));
                                file.setAbsolutePath(JSONFile.getString("absolute_path"));
                                file.setPublicLinkPath(JSONFile.getString("public_link_path"));
                                file.setIsPublic(JSONFile.getBoolean("is_public"));
                                file.setAccessKey(JSONFile.getString("access_key"));
                                file.setSize(JSONFile.getDouble("size"));
                                //file.setShare();

                                arrayFiles.add(file);
                            }

                        }else{ // Folder detail

                            JSONObject jsonObject = data.getJSONObject("folder");
                            Folder vfolder = new Folder();
                            vfolder.setId(jsonObject.getInt("id"));
                            vfolder.setName(jsonObject.getString("name"));
                            vfolder.setCreationDate(StringToDate(jsonObject.getJSONObject("creation_date").getString("date")));
                            vfolder.setLastUpdateDate(StringToDate(jsonObject.getJSONObject("last_update_date").getString("date")));
                            vfolder.setIsPublic(jsonObject.getBoolean("is_public"));
                            vfolder.setAccessKey(jsonObject.getString("access_key"));
                            //vfolder.setShare(jsonObject.get());
                            vfolder.setParentID(jsonObject.optInt("parent", -1));
                            Data.currentFolder = vfolder;

                            // Folders
                            JSONArray JSONArrayFolders = jsonObject.getJSONArray("folders");
                            for (int i=0; i < JSONArrayFolders.length(); i++) {

                                JSONObject JSONFolder = JSONArrayFolders.getJSONObject(i);
                                Folder folder = new Folder();

                                folder.setId(JSONFolder.getInt("id"));
                                folder.setName(JSONFolder.getString("name"));
                                folder.setCreationDate(StringToDate(JSONFolder.getJSONObject("creation_date").getString("date")));
                                folder.setLastUpdateDate(StringToDate(JSONFolder.getJSONObject("last_update_date").getString("date")));
                                folder.setIsPublic(JSONFolder.getBoolean("is_public"));
                                folder.setAccessKey(JSONFolder.getString("access_key"));
                                //folder.setShare(JSONFolder.get());
                                folder.setParentID(JSONFolder.optInt("parent", -1));

                                arrayFolders.add(folder);
                            }

                            // Files
                            JSONArray JSONArrayFiles = jsonObject.getJSONArray("files");
                            for (int i=0; i < JSONArrayFiles.length(); i++) {

                                JSONObject JSONFile = JSONArrayFiles.getJSONObject(i);
                                File file = new File();

                                file.setId(JSONFile.getInt("id"));
                                file.setName(JSONFile.getString("name"));
                                file.setCreationDate(StringToDate(JSONFile.getJSONObject("creation_date").getString("date")));
                                file.setLastUpdateDate(StringToDate(JSONFile.getJSONObject("last_update_date").getString("date")));
                                file.setAbsolutePath(JSONFile.getString("absolute_path"));
                                file.setPublicLinkPath(JSONFile.getString("public_link_path"));
                                file.setIsPublic(JSONFile.getBoolean("is_public"));
                                file.setAccessKey(JSONFile.getString("access_key"));
                                file.setSize(JSONFile.getDouble("size"));
                                //file.setShare();

                                arrayFiles.add(file);
                            }

                            back = new Back(ctx.getResources().getString(R.string.previous_folder));

                        }

                        return ArrayToObject(arrayFolders, arrayFiles, back);

                    } catch (JSONException e) {
                        DisplayToastHome(ctx, "Error parsing data from JSON..");
                        Log.e("JSON Parser", "Error parsing data " + e.toString());
                        return null;
                    }

                } catch (Exception e) {
                    DisplayToastHome(ctx, "Error converting result from request..");
                    Log.e("Buffer Error", "Error converting result " + e.toString());
                    return null;
                }


            } catch (UnsupportedEncodingException e) {
                DisplayToastHome(ctx, "Unsupported encoding error..");
                e.printStackTrace();
                return null;
            }

        } catch (Exception e) {
            DisplayToastHome(ctx, ctx.getResources().getString(R.string.connection_error));
            e.printStackTrace();
            return null;
        }
    }


	/** API USER **/
	/** ******** **/
	
	/* ROUTES
	/api/user/login (post)
	/api/user/register (post)
	/api/user/update/{id} (put)
	/api/user/delete/{id} (delete)
	/api/user/details/{id}/ (get)
	*/

    public static final String USER_LOGIN_URL = "http://cubbyhole.name/api/user/login";
    public static final String USER_REGISTRATION_URL = "http://cubbyhole.name/api/user/register";
    public static final String USER_RETRIEVE_URL = "http://cubbyhole.name/api/user/forget";

    public static final String JSON_USER_EMAIL = "email";
    public static final String JSON_USER_PASSWORD = "password";
    public static final String JSON_USER_ID = "id";
    public static final String JSON_USER_ISADMIN = "is_admin";
    public static final String JSON_USER_TOKEN = "token";

    public static final String JSON_MESSAGE = "message";
    public static final String JSON_ERROR = "error";

    public static final String MASTER_API_KEY = "5422e102a743fd70a22ee4ff7c2ebbe8";
    public static final String X_API_KEY = "X-API-KEY";


    public static User LoginPostHTTP(String url, List<NameValuePair> nameValuePairs) {

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        
        try {

            try {
                httppost.setHeader(X_API_KEY, MASTER_API_KEY);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();
                InputStream is = httpEntity.getContent();

                try {

                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            is, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    String json = sb.toString();

                    // Parsing du JSON de retour
                    try {
                        JSONObject jObj = new JSONObject(json);
                        JSONObject dataObj = jObj.getJSONObject("data");

                        // User
                        JSONObject userObj = dataObj.getJSONObject("user");
                        String id = userObj.getString(JSON_USER_ID);
                        String email = userObj.getString(JSON_USER_EMAIL);
                        String password = nameValuePairs.get(1).getValue();
                        Boolean isAdmin = userObj.getBoolean(JSON_USER_ISADMIN);

                        String token = dataObj.getString(JSON_USER_TOKEN);

                       if (id != null && email != null){
                           User user = new User();
                           user.setId(Integer.parseInt(id));
                           user.setEmail(email);
                           user.setPassword(password);
                           user.setIsAdmin(isAdmin);
                           user.setToken(token);
                           return user;
                       }else{
                           return null;
                       }

                    } catch (JSONException e) {
                        Log.e("JSON Parser", "Error parsing data " + e.toString());
                        return null;
                    }

                } catch (Exception e) {
                    Log.e("Buffer Error", "Error converting result " + e.toString());
                    return null;
                }


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static User RegisterPostHTTP(String url, List<NameValuePair> nameValuePairs) {

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        try {

            try {
                httppost.setHeader(X_API_KEY, MASTER_API_KEY);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();
                InputStream is = httpEntity.getContent();

                try {

                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            is, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    String json = sb.toString();

                    // Parsing du JSON de retour
                    try {
                        JSONObject jObj = new JSONObject(json);
                        JSONObject dataObj = jObj.getJSONObject("data");

                        // User
                        JSONObject userObj = dataObj.getJSONObject("user");
                        String id = userObj.getString(JSON_USER_ID);
                        String email = userObj.getString(JSON_USER_EMAIL);
                        String password = nameValuePairs.get(1).getValue();
                        Boolean isAdmin = userObj.getBoolean(JSON_USER_ISADMIN);

                        String token = dataObj.getString(JSON_USER_TOKEN);

                        if (id != null && email != null){
                            User user = new User();
                            user.setId(Integer.parseInt(id));
                            user.setEmail(email);
                            user.setPassword(password);
                            user.setIsAdmin(isAdmin);
                            user.setToken(token);
                            return user;
                        }else{
                            String errorMessage = jObj.getString(JSON_MESSAGE);
                            return new User(errorMessage);
                        }

                    } catch (JSONException e) {

                        JSONObject jObj = new JSONObject(json);
                        String errorMessage = jObj.getString(JSON_MESSAGE);

                        Log.e("JSON Parser", "Register Error parsing data " + e.toString()+ " & error message : "+errorMessage);

                        return new User(errorMessage);

                    }

                } catch (Exception e) {
                    Log.e("Buffer Error", "Error converting result " + e.toString());
                    return null;
                }


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Boolean RetrievePostHTTP(String url, List<NameValuePair> nameValuePairs) {

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        String message = null;
        Boolean isError = true;

        try {

            try {
                httppost.setHeader(X_API_KEY, MASTER_API_KEY);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();
                InputStream is = httpEntity.getContent();

                try {

                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            is, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    String json = sb.toString();

                    // Parsing du JSON de retour
                    try {

                        JSONObject jObj = new JSONObject(json);
                        isError = jObj.getBoolean(JSON_ERROR);
                        message = jObj.getString(JSON_MESSAGE);

                        return !isError;

                    } catch (JSONException e) {
                        Log.e("JSON Parser", "Error parsing data " + e.toString());
                        return false;
                    }

                } catch (Exception e) {
                    Log.e("Buffer Error", "Error converting result " + e.toString());
                    return false;
                }


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Storage SharedPreferences **/
    /** ************************* **/

    public static User getUserFromSharedPreferences(Context ctx){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);

        Gson gson = new Gson();
        String json = preferences.getString("user", "");
        User user = gson.fromJson(json, User.class);

        return user;

    }

    public static void setUserFromSharedPreferences(Context ctx, User user){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);

        SharedPreferences.Editor prefsEditor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        prefsEditor.putString("user", json);
        prefsEditor.commit();

    }

    public static void removeUserFromSharedPreferences(Context ctx){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);

        SharedPreferences.Editor prefsEditor = preferences.edit();
        prefsEditor.remove("user");
        prefsEditor.commit();

    }


    /** Quick Actions **/
    /** ************* **/

    public static final int QUICKACTION_ID_DELETE 	= 1;
    public static final int QUICKACTION_ID_RENAME   = 2;
    public static final int QUICKACTION_ID_MANAGE   = 3;
    public static final int QUICKACTION_ID_MOVE   	= 4;
    
    /** CONNECTIVITY **/
    /** ************ **/

    public static boolean IsNetworkAvailable(Activity mActivity) {
        Context context = mActivity.getApplicationContext();
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** PICTURES **/
    /** ******* **/
    
    public static int GetScreenWidth(Context context) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.widthPixels;
    }

    public static int GetScreenHeight(Context context) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }

    public static float ConvertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public static float ConvertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    public static Bitmap GetResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();

        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // RECREATE THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);
        return resizedBitmap;
    }


    /** JSON  **/
    /** ***** **/

    public JSONObject HttpGetJSONFromUrl(String url) {

        InputStream is = null;
        String json = "";
        JSONObject jObj = null;

        // Making HTTP request
        try {

            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        // return JSONObject
        return jObj;
    }

    /** Array **/
    /** ***** **/

    public static List<Object> ArrayToObject(List<Folder> folders, List<File> files, Back back){

        List<Object> returnList = new ArrayList<Object>();

        if (back != null){
            returnList.add(back);
        }

        for(Folder folder : folders){
            returnList.add(folder);
        }

        for(File file : files){
            returnList.add(file);
        }

        return returnList;

    }
    
    public static String GetNewFileNameWithExtension(String baseName, String destName){
    	
    	if (baseName.contains(".tar.gz")){
    		
        	return destName+".tar.gz";
    		
    	}else{
    		
    		String extension = FilenameUtils.getExtension(baseName);
        	
        	if (extension != null && !extension.trim().isEmpty()){
        		String retour = destName+"."+extension;
        		return retour;
        	}
        	return baseName;
    		
    	}
    }


    /**
     * DATE
     */

    public static String DateToString(Date date)
    {
    	DateFormat mediumDateFormatFR = DateFormat.getDateTimeInstance(
        DateFormat.MEDIUM,DateFormat.MEDIUM, new Locale("FR","fr"));
    	
    	return mediumDateFormatFR.format(date);
    }
    
    public static Date StringToDate(String dateString)
    {
        SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(dateString);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
    *   Activity
    */

    public static void DisplayToastHome(final Context ctx, final String message){

        Runnable setErrorMessage = new Runnable() {
            @Override
            public void run() {

                Toast toast = Toast.makeText(ctx, message,
                        Toast.LENGTH_LONG);
                toast.show();
            }
        };
        Activity home = (Activity) ctx;
        home.runOnUiThread(setErrorMessage);

    }
    
    /**
     *  Data
     */
    
    public static void OpenFile(Activity ctx, java.io.File file){
	    MimeTypeMap myMime = MimeTypeMap.getSingleton();
	
	    Intent newIntent = new Intent(android.content.Intent.ACTION_VIEW);
	
	    String mimeType = myMime.getMimeTypeFromExtension(fileExt(file.toString()).substring(1));
	    newIntent.setDataAndType(Uri.fromFile(file),mimeType);
	    //newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    try {
	    	ctx.startActivityForResult(newIntent, Utils.INTENT_OPEN);
	    } catch (android.content.ActivityNotFoundException e) {
	       Utils.DisplayToastHome(ctx, "Aucune application ne peut ouvrir ce type de fichier..");
	    }
    }
    
    public static String fileExt(String url) {
        if (url.indexOf("?")>-1) {
            url = url.substring(0,url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") );
            if (ext.indexOf("%")>-1) {
                ext = ext.substring(0,ext.indexOf("%"));
            }
            if (ext.indexOf("/")>-1) {
                ext = ext.substring(0,ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }
    
    public static String GetRealPathFromURI(Context ctx, Uri contentUri) {
  		String[] proj = { MediaStore.Images.Media.DATA };
  		
  		CursorLoader cursorLoader = new CursorLoader(
  		          ctx, 
  		          contentUri, proj, null, null, null);        
  		Cursor cursor = cursorLoader.loadInBackground();
  		
  		int column_index = 
  				cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
  		cursor.moveToFirst();
  		return cursor.getString(column_index);	
  	}
    
}
