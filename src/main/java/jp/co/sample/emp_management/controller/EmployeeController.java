package jp.co.sample.emp_management.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jp.co.sample.emp_management.domain.Employee;
import jp.co.sample.emp_management.domain.EmployeeSearch;
import jp.co.sample.emp_management.form.SearchEmployeeForm;
import jp.co.sample.emp_management.form.UpdateEmployeeForm;
import jp.co.sample.emp_management.service.EmployeeService;

/**
 * 従業員情報を操作するコントローラー.
 * 
 * @author igamasayuki
 *
 */
@Controller
@RequestMapping("/employee")
public class EmployeeController {

	@Autowired
	private EmployeeService employeeService;
	
	/**
	 * 使用するフォームオブジェクトをリクエストスコープに格納する.
	 * 
	 * @return フォーム
	 */
	@ModelAttribute
	public UpdateEmployeeForm setUpForm() {
		return new UpdateEmployeeForm();
	}
	
	@ModelAttribute
	public SearchEmployeeForm setUpForm2() {
		return new SearchEmployeeForm();
	}
	/////////////////////////////////////////////////////
	// ユースケース：従業員一覧を表示する
	/////////////////////////////////////////////////////
	/**
	 * 従業員一覧画面を出力します.
	 * 
	 * @param model モデル
	 * @return 従業員一覧画面
	 */
	@RequestMapping("/showList")
	public String showList(Model model, @PageableDefault(page = 0, size = 10) Pageable pageable) {
		Page<Employee> employeeList = employeeService.showList(pageable);
		model.addAttribute("page", employeeList);
		model.addAttribute("employeeList", employeeList.getContent());
		return "employee/list";
	}

	
	/////////////////////////////////////////////////////
	// ユースケース：従業員詳細を表示する
	/////////////////////////////////////////////////////
	/**
	 * 従業員詳細画面を出力します.
	 * 
	 * @param id リクエストパラメータで送られてくる従業員ID
	 * @param model モデル
	 * @return 従業員詳細画面
	 */
	@RequestMapping("/showDetail")
	public String showDetail(String id, Model model) {
		Employee employee = employeeService.showDetail(Integer.parseInt(id));
		model.addAttribute("employee", employee);
		return "employee/detail";
	}
	
	/////////////////////////////////////////////////////
	// ユースケース：従業員詳細を更新する
	/////////////////////////////////////////////////////
	/**
	 * 従業員詳細(ここでは扶養人数のみ)を更新します.
	 * 
	 * @param form
	 *            従業員情報用フォーム
	 * @return 従業員一覧画面へリダクレクト
	 */
	@RequestMapping("/update")
	public String update(@Validated UpdateEmployeeForm form, BindingResult result, Model model) {
		if(result.hasErrors()) {
			return showDetail(form.getId(), model);
		}
		Employee employee = new Employee();
		employee.setId(form.getIntId());
		employee.setDependentsCount(form.getIntDependentsCount());
		employeeService.update(employee);
		return "redirect:/employee/showList";
	}
	
	@RequestMapping("/fuzzySearch")
	public String fuzzySearch(SearchEmployeeForm form, String name, RedirectAttributes redirectAttributes, Model model, @PageableDefault(page = 0, size = 10)Pageable pageable) {
		EmployeeSearch employeeSearch = new EmployeeSearch();
		employeeSearch.setName(form.getName());
		Page<Employee> employeeList = employeeService.findByLikeName(employeeSearch, pageable);
		//System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n" + employeeList.getContent() + "\n\n\n\n\n\n\n\n\\n\n\n\n\n");
		if(employeeList.getContent().size() == 0) {
			redirectAttributes.addFlashAttribute("noSearch", "一件もありませんでした");
			return "redirect:/employee/showList";
		}
		model.addAttribute("page", employeeList);
		model.addAttribute("employeeList", employeeList.getContent());
		return "employee/list";
	}
}
