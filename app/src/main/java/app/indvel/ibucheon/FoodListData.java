package app.indvel.ibucheon;

/**
 * Created by RSB on 2016-11-08.
 */

public class FoodListData {

    //날짜
    private String fDate;

    //중식
    private String lunchFood;

    //석식
    private String dinnerFood;

    //급식 사진
    private String imgUrl;

    public void setFoodDate(String date) {
        fDate = date;
    }

    public void setLunchFood(String content) {
        lunchFood = content;
    }

    public void setDinnerFood(String content) {
        dinnerFood = content;
    }

    public void setImgUrl(String content) { imgUrl = content; }

    public String getFoodDate() {
        return this.fDate;
    }

    public String getLunchFood() {
        return this.lunchFood;
    }

    public String getDinnerFood() {
        return this.dinnerFood;
    }

    public String getImgUrl() { return this.imgUrl; }
}
