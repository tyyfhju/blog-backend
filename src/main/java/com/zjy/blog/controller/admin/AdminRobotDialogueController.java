package com.zjy.blog.controller.admin;

import com.zjy.blog.entity.RobotDialogue;
import com.zjy.blog.repository.RobotDialogueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/private/robot-dialogues")
public class AdminRobotDialogueController {

    @Autowired
    private RobotDialogueRepository robotDialogueRepository;

    @GetMapping
    public List<RobotDialogue> list() {
        return robotDialogueRepository.findAll();
    }

    @PostMapping
    public RobotDialogue create(@RequestBody RobotDialogue dialogue) {
        return robotDialogueRepository.save(dialogue);
    }

    @PutMapping("/{id}")
    public RobotDialogue update(@PathVariable Long id, @RequestBody RobotDialogue dialogue) {
        RobotDialogue existing = robotDialogueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("台词不存在"));
        existing.setContent(dialogue.getContent());
        existing.setEnabled(dialogue.getEnabled());
        return robotDialogueRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        robotDialogueRepository.deleteById(id);
        Map<String, Object> res = new HashMap<>();
        res.put("message", "删除成功");
        return ResponseEntity.ok(res);
    }
}
