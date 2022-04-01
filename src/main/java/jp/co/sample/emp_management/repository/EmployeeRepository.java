package jp.co.sample.emp_management.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import jp.co.sample.emp_management.domain.Employee;
import jp.co.sample.emp_management.domain.EmployeeSearch;

/**
 * employeesテーブルを操作するリポジトリ.
 * 
 * @author igamasayuki
 * 
 */
@Repository
public class EmployeeRepository {

	/**
	 * Employeeオブジェクトを生成するローマッパー.
	 */
	private static final RowMapper<Employee> EMPLOYEE_ROW_MAPPER = (rs, i) -> {
		Employee employee = new Employee();
		employee.setId(rs.getInt("id"));
		employee.setName(rs.getString("name"));
		employee.setImage(rs.getString("image"));
		employee.setGender(rs.getString("gender"));
		employee.setHireDate(rs.getDate("hire_date"));
		employee.setMailAddress(rs.getString("mail_address"));
		employee.setZipCode(rs.getString("zip_code"));
		employee.setAddress(rs.getString("address"));
		employee.setTelephone(rs.getString("telephone"));
		employee.setSalary(rs.getInt("salary"));
		employee.setCharacteristics(rs.getString("characteristics"));
		employee.setDependentsCount(rs.getInt("dependents_count"));
		return employee;
	};

	@Autowired
	private NamedParameterJdbcTemplate template;

	/**
	 * 従業員一覧情報を入社日順で取得します.
	 * 
	 * @return 全従業員一覧 従業員が存在しない場合はサイズ0件の従業員一覧を返します
	 */
	public Page<Employee> findAll(Pageable pageable) {
		//String sql = "SELECT id,name,image,gender,hire_date,mail_address,zip_code,address,telephone,salary,characteristics,dependents_count FROM employees ORDER BY hire_date";
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT id,name,image,gender,hire_date,mail_address,zip_code,address,telephone,salary,characteristics,dependents_count FROM employees ORDER BY hire_date ");
		
		//System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n" + pageable.getOffset() + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		//何件データを取得したいのかをLIMIT句を用いて表す
		sql.append("LIMIT " + pageable.getPageSize() + " ");
		
		//データの取得位置をOFFSET句の後に書く
		sql.append("OFFSET " + pageable.getOffset());
		
		//データが合計何件あるのかを取得する
		String totalSql = "SELECT COUNT(*) FROM employees";
		int total = template.queryForObject(totalSql, new MapSqlParameterSource(), Integer.class);
		
		List<Employee> developmentList = template.query(sql.toString(), EMPLOYEE_ROW_MAPPER);
		
		//Pageクラスのコンストラクタを返す
		return new PageImpl<Employee>(developmentList, pageable, total);
	}

	/**
	 * 主キーから従業員情報を取得します.
	 * 
	 * @param id 検索したい従業員ID
	 * @return 検索された従業員情報
	 * @exception org.springframework.dao.DataAccessException 従業員が存在しない場合は例外を発生します
	 */
	public Employee load(Integer id) {
		String sql = "SELECT id,name,image,gender,hire_date,mail_address,zip_code,address,telephone,salary,characteristics,dependents_count FROM employees WHERE id=:id";

		SqlParameterSource param = new MapSqlParameterSource().addValue("id", id);

		Employee development = template.queryForObject(sql, param, EMPLOYEE_ROW_MAPPER);

		return development;
	}

	/**
	 * 従業員情報を変更します.
	 */
	public void update(Employee employee) {
		SqlParameterSource param = new BeanPropertySqlParameterSource(employee);

		String updateSql = "UPDATE employees SET dependents_count=:dependentsCount WHERE id=:id";
		template.update(updateSql, param);
	}
	
	public Page<Employee> findByLikeName(EmployeeSearch employeeSearch, Pageable pageable){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM employees WHERE name LIKE :name ORDER BY hire_date ");
		sql.append("LIMIT " + pageable.getPageSize() + " ");
		sql.append("OFFSET " + pageable.getOffset());
		
		//nameに値を入れている
		SqlParameterSource param = new MapSqlParameterSource().addValue("name", '%' + employeeSearch.getName() + '%');
		
		String totalSql = "SELECT COUNT(*) FROM employees WHERE name LIKE :name";
		int total = template.queryForObject(totalSql, param, Integer.class);
		
		//System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n" + total + "\n\n\n\n\n\n\n\n\n\n\n\n");
		List<Employee> employeeList = template.query(sql.toString(), param, EMPLOYEE_ROW_MAPPER);
		return new PageImpl<Employee>(employeeList, pageable, total);
	}
	
	
	//従業員情報を追加します。
	public void insert(Employee employee) {
		//最大のIdの値を取得する
		String maxIdSql = "SELECT MAX(id) FROM employees";
		int maxId = template.queryForObject(maxIdSql, new MapSqlParameterSource(), Integer.class);
		
		//maxIdを+1する
		maxId += 1;
		
		String sql = "INSERT INTO employees(id, name, image, gender, hire_date, mail_address, zip_code, address, telephone, salary, characteristics, dependents_count) VALUES(" + maxId + ", :name, :image, :gender, :hireDate, :mailAddress, :zipCode, :address, :telephone, :salary, :characteristics, :dependentsCount)";
		//プレースホルダに値を埋め込む
		SqlParameterSource param = new BeanPropertySqlParameterSource(employee);
		template.update(sql, param);
	}
	
	//取得したIDに紐づく従業員を削除する
	public void delete(Integer id) {
		String sql = "DELETE FROM employees WHERE id=:id";
		SqlParameterSource param = new MapSqlParameterSource().addValue("id", id);
		template.update(sql, param);
	}
	
	public Page<Employee> sort(String sort, Pageable pageable){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM employees ORDER BY hire_date ");
		sql.append(sort + " ");
		sql.append("LIMIT " + pageable.getPageSize() + " ");
		sql.append("OFFSET " + pageable.getOffset());
		List<Employee> employeeList = template.query(sql.toString(), EMPLOYEE_ROW_MAPPER);
		String totalSql = "SELECT COUNT(*) FROM employees";
		int total = template.queryForObject(totalSql, new MapSqlParameterSource(), Integer.class);
		return new PageImpl<>(employeeList, pageable, total);
	}
}
