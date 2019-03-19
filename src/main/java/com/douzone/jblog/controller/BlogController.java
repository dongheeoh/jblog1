package com.douzone.jblog.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.douzone.dto.JSONResult;
import com.douzone.jblog.service.BlogService;
import com.douzone.jblog.service.FileuploadService;
import com.douzone.jblog.service.UserService;
import com.douzone.jblog.vo.BlogVo;
import com.douzone.jblog.vo.CategoryVo;
import com.douzone.jblog.vo.PostVo;
import com.douzone.jblog.vo.UserVo;
import com.douzone.security.AuthUser;

@Controller
@RequestMapping("/{id:(?!assets).*}")
public class BlogController {
	
	@Autowired
	private FileuploadService fileuploadService;
	@Autowired
	private UserService userService;
	@Autowired
	private BlogService blogService;
	
	@RequestMapping({"","/{category_no}","/{category_no}/{post_no}"})
	public String blog_main(
			@PathVariable("id") String id,
			@PathVariable("category_no") Optional<Long> category_no,
			@PathVariable("post_no") Optional<Long> post_no,
			Model model) {
			
		if(category_no.isPresent() && post_no.isPresent()) {
			Map<String, Object> map = 
					blogService.getAll(id, category_no.get(), post_no.get());
			model.addAllAttributes(map);		
			} else {
			Map<String, Object> map = blogService.getAll(id, category_no.orElse(0L), post_no.orElse(0L));
			System.out.println("nullcheck===>>" + map);
			
			model.addAllAttributes(map); //map All put

		}

			return "blog/blog-main";			
	}
//================================================================================

	@RequestMapping("/admin/basic")
	public String blog_admin_basic(@AuthUser UserVo authuser,
			@PathVariable("id") String id, Model model) {
		
		if(authuser == null) {
			return "user/login";
		}
		BlogVo blogVo = blogService.get(id); // id로 찾아서 blog정보 get
		model.addAttribute("blogVo",blogVo); //blogVo로 view에 뿌려줌
		
		return "blog/blog-admin-basic";
	}

	
	@RequestMapping(value="/update", method =RequestMethod.POST)
	public String blog_update(@AuthUser UserVo authuser,
			@PathVariable("id") String id,
			@ModelAttribute BlogVo blogVo,
			@RequestParam(value="logo-file") MultipartFile multipartFile) {
		
		if(authuser == null) {
			return "user/login";
		}
		long user_no = userService.checkId(id).getNo();		 // id값 넘어온걸 이용해 UserNo 뽑아냄
		blogVo.setUser_no(user_no);  //바뀐 blog내용을 UserNo에 바꿈
		String logo = fileuploadService.restore(multipartFile); //파일업로드서비스로 파일변경
		blogVo.setLogo(logo); 
		blogService.update(blogVo);  //바뀐 정보들을 update
	
		return "redirect:/"+id+"/admin/basic";
	}

//================================================================================
	
	@RequestMapping("/admin/write")
	public String blog_write(@AuthUser UserVo authuser,
			@PathVariable("id") String id,
			Model model) {
		if(authuser == null) {
			return "user/login";
		}
		BlogVo blogVo = blogService.get(id);
		List<CategoryVo> categoryList = blogService.getCategoryList(id);
		model.addAttribute("categoryList", categoryList); //combox에 카테고리 내용
		model.addAttribute("blogVo",blogVo); //블로그 내용(해당하는 User의 blog)
		return "blog/blog-admin-write";
	}
	
	@RequestMapping(value="/write", method= RequestMethod.POST)
	public String blog_write(@AuthUser UserVo authuser,
			@PathVariable("id") String id,
			@ModelAttribute PostVo postVo) {
		if(authuser == null) {
			return "user/login";
		}
		System.out.println("변경된 postVo=========>"+postVo);
		blogService.insertPost(postVo);
		return "redirect:/"+id;
	}
//================================================================================
	
	@RequestMapping("/admin/category")
	public String blog_category(
			@AuthUser UserVo authuser,
			@PathVariable("id") String id,
			Model model) {
		if(authuser == null) {
			return "user/login";
		}
		BlogVo blogVo = blogService.get(id);
		model.addAttribute("blogVo",blogVo);
		return "blog/blog-admin-category";
	}
	
	@ResponseBody
	@RequestMapping("/admin/category/list")
	public JSONResult blog_category(
			@PathVariable("id") String id,
			Model model) {
		
		BlogVo blogVo = blogService.get(id);
		model.addAttribute("blogVo",blogVo);
		
		return JSONResult.success(blogService.getCategoryListAndPost(id));
	}
	
	@ResponseBody
	@RequestMapping(value="/admin/category/insert", method=RequestMethod.POST)
	public JSONResult blog_category_insert(
			@RequestParam(value="name") String name,
			@RequestParam(value="description") String description,
			@PathVariable("id") String id,
			Model model) {
		
		BlogVo blogVo = blogService.get(id);
		model.addAttribute("blogVo",blogVo);
		
		CategoryVo categoryVo1 = new CategoryVo();
		categoryVo1.setName(name);
		categoryVo1.setDescription(description);
		categoryVo1.setUser_no(blogVo.getUser_no());
		
		return JSONResult.success(blogService.getCategory(categoryVo1));
	}
	@ResponseBody
	@RequestMapping(value="/admin/category/delete", method=RequestMethod.POST)
	public JSONResult category_delete(
			@RequestParam(value="no") long no,
			@PathVariable("id") String id,
			Model model){
		BlogVo blogVo = blogService.get(id);
		model.addAttribute("blogVo",blogVo);
		
		return JSONResult.success(blogService.delete(no));
		
	}

}
