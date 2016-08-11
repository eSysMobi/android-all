package mobi.esys.instagram;

public class InstagramItem {
    private String igPhotoID;
    private String igThumbURL;
    private String igOriginURL;
    private int igLikes;

    public InstagramItem(final String igPhotoID,
                         final String igThumbURL,
                         final String igOriginURL,
                         int igLikes) {
        this.igPhotoID = igPhotoID;
        this.igThumbURL = igThumbURL;
        this.igOriginURL = igOriginURL;
        this.igLikes = igLikes;
    }

    /**
     * @return the igPhotoID
     */
    public String getIgPhotoID() {
        return igPhotoID;
    }

    /**
     * @return the igThumbURL
     */
    public String getIgThumbURL() {
        return igThumbURL;
    }

    /**
     * @return the igOriginURL
     */
    public String getIgOriginURL() {
        return igOriginURL;
    }

    /**
     * @return the igLikes
     */
    public int getIgLikes() {
        return igLikes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InstagramItem{");
        sb.append("igPhotoID='").append(igPhotoID).append('\'');
        sb.append(", igThumbURL='").append(igThumbURL).append('\'');
        sb.append(", igOriginURL='").append(igOriginURL).append('\'');
        sb.append(", igLikes=").append(igLikes);
        sb.append('}');
        return sb.toString();
    }
}
