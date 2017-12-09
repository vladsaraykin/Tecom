package moduls;

import java.util.List;

public class NetworkPrinter {
    private String name;
    private String location;
    private float timeWork;
    private List contacts;
    private boolean state = false;
    //число интерфейсов

    public void start() {
        state = true;
        System.out.println(name + " started!");
    }

    public void stop() {
        state = false;
        System.out.println(name + " stoped!");
    }

    public boolean checkStart() {
        if (state) {
            System.out.println(name + " on");
            return state;
        }
        System.out.println(name + " off");
        return state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public float getTimeWork() {
        return timeWork;
    }

    public void setTimeWork(float timeWork) {
        this.timeWork = timeWork;
    }

    public List getContacts() {
        return contacts;
    }

    public void setContacts(List contacts) {
        this.contacts = contacts;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
