package mobi.esys.dastarhan.database;

public interface UserInfoRepository {

    void update(UserInfo userInfo);

    UserInfo get();
}