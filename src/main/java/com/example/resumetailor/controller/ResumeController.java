package com.example.resumetailor.controller;

import com.example.resumetailor.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @PostMapping("/tailor")
    public Map<String, String> tailorResume(@RequestParam("file") MultipartFile file,
                                            @RequestParam("jobDescription") String jobDescription) {
        String result = resumeService.generateTailoredSummary(file, jobDescription);
        Map<String, String> response = new HashMap<>();
        response.put("tailoredSummary", result);
        response.put("jobDescription", jobDescription);
        return response;
    }
}
