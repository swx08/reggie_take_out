package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.entity.Employee;
import com.itheima.service.EmployeeService;
import com.itheima.service.impl.EmployeeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.zip.ZipEntry;

/**
 * ClassName:EmployeeController
 * Package:com.itheima.controller
 * Description
 *
 * @Author:@wenxueshi
 * @Create:2023/2/7 - 19:58
 * @Version:v1.0
 */

/**
 * 员工管理
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录功能
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request,@RequestBody Employee employee){
        //1.将页面提交的password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2.根据username进行数据库查询
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        //3.验证是否有此username用户
        if(emp == null){
            return R.error("登录失败,用户不存在!");
        }
        //4.有此username用户，但是要进行password对比
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败,用户密码错误!");
        }
        //5.查看员工状态是否为禁用状态,如果为1则不是禁用状态
        if(emp.getStatus() == 0){
            return R.error("登录失败,用户账号已被禁用!");
        }
        //6.登录成功，将员工id存入Session并返回登录结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 退出功能
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //删除登录时保存的Session Id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功!");
    }

    /**
     * 新增员工
     * @param request
     * @param employee
     * @return
     */
    @RequestMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("添加员工");
        //1.设置初始密码，md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //2.补充其他值
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
        //获取当前登录的用户Id
        //long empId = (long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);
        employeeService.save(employee);
        return R.success("添加成功!");
    }

    /**
     * 员工管理分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件(模糊查询)
        queryWrapper.like(!StringUtils.isEmpty(name),Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 用户信息修改、启用、禁用修改
     * @param employee
     * @param request
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Employee employee,HttpServletRequest request){
        log.info("用户信息修改...");
        //long empId = (Long) request.getSession().getAttribute("employee");
        //补充其他值
//        employee.setUpdateUser(empId);
//        employee.setUpdateTime(LocalDateTime.now());
        //进行用户信息修改
        employeeService.updateById(employee);
        return R.success("用户信息修改成功!");
    }

    /**
     * 用户信息修改数据回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> update(@PathVariable Long id){
        //数据库查询
        Employee employee = employeeService.getById(id);
        if(employee == null){
            R.error("没有查询到对应员工信息!");
        }
        return R.success(employee);
    }
}
