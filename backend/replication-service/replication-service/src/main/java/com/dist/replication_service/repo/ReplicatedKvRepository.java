package com.dist.replication_service.repo;

import com.dist.replication_service.entity.ReplicatedKvEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReplicatedKvRepository extends JpaRepository<ReplicatedKvEntry, UUID> {
}
