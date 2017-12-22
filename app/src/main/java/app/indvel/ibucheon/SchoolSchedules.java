package app.indvel.ibucheon;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class SchoolSchedules extends AppCompatActivity {

    private String url = "http://ibucheon.hs.kr/calendar.list?ym=";
    public static ScheduleAsyncTask scheduleAsyncTask;
    private ListView mListView;
    private ArrayList<ListData> mListData = new ArrayList<>();
    private ListViewAdapter mAdapter = null;
    ProgressDialog mDialog;
    Context mContext = this;
    Calendar mCalendar = Calendar.getInstance();
    Integer calYear;
    Integer calMonth;
    Integer realCalMonth;
    Integer calDay;
    String calWeek;
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
        setContentView(R.layout.activity_school_schedule);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(isInternetCon() == false) {
            Toast.makeText(this, "인터넷에 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            scheduleAsyncTask = new ScheduleAsyncTask();
            scheduleAsyncTask.execute();
        }

        FloatingActionButton schcal= (FloatingActionButton) findViewById(R.id.schcal);
        schcal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogDatePicker();
            }
        });

        mListView = (ListView) findViewById(R.id.list);
        mListView.setFastScrollEnabled(true);
        mAdapter = new ListViewAdapter(this);
        mListView.setAdapter(mAdapter);
    }


    public class ScheduleAsyncTask extends AsyncTask<String,Void,String> {

        public String result;

        @Override
        protected void onPreExecute() {

            mDialog = new ProgressDialog(SchoolSchedules.this);
            mDialog.setMessage("학사 일정을 가져오는 중 입니다...");
            mDialog.setCancelable(false);
            mDialog.show();

            calYear = mCalendar.get(Calendar.YEAR);
            calMonth = mCalendar.get(Calendar.MONTH)+1;
            realCalMonth = mCalendar.get(Calendar.MONTH);
            calDay = mCalendar.get(Calendar.DAY_OF_MONTH);

            mListData = new ArrayList<>();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            try {

                String ym = String.valueOf(calYear) + String.format("%02d", calMonth);

                Document doc = Jsoup.connect(url + ym)
                        .timeout(2000)
                        .get();

                Elements schedule = doc.select("div#calendarArea > table > tbody > tr > td");

                for(Element e : schedule) {
                    String span = e.select("span").text();
                    String content = e.select("ul > li > a").text();
                    if(span != "" && content != "") {
                        mListData.add(new ListData(calYear + "년 " + calMonth + "월 " + span + "일 " + getWeek(calYear, realCalMonth, Integer.valueOf(span)) + "요일", content));
                    } else if(span != "" && content == "") {
                        mListData.add(new ListData(calYear + "년 " + calMonth + "월 " + span + "일 " + getWeek(calYear, realCalMonth, Integer.valueOf(span)) + "요일", "일정이 없습니다"));
                    }
                }

            } catch(IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {

            mAdapter.notifyDataSetChanged();
            mListView.setSelection(calDay - 1);
            mDialog.dismiss();
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

        int month = calMonth-1;

        DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            // onDateSet method
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                if(month == monthOfYear) {
                    mListView.setSelection(dayOfMonth - 1);
                } else {

                    mCalendar.set(year, monthOfYear, dayOfMonth);
                    scheduleAsyncTask = new ScheduleAsyncTask();
                    scheduleAsyncTask.execute();
                }
            }
        };
        DatePickerDialog alert = new DatePickerDialog(this, mDateSetListener,
                calYear, month, calDay);
        alert.show();
    }

    private class ViewHolder {
        public TextView mDate;
        public TextView mContent;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;

        public ListViewAdapter(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.schedule_item, null);

                holder.mDate = (TextView) convertView.findViewById(R.id.schedule_date);
                holder.mContent = (TextView) convertView.findViewById(R.id.schedule_content);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ListData listViewItem = mListData.get(position);

            holder.mDate.setText(listViewItem.mDate);
            holder.mContent.setText(listViewItem.mContent);
            holder.mContent.setTextColor(Color.parseColor("#5d5d5d"));

            if(!listViewItem.mContent.equals("일정이 없습니다")) {
                holder.mContent.setTextColor(Color.parseColor("#43a047"));
            }

            if(listViewItem.mDate.contains("1월 1일") && listViewItem.mContent.equals("일정이 없습니다")) {
                holder.mContent.setTextColor(Color.parseColor("#dd2c00"));
                holder.mContent.setText("신정");

            } else if(listViewItem.mDate.contains("3월 1일") && listViewItem.mContent.equals("일정이 없습니다")) {
                holder.mContent.setTextColor(Color.parseColor("#dd2c00"));
                holder.mContent.setText("3·1절");

            } else if(listViewItem.mDate.contains("5월 5일") && listViewItem.mContent.equals("일정이 없습니다")) {
                holder.mContent.setTextColor(Color.parseColor("#dd2c00"));
                holder.mContent.setText("어린이날");

            } else if(listViewItem.mContent.contains("석가탄신일")) {
                holder.mContent.setTextColor(Color.parseColor("#dd2c00"));

            } else if(listViewItem.mDate.contains("6월 6일") && listViewItem.mContent.equals("일정이 없습니다")) {
                holder.mContent.setTextColor(Color.parseColor("#dd2c00"));
                holder.mContent.setText("현충일");

            } else if(listViewItem.mDate.contains("8월 15일") && listViewItem.mContent.equals("일정이 없습니다")) {
                holder.mContent.setTextColor(Color.parseColor("#dd2c00"));
                holder.mContent.setText("어버이날");

            } if(listViewItem.mDate.contains("10월 3일") && listViewItem.mContent.equals("일정이 없습니다")) {
                holder.mContent.setTextColor(Color.parseColor("#dd2c00"));
                holder.mContent.setText("개천절");

            } else if(listViewItem.mDate.contains("10월 9일") && listViewItem.mContent.equals("일정이 없습니다")) {
                holder.mContent.setTextColor(Color.parseColor("#dd2c00"));
                holder.mContent.setText("한글날");

            } else if(listViewItem.mDate.contains("12월 25일") && listViewItem.mContent.equals("일정이 없습니다")) {
                holder.mContent.setTextColor(Color.parseColor("#dd2c00"));
                holder.mContent.setText("성탄절");

            } else if(listViewItem.mContent.contains("선거")) {
                holder.mContent.setTextColor(Color.parseColor("#dd2c00"));

            } else if(listViewItem.mDate.contains("토요일")) {
                holder.mContent.setTextColor(Color.parseColor("#00b0ff"));

            } else if(listViewItem.mDate.contains("일요일")) {
                holder.mContent.setTextColor(Color.parseColor("#dd2c00"));

            }

            return convertView;
        }
    }

    public class ListData {

        public String mDate;
        public String mContent;

        public ListData(String mDate, String mContent) {
            this.mDate = mDate;
            this.mContent = mContent;
        }
    }
}
