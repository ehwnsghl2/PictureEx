package com.timedo.pictureex;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.github.siyamed.shapeimageview.CircularImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity {

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_iMAGE = 2;

    private Uri mlmageCaptureUri;
    private CircularImageView iv_UserPhoto;
    private int id_view;
            private String absoultePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        iv_UserPhoto = (CircularImageView)findViewById(R.id.circle);

        iv_UserPhoto.setOnClickListener(click);

    }

    public void doTakePhotoAction(){

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        mlmageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));

        intent.putExtra(MediaStore.EXTRA_OUTPUT, mlmageCaptureUri);
        startActivityForResult(intent, PICK_FROM_CAMERA);

    }


    public void doTakeAlbumAction(){

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK)
            return;


        switch (requestCode){

            case PICK_FROM_ALBUM:
            {
                mlmageCaptureUri = data.getData();
                Log.d("SmartWheel", mlmageCaptureUri.getPath().toString());
            }

            case PICK_FROM_CAMERA:
            {

                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mlmageCaptureUri, "image/*");

                intent.putExtra("outputX", 200);
                intent.putExtra("outputY", 200);
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, CROP_FROM_iMAGE);
                break;
            }

            case CROP_FROM_iMAGE:
            {

                if(resultCode != RESULT_OK){
                    return;
                }

                final Bundle extras = data.getExtras();

                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+
                        "/SmartWheel/"+System.currentTimeMillis()+".jpg";

                if(extras != null){

                    Bitmap photo = extras.getParcelable("data");
                    iv_UserPhoto.setImageBitmap(photo);

                    storeCropImage(photo, filePath);
                    absoultePath = filePath;
                    break;

                }


                File f = new File(mlmageCaptureUri.getPath());
                if(f.exists()){
                    f.delete();
                }

            }


        }

    }

    View.OnClickListener click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()){

                case R.id.circle:

                    final CharSequence[] items = {
                            "사진 촬영", "앨범에서 사진 선택", "기본이미지로 변경"
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("프로필");


                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String str = (String)items[which];


                            switch (str){
                                case "사진 촬영":

                                    doTakePhotoAction();


                                    break;

                                case "앨범에서 사진 선택":
                                    doTakeAlbumAction();



                                    break;
                                case "기본이미지로 변경":
                                    iv_UserPhoto.setImageBitmap(null);
                                    iv_UserPhoto.setBackgroundResource(R.mipmap.ic_launcher);
                                    dialog.cancel();


                                    break;

                            }


                        }
                    });

                    AlertDialog dialog = builder.create();    // 알림창 객체 생성
                    dialog.show();

                    break;


            }

        }
    };


    private void storeCropImage(Bitmap bitmap, String filePath){

        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/SmartWheel";
        File directory_SmartWheel = new File(dirPath);


        if(!directory_SmartWheel.exists())
            directory_SmartWheel.mkdir();


        File copyFile = new File(filePath);
        BufferedOutputStream out = null;


        try{
            copyFile.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(copyFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            sendBroadcast(new Intent(getIntent().ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(copyFile)));

            out.flush();
            out.close();

        }catch (Exception e){
            e.printStackTrace();
        }

    }



}
