package com.kevin.rfidmanager.Fragments;

/**
 * Created by Kevin on 2017/1/26.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kevin.rfidmanager.Activity.MainActivity;
import com.kevin.rfidmanager.Adapter.GallaryAdaper;
import com.kevin.rfidmanager.Adapter.KeyDesListAdapter;
import com.kevin.rfidmanager.MyApplication;
import com.kevin.rfidmanager.R;
import com.kevin.rfidmanager.Utils.BitMapUtil;
import com.kevin.rfidmanager.Utils.ConstantManager;
import com.kevin.rfidmanager.Utils.DatabaseUtil;
import com.kevin.rfidmanager.database.DaoSession;
import com.kevin.rfidmanager.database.ImagesPath;
import com.kevin.rfidmanager.database.Items;
import com.kevin.rfidmanager.database.KeyDescription;
import com.kevin.rfidmanager.database.KeyDescriptionDao;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import static com.kevin.rfidmanager.Utils.ConstantManager.PERMISSION_REQUEST_CODE;

public class ItemDetailFrag extends android.support.v4.app.Fragment {
    private TextView itemName, addKeyDes;
    private ListView key_des_list;
    private ImageView mainImage;
    private AppCompatButton addGalleryButton;
    private EditText detailDescription;

    private RecyclerView recyclerView;
    private GallaryAdaper gallaryAdaper;
    private KeyDesListAdapter desListAdapter;

    private Button saveButton;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.item_add_layout, container, false);
        this.view = v;
        initUI(v);
        return v;
    }

    private void initUI(View v) {
        itemName = (TextView) v.findViewById(R.id.item_name);
        itemName.setText(DatabaseUtil.getCurrentItem(getActivity()).getItemName());

        key_des_list = (ListView) v.findViewById(R.id.listview_item_key_des);
        desListAdapter = new KeyDesListAdapter(getActivity(),
                DatabaseUtil.queryItemsKeyDes(getActivity(),
                        ((MyApplication) getActivity().getApplication()).getCurrentItemID()),true);
        key_des_list.setAdapter(desListAdapter);
        desListAdapter.setCurrentActivity(getActivity());

        mainImage = (ImageView) v.findViewById(R.id.iamgeview_main_image);
        String mainImagePath = DatabaseUtil.getCurrentItem(getActivity()).getMainImagePath();
        if (mainImagePath != null){
            Bitmap bitmap = null;
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                bitmap = BitmapFactory.decodeFile(mainImagePath);
            } else {
                bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.image_read_fail);
            }
            if (mainImage!=null&&mainImage.getWidth()!=0&&mainImage.getHeight()!=0 ){
                bitmap = BitMapUtil.createScaleBitmap(bitmap, mainImage.getWidth(), mainImage.getHeight(), 4);
            }else{
                bitmap = BitMapUtil.createScaleBitmap(bitmap, ConstantManager.DEFAULT_IMAGE_WIDTH,
                        ConstantManager.DEFAULT_IMAGE_HEIGHT, 4);
            }
            mainImage.setImageBitmap(bitmap);
        }else {  // Do not set main image, use default one
            Bitmap bitmap = null;
            bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.image_read_fail);
            if (mainImage!=null&&mainImage.getWidth()!=0&&mainImage.getHeight()!=0 ){
                bitmap = BitMapUtil.createScaleBitmap(bitmap, mainImage.getWidth(), mainImage.getHeight(), 4);
            }else{
                bitmap = BitMapUtil.createScaleBitmap(bitmap, ConstantManager.DEFAULT_IMAGE_WIDTH,
                        ConstantManager.DEFAULT_IMAGE_HEIGHT, 4);
            }
            mainImage.setImageBitmap(bitmap);
        }
        mainImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                } else {
                    new MaterialFilePicker()
                            .withSupportFragment(ItemDetailFrag.this)
                            .withRequestCode(ConstantManager.REQUEST_MAIN_IMAGE_FILE)
//                            .withFilter(Pattern.compile(".*\\.jpg$")) // Filtering files and directories by file name using regexp
                            .withFilterDirectories(false) // Set directories filterable (false by default)
                            .withHiddenFiles(true) // Show hidden files and folders
                            .start();
                }
            }
        });

        addKeyDes = (TextView) v.findViewById(R.id.button_add_item_key_des);
        addKeyDes.setVisibility(View.GONE);

        addGalleryButton = (AppCompatButton) v.findViewById(R.id.add_gallery_image);
        addGalleryButton.setVisibility(View.GONE);

        detailDescription = (EditText) v.findViewById(R.id.detail_description);
        detailDescription.setText(DatabaseUtil.getCurrentItem(getActivity()).getDetailDescription());
        detailDescription.setEnabled(false);

        saveButton = (Button) v.findViewById(R.id.save_des);
        saveButton.setVisibility(View.GONE);

        recyclerView = (RecyclerView) v.findViewById(R.id.recycle_gallery);
        gallaryAdaper = new GallaryAdaper(getActivity(), DatabaseUtil.queryImagesPaths(getActivity()));
        recyclerView.setAdapter(gallaryAdaper);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        layoutManager.scrollToPosition(0);// Optionally customize the position you want to default scroll to
        recyclerView.setLayoutManager(layoutManager);// Attach layout manager to the RecyclerView
        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);// First param is number of columns and second param is orientation i.e Vertical or Horizontal
        recyclerView.setLayoutManager(gridLayoutManager);// Attach the layout manager to the recycler view
        recyclerView.setHasFixedSize(true);

    }

    public void refreshUI(){
        if (view!=null)
            initUI(view);
    }
}
