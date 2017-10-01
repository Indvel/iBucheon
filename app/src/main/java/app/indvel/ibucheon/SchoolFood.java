package app.indvel.ibucheon;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.hyunjun.school.School;
import org.hyunjun.school.SchoolMenu;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SchoolFood extends AppCompatActivity {

    public TextView fdate;
    public TextView fcontent;
    public TextView fdate_dinner;
    public TextView fcontent_dinner;
    public static MealAsyncTask asyncTask;
    private ListView fListView = null;
    private ListViewAdapter fAdapter = null;
    Calendar mCalendar = Calendar.getInstance();
    Integer foodYear;
    Integer foodMonth;
    Integer realMonth;
    Integer foodDay;
    String foodWeek;
    List<SchoolMenu> menu;
    String date = "";
    String content = "";
    String content_dinner = "";
    ProgressDialog mDialog;
    School api = new School(School.Type.HIGH, School.Region.GYEONGGI, "J100000585");
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
            fAdapter = new ListViewAdapter();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            menu = api.getMonthlyMenu(foodYear, foodMonth);

            for(int i = 0; i < menu.size(); i++) {
                if(String.valueOf(menu.get(i)) != "") {
                    String lunch = (menu.get(i).lunch).replaceAll("[0-9]","").replaceAll("\\.","");
                    String dinner = (menu.get(i).dinner).replaceAll("[0-9]","").replaceAll("\\.","");
                    fAdapter.addItem(foodYear + "년 " + foodMonth + "월 " + (i + 1) + "일 " + getWeek(foodYear, realMonth, (i + 1)) + "요일",String.valueOf(lunch),String.valueOf(dinner));
                }
            }

            return null;
        }


        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {

            mDialog.dismiss();
            super.onPostExecute(s);
            fListView.setAdapter(fAdapter);
            fAdapter.notifyDataSetChanged();
            fListView.setSelection(foodDay - 1);
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
                    Log.d("Selected Date", mCalendar.get(Calendar.YEAR) + "-" + (mCalendar.get(Calendar.MONTH) + 1) + "-" + mCalendar.get(Calendar.DAY_OF_MONTH));
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
        private ArrayList<FoodListData> fListData = new ArrayList<FoodListData>();

        public ListViewAdapter() {

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

        public void addItem(String mDate, String lunch, String dinner){
            FoodListData mData = new FoodListData();

            mData.setFoodDate(mDate);
            mData.setLunchFood(lunch);
            mData.setDinnerFood(dinner);
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
