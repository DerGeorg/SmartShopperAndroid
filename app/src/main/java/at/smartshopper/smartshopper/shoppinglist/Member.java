package at.smartshopper.smartshopper.shoppinglist;

public class Member {
    private String uid, msid;

    public Member(String uid, String msid){
        this.uid = uid;
        this.msid = msid;
    }

    public String getUid() {
        return uid;
    }

    public String getMsid() {
        return msid;
    }
}
