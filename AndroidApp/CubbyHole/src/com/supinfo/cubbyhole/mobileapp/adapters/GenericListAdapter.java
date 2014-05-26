package com.supinfo.cubbyhole.mobileapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.supinfo.cubbyhole.mobileapp.R;
import com.supinfo.cubbyhole.mobileapp.activities.Home;
import com.supinfo.cubbyhole.mobileapp.activities.Home.DeleteData;
import com.supinfo.cubbyhole.mobileapp.activities.Home.UpdateData;
import com.supinfo.cubbyhole.mobileapp.models.Back;
import com.supinfo.cubbyhole.mobileapp.models.Empty;
import com.supinfo.cubbyhole.mobileapp.models.File;
import com.supinfo.cubbyhole.mobileapp.models.Folder;
import com.supinfo.cubbyhole.mobileapp.quickactions.ActionItem;
import com.supinfo.cubbyhole.mobileapp.quickactions.QuickAction;
import com.supinfo.cubbyhole.mobileapp.utils.Utils;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by anthonyvialleton on 04/04/14.
 */

public class GenericListAdapter extends ArrayAdapter<Object> {

    private Context context;
    private List<Object> items;
    private QuickAction quickAction;
    
    public GenericListAdapter(Context context, int resource, List<Object> items) {
        super(context, resource, items);

        this.items = items;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        /*
         *  View set
         */
        
        LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        View v = vi.inflate(R.layout.listview_item, null);
        final Object o = items.get(position);

        ImageView iconImg = (ImageView) v.findViewById(R.id.item_icon_img);
        ImageView arrow = (ImageView) v.findViewById(R.id.item_arrow);
        TextView nameTv = (TextView) v.findViewById(R.id.item_name_tv);
        TextView lastModificationDateTv = (TextView) v.findViewById(R.id.item_lastmodification_tv);
        LinearLayout ll = (LinearLayout) v.findViewById(R.id.item_ll);

        if (o != null){
        	
            if (o instanceof File){

                File file = (File)o;
                	
                arrow.setVisibility(View.GONE);
                iconImg.setImageDrawable(this.context.getResources().getDrawable(R.drawable.file));

                if (file.getName() != null){
                    nameTv.setText(file.getName());
                }

                if (file.getLastUpdateDate() != null){
                	lastModificationDateTv.setText(Utils.DateToString(file.getLastUpdateDate()));
                }
                
                arrow.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						quickAction.show(view);
	                    quickAction.setAnimStyle(QuickAction.ANIM_REFLECT);
						
					}
				});

            }else if (o instanceof Folder){

                final Folder folder = (Folder)o;

                iconImg.setImageDrawable(this.context.getResources().getDrawable(R.drawable.folder));

                if (folder.getName() != null){
                    nameTv.setText(folder.getName());
                }

                if (folder.getLastUpdateDate() != null){
                	lastModificationDateTv.setText(Utils.DateToString(folder.getLastUpdateDate()));
                }
                
                arrow.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						
						 quickAction.show(view);
	                     quickAction.setAnimStyle(QuickAction.ANIM_REFLECT);
	                     
					}
				});

            }else if (o instanceof Back){

                Back back = (Back)o;

                arrow.setVisibility(View.GONE);
                lastModificationDateTv.setVisibility(View.GONE);

                ll.setOrientation(LinearLayout.VERTICAL);

                iconImg.setImageDrawable(this.context.getResources().getDrawable(R.drawable.arrow_up));

                LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity= Gravity.CENTER;

                iconImg.setLayoutParams(layoutParams);

                if (back.getValue()!= null){
                    nameTv.setText(back.getValue());
                    nameTv.setLayoutParams(layoutParams);
                }
                
            }else if (o instanceof Empty){
            	
            	Empty empty = (Empty)o;
            	
            	arrow.setVisibility(View.GONE);
                lastModificationDateTv.setVisibility(View.GONE);

                ll.setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity= Gravity.CENTER;

                if (empty.getValue()!= null){
                    nameTv.setText(empty.getValue());
                    nameTv.setLayoutParams(layoutParams);
                }
            	
            }
            
            
            
            
            /*// item.setSticky permet de desactiver le dismiss de la dialog apres le clic sur l'item
            ActionItem moveItem 	= new ActionItem(Utils.QUICKACTION_ID_MOVE, "Déplacer", this.context.getResources().getDrawable(R.drawable.dark_rename));
            ActionItem copyItem 	= new ActionItem(Utils.QUICKACTION_ID_COPY, "Copier", this.context.getResources().getDrawable(R.drawable.dark_rename));
            ActionItem renameItem 	= new ActionItem(Utils.QUICKACTION_ID_RENAME, "Renommer", this.context.getResources().getDrawable(R.drawable.dark_rename));
            ActionItem deleteItem 	= new ActionItem(Utils.QUICKACTION_ID_DELETE, "Supprimer", this.context.getResources().getDrawable(R.drawable.dark_delete));

            quickAction = new QuickAction(this.context, QuickAction.HORIZONTAL);

            quickAction.addActionItem(moveItem);
            quickAction.addActionItem(copyItem);
            quickAction.addActionItem(renameItem);
            quickAction.addActionItem(deleteItem);
        	
            quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
                @Override
                public void onItemClick(QuickAction source, int pos, int actionId) {

                    ActionItem actionItem = quickAction.getActionItem(pos);
                    
                     * 	Rename
                     
                    
                    if (actionId == Utils.QUICKACTION_ID_RENAME) {
                    	
                    	if (o != null && o instanceof Folder){
                    		
                    		AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    		alert.setTitle("Edition du dossier");
                    		alert.setMessage("Merci de spécifier un nouveau nom pour ce dossier :");
                    		final EditText input = new EditText(context);
                    		alert.setView(input);
                    		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    		public void onClick(DialogInterface dialog, int whichButton) {

                    			String value = input.getText().toString();
                    			
                    			if (!value.trim().isEmpty()){

                    				final Folder folderSelected = (Folder)o;
                    				folderSelected.setName(input.getText().toString());
                    				
                    				Home home = new Home();
                    				Home.UpdateData async = (UpdateData) home.new UpdateData(context, folderSelected).execute();
                    			
                    			}else{
                    				Utils.DisplayToastHome(context, "Le champs nom ne peut pas etre vide..");
                    			}
                    			
                    		}
                    		});
                    		alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    		  public void onClick(DialogInterface dialog, int whichButton) {
                    		  }
                    		});
                    		alert.show();
                    		
                    	}else if (o != null && o instanceof File){
                    		
                    		AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    		alert.setTitle("Edition du fichier");
                    		alert.setMessage("Merci de spécifier un nouveau nom pour ce fichier :");
                    		final EditText input = new EditText(context);
                    		alert.setView(input);
                    		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    		public void onClick(DialogInterface dialog, int whichButton) {

                    			String value = input.getText().toString();
                    			
                    			if (!value.trim().isEmpty()){

                    				final File fileSelected = (File)o;
                    				fileSelected.setName(input.getText().toString());
                    				
                    				Home home = new Home();
                    				Home.UpdateData async = (UpdateData) home.new UpdateData(context, fileSelected).execute();
                    			
                    			}else{
                    				Utils.DisplayToastHome(context, "Le champs nom ne peut pas etre vide..");
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
                    
                     *  Delete
                     
                    
                    else if (actionId == Utils.QUICKACTION_ID_DELETE) {

                    	if (o != null && o instanceof Folder){

                    		
                    	}else if (o != null && o instanceof File){
                    		
                    		
                    		
                    	}
                    	
                    }
                    
                     *  Copy
                     
                    
                    else if (actionId == Utils.QUICKACTION_ID_COPY){
                    	
                    	
                    	if (o != null && o instanceof Folder){
                    		
                    	}else if (o != null && o instanceof File){
                    		
                    	}

                    }else if (actionId == Utils.QUICKACTION_ID_MOVE){

                    	if (o != null && o instanceof Folder){
                    		
                    	}else if (o != null && o instanceof File){
                    		
                    	}
                    }
                }
            });*/
            
            
            
        }

        return v;
    }
}
