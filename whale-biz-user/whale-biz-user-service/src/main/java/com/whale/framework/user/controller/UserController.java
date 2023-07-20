package com.whale.framework.user.controller;

import com.whale.framework.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Whale
 * @ProjectName: whale-biz
 * @See: com.whale.framework.user.controller
 * @Description:
 * @Date: 2023/7/10 10:27 AM
 */
@Tag(name = "用户相关")
@Slf4j
@RequestMapping("/user")
@RestController
public class UserController {

    @Autowired
    UserService userService;

    @Operation(summary = "发送邮件")
    @GetMapping("/get/user")
    public void getUser(@RequestParam String userId) {
        userService.getUser(userId);
    }


}
