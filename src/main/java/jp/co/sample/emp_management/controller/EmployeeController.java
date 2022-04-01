package jp.co.sample.emp_management.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.servlet.http.HttpSession;

import org.springframework.beans.BeanUtils;
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
import jp.co.sample.emp_management.form.InsertEmployeeForm;
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
	
	@Autowired
	private HttpSession session;
	
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
	
	@ModelAttribute
	public InsertEmployeeForm setUpInsertEmployeeForm() {
		return new InsertEmployeeForm();
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
		String link = "showList";
		model.addAttribute("link", link);
		model.addAttribute("list", "list");
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
	public String fuzzySearch(SearchEmployeeForm form, RedirectAttributes redirectAttributes, Model model, @PageableDefault(page = 0, size = 10)Pageable pageable) {
		EmployeeSearch employeeSearch = new EmployeeSearch();
		//System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n" + form.getName() + "\n\n\n\n\n\n\n\n\n\n\n\n\n");
		employeeSearch.setName(form.getName());
		model.addAttribute("name", form.getName());
		//session.setAttribute("employeeSearch", employeeSearch);
		Page<Employee> employeeList = employeeService.findByLikeName(employeeSearch, pageable);
		//System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n" + employeeList.getContent() + "\n\n\n\n\n\n\n\n\\n\n\n\n\n");
		if(employeeList.getContent().size() == 0) {
			redirectAttributes.addFlashAttribute("noSearch", "一件もありませんでした");
			return "redirect:/employee/showList";
		}
		model.addAttribute("page", employeeList);
		model.addAttribute("employeeList", employeeList.getContent());
		model.addAttribute("link", "fuzzy");
		return "employee/list";
	}
	
	@RequestMapping("/insert")
	public String insert(InsertEmployeeForm insertEmployeeForm) {
		
		//画像の保存処理
		saveImage(insertEmployeeForm);
		
		String saveImageName = insertEmployeeForm.getImage().getOriginalFilename();
		
		Employee employee = new Employee();
		//formからdomainに値をコピー
		//BeanUtils.copyProperties(insertEmployeeForm, employee);
		employee.setName(insertEmployeeForm.getName());
		employee.setImage(saveImageName);
		employee.setGender(insertEmployeeForm.getGender());
		employee.setHireDate(insertEmployeeForm.getHireDate());
		employee.setMailAddress(insertEmployeeForm.getMailAddress());
		employee.setZipCode(insertEmployeeForm.getZipCode());
		employee.setAddress(insertEmployeeForm.getAddress());
		employee.setTelephone(insertEmployeeForm.getTelephone());
		employee.setSalary(insertEmployeeForm.getSalary());
		employee.setCharacteristics(insertEmployeeForm.getCharacteristics());
		employee.setDependentsCount(insertEmployeeForm.getDependentsCount());
		
		
		employeeService.insert(employee);
		return "forward:/employee/showList";
	}
	
	@RequestMapping("/toInsert")
	public String toInsert() {
		return "employee/insert";
	}
	
	//画像の名前を取得するメソッド
	public void saveImage(InsertEmployeeForm insertEmployeeForm) {
		String ImageName = insertEmployeeForm.getImage().getOriginalFilename();
		String saveImageName = null;
		saveImageName = ImageName;
		
		//画像保存先のパスオブジェクトを作成
		Path imagePath = Paths.get("src/main/resources/static/img/" + saveImageName);
		try {
			//アップデートファイルをバイト値に変換
			byte[] bytes = insertEmployeeForm.getImage().getBytes();
			
			//バイト値を書き込むためのファイルを作成して指定したパスに格納
			OutputStream os = Files.newOutputStream(imagePath);
			
			//ファイルに書き込み
			os.write(bytes);
			
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	//従業員を削除するメソッド
	@RequestMapping("/delete")
	public String delete(Integer id) {
		employeeService.delete(id);
		return "forward:/employee/showList";
	}
	
	@RequestMapping("/sort")
	public String sort(String sort, @PageableDefault(page = 0, size = 10)Pageable pageable, Model model) {
		Page<Employee> employeeListPage = employeeService.sort(sort, pageable);
		model.addAttribute("page", employeeListPage);
		model.addAttribute("employeeList", employeeListPage.getContent());
		String link = "sort";
		model.addAttribute("link", link);
		model.addAttribute("sort", sort);
		return "employee/list";
	}
	
}
