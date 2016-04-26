package mobi.esys.upnews_tube.system;

/**
 * Created by ZeyUzh on 17.04.2016.
 */
public class YoutubePlaylistElement {
    private String name;
    private String id;

    public YoutubePlaylistElement(String incomingName, String incomingID) {
        name = incomingName;
        id = incomingID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
