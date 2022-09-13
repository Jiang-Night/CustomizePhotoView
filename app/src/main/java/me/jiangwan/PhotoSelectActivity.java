package me.jiangwan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

public class PhotoSelectActivity extends Activity {
    private static ImageSelect imageSelect;

    public static void setImage(Context context, ImageSelect imageSelect){
        if (imageSelect == null) return;
        PhotoSelectActivity.imageSelect = imageSelect;
        context.startActivity(new Intent(context,PhotoSelectActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 0x11);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x11 && resultCode == RESULT_OK){
            if (data != null){
                String path = getPicPath(data.getData());
                imageSelect.setImage(path);
            }
        }

        imageSelect = null;
        finish();
    }

    // 获取图片路径
    String getPicPath(Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);

        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String path = cursor.getString(columnIndex);
        cursor.close();
        return path;
    }

    public interface ImageSelect{
        void setImage(String path);
    }
}
