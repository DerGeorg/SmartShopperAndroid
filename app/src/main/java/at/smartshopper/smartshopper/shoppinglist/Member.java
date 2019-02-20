package at.smartshopper.smartshopper.shoppinglist;

public class Member {
    private String uid, msid, name, pic, email;

    public Member(String uid, String msid, String name, String pic, String email){
        this.uid = uid;
        this.msid = msid;
        this.name = name;
        this.pic = pic;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getPic() {
        return pic;
    }

    public String getEmail() {
        return email;
    }

    public String getUid() {
        return uid;
    }

    public String getMsid() {
        return msid;
    }
}
