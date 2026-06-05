package org.example.web3.db;

import jakarta.persistence.*;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Entity
@Table(name = "hits")
public class HitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false) private Integer x;
    @Column(nullable = false) private Double y;
    @Column(nullable = false) private Integer r;
    @Column(nullable = false) private boolean isHit;
    @Column(nullable = false) private String nowTime;
    @Column(nullable = false) private Double workTime;


    public HitEntity() {}
    public HitEntity(Integer x, Double y, Integer r, boolean isHit, Long startTime) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.isHit = isHit;
        this.nowTime =  LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy"));
        this.workTime = (double) (System.nanoTime() - startTime) / 1000000;
    }

    public long getId() { return id; }

    public Integer getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public Integer getR() {
        return r;
    }

    public boolean getIsHit() {
        return isHit;
    }

    public String getNowTime() {
        return nowTime;
    }

    public Double getWorkTime() {
        return workTime;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public void setR(Integer r) {
        this.r = r;
    }

    public void setIsHit(boolean hit) {
        isHit = hit;
    }

    public void setNowTime(String nowTime) {
        this.nowTime = nowTime;
    }

    public void setWorkTime(Double workTime) {
        this.workTime = workTime;
    }
}
