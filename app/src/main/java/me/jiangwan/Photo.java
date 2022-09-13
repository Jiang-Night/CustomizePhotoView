package me.jiangwan;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import me.jiangwan.util.AnimHelper;
import me.jiangwan.util.AnyLayer;
import me.jiangwan.util.LayerManager;


public class Photo extends ImageView implements PhotoSelectActivity.ImageSelect {
    AnyLayer dialog;
    XEdit xEdit;
    public Photo(Context context) {
        super(context);
        init();
    }

    public Photo(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        //读取配置
        final SharedPreferences sharedPreferences = getConfig();
        String ImagePath = sharedPreferences.getString("图片路径", "");
        boolean RandomKey = sharedPreferences.getBoolean("随机", false);
        if (RandomKey) {
            RandomImage();
        } else {
            //判断路径是否设置图片，没有就默认，有就加载
            if (TextUtils.isEmpty(ImagePath)) {
                DefaultImage(); //调用默认方法
            } else {
                //判断是否http开头
                if (ImagePath.startsWith("http")) {
                    UrlImage(ImagePath);
                } else {
                    LocalImage(ImagePath);
                }

            }

        }
        setOnLongClickListener(new OnLongClickListener() {

            //绑定布局
            @Override
            public boolean onLongClick(View v) {

                dialog=AnyLayer.with(getContext())
                        .backgroundColorInt(Color.parseColor("#00000000"))
                        .contentView(addViewDialog())
                        .contentAnim(new LayerManager.IAnim() {
                            @Override
                            public Animator inAnim(View target) {
                                return AnimHelper.createAlphaInAnim(target);
                            }

                            @Override
                            public Animator outAnim(View target) {
                                return AnimHelper.createAlphaOutAnim(target);
                            }
                        })
                        .gravity(Gravity.CENTER);
                        dialog.show();

                return true;
            }
        });

    }

    private void selectPhoto() {
        PhotoSelectActivity.setImage(getContext(), this);
    }


    public void setImage(String path) {
        SharedPreferences.Editor editor = getConfig().edit();
        editor.putString("图片路径", path);
        editor.putBoolean("随机", false);
        editor.apply();
        //显示图片
        File file = new File(path);
        Picasso.with(getContext()).load(file).fit().centerCrop().into(this);
    }


    private SharedPreferences getConfig() {
        String tag = "default";
        if (getTag() != null) {
            tag = getTag().toString();
        }
        return getContext().getSharedPreferences(tag, 0);
    }

    private void DefaultImage() {
        Picasso.with(getContext()).load("file:///android_asset/Jiang_Night/Resources/ic_loop.png").fit().centerCrop().into(this);
    }

    private void RandomImage() {
        Picasso.with(getContext()).load("http://www.dmoe.cc/random.php").fit().centerCrop().into(this);
    }

    private void UrlImage(String ImageUrl) {
        Picasso.with(getContext()).load(ImageUrl).fit().centerCrop().into(this);
    }

    private void LocalImage(String ImagePath) {
        Picasso.with(getContext()).load(new File(ImagePath)).fit().centerCrop().into(this);
    }


    private View addViewDialog(){
        FrameLayout frameLayout = new FrameLayout(getContext());

        LinearLayout liGroup =  new LinearLayout(getContext());//linearLayout垂直父布局
        liGroup.setOrientation(LinearLayout.VERTICAL);//设置垂直
        LinearLayout li1=new LinearLayout(getContext());//第一层垂直向线性布局
        li1.setOrientation(LinearLayout.VERTICAL);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels - dip2px(getContext(),80f),-2);
        layoutParams.gravity = Gravity.CENTER;
        frameLayout.addView(liGroup,layoutParams);
        //主背景
        GradientDrawable backgroundDrawable=new GradientDrawable();
        backgroundDrawable.setCornerRadius(dip2px(getContext(),16));
        backgroundDrawable.setColor(0xFFFFFFFF); //添加颜色组
        li1.setBackground(backgroundDrawable);
        liGroup.addView(li1);//把垂直子布局放在垂直父布局中

        Button button= new Button(getContext());
        button.setTextColor(0xffffffff);

