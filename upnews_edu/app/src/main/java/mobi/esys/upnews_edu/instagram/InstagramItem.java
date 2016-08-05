package mobi.esys.upnews_edu.instagram;


public class InstagramItem {
    private String igPhotoID;
    private String igThumbURL;
    private String igOriginURL;

    public InstagramItem(String igPhotoID, String igThumbURL, String igOriginURL) {
        this.igPhotoID = igPhotoID;
        this.igThumbURL = igThumbURL;
        this.igOriginURL = igOriginURL;
    }

    public String getIgPhotoID() {
        return igPhotoID;
    }

    public String getIgThumbURL() {
        return igThumbURL;
    }

    public String getIgOriginURL() {
        return igOriginURL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstagramItem that = (InstagramItem) o;

        if (igPhotoID != null ? !igPhotoID.equals(that.igPhotoID) : that.igPhotoID != null)
            return false;
        if (igThumbURL != null ? !igThumbURL.equals(that.igThumbURL) : that.igThumbURL != null)
            return false;
        return !(igOriginURL != null ? !igOriginURL.equals(that.igOriginURL) : that.igOriginURL != null);

    }

    @Override
    public int hashCode() {
        int result = igPhotoID != null ? igPhotoID.hashCode() : 0;
        result = 31 * result + (igThumbURL != null ? igThumbURL.hashCode() : 0);
        result = 31 * result + (igOriginURL != null ? igOriginURL.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "InstagramItem{" + "igPhotoID='" + igPhotoID + '\'' + ", igThumbURL='" + igThumbURL + '\'' + ", igOriginURL='" + igOriginURL + '\'' + '}';
    }

}