package app.indvel.ibucheon;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

public class SchoolFood extends AppCompatActivity {

    private String url = "http://ibucheon.hs.kr/lunch.list?ym=";
    private String foodUrl = "";
    public static MealAsyncTask asyncTask;
    public static FoodImageTask imageTask;
    private ListView fListView = null;
    private ListViewAdapter fAdapter = null;
    private ArrayList<FoodListData> fListData = new ArrayList();
    Calendar mCalendar = Calendar.getInstance();
    Integer foodYear;
    Integer foodMonth;
    Integer realMonth;
    Integer foodDay;
    Bitmap foodImage = null;
    ProgressDialog mDialog;
    ConnectivityManager cManager;

    @Override
    protected void onStop() { //멈추었을때 다이어로그를 제거해주는 메서드
        super.onStop();
        if (mDialog != null)
            mDialog.dismiss(); //다이어로그가 켜져있을경우 (!null) 종료시켜준다
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_food);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(isInternetCon() == false) {
            Toast.makeText(this, "인터넷에 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            asyncTask = new MealAsyncTask();
            asyncTask.execute();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogDatePicker();
            }
        });

        fListView = (ListView) findViewById(R.id.listView);
        fListView.setFastScrollEnabled(true);
        fAdapter = new ListViewAdapter(this);
        fListView.setAdapter(fAdapter);

        fListView.setOnItemClickListener((AdapterView<?> parent, View v, int position, long id) -> {
            FoodListData mData = fListData.get(position);
            foodUrl = mData.getImgUrl();
            imageTask = new FoodImageTask();
            imageTask.execute();
        });
    }

    public void showFoodImage(Bitmap bitmap) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        AlertDialog dialog;

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_dialog, null);

        ImageView iv = (ImageView) layout.findViewById(R.id.foodImage);
        iv.setImageBitmap(bitmap);

        alert.setTitle("급식 미리보기");
        alert.setView(layout);
        alert.setPositiveButton("확인", null);

        dialog = alert.create();
        dialog.show();
    }

    public class MealAsyncTask extends AsyncTask<String,Void, String> {
        public String result;

        @Override
        protected void onPreExecute() {

            mDialog = new ProgressDialog(SchoolFood.this);
            mDialog.setMessage("급식 정보를 가져오는 중 입니다...");
            mDialog.setCancelable(false);
            mDialog.show();

            foodYear = mCalendar.get(Calendar.YEAR);
            foodMonth = mCalendar.get(Calendar.MONTH) + 1;
            realMonth = mCalendar.get(Calendar.MONTH);
            foodDay = mCalendar.get(Calendar.DAY_OF_MONTH);

            fListData = new ArrayList<>();
            fAdapter = new ListViewAdapter(SchoolFood.this);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            try {

                String ym = String.valueOf(foodYear) + String.format("%02d", foodMonth);

                Document doc = Jsoup.connect(url + ym)
                        .timeout(2000)
                        .get();

                Elements menu = doc.select("div#foodListArea > table > tbody > tr > td");

                for(Element e : menu) {
                    String span =  e.select("div.dayBox > span").text();
                    String content = e.select("div.content > div.lunch > div.tabContent.on > span > a").text();
                    String imgurl = e.select("div.content > div.lunch > div.tabContent.on > span > a").attr("href");

                    if(span == "") {
                        span = e.select("span").text();
                    }

                   if (span != "" && content != "") {
                        fAdapter.addItem(foodYear + "년 " + foodMonth + "월 " + span + "일 " + getWeek(foodYear, realMonth, Integer.valueOf(span)) + "요일", content, "석식 없음", imgurl);
                    } else if(span != "" && content == "") {
                        fAdapter.addItem(foodYear + "년 " + foodMonth + "월 " + span + "일 " + getWeek(foodYear, realMonth, Integer.valueOf(span)) + "요일", "중식 없음", "석식 없음", imgurl);
                    }
                }

            } catch(IOException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {

            fAdapter.notifyDataSetChanged();
            fListView.setAdapter(fAdapter);
            fListView.setSelection(foodDay - 1);
            mDialog.dismiss();
            super.onPostExecute(s);
        }
    }

    public class FoodImageTask extends AsyncTask<String,Void, String> {
        public String result;

        @Override
        protected void onPreExecute() {

            mDialog = new ProgressDialog(SchoolFood.this);
            mDialog.setMessage("잠시만 기다려 주세요...");
            mDialog.setCancelable(true);
            mDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            try {

                String mainUrl = "http://ibucheon.hs.kr";

                Document document = Jsoup.connect(mainUrl + foodUrl)
                        .timeout(2000)
                        .get();

                String sel = document.select("div.objContent1 > ul.calorie > a > img").attr("src");

                URL link = new URL(mainUrl + sel);

                HttpURLConnection conn = (HttpURLConnection) link.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(false);
                conn.connect();

                foodImage = BitmapFactory.decodeStream(conn.getInputStream());

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            mDialog.dismiss();
            showFoodImage(foodImage);
            super.onPostExecute(s);
        }
    }

    private boolean isInternetCon() {
        cManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        return cManager.getActiveNetworkInfo() != null;
    }

    private String getWeek(Integer year, Integer month, Integer day) {

        Calendar cal;
        cal = Calendar.getInstance();
        cal.set(year, month, day);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            return cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, getResources().getConfiguration().getLocales().get(0));
        } else {
            return cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, getResources().getConfiguration().locale);
        }
    }

    private void DialogDatePicker() {

        int month = foodMonth-1;

        DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            // onDateSet method
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                if(month == monthOfYear) {
                    fListView.setSelection(dayOfMonth - 1);
                } else {

                    mCalendar.set(year, monthOfYear, dayOfMonth);
                    asyncTask = new MealAsyncTask();
                    asyncTask.execute();
                }
            }
        };
        DatePickerDialog alert = new DatePickerDialog(this, mDateSetListener,
                foodYear, month, foodDay);
        alert.show();
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;

        public ListViewAdapter(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return fListData.size();
        }

        @Override
        public Object getItem(int position) {
            return fListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void addItem(String mDate, String lunch, String dinner, String imgUrl){
            FoodListData mData = new FoodListData();

            mData.setFoodDate(mDate);
            mData.setLunchFood(lunch);
            mData.setDinnerFood(dinner);
            mData.setImgUrl(imgUrl);
            fListData.add(mData);
        }

        public void remove(int position){
            fListData.remove(position);
            dataChange();
        }

        public void dataChange(){
            fAdapter.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            final Context context = parent.getContext();

            // "listview_item" Layout을 inflate하여 convertView 참조 획득.
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.food_item, parent, false);
            }

            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            TextView foodDate = (TextView) convertView.findViewById(R.id.food_date);
            TextView lunchFood = (TextView) convertView.findViewById(R.id.food_content);
            TextView dinnerFood = (TextView) convertView.findViewById(R.id.food_content_dinner);
            CardView fcv = (CardView) convertView.findViewById(R.id.foodcv);

            // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
            FoodListData listViewItem = fListData.get(position);

            // 아이템 내 각 위젯에 데이터 반영
            foodDate.setText(listViewItem.getFoodDate());
            lunchFood.setText(listViewItem.getLunchFood());
            dinnerFood.setText(listViewItem.getDinnerFood());

            return convertView;
        }
    }
}
