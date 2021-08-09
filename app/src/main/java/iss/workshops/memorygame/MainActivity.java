package iss.workshops.memorygame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
{

    String[] standardNum = {"1", "2", "3", "4", "5", "6"};
    int i = 0;
    int imgCount = 0;

    static  int rowCount=0;
    static  int imageCount=0;

    ProgressBar progressBar;
    TextView progressBarTextView;

    EditText mUrl;
    Button searchBtn;
    List<String> pics;
    String urlLink;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
         progressBar.setMax(20);
        progressBarTextView = (TextView) findViewById(R.id.progressBarTextView);
        searchBtn=findViewById(R.id.fetchButton);


        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mUrl = findViewById(R.id.urlSource);
                urlLink = mUrl.getText().toString();
                if(urlLink.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Enter URL", Toast.LENGTH_LONG).show();
                }
                else
                {
                    getWebsite(urlLink);

                }


            }
        });

    }


//getWebsite() is used for fetching image link from a website and downloading it.

    public void getWebsite(String url)
    {
        ImageView imageView1 = findViewById(R.id.image1);
        ImageView imageView2 = findViewById(R.id.image2);
        ImageView imageView3 = findViewById(R.id.image3);
        ImageView imageView4 = findViewById(R.id.image4);
        ImageView imageView5 = findViewById(R.id.image5);
        ImageView imageView6 = findViewById(R.id.image6);
        ImageView imageView7 = findViewById(R.id.image7);
        ImageView imageView8 = findViewById(R.id.image8);
        ImageView imageView9 = findViewById(R.id.image9);
        ImageView imageView10 = findViewById(R.id.image10);
        ImageView imageView11 = findViewById(R.id.image11);
        ImageView imageView12 = findViewById(R.id.image12);
        ImageView imageView13 = findViewById(R.id.image13);
        ImageView imageView14 = findViewById(R.id.image14);
        ImageView imageView15 = findViewById(R.id.image15);
        ImageView imageView16= findViewById(R.id.image16);
        ImageView imageView17 = findViewById(R.id.image17);
        ImageView imageView18 = findViewById(R.id.image18);
        ImageView imageView19 = findViewById(R.id.image19);
        ImageView imageView20= findViewById(R.id.image20);

        ImageView[] imgViews = {imageView1,imageView2,imageView3,imageView4,imageView5,imageView6,
                        imageView7,imageView8,imageView9,imageView10,imageView11,imageView12,imageView13,
                imageView14,imageView15,imageView16,imageView17,imageView18,imageView19,imageView20};
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {


                try {
                    Document doc = Jsoup.connect(url).get();
                    String title = doc.title();
                    Elements links = doc.select("img[src]");
                    rowCount = 0;
                    imageCount = 0;

                        for (Element link : links)
                        {

                                String imageLink = link.attr("src");
                                if(imageLink.contains(".png") || imageLink.contains(".jpg") || imageLink.contains(".jpeg"))
                                {
                                    if (imageCount < 20) {
                                    String destFilename = UUID.randomUUID().toString() + imageLink.lastIndexOf(".") + 1;
                                    File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                                    File destFile = new File(dir, destFilename);
                                    ImageDownloader imgDwnld = new ImageDownloader();




                                    if (rowCount == 20) {
                                        rowCount=0;
                                    }

                                    if (imgDwnld.downloadImage(imageLink, destFile) && rowCount<20) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                //List of image view
                                                String absolutePath = destFile.getAbsolutePath();

                                                Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);



                                                imgViews[rowCount].setImageBitmap(bitmap);
                                                imageCount = imageCount + 1;
                                                rowCount = rowCount + 1;
                                                progressBar.setVisibility(View.VISIBLE);
                                                progressBar.setProgress(rowCount);
                                                progressBarTextView.setText
                                                        (String.format("Download %d of %d images", rowCount, 20));
                                            }
                                        });
                                    }
                                }

                            }
                    }
                }


                catch (Exception e) {
                      e.printStackTrace();
                }


            }
        }).start();

    }



    //
    private String saveToFile(Bitmap bitmapImage) {

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
         File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir

        File mypath = new File(directory, standardNum[i]);
        i++;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                fos.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }



    public void sendSelectedImage(View v) throws FileNotFoundException {
        ImageView imageView = (ImageView) v;

        Drawable highlight = getResources().getDrawable(R.drawable.on_click);
        imageView.setBackground(highlight);

        BitmapDrawable bd = (BitmapDrawable) imageView.getDrawable();
        Bitmap b = bd.getBitmap();
        String filepath = saveToFile(b);
        imgCount++;

        if (imgCount > 5)
        {
            Intent i = new Intent(MainActivity.this, PlayActivity.class);
            i.putExtra("img_path", filepath);
            startActivity(i);
        }
    }

}


