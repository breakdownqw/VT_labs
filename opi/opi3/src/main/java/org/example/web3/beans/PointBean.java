package org.example.web3.beans;


import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.example.web3.HitChecker;
import org.example.web3.db.HitEntityManager;
import org.example.web3.db.HitEntity;

import java.io.Serial;
import java.io.Serializable;

@Named("pointBean")
@ViewScoped
public class PointBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Inject private HitEntityManager hitEntityManager;
    @Inject private TableBean tableBean;

    private int x;
    private int r;
    private Double y;

    public Double getY() { return y; }
    public void setY(Double y) { this.y = y; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getR() { return r; }
    public void setR(int r) { this.r = r; }

    public String submit() {
        long start = System.nanoTime();
        boolean isHit = HitChecker.isHit(x, y, r);
        HitEntity saved = hitEntityManager.add(new HitEntity(x, y, r, isHit, start));
        tableBean.add(saved);
        return "main?faces-redirect=true";
    }


}
