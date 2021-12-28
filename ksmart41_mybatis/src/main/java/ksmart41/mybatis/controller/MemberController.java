package ksmart41.mybatis.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.origin.SystemEnvironmentOrigin;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ksmart41.mybatis.dto.Member;
import ksmart41.mybatis.dto.MemberLevel;
import ksmart41.mybatis.mapper.MemberMapper;
import ksmart41.mybatis.service.MemberService;

@Controller
@RequestMapping(value = "/member")
public class MemberController {
	
	
	private static final Logger log = LoggerFactory.getLogger(MemberController.class);

	
	/**
	 * DI (의존성 주입) -> @autowired
	 * 1. 프로퍼티
	 * 2. 세터메서드
	 * 3. 생성자메서드
	 */
	
	/*
	//프로퍼티 DI 주입방식
	@Autowired 
	private MemberService memberService;
	
	//세터 DI 주입방식
	private MemberService memberService;
	
	@Autowired
	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
	*/
	
	//생성자메소드 주입방식 (SpringFramwork 4.3 이후 부터는 생성자 메소드에 @autowired 생략가능)
	private MemberService memberService;
	
	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}
	@PostMapping("/removeMember")
	public String removeMember(
			@RequestParam(value="memberId", required = false) String memberId
		   ,@RequestParam(value="memberPw", required = false) String memberPw
		   ,RedirectAttributes reAttr) {
		// 화면에서 전달받은 회원아이디 확인
		log.info("화면에서 전달받은 회원아이디 : {}", memberId);
		log.info("화면에서 전달받은 회원비밀번호 : {}", memberPw);
		
		//service
		String result = memberService.removeMember(memberId, memberPw);
		// 회원정보 확인
		if("회원탈퇴 실패".equals(result)) {
			reAttr.addAttribute("result", result);
			
			// /member/removeMember/id001?result=회원삭제 실패
			return "redirect:/member/removeMember/" + memberId;
		}
				
		return "redirect:/member/memberList";
	}
	
	@GetMapping("/removeMember/{memberId}")
	public String removeMember(
			@PathVariable(value = "memberId", required = false) String memberId
		   ,@RequestParam(value = "result", required = false) String result
		   ,Model model) {
		
		log.info("삭제요청시 화면에 전달될 memberId 값: {}", memberId);
		
		model.addAttribute("title", "회원탈퇴폼");
		model.addAttribute("memberId", memberId);
		if(result != null) model.addAttribute("result", result);
		return "member/removeMember";
	}
	
	@PostMapping("/modifyMember")
	public String modifyMember(Member member) {
		log.info("회원 수정 화면에서 입력 받은 회원정보 : {}", member );
		
		//회원수정
		memberService.modifyMember(member);
		
		return "redirect:/member/memberList";
	}
	
	@GetMapping("/modifyMember")
	public String modifyMember(@RequestParam(value="memberId", required = false) String memberId
							   , Model model) {
		//memberId 콘솔에 출력(log4j)
		log.info("modifyMember memberId: {}", memberId);
		
		//회원의 정보
		if(memberId != null && !"".equals(memberId)) {
			Member memberInfo = memberService.getMemberInfoByMemberId(memberId);
			model.addAttribute("memberInfo", memberInfo);
		}
		
		model.addAttribute("title", "회원수정화면");
		model.addAttribute("memberLevelList", memberService.getMemberLevelList());
		
		
		return "member/modifyMember";
	}
	
	@PostMapping("/idCheck")
	@ResponseBody
	public boolean idCheck(@RequestParam(value="memberId", required = false) String memberId) {
		
		System.out.println("ajax 통신으로 요청받은 파라미터 memberId: " + memberId);
		
		boolean checkResult = false;
		
		int check = memberService.getMemberByMemberId(memberId);
		
		if(check > 0) checkResult = true;
				
		return checkResult;
	}
	
	
	/**
	 * 커맨드객체 : controller 클래스 안 메소드의 매개변수 dto 
	 */
	
	@PostMapping("/addMember")
	public String addMember(Member member) {
		
		System.out.println("MemberController 회원등록 화면에서 입력받은 값: " + member);
		//insert 처리
		//null 체크 
		String memberId = member.getMemberId();
		if(memberId != null && !"".equals(memberId)) {
			memberService.addMember(member);
		}
		
		return "redirect:/member/memberList";
	}
	
	@GetMapping("/addMember")
	public String addMember(Model model) {
		System.out.println("/addMember GET방식 요청");
		model.addAttribute("title", "회원등록");
		//DB 레벨 등급 LIST
		List<MemberLevel> memberLevelList = memberService.getMemberLevelList();
		model.addAttribute("memberLevelList", memberLevelList);
		return "member/addMember";
	}

	@PostMapping("/memberList")
	public String getSearchMemberList(
			 @RequestParam(value="searchKey", required = false) String searchKey
			,@RequestParam(value="searchValue", required = false) String searchValue
			,Model model) {
		
		if(searchKey != null && "memberId".equals(searchKey)) {
			searchKey = "m_id";
		}else if(searchKey != null && "memberName".equals(searchKey)) {
			searchKey = "m_name";
		}else {
			searchKey = "m_email";
		}
		// 검색키 검색어를 통해서 회원목록 조회
		
		List<Member> memberList = memberService.getMemberListBySearchKey(searchKey, searchValue);
		
		// 조회된 회원목록 model에 값을 저장
		model.addAttribute("title", "회원목록조회");
		model.addAttribute("memberList", memberList);
		
		
		return "member/memberList";
	}
	
	/**
	 * localhost/member/memberList
	 */
	@GetMapping("/memberList")
	public String getMemberList(Model model) {
		List<Member> memberList = memberService.getMemberList();
		
		model.addAttribute("title", "회원전체조회");
		model.addAttribute("memberList", memberList);
		//model.addAttribute("memberList", memberService.getMemberList());
		
		return "member/memberList";
	}
}
