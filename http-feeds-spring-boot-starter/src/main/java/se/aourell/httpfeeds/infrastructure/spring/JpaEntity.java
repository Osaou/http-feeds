package se.aourell.httpfeeds.infrastructure.spring;

import org.springframework.data.domain.Persistable;

import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Transient;

@MappedSuperclass
public abstract class JpaEntity<ID> implements Persistable<ID> {

  @Transient
  private boolean isNew = true;

  @Override
  public boolean isNew() {
    return isNew;
  }

  @PrePersist
  @PostLoad
  void markNotNew() {
    this.isNew = false;
  }
}
