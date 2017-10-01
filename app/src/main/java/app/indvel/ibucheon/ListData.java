package app.indvel.ibucheon;

public class ListData {
    /**
     * 리스트 정보를 담고 있을 객체 생성
     */

    // 날짜
    private String mDate;

    // 내용
    private String mContent;

    public void setDate(String date) {
        mDate = date;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getDate() {
        return this.mDate;
    }

    public String getContent() {
        return this.mContent;
    }
}
