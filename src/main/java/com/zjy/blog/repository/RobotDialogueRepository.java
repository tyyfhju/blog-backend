package com.zjy.blog.repository;

import com.zjy.blog.entity.RobotDialogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface RobotDialogueRepository extends JpaRepository<RobotDialogue, Long> {

    List<RobotDialogue> findByEnabledOrderByCreateTimeDesc(Integer enabled);

    @Query(value = "SELECT * FROM robot_dialogue WHERE enabled = 1 ORDER BY RAND() LIMIT 1", nativeQuery = true)
    RobotDialogue findRandomEnabled();
}
