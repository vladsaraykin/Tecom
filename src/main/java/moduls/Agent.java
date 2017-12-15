package moduls;

import java.util.List;

public abstract class Agent implements ILifeCycle {
    private String name;
    private String location;
    private double timeWork = 0;
    private List contacts;
    private boolean state = false;
    //число интерфейсов

    public Agent(String name, String location) {
        this.name = name;
        this.location = location;

    }

    public Agent() {

    }

    public void start() {
        state = true;
        timeWork = System.currentTimeMillis();
        System.out.println(name + " started!");

    }

    public void stop() {
        state = false;
        System.out.println(name + " stoped!");
        timeWork = (System.currentTimeMillis() - timeWork) / 1000;
        System.out.println("Время работы " + timeWork + " секунд");
    }

    public void checkStart() {
        if (state) {
            System.out.println(name + " on");
            return;
        }
        System.out.println(name + " off");
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

    public double getTimeWork() {
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
