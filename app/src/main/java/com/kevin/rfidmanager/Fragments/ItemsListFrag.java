package com.kevin.rfidmanager.Fragments;

/**
 * Created by Kevin on 2017/1/26.
 */

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.kevin.rfidmanager.Activity.MainActivity;
import com.kevin.rfidmanager.Adapter.ItemListAdaper;
import com.kevin.rfidmanager.MyApplication;
import com.kevin.rfidmanager.R;
import com.kevin.rfidmanager.Utils.ConstantManager;
import com.kevin.rfidmanager.Utils.DatabaseUtil;
import com.kevin.rfidmanager.database.DaoSession;
import com.kevin.rfidmanager.database.Items;
import com.kevin.rfidmanager.database.ItemsDao;
import com.kevin.rfidmanager.database.UsersDao;

import java.util.List;

public class ItemsListFrag extends android.support.v4.app.Fragment {
    private RecyclerView recyclerView;
    private ItemListAdaper itemListAdapter;
    FloatingActionButton addButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.item_list_layout, container, false);
        initUI(v);
        return v;
    }

    private void initUI(View v) {
        recyclerView = (RecyclerView) v.findViewById(R.id.recycle_item_list);
        List<Items> items = DatabaseUtil.queryItems(getActivity());

        if(((MyApplication) getActivity().getApplication()).getCurrentItemID() == ConstantManager.DEFAULT_RFID && items.size() != 0)
            ((MyApplication) getActivity().getApplication()).setCurrentItemID(items.get(0).getRfid());

        itemListAdapter = new ItemListAdaper(getActivity(), items);
        recyclerView.setAdapter(itemListAdapter);
//        StaggeredGridLayoutManager gridLayoutManager =
//                new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);// First param is number of columns and second param is orientation i.e Vertical or Horizontal
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);// Attach the layout manager to the recycler view
        recyclerView.setHasFixedSize(true);

        addButton = (FloatingActionButton) v.findViewById(R.id.floatingAddButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewItem();
            }
        });
    }

    /*
       This is a dialog used for add new key description
        */
    public void addNewItem() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_layout_two_edit_text, null);
        dialogBuilder.setView(dialogView);

        final TextInputEditText itemID = (TextInputEditText) dialogView.findViewById(R.id.edit_key_des_text_editor);
        final TextInputEditText itemName = (TextInputEditText) dialogView.findViewById(R.id.item_name_edit);
        final Button saveButton = (Button) dialogView.findViewById(R.id.dialog_change);
        final Button cancleButton = (Button) dialogView.findViewById(R.id.dialog_cancle);

        dialogBuilder.setTitle("Just input a number as a ID of RFID card and a name of item");
        final AlertDialog b = dialogBuilder.create();
        b.show();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long new_id=null;
                try {
                    new_id = Long.parseLong(itemID.getText().toString());
                }catch (NumberFormatException e){
                    Toast.makeText(getActivity(), "please input number as ID", Toast.LENGTH_LONG).show();
                    return;
                }
                // Are there any user info?
                DaoSession daoSession = ((MyApplication) getActivity().getApplication()).getDaoSession();
                ItemsDao itemsDao = daoSession.getItemsDao();
                List<Items> items = itemsDao.queryBuilder().where(ItemsDao.Properties.Rfid.eq(new_id)).build().list();
                if (items.size()>0){
                    Toast.makeText(getActivity(), "The ID card is exist, please change a ID", Toast.LENGTH_LONG).show();
                    return;
                }
                ((MyApplication) getActivity().getApplication()).setCurrentItemID(Long.parseLong(itemID.getText().toString()));
                DatabaseUtil.insertNewItem(getActivity(),
                        Long.parseLong(itemID.getText().toString()),
                        itemName.getText().toString());
                ((MainActivity)getActivity()).viewPager.setCurrentItem(ConstantManager.EDIT, false);
                b.dismiss();

            }
        });

        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b.dismiss();
            }
        });
    }
}
