package vn.com.goldtek.facenotify;

public class NotificationModel {

    private String ID;
    private String Name;
    private String Time;
    private String Group;
    private String ImageUrl;

    public NotificationModel(String name, String time, String group, String imageurl) {
        this.Name = name;
        this.Time = time;
        this.Group = group;
        this.ImageUrl = imageurl;
    }

    public NotificationModel() {
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }


    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getGroup() {
        return Group;
    }

    public void setGroup(String group) {
        Group = group;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String url) {
        ImageUrl = url;
    }
}
