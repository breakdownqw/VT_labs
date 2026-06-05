package org.example.web3.beans;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.example.web3.db.HitEntity;
import org.example.web3.db.HitEntityManager;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Named("tableBean")
@SessionScoped
public class TableBean implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Inject
    private HitEntityManager hitEntityManager;

    private List<HitEntity> hitEntities;

    @PostConstruct
    public void init() {
        hitEntities = hitEntityManager.getHits();
    }

    public void add(HitEntity hitEntity) {
        hitEntities.add(0, hitEntity);
    }

    public void clearTable(){
        hitEntityManager.delete();
        refreshHits();
    }

    public void refreshHits(){
        hitEntities = hitEntityManager.getHits();
    }

    public List<HitEntity> getHitEntities() {
        return hitEntities;
    }
}
