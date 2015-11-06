package mobi.esys.upnews_tv.instagram;


public class InstagramItem {
    private String igPhotoID;
    private String igThumbURL;
    private String igOriginURL;
    private String igComment;
    private String igUserName;

    public InstagramItem(String igPhotoID, String igThumbURL, String igOriginURL, String igComment, String igUserName) {
        this.igPhotoID = igPhotoID;
        this.igThumbURL = igThumbURL;
        this.igOriginURL = igOriginURL;
        this.igComment = igComment;
        this.igUserName = igUserName;
    }



    public final  String getIgOriginURL() {
        return igOriginURL;
    }



    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof InstagramItem)) return false;

        InstagramItem that = (InstagramItem) o;

        if (!igPhotoID.equals(that.igPhotoID)) return false;
        if (!igThumbURL.equals(that.igThumbURL)) return false;
        if (!igOriginURL.equals(that.igOriginURL)) return false;
        if (!igComment.equals(that.igComment)) return false;
        return igUserName.equals(that.igUserName);

    }

    @Override
    public int hashCode() {
        int result = igPhotoID.hashCode();
        result = 31 * result + igThumbURL.hashCode();
        result = 31 * result + igOriginURL.hashCode();
        result = 31 * result + igComment.hashCode();
        result = 31 * result + igUserName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "InstagramItem{" + "igPhotoID='" + igPhotoID + '\'' + ", igThumbURL='" + igThumbURL + '\'' + ", igOriginURL='" + igOriginURL + '\'' + ", igComment='" + igComment + '\'' + ", igUserName='" + igUserName + '\'' + '}';
    }

    public String getIgUserName() {
        return igUserName;
    }

}