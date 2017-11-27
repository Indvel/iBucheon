package app.indvel.ibucheon;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class SchoolNotice extends AppCompatActivity {

    private static String URL_PRIMARY = "http://ibucheon.hs.kr"; //홈페이지 원본 주소이다.
    private static String GETNOTICE = "/board.list?mcode=1510&cate=1510"; //홈페이지 의 게시판을 나타내는 뒤 주소, 비슷한 게시판들은 거의 파싱이 가능하므로 응용하여 사용하자.
    ArrayList<ListData> mListData = new ArrayList<>();
    private ProgressDialog mDialog;
    private BBSListAdapter BBSAdapter = null;
    private ListView BBSList;
    private ConnectivityManager cManager;
    private NetworkInfo mobile;
    private NetworkInfo wifi;
    NoticeTask asyncTask;

    @Override
    protected void onStop() { //멈추었을때 다이어로그를 제거해주는 메서드
        super.onStop();
        if (mDialog != null)
            mDialog.dismiss(); //다이어로그가 켜져있을경우 (!null) 종료시켜준다
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_notice);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (isInternetCon() == false) {
            Toast.makeText(SchoolNotice.this, "인터넷에 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            asyncTask = new NoticeTask();
            asyncTask.execute();
        }

        BBSList = (ListView) findViewById(R.id.listView); //리스트선언
        BBSList.setFastScrollEnabled(true);
        BBSAdapter = new BBSListAdapter(this);
        BBSList.setAdapter(BBSAdapter); //리스트에 어댑터를 먹여준다.
        BBSList.setOnItemClickListener((AdapterView<?> parent, View v, int position, long id) -> {
                        ListData mData = mListData.get(position); // 클릭한 포지션의 데이터를 가져온다.
                        String URL_BCS = mData.mUrl; //가져온 데이터 중 url 부분만 적출해낸다.
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL_PRIMARY + URL_BCS))); //적출해낸 url 을 이용해 URL_PRIMARY 와 붙이고 웹페이지를 연다.
                    });
    }

    public class NoticeTask extends AsyncTask<String,Void, String> {
        public String result;

        @Override
        protected void onPreExecute() {

            mDialog = new ProgressDialog(SchoolNotice.this);
            mDialog.setMessage("공지사항을 가져오는 중 입니다...");
            mDialog.setCancelable(false);
            mDialog.show();

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            try {

                Document document = Jsoup.connect(URL_PRIMARY + GETNOTICE)
                        .timeout(5000)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")
                        .referrer("http://ibucheon.hs.kr/board.list?mcode=1511&cate=1511")
                        .get();

                Elements table = document.select("table.boardList > tbody > tr");

                for(Element e : table) {

                    String title = e.select("td.title a").text();
                    String type = title.substring(title.indexOf("[")+1, title.lastIndexOf("]"));
                    String url = e.select("td.title a").attr("href");
                    String writer = e.select("td").get(2).select("a").text();
                    String date = e.select("td.date").text();

                    mListData.add(new ListData(type, title, url, writer, date));

                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            mDialog.dismiss();
            super.onPostExecute(s);
            BBSAdapter.notifyDataSetChanged();
            BBSList.setAdapter(BBSAdapter);
        }
    }


    private boolean isInternetCon() {
        cManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        return cManager.getActiveNetworkInfo() != null;
    }

    private void listViewHeightSet(Adapter listAdapter, ListView listView) {
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    // <리스트 적용부분
    class ViewHolder {

        public TextView mType;
        public TextView mTitle;
        public TextView mUrl;
        public TextView mWriter;
        public TextView mDate;
    }


    public class BBSListAdapter extends BaseAdapter {
        private Context mContext = null;

        public BBSListAdapter(Context mContext) {
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
                convertView = inflater.inflate(R.layout.itemstyle, null);

                holder.mTitle = (TextView) convertView.findViewById(R.id.item_title);
                holder.mTitle.setSelected(true);
                holder.mWriter = (TextView) convertView.findViewById(R.id.item_writer);
                holder.mDate = (TextView) convertView.findViewById(R.id.item_date);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ListData mData = mListData.get(position);

            if(mData.mTitle.contains("[공지]")) {

                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
                   holder.mTitle.setText(Html.fromHtml("<font color=#f44336>[공지] </font>" + mData.mTitle.replace("[공지]", "")));
                } else {
                    holder.mTitle.setText(Html.fromHtml("<font color=#f44336>[공지] </font>" + mData.mTitle.replace("[공지]", ""), Html.FROM_HTML_MODE_LEGACY));
                }
            } else if(mData.mTitle.contains("[일반]")) {

                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
                    holder.mTitle.setText(Html.fromHtml("<font color=#03a9f4>[일반] </font>" + mData.mTitle.replace("[일반]", "")));
                } else {
                    holder.mTitle.setText(Html.fromHtml("<font color=#03a9f4>[일반] </font>" + mData.mTitle.replace("[일반]", ""), Html.FROM_HTML_MODE_LEGACY));
                }
            } else {
                holder.mTitle.setText(mData.mTitle);
            }

            holder.mWriter.setText("작성자: " + mData.mWriter);
            holder.mDate.setText(mData.mDate);

            return convertView;

        }


    }

    public class ListData { // 데이터를 받는 클래스

        public String mType;
        public String mTitle;
        public String mUrl;
        public String mWriter;
        public String mDate;

        public ListData(String mType, String mTitle, String mUrl, String mWriter, String mDate) { //데이터를 받는 클래스 메서드
            this.mType = mType;
            this.mTitle = mTitle;
            this.mUrl = mUrl;
            this.mWriter = mWriter;
            this.mDate = mDate;

        }

    }
    // 리스트 적용부분 >
}
