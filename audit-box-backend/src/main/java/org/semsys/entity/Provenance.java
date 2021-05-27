package org.semsys.entity;

import java.util.ArrayList;
import java.util.List;

public class Provenance {

    private List<Activity> activities =  new ArrayList<>();
    private List<Agent> agents =  new ArrayList<>();
    private List<Entity> entities =  new ArrayList<>();
    private String time = "";

    public void addActivity(Activity activity) {
        this.activities.add(activity);
    }

    public void addAgent(Agent agent) {
        this.agents.add(agent);
    }

    public void addEntity(Entity entity) {
        this.entities.add(entity);
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
