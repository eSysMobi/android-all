package mobi.esys.dastarhan.utils;

/**
 * Created by ZeyUzh on 09.06.2016.
 */
public class FoodCheckElement {
    private int restID;
    private long timeCheck;

    public FoodCheckElement(int restID, long timeCheck){
        this.restID = restID;
        this.timeCheck = timeCheck;
    }

    public void setRestID(int restID) {
        this.restID = restID;
    }

    public void setTimeCheck(long timeCheck) {
        this.timeCheck = timeCheck;
    }

    public int getRestID() {
        return restID;
    }

    public long getTimeCheck() {
        return timeCheck;
    }
}
