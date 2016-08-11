package mobi.esys.consts;


public final class ISConsts {


    public static final class globals {
        public static final String base_dir = "upnewshashtag";
        public static final String pref_prefix = "UNPref";
        public static final String photo_dir = "photo";
        public static final String logo_dir = "logo";
        public static final String logo_name = "upnews_logo_w2.png";
        public static final String default_color = "<font color='#11A2F0'>";
        public static final String default_divider = default_color.concat("@</font>");
        public static final String default_hashtag = "#news";
    }

    public static final class twitterconsts {
        public static final String twitter_key = "SZ0iHmFvfVODuQSQBPrIUWNKK";
        public static final String twitter_secret = "1iaQEDahvyTKEPaolhYxhL8qHC3RGlQulEELjoUESgdGnHW7DW";
    }

    public static final class instagramconsts {
        public static final String INSTAGRAM_CLIENT_ID="99e0e76995614c899a26674a05fcb71a";
        public static final String INSTAGRAM_CLIENT_SECRET="018be0b1d6ad4333b4ea61e8a3183a87";
        public static final String INSTAGRAM_REDIRECT_URI="http://esys.mobi/app/upnews/tv/auth";
        public static final int INSTAGRAM_PAGE_COUNT = 100;
        public static final int PAGINATION_MAX_PAGES = 3;
    }


    public static final class times {
        public static final int app_start_delay = 10000;
        public static final int twitter_get_feed_delay = 10000;

        public static final long SLIDE_CHANGE_INTERVAL = 12 * 1000;
        public static final int CHECK_SLIDES_INTERVAL = 10;
    }

    public static final class prefstags {
        public static final String twitter_allow = "twitter_allow";
        public static final String twitter_hashtag = "twHashTag";
        public static final String instagram_hashtag = "igHashTag";
    }

}
