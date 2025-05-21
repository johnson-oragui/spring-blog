package com.johnson.database.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import com.johnson.utilities.UUIDGenerator;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseModel {

  @Id
  @Column(unique = true, nullable = false)
  private String id = UUIDGenerator.generateUUIDv7();

  @CreationTimestamp(source = SourceType.DB)
  @Column(updatable = false, name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime createdAt;

  @UpdateTimestamp(source = SourceType.VM)
  @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    if (this.id == null) {
      this.id = UUIDGenerator.generateUUIDv7();
    }
    this.createdAt = OffsetDateTime.now();
    this.updatedAt = OffsetDateTime.now();
  }
}
