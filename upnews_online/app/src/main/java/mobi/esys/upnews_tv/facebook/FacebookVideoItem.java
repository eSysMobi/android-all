package mobi.esys.upnews_tv.facebook;


import com.google.gson.annotations.SerializedName;

public class FacebookVideoItem {
    @SerializedName("id")
    private String id;
    @SerializedName("description")
    private String description;
    @SerializedName("source")
    private String source;

    public FacebookVideoItem(final String id, final String description, final String source) {
        this.id = id;
        this.description = description;
        this.source = source;
    }

    public final String getId() {
        return id;
    }

    public final void setId(final String id) {
        this.id = id;
    }

    public final String getDescription() {
        return description;
    }

    public final void setDescription(final String description) {
        this.description = description;
    }

    public final String getSource() {
        return source;
    }

    public final void setSource(final String source) {
        this.source = source;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof FacebookVideoItem)) return false;

        FacebookVideoItem that = (FacebookVideoItem) o;

        if (!id.equals(that.id)) return false;
        if (!description.equals(that.description)) return false;
        return source.equals(that.source);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + source.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "FacebookVideoItem{" + "id='" + id + '\'' + ", description='" + description + '\'' + ", source='" + source + '\'' + '}';
    }
}
