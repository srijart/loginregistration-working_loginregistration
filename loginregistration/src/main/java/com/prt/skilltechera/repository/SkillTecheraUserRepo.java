package com.prt.skilltechera.repository;

import com.prt.skilltechera.entity.SkillTecheraUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillTecheraUserRepo extends JpaRepository<SkillTecheraUser, Long> {
    SkillTecheraUser findByEmail(String email);

    SkillTecheraUser findByTemporaryEmail(String newEmail);

}