        //按钮背景
        GradientDrawable ButtonBackground=new GradientDrawable();
        ButtonBackground.setCornerRadius(dip2px(getContext(),25));
        int[] colors={0xff7d7df9,0xff9195fb,0xffa0a7fc,0xffb2bcfd,0xffc8d6ff};
        ButtonBackground.setColors(colors); //添加颜色组
        ButtonBackground.setGradientType(GradientDrawable.LINEAR_GRADIENT);//设置线性渐变
        ButtonBackground.setOrientation(GradientDrawable.Orientation.RIGHT_LEFT);//设置渐变方向
        button.setBackground(ButtonBackground);
        button.setTextSize(dip2px(getContext(),5));
        button.setText("CONFIRM");
        LinearLayout.LayoutParams ButtonLayout =new LinearLayout.LayoutParams(dip2px(getContext(),110),dip2px(getContext(),42));
        ButtonLayout.gravity =Gravity.CENTER_HORIZONTAL;
        ButtonLayout.setMargins(0,dip2px(getContext(),20),0,0);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(xEdit.getText())){
                    String path = xEdit.getText().toString();
                    setImage(path);
                    dialog.dismiss();
                    //判断是否http开头
                    if (path.startsWith("http")) {
                        UrlImage(path);
                        SharedPreferences.Editor editor = getConfig().edit();
                        editor.putBoolean("随机", false);
                        editor.putString("图片路径", path);
                        editor.apply();
                        return;
                    }
                    //如果bitmap不等于空 图片有效设为背景 如果是空 说明文件无效 设置默认
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    if (bitmap == null) {
                        Toast.makeText(getContext(), "你输入的有误，检查一下吧！", Toast.LENGTH_SHORT).show();
                        DefaultImage();
                    } else {
                        bitmap.recycle(); //bitmap释放内存
                        LocalImage(path);
                        SharedPreferences.Editor editor = getConfig().edit();
                        editor.putString("图片路径", path);
                        editor.putBoolean("随机", false);
                        editor.apply();
                    }
                } else {
                    Toast.makeText(getContext(),"你输入了空气呢",Toast.LENGTH_SHORT).show();
                }

            }
        });
        liGroup.addView(button,ButtonLayout);//按钮放在父布局中

        LinearLayout li2=new LinearLayout(getContext());//第一层横向线性布局
        li2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams Layout = new LinearLayout.LayoutParams(-1,-2);
        Layout.setMargins(dip2px(getContext(),10),dip2px(getContext(),12),dip2px(getContext(),10),dip2px(getContext(),8));
        ImageView li2Image=new ImageView(getContext());
        try {
            InputStream li2Stream = getResources().getAssets().open("Jiang_Night/Arrow_left.png");
            li2Image.setImageBitmap(BitmapFactory.decodeStream(li2Stream));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //图片的固定位置大小
        LinearLayout.LayoutParams ImageLayout=new LinearLayout.LayoutParams(dip2px(getContext(),40),dip2px(getContext(),40));
        ImageLayout.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        ImageLayout.setMargins(dip2px(getContext(),15),dip2px(getContext(),10),0,dip2px(getContext(),10));

        LinearLayout.LayoutParams TextLayout=new LinearLayout.LayoutParams(-2,-2);
        TextLayout.gravity = Gravity.CENTER_VERTICAL;
        TextLayout.weight = 1;
        TextLayout.setMargins(dip2px(getContext(),20),dip2px(getContext(),12),dip2px(getContext(),20),dip2px(getContext(),12));

        LinearLayout.LayoutParams ImageLayout2=new LinearLayout.LayoutParams(dip2px(getContext(),20),dip2px(getContext(),20));
        ImageLayout2.gravity=Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        ImageLayout2.setMargins(dip2px(getContext(),5),dip2px(getContext(),8),dip2px(getContext(),8),dip2px(getContext(),8));

        TextView li2Text=new TextView(getContext());
        li2Text.setTextColor(0xFF717171);
        li2Text.setText("图片自定义");

        li2Text.setTextSize(dip2px(getContext(),8));
        li2.addView(li2Image,ImageLayout);//图片放在第一个横向布局中

        LinearLayout.LayoutParams titleLayoutParams = new LinearLayout.LayoutParams(-2,-2);
        titleLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        titleLayoutParams.setMargins(dip2px(getContext(),10),dip2px(getContext(),6),dip2px(getContext(),20),dip2px(getContext(),5));
        li2.addView(li2Text,titleLayoutParams);//文字放在第一个横向布局中
        li1.addView(li2,Layout);//第一个横向布局放在垂直子布局


        LinearLayout li3=new LinearLayout(getContext());//第三层横向线性布局
        li3.setOrientation(LinearLayout.HORIZONTAL);
        GradientDrawable li3Drawable=new GradientDrawable();
        li3Drawable.setCornerRadius(dip2px(getContext(),26));
        li3Drawable.setStroke(dip2px(getContext(),3),0xFFEDF0F5);
        li3.setBackground(li3Drawable);
        ImageView li3Image =new ImageView(getContext());
        try {
            InputStream inputStream = getResources().getAssets().open("Jiang_Night/shijian.png");
            li3Image.setImageBitmap(BitmapFactory.decodeStream(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextView li3Text =new TextView(getContext());
        li3Text.setTextColor(0xFF717171);
        li3Text.setText("图片转链接");
        li3Text.setTextSize(dip2px(getContext(),6));
        ImageView li3Image2=new ImageView(getContext());
        try {
            InputStream li4Strem2 = getResources().getAssets().open("Jiang_Night/Arrow.png");
            li3Image2.setImageBitmap(BitmapFactory.decodeStream(li4Strem2));
        } catch (IOException e) {
            e.printStackTrace();
        }
        li3.addView(li3Image,ImageLayout);
        li3.addView(li3Text,TextLayout);
        li3.addView(li3Image2,ImageLayout2);
        li3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent();
                intent.setAction( "android.intent.action.VIEW" );
                Uri content_url = Uri.parse( "https://sm.ms/" );
                intent.setData(content_url);
                getContext().startActivity(intent);

                dialog.dismiss();
            }
        });
        li1.addView(li3,Layout);//第二层横向布局放在垂直子布局中

        LinearLayout li4=new LinearLayout(getContext());//第二层横向线性布局
        li4.setOrientation(LinearLayout.HORIZONTAL);
        GradientDrawable li4Drawable=new GradientDrawable();
        li4Drawable.setCornerRadius(dip2px(getContext(),26));
        li4Drawable.setStroke(dip2px(getContext(),3),0xFFEDF0F5);
        li4.setBackground(li4Drawable);
        ImageView li4Image =new ImageView(getContext());
        try {
            InputStream inputStream = getResources().getAssets().open("Jiang_Night/tupian.png");
            li4Image.setImageBitmap(BitmapFactory.decodeStream(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextView li4Text =new TextView(getContext());
        li4Text.setTextColor(0xFF717171);
        li4Text.setText("网络高清壁纸");
        li4Text.setTextSize(dip2px(getContext(),6));
        ImageView li4Image2=new ImageView(getContext());
        try {
            InputStream li4Strem2 = getResources().getAssets().open("Jiang_Night/Arrow.png");
            li4Image2.setImageBitmap(BitmapFactory.decodeStream(li4Strem2));
        } catch (IOException e) {
            e.printStackTrace();
        }

        li4.addView(li4Image,ImageLayout);
        li4.addView(li4Text,TextLayout);
        li4.addView(li4Image2,ImageLayout2);
        li4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent();
                intent.setAction( "android.intent.action.VIEW" );
                Uri content_url = Uri.parse( "http://www.deskcity.org/bizhi/2124.html" );
                intent.setData(content_url);
                getContext().startActivity(intent);
                dialog.dismiss();
            }
        });
        li1.addView(li4,Layout);//第二层横向布局放在垂直子布局中


        LinearLayout li5=new LinearLayout(getContext());//第二层横向线性布局
        li5.setOrientation(LinearLayout.HORIZONTAL);
        GradientDrawable li5Drawable=new GradientDrawable();
        li5Drawable.setCornerRadius(dip2px(getContext(),26));
        li5Drawable.setStroke(dip2px(getContext(),3),0xFFEDF0F5);
        li5.setBackground(li5Drawable);
        ImageView li5Image =new ImageView(getContext());
        try {
            InputStream inputStream = getResources().getAssets().open("Jiang_Night/wenjianjia.png");
            li5Image.setImageBitmap(BitmapFactory.decodeStream(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextView li5Text =new TextView(getContext());
        li5Text.setTextColor(0xFF717171);
        li5Text.setText("本地相册选图");
        li5Text.setTextSize(dip2px(getContext(),6));
        ImageView li5Image2=new ImageView(getContext());
        try {
            InputStream li5Strem2 = getResources().getAssets().open("Jiang_Night/Arrow.png");
            li5Image2.setImageBitmap(BitmapFactory.decodeStream(li5Strem2));
        } catch (IOException e) {
            e.printStackTrace();
        }
        li5.addView(li5Image,ImageLayout);
        li5.addView(li5Text,TextLayout);
        li5.addView(li5Image2,ImageLayout2);
        li5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPhoto();
                dialog.dismiss();
            }
        });

        li1.addView(li5,Layout);//第二层横向布局放在垂直子布局中

        TextView tips = new TextView(getContext());
        tips.setTextColor(0xff8B909E);
        tips.setTextSize(dip2px(getContext(),4));
        tips.setText("Tips:本地相册无选择状态返回为随机换图模式");
        LinearLayout.LayoutParams tipsLayout =new LinearLayout.LayoutParams(-2,-2);
        tipsLayout.gravity=Gravity.CENTER_HORIZONTAL;
        tipsLayout.setMargins(dip2px(getContext(),20),dip2px(getContext(),10),dip2px(getContext(),20),dip2px(getContext(),10));
        xEdit =new XEdit(getContext());
        li1.addView(xEdit,Layout);
        li1.addView(tips,tipsLayout);
        return frameLayout;

    }

    //把dp转为px，用来适配不同屏幕
    public static int dip2px(Context context, float dipValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.3f);
    }

}


