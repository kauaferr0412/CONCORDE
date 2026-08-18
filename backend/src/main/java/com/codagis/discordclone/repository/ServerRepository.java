package com.codagis.discordclone.repository;

import com.codagis.discordclone.domain.Server;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerRepository extends JpaRepository<Server, Long> {
}
