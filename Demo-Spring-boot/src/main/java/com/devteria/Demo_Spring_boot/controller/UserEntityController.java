//package com.devteria.Demo_Spring_boot.controller;
//
//import com.devteria.Demo_Spring_boot.entity.UserEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@Controller //trả về view
//@RestController//trả về json thay vì view
//@RequestMapping("/user")
//public class UserEntityController {
//    @GetMapping // trả về phương thức get
//    public String getUser(Model model){
//        UserEntity user = new UserEntity(1L, "Nguyen Van A", "nva@gmail.com");
//        model.addAttribute("user", user);//truyền dữ liệu vào model
//        return "index.html";//trả về tên fontend với file index.html
//    }
//}
