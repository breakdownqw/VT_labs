package org.example.web3.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@Named
@ApplicationScoped
public class HitEntityManager {
    @PersistenceContext(unitName = "pgPU")
    private EntityManager em;

    @Transactional
    public HitEntity add(HitEntity hitEntity) {
        em.persist(hitEntity);
        return hitEntity;
    }

    @Transactional
    public void delete() {
        em.createQuery("DELETE FROM HitEntity").executeUpdate();
        em.clear();
    }

    public List<HitEntity> getHits() {
        return em.createQuery("SELECT h FROM HitEntity h ORDER BY h.id DESC", HitEntity.class)
                .getResultList();
    }
}
